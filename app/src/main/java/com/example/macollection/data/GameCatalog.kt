package com.example.macollection.data

import com.example.macollection.BuildConfig
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Informations d'un jeu issues du catalogue en ligne (RAWG). */
data class GameInfo(
    val sourceId: Int?,
    val name: String,
    val platforms: String,
    val genres: String,
    val releaseYear: Int?,
    val description: String,
    val coverUrl: String?,
    /** Éditeur (publisher) — uniquement disponible via la fiche détaillée [GameCatalog.detail]. */
    val publisher: String? = null,
    /** API d'origine ("rawg" ou "igdb") — [sourceId] n'a de sens que pour RAWG. */
    val source: String = "rawg"
)

// --- DTO RAWG (réponses JSON) ---
private data class RawgSearchResponse(val results: List<RawgGame>?)
private data class RawgGame(
    val id: Int,
    val name: String?,
    val released: String?,
    val background_image: String?,
    val genres: List<RawgNamed>?,
    val platforms: List<RawgPlatformWrap>?
)
private data class RawgNamed(val name: String?)
private data class RawgPlatformWrap(val platform: RawgNamed?)
private data class RawgGameDetail(
    val id: Int,
    val name: String?,
    val released: String?,
    val background_image: String?,
    val description_raw: String?,
    val genres: List<RawgNamed>?,
    val platforms: List<RawgPlatformWrap>?,
    val publishers: List<RawgNamed>?
)
private data class RawgMoviesResponse(val count: Int?, val results: List<RawgMovie>?)
private data class RawgMovie(val id: Int, val name: String?, val preview: String?, val data: RawgMovieData?)
private data class RawgMovieData(
    @SerializedName("480") val v480: String?,
    val max: String?
)

private interface RawgApi {
    @GET("api/games")
    suspend fun searchGames(
        @Query("key") key: String,
        @Query("search") query: String,
        @Query("platforms") platforms: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): RawgSearchResponse

    @GET("api/games/{id}")
    suspend fun gameDetail(
        @Path("id") id: Int,
        @Query("key") key: String
    ): RawgGameDetail

    @GET("api/games")
    suspend fun gamesByPlatform(
        @Query("key") key: String,
        @Query("platforms") platform: Int,
        @Query("ordering") ordering: String = "-added",
        @Query("page_size") pageSize: Int = 40
    ): RawgSearchResponse

    @GET("api/games/{id}/movies")
    suspend fun movies(
        @Path("id") id: Int,
        @Query("key") key: String
    ): RawgMoviesResponse
}

/**
 * Service de catalogue de jeux en ligne (RAWG).
 * La clé API est lue depuis local.properties via BuildConfig.RAWG_API_KEY.
 */
object GameCatalog {

