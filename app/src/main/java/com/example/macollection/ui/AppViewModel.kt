package com.example.macollection.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.macollection.BuildConfig
import com.example.macollection.data.AccessoryImages
import com.example.macollection.data.AccessoryRecognition
import com.example.macollection.data.accessoryPresets
import com.example.macollection.data.AppDatabase
import com.example.macollection.data.AppPrefs
import com.example.macollection.data.BackupManager
import com.example.macollection.data.CollectionItem
import com.example.macollection.data.Condition
import com.example.macollection.data.ConsoleImages
import com.example.macollection.data.ConsolePreset
import com.example.macollection.data.AccessoryPreset
import com.example.macollection.data.ConsoleRecognition
import com.example.macollection.data.consolePresets
import com.example.macollection.data.CurrencyRates
import com.example.macollection.data.CustomPreset
import com.example.macollection.data.GeminiVision
import com.example.macollection.data.Region
import com.example.macollection.data.ExcelExport
import com.example.macollection.data.EbayPrices
import com.example.macollection.data.GameCatalog
import com.example.macollection.data.ConsolePlatforms
import com.example.macollection.data.HybridGameSearch
import com.example.macollection.data.IgdbCatalog
import com.example.macollection.data.ItemPhoto
import com.example.macollection.data.ItemType
import com.example.macollection.data.MediaUtils
import com.example.macollection.data.PresetPhotoOverride
import com.example.macollection.data.PriceHistory
import com.example.macollection.data.RetroNewsEntry
import com.example.macollection.data.RetroNewsRepository
import com.example.macollection.data.TavilyPriceEstimate
import com.example.macollection.data.combinedPremiumFlow
import com.example.macollection.data.verifiedUnlockedItemIds
import com.example.macollection.ui.billing.BillingManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/** Plafond d'objets de collection dans la variante TEST (voir BuildConfig.IS_TEST). */
const val TEST_ITEM_LIMIT = 10

/**
 * Plafond DE BASE d'objets de collection en compte GRATUIT (hors variante TEST) : consoles +
 * jeux + accessoires cumulés, souhaits non comptés (même convention que [TEST_ITEM_LIMIT]).
 * Quota RÉEL = [FREE_COLLECTION_LIMIT] + [AppPrefs.extraCollectionSlots] (+5 par pub récompensée
 * visionnée en entier, cumulable). Passer Premium (abonnement, achat à vie ou points, voir
 * [com.example.macollection.data.combinedPremiumFlow]) lève cette limite entièrement.
 */
const val FREE_COLLECTION_LIMIT = 50

/** Concurrence bornée pour les phases réseau de [AppViewModel.saveBatch] : assez pour accélérer
 *  un gros lot sans bombarder eBay/Tavily/Groq de requêtes simultanées (quotas par minute bas). */
private const val BATCH_CONCURRENCY = 3

