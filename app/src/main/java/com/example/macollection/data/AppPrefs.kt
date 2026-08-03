package com.example.macollection.data

import android.content.Context
import androidx.compose.runtime.mutableStateOf

/**
 * Préférences simples de l'application (langue des fiches en ligne).
 * Persistées dans les SharedPreferences.
 */
object AppPrefs {
    private const val PREFS = "macollection_prefs"
    private const val KEY_LANG = "lang"
    private const val KEY_TYPE = "lastType"
    private const val KEY_ONBOARDING_SEEN = "onboardingSeen"
    private const val KEY_BACKGROUND_URI = "backgroundUri"
    private const val KEY_CUSTOM_BRANDS = "customBrands"
    private const val KEY_CURRENCY = "currency"
    private const val KEY_CURRENCY_MANUAL = "currencyManual"
    private const val KEY_RATES = "currencyRates"
    private const val KEY_RATES_AT = "currencyRatesAt"
    private const val KEY_BACKUP_EXPORT_COUNT = "backupExportCount"
    private const val KEY_EXTRA_BACKUP_PACKS = "extraBackupPacks"
    private const val KEY_EXTRA_COLLECTION_SLOTS = "extraCollectionSlots"
    private const val KEY_QUIZ_SESSIONS = "quizSessionsCompleted"
    private const val KEY_QUIZ_LEVEL = "quizLevel"
    private const val KEY_THEME = "selectedTheme"
    private const val KEY_GAME_INTROS_SEEN = "gameIntrosSeen"
    private const val KEY_LAST_BACKUP_URI = "lastBackupUri"
    private const val KEY_AD_REWARDS_TODAY = "adRewardsToday"
    private const val KEY_AD_REWARDS_DAY = "adRewardsDay"
    private const val KEY_DAILY_CHEST_LAST_OPENED = "dailyChestLastOpened"
    private const val KEY_BARCODE_SCANS_TODAY = "barcodeScansToday"
    private const val KEY_BARCODE_BONUS_TODAY = "barcodeBonusScansToday"
    private const val KEY_BARCODE_SCANS_DAY = "barcodeScansDay"

    /**
     * Image de fond personnalisée (URI "file://..."), ou null pour le dégradé par défaut.
     * État Compose (pas juste un `var`) pour que l'arrière-plan se mette à jour immédiatement
     * partout dans l'app dès qu'elle est changée, sans redémarrage.
     */
    val backgroundImageUri = mutableStateOf<String?>(null)

    /** Marques ajoutées manuellement par l'utilisateur (absentes de [KnownBrands]), pour qu'elles
     * réapparaissent ensuite dans la liste déroulante du champ Marque. */
    val customBrands = mutableStateOf<Set<String>>(emptySet())

    /** Devise affichée/éditée (la cote reste stockée en euros : conversion à l'affichage uniquement). */
    val currency = mutableStateOf("EUR")

    /** Derniers taux de change connus (base EUR), mis à jour au lancement si le réseau répond. */
    val currencyRates = mutableStateOf<Map<String, Double>>(emptyMap())

    /** true dès que l'utilisateur a choisi sa devise lui-même : un changement de langue ne doit
     * alors plus jamais l'écraser silencieusement. */
    @Volatile
    private var currencyManuallySet: Boolean = false

    /** "fr" ou "en". Par défaut français. */
    @Volatile
    var language: String = "fr"
        private set

    /** Dernier type d'objet choisi (mémorisé). Par défaut CONSOLE. */
    @Volatile
    var lastType: String = "CONSOLE"
        private set

    /** true une fois que le tutoriel de premier lancement a été vu (ou passé). */
    @Volatile
    var onboardingSeen: Boolean = false
        private set

    /**
     * Nombre d'exports de sauvegarde (.zip) déjà effectués. Au-delà du quota gratuit
     * ([com.example.macollection.data.GameShopCatalog.FREE_BACKUP_EXPORTS]), il faut débloquer
     * un pack de sauvegardes supplémentaires via la Boutique (points) ou le statut Premium.
     */
    val backupExportCount = mutableStateOf(0)

    /**
     * Nombre de packs "+5 sauvegardes" achetés dans la Boutique (rachetable plusieurs fois,
     * contrairement aux autres déblocages qui sont uniques) : quota total = FREE_BACKUP_EXPORTS
     * + extraBackupPacks * 5.
     */
    val extraBackupPacks = mutableStateOf(0)

    /**
     * Objets de collection supplémentaires débloqués en compte gratuit via une pub récompensée
     * (+5 par pub, cumulable sans limite — contrairement aux packs de sauvegardes, il n'y a pas
     * d'équivalent payable en points pour ce quota). S'ajoute à
     * [com.example.macollection.ui.FREE_COLLECTION_LIMIT] ; ignoré si Premium (déjà illimité).
     */
    val extraCollectionSlots = mutableStateOf(0)