    private val api: RawgApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.rawg.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RawgApi::class.java)
    }

    private val key: String get() = BuildConfig.RAWG_API_KEY

    /** Vrai si une clé API a été configurée. */
    fun isConfigured(): Boolean = key.isNotBlank()

    /**
     * Recherche de jeux par titre, éventuellement priorisée sur une plateforme RAWG (voir
     * [ConsolePlatforms]) pour faire remonter d'abord les résultats pertinents pour cette
     * console. Le filtre par plateforme de RAWG est cependant parfois trop strict (jeu mal
     * étiqueté, rééditions, version régionale...) et fait totalement disparaître un jeu qu'une
     * recherche Google trouverait sans peine — on complète donc TOUJOURS avec une recherche non
     * filtrée, en ajoutant les résultats absents de la première liste (jamais de doublons).
     * Liste vide en cas d'échec total.
     */
    suspend fun search(query: String, platformId: Int? = null, page: Int = 1): List<GameInfo> {
        if (!isConfigured()) return emptyList()
        val filtered = if (platformId != null) {
            try {
                api.searchGames(key, query, platforms = platformId.toString(), page = page).results.orEmpty().map { localizeGenres(it.toGameInfo()) }
            } catch (e: Exception) {
                emptyList()
            }
        } else emptyList()

        val unfiltered = try {
            api.searchGames(key, query, page = page).results.orEmpty().map { localizeGenres(it.toGameInfo()) }
        } catch (e: Exception) {
            emptyList()
        }

        if (filtered.isEmpty()) return unfiltered
        val knownIds = filtered.mapNotNull { it.sourceId }.toSet()
        return filtered + unfiltered.filter { it.sourceId == null || it.sourceId !in knownIds }
    }

    /** Liste des jeux d'une plateforme (par popularité). Vide en cas d'échec. */
    suspend fun gamesForPlatform(platformId: Int): List<GameInfo> {
        if (!isConfigured()) return emptyList()
        return try {
            api.gamesByPlatform(key, platformId).results.orEmpty().map { it.toGameInfo() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * URL d'un court extrait vidéo (mp4) du jeu, façon Batocera/Recalbox — uniquement
     * disponible pour une partie des jeux populaires sur RAWG. Null si aucun extrait.
     */
    suspend fun movieUrl(id: Int): String? {
        if (!isConfigured()) return null
        return try {
            val movie = api.movies(id, key).results.orEmpty().firstOrNull()
            movie?.data?.v480 ?: movie?.data?.max
        } catch (e: Exception) {
            null
        }
    }

    /** Fiche détaillée d'un jeu (inclut la description). Null si échec. */
    suspend fun detail(id: Int): GameInfo? {
        if (!isConfigured()) return null
        return try {
            localize(api.gameDetail(id, key).toGameInfo())
        } catch (e: Exception) {
            null
        }
    }

    // --- Localisation (français) ---

    private val genreFr = mapOf(
        "action" to "Action",
        "adventure" to "Aventure",
        "rpg" to "RPG",
        "role-playing games (rpg)" to "RPG",
        "shooter" to "Tir",
        "puzzle" to "Réflexion",
        "racing" to "Course",
        "sports" to "Sport",
        "strategy" to "Stratégie",
        "fighting" to "Combat",
        "platformer" to "Plateforme",
        "arcade" to "Arcade",
        "simulation" to "Simulation",
        "indie" to "Indé",
        "casual" to "Casual",
        "family" to "Famille",
        "card" to "Cartes",
        "board games" to "Jeux de société",
        "educational" to "Éducatif",
        "music" to "Musique",
        "massively multiplayer" to "Multijoueur massif",
        // Libellés spécifiques à IGDB (la recherche principale) :
        "platform" to "Plateforme",
        "role-playing (rpg)" to "RPG",
        "hack and slash/beat 'em up" to "Beat'em up",
        "real time strategy (rts)" to "Stratégie temps réel",
        "turn-based strategy (tbs)" to "Stratégie tour par tour",
        "point-and-click" to "Point-and-click",
        "quiz/trivia" to "Quiz",
        "pinball" to "Flipper",
        "tactical" to "Tactique",
        "simulator" to "Simulation",
        "card & board game" to "Cartes et plateau",
        "visual novel" to "Visual novel"
    )

    internal fun localizeGenres(g: GameInfo): GameInfo {
        if (AppPrefs.language != "fr" || g.genres.isBlank()) return g
        val fr = g.genres.split(",").joinToString(", ") { raw ->
            val t = raw.trim()
            genreFr[t.lowercase()] ?: t
        }
        return g.copy(genres = fr)
    }

    private suspend fun localize(g: GameInfo): GameInfo {
        if (AppPrefs.language != "fr") return g
        val withGenres = localizeGenres(g)
        val frDesc = ImageSearch.frenchDescription(g.name)
        return if (frDesc != null) withGenres.copy(description = frDesc) else withGenres
    }

    private fun RawgGame.toGameInfo() = GameInfo(
        sourceId = id,
        name = name.orEmpty(),
        platforms = platforms.orEmpty().mapNotNull { it.platform?.name }.joinToString(", "),
        genres = genres.orEmpty().mapNotNull { it.name }.joinToString(", "),
        releaseYear = released?.take(4)?.toIntOrNull(),
        description = "",
        coverUrl = background_image
    )

    // Mots-clés de variantes/rééditions qui changent la valeur de collection (une "Collector" ou
    // "Platinum" se revend souvent bien plus cher que l'édition standard du même jeu) mais que
    // RAWG/IGDB ne modélisent JAMAIS comme des jeux séparés : leur catalogue s'arrête au titre de
    // base, ces mentions ne remontent donc dans aucun résultat de recherche.
    private val editionKeywords = listOf(
        "collector", "platinum", "greatest hits", "player's choice", "players choice",
        "director's cut", "directors cut", "deluxe", "goty", "game of the year",
        "complete edition", "definitive edition", "special edition", "limited edition",
        "day one", "ultimate edition", "essentials", "classics", "premium edition"
    )

    /**
     * Retire ces mentions d'édition d'un texte de recherche : le moteur de RAWG classe très mal
     * les requêtes qui les contiennent (constaté en conditions réelles : chercher "Rayman Legends
     * Definitive Edition" ne retrouve même pas "Rayman Legends" dans les 5 premiers résultats,
     * noyé par d'autres jeux "Definitive Edition" sans rapport, alors que "Rayman Legends" seul le
     * trouve instantanément). On les retire donc de la requête envoyée aux catalogues, et on les
     * réinjecte ensuite via [preserveEditionSuffix] une fois le bon jeu retrouvé.
     */
    fun stripEditionKeywords(text: String): String {
        var result = text
        for (kw in editionKeywords) {
            // Bordures de mot obligatoires : sans elles, "collector" matchait aussi en sous-chaîne
            // à l'intérieur d'un mot plus long (ex. "Collectors Item" mutilé en "s Item").
            result = result.replace(Regex("(?i)\\b" + Regex.escape(kw) + "\\b"), " ")
        }
        return result.replace(Regex("\\s+"), " ").trim()
    }

    /**
     * Si [typed] (nom saisi/détecté par photo AVANT de choisir un résultat du catalogue) mentionne
     * une variante connue absente de [catalogName] (le titre "propre" renvoyé par RAWG/IGDB), on la
     * rajoute entre parenthèses au lieu de la perdre — sinon choisir un jeu dans le catalogue
     * écraserait silencieusement "Resident Evil Director's Cut" en simple "Resident Evil", ce qui
     * fausse ensuite la recherche de cote (une réédition ne se revend pas au même prix).
     */
    fun preserveEditionSuffix(typed: String, catalogName: String): String {
        val typedLower = typed.lowercase()
        val catalogLower = catalogName.lowercase()
        val found = editionKeywords.firstOrNull { typedLower.contains(it) && !catalogLower.contains(it) } ?: return catalogName
        val idx = typedLower.indexOf(found)
        val original = typed.substring(idx, idx + found.length).trim()
        return "$catalogName ($original)"
    }

    private fun RawgGameDetail.toGameInfo() = GameInfo(
        sourceId = id,
        name = name.orEmpty(),
        platforms = platforms.orEmpty().mapNotNull { it.platform?.name }.joinToString(", "),
        genres = genres.orEmpty().mapNotNull { it.name }.joinToString(", "),
        releaseYear = released?.take(4)?.toIntOrNull(),
        description = description_raw.orEmpty(),
        coverUrl = background_image,
        publisher = publishers.orEmpty().mapNotNull { it.name }.firstOrNull()
    )
}