/** Critères de tri proposés dans l'onglet Collection. */
enum class SortOption(val label: String) {
    NAME("Nom"),
    BRAND("Marque"),
    PRICE_DESC("Prix (élevé → bas)"),
    PRICE_ASC("Prix (bas → élevé)"),
    RELEASE("Date de sortie (ancien → récent)"),
    RELEASE_DESC("Date de sortie (récent → ancien)")
}

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.get(app)
    private val collectionDao = db.collectionDao()
    private val historyDao = db.priceHistoryDao()
    private val photoDao = db.itemPhotoDao()
    private val customPresetDao = db.customPresetDao()
    private val photoOverrideDao = db.presetPhotoOverrideDao()
    private val retroNewsDao = db.retroNewsDao()
    private val unlockedItemDao = db.unlockedItemDao()

    private val billing = BillingManager.get(app)

    /**
     * Statut Premium unifié (voir [combinedPremiumFlow]) : lève le plafond gratuit de
     * [FREE_COLLECTION_LIMIT] objets. Même définition EXACTE que [GameViewModel.isPremiumAllAccess]
     * (même helper partagé), pour que la Collection et l'onglet Jeux ne divergent jamais.
     */
    val isPremium: StateFlow<Boolean> =
        combinedPremiumFlow(verifiedUnlockedItemIds(unlockedItemDao), billing)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** Fiches de catalogue ajoutées manuellement par l'utilisateur (console/accessoire). */
    val customPresets: StateFlow<List<CustomPreset>> =
        customPresetDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Actus retrogaming à venir (écran Actus de l'Encyclopédie), rafraîchies au lancement. */
    val retroNews: StateFlow<List<RetroNewsEntry>> =
        retroNewsDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Photos personnalisées (nom du preset -> URI locale), qui priment sur l'image par défaut. */
    val photoOverrides: StateFlow<Map<String, String>> =
        photoOverrideDao.observeAll()
            .map { list -> list.associate { it.presetName to it.photoUri } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        // Rétro-compatibilité : les fiches perso ajoutées avant l'unification avaient leur photo
        // seulement dans CustomPreset.photoUri (pas d'override), ce qui la rendait invisible dans le
        // parcours natif. On recopie ces photos vers les overrides une fois au démarrage.
        viewModelScope.launch {
            val presets = customPresetDao.observeAll().first()
            val existing = photoOverrideDao.observeAll().first().mapTo(HashSet()) { it.presetName }
            presets.forEach { p ->
                val uri = p.photoUri
                if (uri != null && p.name !in existing) {
                    photoOverrideDao.upsert(PresetPhotoOverride(p.name, uri))
                }
            }
        }
        // Rafraîchit les actus retrogaming au lancement (best-effort, voir RetroNewsRepository).
        viewModelScope.launch {
            RetroNewsRepository.fetchLatest()?.let { retroNewsDao.upsertAll(it) }
        }
        // Fusionne les fiches déjà en base dont la console associée a été enregistrée sous deux
        // libellés différents avant l'ajout de ConsoleRecognition.canonicalize (ex. "PS3" et
        // "PlayStation 3" traités comme deux consoles distinctes au tri/regroupement) : une
        // passe silencieuse, une fois au démarrage, ne touche que les lignes dont la valeur change
        // réellement une fois canonicalisée.
        viewModelScope.launch {
            collectionDao.observeAll().first().forEach { item ->
                val raw = item.platform
                if (!raw.isNullOrBlank()) {
                    val canonical = ConsoleRecognition.canonicalizePlatformList(raw)
                    if (canonical != raw) collectionDao.update(item.copy(platform = canonical))
                }
            }
        }
    }

    fun setPresetPhoto(presetName: String, photoUri: String) = viewModelScope.launch {
        photoOverrideDao.upsert(PresetPhotoOverride(presetName, photoUri))
    }

    fun resetPresetPhoto(presetName: String) = viewModelScope.launch {
        photoOverrideDao.delete(presetName)
    }

    fun addCustomPreset(preset: CustomPreset) = viewModelScope.launch {
        customPresetDao.insert(preset.copy(createdAt = System.currentTimeMillis()))
        // La fiche perso est traitée comme une fiche native : sa photo initiale est enregistrée
        // comme override (clé = nom) pour que l'affichage et l'ajout/changement de photo passent
        // par le même mécanisme que les fiches du catalogue intégré.
        preset.photoUri?.let { photoOverrideDao.upsert(PresetPhotoOverride(preset.name, it)) }
    }

    /**
     * Modifie une fiche perso existante. Le rendu d'une fiche perso passe par le nom (clé des
     * [PresetPhotoOverride] et des vues de catalogue), donc en cas de renommage on déplace l'override
     * photo de l'ancien nom vers le nouveau et on nettoie l'ancien fichier photo s'il a été remplacé.
     */
    fun updateCustomPreset(old: CustomPreset, updated: CustomPreset) = viewModelScope.launch {
        customPresetDao.update(updated.copy(id = old.id, createdAt = old.createdAt))
        val renamed = !old.name.equals(updated.name, ignoreCase = true)
        // Photo réellement affichée avant modification (l'override prime sur CustomPreset.photoUri,
        // car l'utilisateur a pu la changer depuis la fiche).
        val currentPhoto = photoOverrideDao.observeAll().first()
            .firstOrNull { it.presetName == old.name }?.photoUri
        if (renamed) photoOverrideDao.delete(old.name)
        // La photo effectivement conservée est celle saisie dans le formulaire (updated.photoUri)
        // si elle a changé, sinon celle qui était déjà affichée avant modification (currentPhoto) :
        // un simple renommage (sans toucher à la photo) ne doit pas faire perdre l'override existant.
        val newPhoto = updated.photoUri ?: currentPhoto
        if (newPhoto != null) photoOverrideDao.upsert(PresetPhotoOverride(updated.name, newPhoto))
        else if (!renamed) photoOverrideDao.delete(updated.name)
        if (currentPhoto != null && currentPhoto != newPhoto) MediaUtils.deleteFile(currentPhoto)
    }

    fun deleteCustomPreset(preset: CustomPreset) = viewModelScope.launch {
        customPresetDao.delete(preset)
        photoOverrideDao.delete(preset.name)
        preset.photoUri?.let { MediaUtils.deleteFile(it) }
    }

    /** Fiche perso correspondant à une fiche de catalogue (par nom + type), ou null si native. */
    fun customPresetFor(name: String, type: ItemType): CustomPreset? =
        customPresets.value.firstOrNull { it.type == type && it.name.equals(name, ignoreCase = true) }

    /** Best-effort, appelé au lancement : laisse les anciens taux en cache si le réseau échoue. */
    fun refreshCurrencyRates() = viewModelScope.launch {
        CurrencyRates.fetchLatest()?.let { AppPrefs.setCurrencyRates(getApplication(), it) }
    }

    /** Sauvegarde toute la collection (données + photos) dans le fichier .zip choisi par l'utilisateur. */
    suspend fun exportBackup(destUri: Uri): Boolean =
        BackupManager.export(getApplication(), db, destUri)

    /** Export "Excel" (tableau HTML avec photos) de toute la collection + des souhaits. */
    suspend fun exportExcel(destUri: Uri): Boolean =
        ExcelExport.export(getApplication(), collectionDao.observeAll().first(), destUri)

    /** Restaure une sauvegarde .zip : remplace entièrement le contenu actuel. */
    suspend fun importBackup(srcUri: Uri): Boolean =
        BackupManager.import(getApplication(), db, srcUri)

    val sortOption = MutableStateFlow(SortOption.NAME)
    val typeFilter = MutableStateFlow<ItemType?>(null)

    /** Liste de la collection (objets réellement possédés), filtrée et triée selon les choix de l'utilisateur. */
    val items: StateFlow<List<CollectionItem>> =
        combine(collectionDao.observeAll(), sortOption, typeFilter) { list, sort, filter ->
            list.filter { !it.isWishlist && (filter == null || it.type == filter) }
                .sortedWith(sorter(sort, filter))
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Liste des acquisitions futures (même fiche que la collection, juste exclue du total). */
    val wishlist: StateFlow<List<CollectionItem>> =
        combine(collectionDao.observeAll(), sortOption, typeFilter) { list, sort, filter ->
            list.filter { it.isWishlist && (filter == null || it.type == filter) }
                .sortedWith(sorter(sort, filter))
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Collection complète (hors acquisitions futures), JAMAIS filtrée par type : contrairement à
     * [items], qui suit le filtre affiché sur l'onglet Collection. Sert aux décomptes de l'onglet
     * Total (nombre de consoles/accessoires/jeux), qui ne doivent pas dépendre de ce que
     * l'utilisateur avait sélectionné en dernier sur l'onglet Collection.
     */
    val allOwnedItems: StateFlow<List<CollectionItem>> =
        collectionDao.observeAll()
            .map { list -> list.filter { !it.isWishlist } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Valeur totale de la collection (somme des cotes connues), hors acquisitions futures. */
    val totalCents: StateFlow<Int> =
        collectionDao.observeAll()
            .map { list -> list.filter { !it.isWishlist }.sumOf { it.priceCents ?: 0 } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Pour les jeux et accessoires, la marque (éditeur/fabricant) est peu pertinente et souvent
    // absente ou peu discriminante : on trie alors par console associée à la place (même option
    // de tri, comportement adapté au type affiché).
    private fun sorter(sort: SortOption, filter: ItemType?): Comparator<CollectionItem> = when (sort) {
        SortOption.NAME -> compareBy { it.name.lowercase() }
        SortOption.BRAND -> if (filter == ItemType.JEU || filter == ItemType.ACCESSOIRE) compareBy { (it.platform ?: "").lowercase() }
            else compareBy { it.brand.lowercase() }
        SortOption.PRICE_DESC -> compareByDescending { it.priceCents ?: -1 }
        SortOption.PRICE_ASC -> compareBy { it.priceCents ?: Int.MAX_VALUE }
        SortOption.RELEASE -> compareBy { it.releaseYear ?: Int.MAX_VALUE }
        SortOption.RELEASE_DESC -> compareByDescending { it.releaseYear ?: Int.MIN_VALUE }
    }

    fun setSort(s: SortOption) { sortOption.value = s }
    fun setTypeFilter(t: ItemType?) { typeFilter.value = t }

    /**
     * Crée l'objet s'il est nouveau (id == 0), sinon met à jour l'existant.
     * Si l'utilisateur a saisi un prix à la main (`priceIsManual`), il est conservé tel quel et
     * ne déclenche AUCUNE recherche en ligne. Sinon, une cote est recherchée sur eBay en tenant
     * compte de l'état, de la boîte et de la notice (voir [EbayPrices.lookup]).
     */
    /**
     * Scan multiple : ajoute les articles cochés d'un lot comme jeux (nom + console détectés).
     * Réutilise [saveCollectionItem] : mêmes cotes eBay et même plafond TEST que l'ajout normal.
     *
     * Pour chaque article, on relance la même recherche (IGDB/RAWG/Wikipédia/Wikidata via
     * [HybridGameSearch]) que lors d'un ajout manuel et on retient le meilleur résultat
     * automatiquement (au lieu de laisser l'utilisateur choisir) : jaquette, description, genre,
     * année et éditeur sont ainsi déjà remplis sans repasser par « Modifier » pour chaque jeu.
     */
    /**
     * Non nul pendant [saveBatch] (peut prendre plus d'une minute pour un gros lot) : nombre
     * d'articles en cours d'ajout, affiché côté UI dans un indicateur de chargement. Null = inactif.
     */
    val batchSaving = MutableStateFlow<Int?>(null)

    fun saveBatch(items: List<GeminiVision.BatchItem>, isWishlist: Boolean) = viewModelScope.launch {
        batchSaving.value = items.size
        try {
            // Phase 1 (parallèle, bornée) : recherche catalogue (IGDB/RAWG/Wikipédia/Wikidata) par
            // jeu — aucun accès base de données, donc sans risque à paralléliser. C'était la première
            // moitié du temps total d'un lot, faite en série auparavant.
            val searchSemaphore = Semaphore(BATCH_CONCURRENCY)
            val prepared = coroutineScope {
                items.map { batchItem ->
                    async {
                        searchSemaphore.withPermit { buildBatchCollectionItem(batchItem, isWishlist) }
                    }
                }.awaitAll()
            }

            // Phase 2 (séquentielle, rapide) : insertion en base dans l'ordre d'origine, pour que
            // le plafond TEST_ITEM_LIMIT reste vérifié un par un sans risque de dépassement par
            // des coroutines concurrentes qui liraient le même compte avant qu'aucune n'ait inséré.
            val inserted = prepared.mapNotNull { insertOrUpdateBareItem(it) }

            // Phase 3 (parallèle, bornée) : résolution des prix (eBay puis IA) — la partie la plus
            // lente d'un lot, désormais menée de front au lieu d'un jeu après l'autre.
            val priceSemaphore = Semaphore(BATCH_CONCURRENCY)
            coroutineScope {
                inserted.map { (id, stored) ->
                    async { priceSemaphore.withPermit { resolvePriceAndFinish(id, stored) } }
                }.awaitAll()
            }
        } finally {
            batchSaving.value = null
        }
    }

    /**
     * Insère en lot des objets déjà entièrement construits (import d'un fichier Excel/CSV externe,
     * voir [SpreadsheetImport]) — contrairement à [saveBatch], pas de recherche catalogue : les
     * champs viennent directement du fichier importé. Insertion séquentielle (plafond TEST_ITEM_LIMIT)
     * puis résolution des prix manquants en parallèle, comme les phases 2/3 de [saveBatch].
     */
    fun importSpreadsheet(items: List<CollectionItem>) = viewModelScope.launch {
        batchSaving.value = items.size
        try {
            val inserted = items.mapNotNull { insertOrUpdateBareItem(it) }
            val priceSemaphore = Semaphore(BATCH_CONCURRENCY)
            coroutineScope {
                inserted.map { (id, stored) ->
                    async { priceSemaphore.withPermit { resolvePriceAndFinish(id, stored) } }
                }.awaitAll()
            }
        } finally {
            batchSaving.value = null
        }
    }

    /**
     * Ajoute en lot des fiches choisies dans l'Encyclopédie (sélection multiple Consoles/
     * Accessoires) directement à la collection ou aux souhaits, sans passer par le formulaire
     * d'édition — même mécanique d'insertion que [importSpreadsheet] (plafond TEST_ITEM_LIMIT
     * respecté un par un, puis résolution des prix manquants en parallèle).
     */
    fun addPresetsToCollection(consoles: List<ConsolePreset>, accessories: List<AccessoryPreset>, isWishlist: Boolean) = viewModelScope.launch {
        val overrides = photoOverrides.value
        val items = consoles.map { p ->
            CollectionItem(
                type = ItemType.CONSOLE,
                name = p.name,
                brand = p.brand,
                region = Region.PAL, condition = Condition.BON,
                hasBox = true, hasManual = true,
                description = p.specsText(),
                imageUri = overrides[p.name] ?: ConsoleImages.urlFor(p.name),
                isWishlist = isWishlist
            )
        } + accessories.map { p ->
            CollectionItem(
                type = ItemType.ACCESSOIRE,
                name = p.name,
                brand = p.brand,
                region = Region.PAL, condition = Condition.BON,
                hasBox = true, hasManual = true,
                description = p.specsText(),
                imageUri = overrides[p.name] ?: AccessoryImages.urlFor(p.name),
                isWishlist = isWishlist
            )
        }
        batchSaving.value = items.size
        try {
            val inserted = items.mapNotNull { insertOrUpdateBareItem(it) }
            val priceSemaphore = Semaphore(BATCH_CONCURRENCY)
            coroutineScope {
                inserted.map { (id, stored) ->
                    async { priceSemaphore.withPermit { resolvePriceAndFinish(id, stored) } }
                }.awaitAll()
            }
        } finally {
            batchSaving.value = null
        }
    }

    /**
     * Recherche catalogue pour un article détecté par lot ; ne touche pas la base. Branche selon
     * [GeminiVision.BatchItem.type] : une console ou un accessoire (ex. une 3DS XL en édition
     * spéciale) ne doit JAMAIS passer par la recherche jeu vidéo (IGDB/RAWG) — avant ce correctif,
     * TOUT article de lot était forcé en `ItemType.JEU`, donc une console scannée par lot ne
     * trouvait aucune correspondance pertinente et finissait mal catégorisée dans les Souhaits.
     */
    private suspend fun buildBatchCollectionItem(batchItem: GeminiVision.BatchItem, isWishlist: Boolean): CollectionItem {
        val title = batchItem.title.trim()
        return when (batchItem.type) {
            "console" -> buildBatchConsoleOrAccessory(title, ItemType.CONSOLE, isWishlist)
            "accessoire" -> buildBatchConsoleOrAccessory(title, ItemType.ACCESSOIRE, isWishlist)
            else -> buildBatchGame(batchItem, title, isWishlist)
        }
    }

    private fun buildBatchConsoleOrAccessory(title: String, type: ItemType, isWishlist: Boolean): CollectionItem {
        val presetName = if (type == ItemType.CONSOLE) ConsoleRecognition.recognize(title) else AccessoryRecognition.recognize(title)
        val consolePreset = presetName?.let { pn -> consolePresets.find { it.name == pn } }
        val accessoryPreset = presetName?.let { pn -> accessoryPresets.find { it.name == pn } }
        return CollectionItem(
            type = type,
            name = presetName ?: title,
            brand = consolePreset?.brand ?: accessoryPreset?.brand ?: "",
            region = Region.PAL, condition = Condition.BON,
            hasBox = true, hasManual = true,
            description = consolePreset?.specsText() ?: accessoryPreset?.specsText(),
            imageUri = presetName?.let { if (type == ItemType.CONSOLE) ConsoleImages.urlFor(it) else AccessoryImages.urlFor(it) },
            isWishlist = isWishlist
        )
    }

    private suspend fun buildBatchGame(batchItem: GeminiVision.BatchItem, title: String, isWishlist: Boolean): CollectionItem {
        val platformId = batchItem.console?.let { ConsolePlatforms.platformId(it) }
        val match = runCatching {
            HybridGameSearch(title, platformId, batchItem.console).loadFirst().firstOrNull()
        }.getOrNull()

        // Jaquette IGDB déjà = vraie couverture ; sinon (résultat RAWG/Wikipédia = capture de
        // gameplay ou logo) on va chercher la jaquette IGDB correspondante, comme le fait
        // l'ajout manuel (GameSearchDialog.onPick dans Screens.kt).
        var coverUrl = match?.coverUrl
        if (match != null && match.source != "igdb") {
            coverUrl = runCatching { IgdbCatalog.coverUrlFor(match.name, match.releaseYear) }.getOrNull() ?: coverUrl
        }

        return CollectionItem(
            type = ItemType.JEU,
            name = match?.name?.takeIf { it.isNotBlank() }?.let { GameCatalog.preserveEditionSuffix(title, it) } ?: title,
            brand = match?.publisher ?: "",
            region = Region.PAL, condition = Condition.BON,
            hasBox = true, hasManual = true,
            platform = batchItem.console?.let { ConsoleRecognition.canonicalizePlatformList(it) },
            releaseYear = match?.releaseYear,
            genre = match?.genres?.takeIf { it.isNotBlank() },
            description = match?.description?.takeIf { it.isNotBlank() },
            imageUri = coverUrl,
            rawgId = match?.sourceId,
            isWishlist = isWishlist
        )
    }

    /** Vrai pendant [fillMissingWishlistPhotos], pour désactiver son bouton et afficher un indicateur. */
    val fillingWishlistPhotos = MutableStateFlow(false)

    /**
     * Relance automatiquement une recherche en ligne (IGDB/RAWG/Wikipédia/Wikidata, même logique
     * que [saveBatch]) pour chaque JEU des souhaits qui n'a pas encore de jaquette, et applique le
     * meilleur résultat trouvé sans repasser par « Modifier » un par un. N'écrase jamais un champ
     * déjà renseigné (description/genre/année/éditeur) : ne comble que ce qui manque.
     */
    fun fillMissingWishlistPhotos() {
        if (fillingWishlistPhotos.value) return
        viewModelScope.launch {
            fillingWishlistPhotos.value = true
            try {
                val missing = collectionDao.observeAll().first()
                    .filter { it.isWishlist && it.type == ItemType.JEU && it.imageUri.isNullOrBlank() }
                for (item in missing) {
                    val platformId = item.platform?.let { ConsolePlatforms.platformId(it) }
                    val match = runCatching {
                        HybridGameSearch(item.name, platformId, item.platform).loadFirst().firstOrNull()
                    }.getOrNull() ?: continue

                    var coverUrl = match.coverUrl
                    if (match.source != "igdb") {
                        coverUrl = runCatching { IgdbCatalog.coverUrlFor(match.name, match.releaseYear) }.getOrNull() ?: coverUrl
                    }
                    if (coverUrl.isNullOrBlank()) continue

                    collectionDao.update(item.copy(
                        imageUri = coverUrl,
                        brand = item.brand.ifBlank { match.publisher ?: "" },
                        releaseYear = item.releaseYear ?: match.releaseYear,
                        genre = item.genre?.takeIf { it.isNotBlank() } ?: match.genres?.takeIf { it.isNotBlank() },
                        description = item.description?.takeIf { it.isNotBlank() } ?: match.description?.takeIf { it.isNotBlank() },
                        rawgId = item.rawgId ?: match.sourceId
                    ))
                }
            } finally {
                fillingWishlistPhotos.value = false
            }
        }
    }

    fun saveCollectionItem(item: CollectionItem, newPhotoUris: List<String> = emptyList()) =
        viewModelScope.launch { saveCollectionItemInternal(item, newPhotoUris) }

    /**
     * Corps de [saveCollectionItem], extrait en suspend function pour que [saveBatch] puisse
     * l'appeler séquentiellement (attendre chaque item avant de passer au suivant) : sinon,
     * plusieurs coroutines concurrentes liraient le même compte AVANT qu'aucune n'ait inséré son
     * item, et le plafond TEST_ITEM_LIMIT pourrait être dépassé lors d'un scan multiple.
     */
    private suspend fun saveCollectionItemInternal(item: CollectionItem, newPhotoUris: List<String> = emptyList()) {
        val (id, stored) = insertOrUpdateBareItem(item) ?: return
        resolvePriceAndFinish(id, stored, newPhotoUris)
    }

    /**
     * Insère (ou met à jour) l'item SANS résoudre son prix — juste la ligne en base. Séparé de
     * [resolvePriceAndFinish] pour que [saveBatch] puisse insérer tous les items d'un lot en
     * séquentiel (respecte le plafond TEST_ITEM_LIMIT, vérifié un par un) PUIS résoudre tous les
     * prix en parallèle (la partie lente : eBay + IA), au lieu de tout faire item par item.
     * Renvoie null si le plafond TEST_ITEM_LIMIT est atteint (rien à faire de plus pour cet item).
     */
    private suspend fun insertOrUpdateBareItem(item: CollectionItem): Pair<Long, CollectionItem>? {
        // Variante TEST : plafond de 10 objets de collection, appliqué au point de sauvegarde
        // (donc incontournable, même via scan photo, ajout depuis l'encyclopédie ou import) —
        // seuls les NOUVEAUX objets de collection (hors souhaits) sont concernés.
        if (BuildConfig.IS_TEST && item.id == 0L && !item.isWishlist) {
            val collectionCount = collectionDao.observeAll().first().count { !it.isWishlist }
            if (collectionCount >= TEST_ITEM_LIMIT) return null
        }
        // Compte gratuit (hors variante TEST) : plafond de FREE_COLLECTION_LIMIT + bonus pubs
        // récompensées, appliqué ici en dernier rempart (en plus du contrôle côté UI dans
        // MainActivity avant même d'ouvrir le formulaire) pour qu'aucun chemin d'ajout (lot,
        // import, presets Encyclo...) ne le contourne. Passer Premium lève ce plafond entièrement.
        if (!BuildConfig.IS_TEST && item.id == 0L && !item.isWishlist && !isPremium.value) {
            val quota = FREE_COLLECTION_LIMIT + AppPrefs.extraCollectionSlots.value
            val collectionCount = collectionDao.observeAll().first().count { !it.isWishlist }
            if (collectionCount >= quota) return null
        }
        val id: Long
        val stored: CollectionItem
        if (item.id == 0L) {
            stored = item.copy(createdAt = System.currentTimeMillis())
            id = collectionDao.insert(stored)
        } else {
            stored = item
            collectionDao.update(stored)
            id = stored.id
        }
        return id to stored
    }

    /** Résout le prix (eBay/IA) d'un item déjà inséré/mis à jour et attache ses éventuelles photos. */
    private suspend fun resolvePriceAndFinish(id: Long, stored: CollectionItem, newPhotoUris: List<String> = emptyList()) {
        if (stored.priceIsManual && stored.priceCents != null) {
            // La cote saisie à la main prime toujours : on ne va pas la chercher en ligne.
            recordPrice(id, stored.priceCents)
        } else {
            val resolved = resolvePrice(
                stored.barcode, stored.type, stored.brand, stored.name, stored.region,
                stored.condition, stored.hasBox, stored.hasManual, stored.platform
            )
            // Si ni eBay ni l'IA ne renvoient rien cette fois (ex. réseau indisponible pendant une
            // simple modif de photo), on garde la précédente cote connue plutôt que de l'effacer :
            // un échec ponctuel de la recherche ne doit pas faire disparaître une cote déjà valide.
            val keptOldPrice = resolved.priceCents == null && stored.priceCents != null
            val finalPriceCents = resolved.priceCents ?: stored.priceCents
            val finalIsAiEstimate = if (keptOldPrice) stored.priceIsAiEstimate else resolved.isAiEstimate
            val finalInfo = if (keptOldPrice) stored.info else resolved.info
            collectionDao.update(stored.copy(
                id = id, priceCents = finalPriceCents, priceIsManual = false,
                priceIsAiEstimate = finalIsAiEstimate, info = finalInfo
            ))
            if (finalPriceCents != null && !keptOldPrice) {
                recordPrice(id, finalPriceCents)
            }
        }

        if (newPhotoUris.isNotEmpty()) {
            attachPhotos(id, newPhotoUris)
        }
    }

    /**
     * Persiste la description traduite d'un objet déjà en base, sans relancer de lookup de cote
     * eBay (contrairement à [saveCollectionItem]) : simple mise à jour de [CollectionItem.description]
     * et [CollectionItem.descriptionLang]. Appelé par la fiche de détail après une traduction Groq.
     * Ignoré pour un objet non encore inséré (id == 0).
     */
    fun updateDescriptionTranslation(item: CollectionItem, description: String, lang: String) = viewModelScope.launch {
        if (item.id == 0L) return@launch
        collectionDao.update(item.copy(description = description, descriptionLang = lang))
    }

    fun deleteCollectionItem(item: CollectionItem) = viewModelScope.launch {
        photoDao.observeForItem(item.id).first().forEach { MediaUtils.deleteFile(it.uri) }
        photoDao.deleteForItem(item.id)
        collectionDao.delete(item)
    }

    /** Photos de galerie d'un objet (en plus de sa photo principale), dans l'ordre d'ajout. */
    fun photosFor(itemId: Long): Flow<List<ItemPhoto>> = photoDao.observeForItem(itemId)

    /** Ajoute une ou plusieurs photos à la galerie d'un objet déjà existant. */
    fun addPhotos(itemId: Long, uris: List<String>) = viewModelScope.launch {
        attachPhotos(itemId, uris)
    }

    /** Retire une photo de la galerie (et supprime son fichier local). */
    fun deletePhoto(photo: ItemPhoto) = viewModelScope.launch {
        photoDao.delete(photo)
        MediaUtils.deleteFile(photo.uri)
    }

    private suspend fun attachPhotos(itemId: Long, uris: List<String>) {
        val startPosition = photoDao.observeForItem(itemId).first().size
        uris.forEachIndexed { index, uri ->
            photoDao.insert(
                ItemPhoto(
                    itemId = itemId,
                    uri = uri,
                    position = startPosition + index,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    /**
     * Appelé à chaque ouverture : réactualise les cotes (collection + souhaits) via eBay.
     * Ne touche JAMAIS un objet dont la cote a été saisie à la main (`priceIsManual`) : la cote
     * manuelle prime toujours sur la cote recherchée en ligne, y compris lors d'une actualisation
     * ou de l'ajout d'un autre objet. Ne touche pas non plus un objet si eBay ne renvoie aucun prix.
     *
     * L'IA n'est sollicitée que pour un objet qui n'a ENCORE aucun prix (`priceCents == null`) :
     * une fois une cote obtenue (eBay ou IA), les ouvertures suivantes ne la réinterrogent plus,
     * seul eBay est revérifié (annonces réelles, comparables si ≥ 3). Avant ce correctif, l'IA était
     * resollicitée à chaque ouverture, ce qui changeait la cote affichée à chaque fois sans raison.
     */
    fun refreshAllPrices() = viewModelScope.launch {
        if (!EbayPrices.isConfigured()) return@launch

        val items = collectionDao.observeAll().first()
        for (it in items) {
            if (it.priceIsManual) continue
            val hadPrice = it.priceCents != null
            val resolved = resolvePrice(
                it.barcode, it.type, it.brand, it.name, it.region, it.condition, it.hasBox, it.hasManual, it.platform,
                allowAiRefresh = !hadPrice
            )
            // Si l'IA n'a pas été resollicitée et qu'eBay ne trouve rien, on garde la cote existante
            // plutôt que de l'écraser par un null.
            if (resolved.priceCents == null && hadPrice) continue
            collectionDao.update(it.copy(
                priceCents = resolved.priceCents, priceIsManual = false,
                priceIsAiEstimate = resolved.isAiEstimate, info = resolved.info
            ))
            if (resolved.priceCents != null) {
                recordPrice(it.id, resolved.priceCents)
            }
        }
    }

    /** Résultat de [resolvePrice] : prix final (centimes), origine et message affiché sous la cote. */
    private data class PriceResolution(val priceCents: Int?, val isAiEstimate: Boolean, val info: String?)

    /**
     * Estimation IA, avec repli automatique : Gemini (recherche intégrée, voir
     * [GeminiVision.estimatePrice]) en premier, puis Tavily+Groq (voir [TavilyPriceEstimate]) si
     * Gemini échoue — son quota gratuit très bas (20 requêtes/JOUR, vérifié en conditions réelles)
     * est épuisé en quelques items dès qu'un scan par lot ou une session d'ajouts un peu chargée le
     * dépasse. Tavily a un quota séparé et bien plus généreux, donc prend le relais le reste du jour.
     */
    private suspend fun aiEstimatePrice(
        type: ItemType, brand: String, name: String,
        condition: Condition, hasBox: Boolean, hasManual: Boolean, platform: String?
    ): Pair<Int, Boolean>? {
        runCatching { GeminiVision.estimatePrice(type, brand, name, condition, hasBox, hasManual, platform) }
            .getOrNull()?.let { return it to false }
        runCatching { TavilyPriceEstimate.estimatePrice(type, brand, name, condition, hasBox, hasManual, platform) }
            .getOrNull()?.let { return it to true }
        return null
    }

    /**
     * Calcule la cote d'un objet en combinant eBay (source principale) et l'IA ([aiEstimatePrice]) :
     *  - eBay ne trouve rien → l'IA prend le relais seule, marquée priceIsAiEstimate (« (IA) »).
     *  - eBay trouve seulement 1 ou 2 annonces (peu fiable pour une médiane) → on demande aussi
     *    l'IA et on fait une moyenne pondérée par le nombre d'annonces eBay, plutôt que de se fier
     *    à une poignée d'annonces isolées qui peuvent être mal catégorisées ou hors marché.
     *  - eBay trouve 3 annonces ou plus → assez fiable, on ne sollicite pas l'IA.
     * Partagé entre [saveCollectionItemInternal] et [refreshAllPrices] pour éviter la divergence.
     */
    private suspend fun resolvePrice(
        barcode: String?, type: ItemType, brand: String, name: String, region: Region,
        condition: Condition, hasBox: Boolean, hasManual: Boolean, platform: String?,
        allowAiRefresh: Boolean = true
    ): PriceResolution {
        val r = EbayPrices.lookup(barcode, type, brand, name, region, condition, hasBox, hasManual, platform)
        if (r.priceCents == null) {
            if (!allowAiRefresh) return PriceResolution(null, false, r.info)
            val ai = aiEstimatePrice(type, brand, name, condition, hasBox, hasManual, platform)
            return if (ai != null) {
                val (priceCents, viaTavily) = ai
                val label = if (viaTavily) {
                    "Estimation par IA (recherche web) — aucune annonce eBay comparable trouvée, quota Gemini indisponible"
                } else {
                    "Estimation par IA (recherche en ligne) — aucune annonce eBay comparable trouvée"
                }
                PriceResolution(priceCents, true, label)
            } else {
                PriceResolution(null, false, r.info)
            }
        }
        if (r.count < 3 && allowAiRefresh) {
            val ai = aiEstimatePrice(type, brand, name, condition, hasBox, hasManual, platform)
            if (ai != null) {
                val (priceCents, _) = ai
                val blended = ((r.priceCents.toLong() * r.count + priceCents) / (r.count + 1)).toInt()
                return PriceResolution(blended, false, "${r.info} — recoupé avec une estimation IA (peu d'annonces eBay disponibles)")
            }
        }
        return PriceResolution(r.priceCents, false, r.info)
    }

    /** Ajoute un point d'historique si le prix a changé depuis le dernier relevé. */
    private suspend fun recordPrice(itemId: Long, priceCents: Int) {
        val last = historyDao.latest(itemId)
        if (last == null || last.priceCents != priceCents) {
            historyDao.insert(
                PriceHistory(itemId = itemId, priceCents = priceCents, timestamp = System.currentTimeMillis())
            )
        }
    }

    /** Historique de prix d'un objet (du plus ancien au plus récent). */
    suspend fun priceHistory(itemId: Long): List<PriceHistory> = historyDao.forItem(itemId)
}