    /** Sessions de quiz terminées (statistique). */
    val quizSessionsCompleted = mutableStateOf(0)

    /** Niveau de difficulté du quiz atteint (1 à 10). Monte quand on réussit une session. */
    val quizLevel = mutableStateOf(1)

    /**
     * Thème visuel appliqué (id : "default" ou l'id boutique d'un skin débloqué, ex.
     * "skin_starwars"). État Compose pour que tout le thème de l'app change immédiatement
     * quand on le sélectionne dans les paramètres, sans redémarrage. Voir [com.example.macollection.ui.theme.AppTheme].
     */
    val selectedTheme = mutableStateOf("default")

    /**
     * Déblocage de test (app NORMALE uniquement, ignoré par la variante TEST) : quand true, tous
     * les jeux d'arcade et tous les thèmes sont considérés débloqués pour pouvoir les essayer,
     * sans toucher aux achats réels ni à la base. Repasser à false pour rétablir l'économie.
     */
    val devUnlockAll = mutableStateOf(false)

    /** Ids des jeux dont l'écran d'explication (règles) a déjà été vu — pour ne le montrer qu'au
     *  premier lancement de chaque jeu. État Compose pour masquer l'intro dès qu'on la valide. */
    val gameIntrosSeen = mutableStateOf<Set<String>>(emptySet())

    /** URI de la dernière sauvegarde .zip exportée, pour rouvrir l'import dans le même dossier. */
    val lastBackupUri = mutableStateOf<String?>(null)

    /**
     * Nombre de pubs récompensées (+points) déjà regardées AUJOURD'HUI — remis à 0 automatiquement
     * dès qu'on change de jour (voir [resetAdRewardsIfNewDay]). Plafonné à
     * [GameShopCatalog.DAILY_AD_REWARD_CAP] par [canWatchAdForPoints] : sans cette limite, la pub
     * récompensée (100 % opt-in) pourrait être regardée en boucle pour atteindre le Premium sans
     * jamais payer ni générer un volume de pub réellement significatif d'un coup.
     */
    val adRewardsToday = mutableStateOf(0)

    /**
     * Horodatage (epoch ms) du dernier Coffre Quotidien ouvert — cooldown glissant de 24h réel
     * (pas un simple "une fois par jour calendaire" comme [adRewardsToday]) : ouvrir le coffre à
     * 23h59 puis reminuit ne doit pas permettre de le rouvrir immédiatement une seconde fois.
     */
    val dailyChestLastOpened = mutableStateOf(0L)

    /** Scans code-barres déjà effectués AUJOURD'HUI (remis à 0 au changement de jour calendaire). */
    val barcodeScansToday = mutableStateOf(0)

    /** Scans bonus débloqués AUJOURD'HUI via pub récompensée, en plus du quota gratuit
     *  ([GameShopCatalog.FREE_DAILY_BARCODE_SCANS]) — remis à 0 au changement de jour aussi. */
    val barcodeBonusScansToday = mutableStateOf(0)

