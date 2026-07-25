package com.example.macollection.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.macollection.data.AppDatabase
import com.example.macollection.data.AppPrefs
import com.example.macollection.BuildConfig
import com.example.macollection.data.GameHighScore
import com.example.macollection.data.GameShopCatalog
import com.example.macollection.data.IntegrityGuard
import com.example.macollection.data.ShopItemCategory
import com.example.macollection.data.PlayerProgress
import com.example.macollection.data.UnlockedItem
import com.example.macollection.ui.billing.BillingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Points gagnés pour une pub récompensée regardée jusqu'au bout. */
const val AD_REWARD_POINTS = 10

class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.get(app)
    private val progressDao = db.playerProgressDao()
    private val unlockedDao = db.unlockedItemDao()
    private val highScoreDao = db.gameHighScoreDao()

    private val billing = BillingManager(app)

    /** Ligne de progression si son empreinte est valide, sinon null (voir [IntegrityGuard]) —
     *  une ligne modifiée directement dans la base (root + éditeur SQLite) n'est jamais créditée. */
    private fun PlayerProgress?.verified(): PlayerProgress? =
        this?.takeIf { IntegrityGuard.signProgress(it.points, it.lifetimePoints) == it.checksum }

    val points: StateFlow<Int> =
        progressDao.observe().map { it.verified()?.points ?: 0 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lifetimePoints: StateFlow<Int> =
        progressDao.observe().map { it.verified()?.lifetimePoints ?: 0 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val unlockedItemIds: StateFlow<Set<String>> =
        unlockedDao.observeAll().map { list ->
            list.filter { IntegrityGuard.signUnlock(it.itemId, it.unlockedAt) == it.checksum }
                .map { it.itemId }.toSet()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    /** Ligne de progression courante, réinitialisée si son empreinte a été altérée hors de l'appli. */
    private suspend fun verifiedProgress(): PlayerProgress =
        progressDao.observe().first().verified() ?: PlayerProgress()

    /** Enregistre [progress] avec une empreinte recalculée à partir de son contenu réel. */
    private suspend fun saveProgress(progress: PlayerProgress) =
        progressDao.upsert(progress.copy(checksum = IntegrityGuard.signProgress(progress.points, progress.lifetimePoints)))

    val highScores: StateFlow<Map<String, Int>> =
        highScoreDao.observeAll().map { list -> list.associate { it.gameId to it.bestScore } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /** true si l'accès total a été payé en points OU acheté réellement via Google Play. */
    val isPremiumAllAccess: StateFlow<Boolean> =
        combine(unlockedItemIds, billing.premiumPurchased) { unlocked, purchased ->
            // Variante TEST : jamais Premium (aucun achat possible) — blocage incontournable.
            if (BuildConfig.IS_TEST) false
            // Édition V2SP (UNLOCK_ALL) : accès total, tous les verrous levés.
            else BuildConfig.UNLOCK_ALL || unlocked.contains(GameShopCatalog.PREMIUM_ALL_ACCESS) || purchased
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** true seulement si le produit Google Play réel est configuré côté Play Console (sinon "Bientôt disponible"). */
    val isRealPremiumPurchaseAvailable: StateFlow<Boolean> = billing.productAvailable

    /** Jeux d'arcade jouables : Pong offert d'origine + ceux débloqués + tous si Premium. */
    val unlockedGameIds: StateFlow<Set<String>> =
        combine(unlockedItemIds, isPremiumAllAccess) { unlocked, premium ->
            val allArcadeIds = GameShopCatalog.lockedGames.map { it.id }.toSet()
            val devAll = !BuildConfig.IS_TEST && AppPrefs.devUnlockAll.value
            GameShopCatalog.defaultUnlockedGames + unlocked.intersect(allArcadeIds) +
                if (premium || devAll) allArcadeIds else emptySet()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GameShopCatalog.defaultUnlockedGames)

    val isExcelExportUnlocked: StateFlow<Boolean> =
        combine(unlockedItemIds, isPremiumAllAccess) { unlocked, premium ->
            premium || unlocked.contains(GameShopCatalog.EXCEL_EXPORT)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isAdsReduced: StateFlow<Boolean> =
        combine(unlockedItemIds, isPremiumAllAccess) { unlocked, premium ->
            premium || unlocked.contains(GameShopCatalog.ADS_REDUCED)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** Crédite [amount] points (Quiz, Pong, pub récompensée...). */
    fun earnPoints(amount: Int) = viewModelScope.launch {
        if (amount <= 0) return@launch
        val current = verifiedProgress()
        saveProgress(current.copy(points = current.points + amount, lifetimePoints = current.lifetimePoints + amount))
    }

    /** Enregistre le score de fin de partie si c'est un nouveau meilleur score pour ce jeu. */
    fun recordHighScore(gameId: String, score: Int) = viewModelScope.launch {
        val current = highScoreDao.get(gameId)
        if (current == null || score > current.bestScore) {
            highScoreDao.upsert(GameHighScore(gameId, score, System.currentTimeMillis()))
        }
    }

    /**
     * Tente de débloquer [itemId] (jeu d'arcade, skin ou fonctionnalité) en dépensant les
     * points nécessaires. [onResult] reçoit true si le déblocage a réussi (assez de points ou
     * déjà débloqué), false sinon (solde insuffisant).
     */
    fun tryUnlock(itemId: String, onResult: (Boolean) -> Unit = {}) = viewModelScope.launch {
        // Variante TEST : achat de jeux d'arcade, de thèmes (skins) et du Premium bloqué —
        // aucun de ces items ne peut être débloqué, quelle que soit la voie.
        if (BuildConfig.IS_TEST) {
            val blocked = GameShopCatalog.find(itemId)
            if (blocked != null && (
                    blocked.category == ShopItemCategory.ARCADE_GAME ||
                    blocked.category == ShopItemCategory.SKIN ||
                    itemId == GameShopCatalog.PREMIUM_ALL_ACCESS
                )
            ) {
                onResult(false)
                return@launch
            }
        }
        if (unlockedItemIds.value.contains(itemId)) {
            onResult(true)
            return@launch
        }
        val item = GameShopCatalog.find(itemId)
        if (item == null) {
            onResult(false)
            return@launch
        }
        val progress = verifiedProgress()
        if (progress.points < item.cost) {
            onResult(false)
            return@launch
        }
        saveProgress(progress.copy(points = progress.points - item.cost))
        val unlockedAt = System.currentTimeMillis()
        unlockedDao.insert(UnlockedItem(itemId, unlockedAt, IntegrityGuard.signUnlock(itemId, unlockedAt)))
        onResult(true)
    }

    /** Achète un pack "+5 sauvegardes" (rachetable, contrairement aux autres déblocages). */
    fun buyExtraBackupPack(onResult: (Boolean) -> Unit = {}) = viewModelScope.launch {
        val cost = GameShopCatalog.find(GameShopCatalog.EXTRA_BACKUPS_5)?.cost ?: return@launch onResult(false)
        val progress = verifiedProgress()
        if (progress.points < cost) {
            onResult(false)
            return@launch
        }
        saveProgress(progress.copy(points = progress.points - cost))
        AppPrefs.addExtraBackupPack(getApplication())
        onResult(true)
    }

    /** Quota total d'exports de sauvegarde gratuits + packs achetés (illimité si Premium). */
    fun remainingFreeBackups(): Int {
        if (isPremiumAllAccess.value) return Int.MAX_VALUE
        val quota = GameShopCatalog.FREE_BACKUP_EXPORTS + AppPrefs.extraBackupPacks.value * GameShopCatalog.BACKUP_PACK_SIZE
        return (quota - AppPrefs.backupExportCount.value).coerceAtLeast(0)
    }

    fun canExportBackupFree(): Boolean = isPremiumAllAccess.value || remainingFreeBackups() > 0

    fun recordBackupExport() = AppPrefs.recordBackupExport(getApplication())

    /** Lance le flux d'achat Premium réel (Google Play). Voir [BillingManager] : no-op si le produit n'est pas encore configuré. */
    fun launchRealPremiumPurchase(activity: android.app.Activity) = billing.launchPurchase(activity)
}
