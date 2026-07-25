package com.example.macollection.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

/** Catégorie d'objet du registre. */
enum class ItemType(val label: String, val labelPlural: String) {
    CONSOLE("Console", "Consoles"),
    JEU("Jeu", "Jeux"),
    ACCESSOIRE("Accessoire", "Accessoires")
}

/** Région / zone d'un objet. */
enum class Region(val label: String) {
    PAL("PAL"),
    JAP("JAP"),
    US("US")
}

/** État physique de l'objet (influence la cote). */
enum class Condition(val label: String) {
    HS("HS"),
    MAUVAIS("Mauvais"),
    BON("Bon"),
    TRES_BON("Très bon"),
    MINT("Mint"),
    NEUF("Neuf")
}

/** Un objet possédé dans la collection. */
@Entity(tableName = "collection_items")
data class CollectionItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: ItemType,
    val name: String,
    val brand: String,
    val region: Region,
    val condition: Condition,
    val hasBox: Boolean,
    val hasManual: Boolean,
    val releaseYear: Int? = null,
    val genre: String? = null,
    val platform: String? = null,
    val priceCents: Int? = null,
    /**
     * true si [priceCents] a été saisi à la main par l'utilisateur : dans ce cas,
     * l'actualisation automatique des cotes (eBay) ne doit JAMAIS l'écraser.
     */
    val priceIsManual: Boolean = false,
    val barcode: String? = null,
    val description: String? = null,
    /**
     * Code langue ("fr", "en"…) dans lequel [description] est actuellement rédigée, une fois
     * traduite/vérifiée par Groq. null = jamais traité : la fiche de détail (re)tentera la
     * traduction à l'affichage jusqu'à ce qu'elle aboutisse, puis mémorise la langue ici pour ne
     * plus rappeler l'API. Permet de rattraper les descriptions restées en anglais faute d'une
     * traduction réussie au moment de l'ajout.
     */
    val descriptionLang: String? = null,
    val imageUri: String? = null,
    /**
     * URL de la page source (fiche produit, Wikipédia, annonce…) saisie par l'utilisateur, à
     * partir de laquelle l'import automatique ([UrlImport]) a pré-rempli/enrichi la fiche.
     * Conservée pour référence et pour pouvoir relancer l'import lors d'une modification.
     */
    val sourceUrl: String? = null,
    /** Message du dernier essai de cote en ligne (ex. "Aucune annonce trouvée"), si pas de prix. */
    val info: String? = null,
    /**
     * true si [priceCents] vient d'une estimation par IA (recherche en ligne via Gemini), utilisée
     * en dernier recours quand aucune annonce eBay comparable n'a été trouvée — moins fiable qu'une
     * vraie annonce, signalé à l'utilisateur par « (IA) » à côté du prix (voir [EbayPrices.lookup]
     * et [GeminiVision.estimatePrice]).
     */
    val priceIsAiEstimate: Boolean = false,
    /** Identifiant RAWG du jeu (si trouvé), pour récupérer plus tard sa fiche/vidéo. */
    val rawgId: Int? = null,
    /**
     * true si cet objet est une acquisition future (onglet « Acquisitions futures ») plutôt
     * qu'un objet réellement possédé : même fiche/formulaire que la collection, mais exclu de
     * la valeur totale ([AppViewModel.totalCents]) et de la liste de l'onglet Collection.
     */
    val isWishlist: Boolean = false,
    val createdAt: Long = 0
)

/** Un point d'historique de prix (cote relevée à une date). */
@Entity(tableName = "price_history")
data class PriceHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long,
    val priceCents: Int,
    val timestamp: Long
)

/**
 * Une fiche de catalogue ajoutée manuellement par l'utilisateur (console ou accessoire non
 * couvert par le catalogue intégré) : apparaît dans l'Encyclopédie au même titre que les
 * fiches intégrées, et peut servir de base de pré-remplissage comme elles.
 */
@Entity(tableName = "custom_presets")
data class CustomPreset(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** CONSOLE ou ACCESSOIRE uniquement (les jeux passent par la recherche RAWG). */
    val type: ItemType,
    val brand: String,
    val name: String,
    val year: Int? = null,
    /** Type de console (ex. « Salon », « Portable ») ou nom de la console associée si accessoire. */
    val consoleOrKind: String,
    val description: String = "",
    val photoUri: String? = null,
    val createdAt: Long = 0
)

/**
 * Vues « fiche de catalogue » d'une fiche perso : une fiche ajoutée par l'utilisateur est
 * présentée dans l'Encyclopédie exactement comme une fiche intégrée (même type, même parcours de
 * détail et d'ajout de photo). Sa photo éventuelle est prise en charge par le mécanisme commun des
 * [PresetPhotoOverride] (clé = nom), qui permet ensuite de la changer/réinitialiser comme pour
 * n'importe quelle fiche native.
 */
fun CustomPreset.toConsolePreset(): ConsolePreset = ConsolePreset(
    brand = brand,
    name = name,
    year = year ?: 0,
    kind = consoleOrKind,
    cpu = "—",
    memory = "—",
    description = description
)

fun CustomPreset.toAccessoryPreset(): AccessoryPreset = AccessoryPreset(
    brand = brand,
    name = name,
    year = year ?: 0,
    console = consoleOrKind,
    description = description
)

/**
 * Photo personnalisée choisie par l'utilisateur pour une fiche du catalogue intégré (console ou
 * accessoire), qui prime sur l'image par défaut (ConsoleImages/AccessoryImages) quand elle existe.
 * Clé = nom exact du preset.
 */
@Entity(tableName = "preset_photo_overrides")
data class PresetPhotoOverride(
    @PrimaryKey val presetName: String,
    val photoUri: String
)

/** Une photo supplémentaire associée à un objet de la collection (galerie). */
@Entity(tableName = "item_photos")
data class ItemPhoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long,
    val uri: String,
    val position: Int = 0,
    val createdAt: Long = 0
)

/** Actu retrogaming à venir (voir `scripts/scrape_retro_news.py` + écran Actus de l'Encyclopédie). */
@Entity(tableName = "retro_news")
data class RetroNewsEntry(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    /** Une des catégories du scraper (RETRO_CONSOLE, COLLECTOR_PACK, UPCOMING_CONSOLE, ARCADE_CABINET, GAME_RELEASE, REISSUE). */
    val category: String,
    val sourceName: String,
    val sourceUrl: String,
    val imageUrl: String,
    val publishedAt: String,
    val scrapedAt: String
)

/** Convertit les énumérations pour le stockage en base. */
class Converters {
    @TypeConverter fun itemTypeToString(v: ItemType): String = v.name
    @TypeConverter fun stringToItemType(v: String): ItemType = ItemType.valueOf(v)
    @TypeConverter fun regionToString(v: Region): String = v.name
    @TypeConverter fun stringToRegion(v: String): Region = Region.valueOf(v)
    @TypeConverter fun conditionToString(v: Condition): String = v.name
    @TypeConverter fun stringToCondition(v: String): Condition = Condition.valueOf(v)
}