    fun load(context: Context) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        language = p.getString(KEY_LANG, "fr") ?: "fr"
        lastType = p.getString(KEY_TYPE, "CONSOLE") ?: "CONSOLE"
        onboardingSeen = p.getBoolean(KEY_ONBOARDING_SEEN, false)
        backgroundImageUri.value = p.getString(KEY_BACKGROUND_URI, null)
        customBrands.value = p.getStringSet(KEY_CUSTOM_BRANDS, emptySet()) ?: emptySet()
        currencyManuallySet = p.getBoolean(KEY_CURRENCY_MANUAL, false)
        currency.value = p.getString(KEY_CURRENCY, null) ?: CurrencyOptions.defaultForLanguage(language)
        currencyRates.value = p.getString(KEY_RATES, null)?.let { raw ->
            raw.split(",").mapNotNull { entry ->
                val parts = entry.split(":")
                val rate = parts.getOrNull(1)?.toDoubleOrNull()
                if (parts.size == 2 && rate != null) parts[0] to rate else null
            }.toMap()
        } ?: emptyMap()
        backupExportCount.value = p.getInt(KEY_BACKUP_EXPORT_COUNT, 0)
        extraBackupPacks.value = p.getInt(KEY_EXTRA_BACKUP_PACKS, 0)
        extraCollectionSlots.value = p.getInt(KEY_EXTRA_COLLECTION_SLOTS, 0)
        quizSessionsCompleted.value = p.getInt(KEY_QUIZ_SESSIONS, 0)
        quizLevel.value = p.getInt(KEY_QUIZ_LEVEL, 1)
        selectedTheme.value = p.getString(KEY_THEME, "default") ?: "default"
        gameIntrosSeen.value = p.getStringSet(KEY_GAME_INTROS_SEEN, emptySet()) ?: emptySet()
        lastBackupUri.value = p.getString(KEY_LAST_BACKUP_URI, null)
        adRewardsToday.value = if (p.getLong(KEY_AD_REWARDS_DAY, 0L) == todayEpochDay()) p.getInt(KEY_AD_REWARDS_TODAY, 0) else 0
        dailyChestLastOpened.value = p.getLong(KEY_DAILY_CHEST_LAST_OPENED, 0L)
        if (p.getLong(KEY_BARCODE_SCANS_DAY, 0L) == todayEpochDay()) {
            barcodeScansToday.value = p.getInt(KEY_BARCODE_SCANS_TODAY, 0)
            barcodeBonusScansToday.value = p.getInt(KEY_BARCODE_BONUS_TODAY, 0)
        } else {
            barcodeScansToday.value = 0
            barcodeBonusScansToday.value = 0
        }
    }

    private fun todayEpochDay(): Long = System.currentTimeMillis() / 86_400_000L

    private fun resetAdRewardsIfNewDay(context: Context) {
        val today = todayEpochDay()
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (p.getLong(KEY_AD_REWARDS_DAY, 0L) != today) {
            adRewardsToday.value = 0
            p.edit().putInt(KEY_AD_REWARDS_TODAY, 0).putLong(KEY_AD_REWARDS_DAY, today).apply()
        }
    }

    /** true si le plafond quotidien de pubs récompensées (points) n'est pas encore atteint. */
    fun canWatchAdForPoints(context: Context): Boolean {
        resetAdRewardsIfNewDay(context)
        return adRewardsToday.value < GameShopCatalog.DAILY_AD_REWARD_CAP
    }

    /** À appeler seulement après un visionnage complet confirmé (jamais sur un simple clic). */
    fun recordAdRewardWatched(context: Context) {
        resetAdRewardsIfNewDay(context)
        val updated = adRewardsToday.value + 1
        adRewardsToday.value = updated
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_AD_REWARDS_TODAY, updated).apply()
    }

    private fun resetBarcodeScansIfNewDay(context: Context) {
        val today = todayEpochDay()
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (p.getLong(KEY_BARCODE_SCANS_DAY, 0L) != today) {
            barcodeScansToday.value = 0
            barcodeBonusScansToday.value = 0
            p.edit()
                .putInt(KEY_BARCODE_SCANS_TODAY, 0)
                .putInt(KEY_BARCODE_BONUS_TODAY, 0)
                .putLong(KEY_BARCODE_SCANS_DAY, today)
                .apply()
        }
    }

    /** true si le quota gratuit du jour (+ bonus pub déjà débloqués) n'est pas encore atteint. */
    fun canScanBarcodeFree(context: Context): Boolean {
        resetBarcodeScansIfNewDay(context)
        return barcodeScansToday.value < GameShopCatalog.FREE_DAILY_BARCODE_SCANS + barcodeBonusScansToday.value
    }

    /** À appeler à chaque scan code-barres effectivement lancé (avant même de connaître le résultat). */
    fun recordBarcodeScanUsed(context: Context) {
        resetBarcodeScansIfNewDay(context)
        val updated = barcodeScansToday.value + 1
        barcodeScansToday.value = updated
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_BARCODE_SCANS_TODAY, updated).apply()
    }

    /** À appeler seulement après un visionnage complet confirmé d'une pub récompensée. */
    fun recordBarcodeBonusScanGranted(context: Context) {
        resetBarcodeScansIfNewDay(context)
        val updated = barcodeBonusScansToday.value + 1
        barcodeBonusScansToday.value = updated
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_BARCODE_BONUS_TODAY, updated).apply()
    }

    private const val CHEST_COOLDOWN_MS = 24 * 60 * 60 * 1000L

    /** true si 24h se sont écoulées depuis le dernier Coffre Quotidien ouvert (ou jamais ouvert). */
    fun canOpenDailyChest(): Boolean =
        System.currentTimeMillis() - dailyChestLastOpened.value >= CHEST_COOLDOWN_MS

    /** Millisecondes restantes avant le prochain Coffre Quotidien (0 si déjà disponible). */
    fun dailyChestRemainingMs(): Long =
        (CHEST_COOLDOWN_MS - (System.currentTimeMillis() - dailyChestLastOpened.value)).coerceAtLeast(0L)

    /** À appeler seulement après un visionnage complet confirmé (jamais sur un simple clic). */
    fun recordDailyChestOpened(context: Context) {
        val now = System.currentTimeMillis()
        dailyChestLastOpened.value = now
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putLong(KEY_DAILY_CHEST_LAST_OPENED, now).apply()
    }

    /** Mémorise le fichier de la dernière sauvegarde exportée (dossier initial de l'import). */
    fun setLastBackupUri(context: Context, uri: String) {
        lastBackupUri.value = uri
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_LAST_BACKUP_URI, uri).apply()
    }

    /** Marque l'intro (règles) d'un jeu comme vue, pour ne plus l'afficher aux lancements suivants. */
    fun markGameIntroSeen(context: Context, gameId: String) {
        if (gameIntrosSeen.value.contains(gameId)) return
        val updated = gameIntrosSeen.value + gameId
        gameIntrosSeen.value = updated
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putStringSet(KEY_GAME_INTROS_SEEN, updated).apply()
    }

    /** Applique et persiste le thème choisi (paramètres → Thème). */
    fun setSelectedTheme(context: Context, id: String) {
        selectedTheme.value = id
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_THEME, id).apply()
    }

    /** Incrémente le compteur de sessions de quiz terminées (statistique). */
    fun recordQuizCompleted(context: Context) {
        val updated = quizSessionsCompleted.value + 1
        quizSessionsCompleted.value = updated
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_QUIZ_SESSIONS, updated).apply()
    }

    /** Fixe le niveau de quiz atteint (borné 1..10) et le persiste. */
    fun setQuizLevel(context: Context, level: Int) {
        val clamped = level.coerceIn(1, 10)
        quizLevel.value = clamped
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_QUIZ_LEVEL, clamped).apply()
    }

    /** Incrémente le compteur d'exports de sauvegarde après un export réussi. */
    fun recordBackupExport(context: Context) {
        val updated = backupExportCount.value + 1
        backupExportCount.value = updated
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_BACKUP_EXPORT_COUNT, updated).apply()
    }

    /** Ajoute un pack "+5 sauvegardes" (achat boutique, rachetable). */
    fun addExtraBackupPack(context: Context) {
        val updated = extraBackupPacks.value + 1
        extraBackupPacks.value = updated
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_EXTRA_BACKUP_PACKS, updated).apply()
    }

    /**
     * Crédite +5 objets de collection gratuits après le visionnage COMPLET confirmé d'une pub
     * récompensée (jamais sur un simple clic) — cumulable sans limite de rachat.
     */
    fun addExtraCollectionSlots(context: Context, amount: Int = 5) {
        val updated = extraCollectionSlots.value + amount
        extraCollectionSlots.value = updated
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_EXTRA_COLLECTION_SLOTS, updated).apply()
    }

    /** Choix explicite de l'utilisateur (réglages) : prime désormais sur la langue à tout jamais. */
    fun setCurrency(context: Context, code: String) {
        currency.value = code
        currencyManuallySet = true
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_CURRENCY, code).putBoolean(KEY_CURRENCY_MANUAL, true).apply()
    }

    /** Devise par défaut associée à [tag], sauf si l'utilisateur en a déjà choisi une lui-même. */
    fun applyLanguageCurrencyDefault(context: Context, tag: String) {
        if (currencyManuallySet) return
        val code = CurrencyOptions.defaultForLanguage(tag)
        currency.value = code
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_CURRENCY, code).apply()
    }

    /** Best-effort, appelé au lancement : garde les anciens taux si la requête réseau échoue. */
    fun setCurrencyRates(context: Context, rates: Map<String, Double>) {
        if (rates.isEmpty()) return
        currencyRates.value = rates
        val serialized = rates.entries.joinToString(",") { "${it.key}:${it.value}" }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_RATES, serialized).putLong(KEY_RATES_AT, System.currentTimeMillis()).apply()
    }

    /** Enregistre [brand] pour les prochaines suggestions si elle n'est pas déjà connue. */
    fun addCustomBrand(context: Context, brand: String) {
        val trimmed = brand.trim()
        if (trimmed.isBlank()) return
        val alreadyKnown = KnownBrands.list.any { it.equals(trimmed, ignoreCase = true) } ||
            customBrands.value.any { it.equals(trimmed, ignoreCase = true) }
        if (alreadyKnown) return
        val updated = customBrands.value + trimmed
        customBrands.value = updated
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putStringSet(KEY_CUSTOM_BRANDS, updated).apply()
    }

    /** uri = null pour revenir au fond par défaut. */
    fun setBackgroundImageUri(context: Context, uri: String?) {
        backgroundImageUri.value = uri
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_BACKGROUND_URI, uri).apply()
    }

    fun setOnboardingSeen(context: Context) {
        onboardingSeen = true
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ONBOARDING_SEEN, true).apply()
    }

    fun setLanguage(context: Context, lang: String) {
        language = lang
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANG, lang).apply()
    }

    fun setLastType(context: Context, type: ItemType) {
        lastType = type.name
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_TYPE, type.name).apply()
    }

    /** Type par défaut du formulaire (dernier choisi, sinon CONSOLE). */
    fun defaultType(): ItemType = try {
        ItemType.valueOf(lastType)
    } catch (e: Exception) {
        ItemType.CONSOLE
    }
}
