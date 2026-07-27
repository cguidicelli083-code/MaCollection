package com.example.macollection.data

import com.example.macollection.BuildConfig
import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.Calendar
import java.util.TimeZone

// --- DTO Twitch / IGDB (réponses JSON) ---
private data class TwitchToken(val access_token: String?, val expires_in: Long?)
private data class IgdbNamed(val name: String?)
private data class IgdbCover(val image_id: String?)
private data class IgdbInvolved(val company: IgdbNamed?, val publisher: Boolean?)
private data class IgdbGame(
    val id: Long,
    val name: String?,
    val first_release_date: Long?, // secondes epoch UTC
    val summary: String?,
    val cover: IgdbCover?,
    val genres: List<IgdbNamed>?,
    val platforms: List<IgdbNamed>?,
    val involved_companies: List<IgdbInvolved>?
)

private interface TwitchAuthApi {
    @POST("oauth2/token")
    suspend fun token(
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String,
        @Query("grant_type") grantType: String = "client_credentials"
    ): TwitchToken
}

private interface IgdbApi {
    @POST("v4/games")
    suspend fun games(
        @Header("Client-ID") clientId: String,
        @Header("Authorization") bearer: String,
        @Body query: RequestBody
    ): List<IgdbGame>
}

/**
 * Catalogue de jeux IGDB (source principale de la recherche en ligne).
 * L'API IGDB s'authentifie via un compte développeur Twitch : Client ID + Secret dans
 * local.properties (voir BuildConfig.TWITCH_CLIENT_ID / TWITCH_CLIENT_SECRET), échangés
 * contre un jeton d'accès temporaire mis en cache ici et renouvelé automatiquement.
 */
object IgdbCatalog {

    /** Taille d'une page de résultats ([search] avec page = 0, 1, 2...). */
    const val PAGE_SIZE = 20

    private val TEXT = "text/plain".toMediaType()

    private val auth: TwitchAuthApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://id.twitch.tv/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TwitchAuthApi::class.java)
    }

    private val api: IgdbApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.igdb.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IgdbApi::class.java)
    }

    private val clientId: String get() = BuildConfig.TWITCH_CLIENT_ID
    private val clientSecret: String get() = BuildConfig.TWITCH_CLIENT_SECRET

    /** Vrai si les identifiants Twitch/IGDB ont été configurés. */
    fun isConfigured(): Boolean = clientId.isNotBlank() && clientSecret.isNotBlank()

    // Jeton d'accès en cache (validité ~60 jours, on garde une marge d'une minute).
    private var token: String? = null
    private var tokenExpiresAt = 0L

    private suspend fun bearer(): String? {
        val now = System.currentTimeMillis()
        token?.let { if (now < tokenExpiresAt - 60_000L) return it }
        return try {
            val t = auth.token(clientId, clientSecret)
            token = t.access_token
            tokenExpiresAt = now + (t.expires_in ?: 0L) * 1000L
            t.access_token
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.e("ScanBarcode", "IGDB token fetch failed: ${e.javaClass.simpleName}: ${e.message}", e)
            null
        }
    }

    /**
     * Une page de résultats IGDB pour [query] (pertinence décroissante), page 0 en premier.
     * Les rééditions/versions alternatives sont écartées (version_parent) pour éviter les
     * quasi-doublons. Liste vide si non configuré ou en cas d'échec réseau.
     */
    suspend fun search(query: String, page: Int, platformId: Int? = null): List<GameInfo> {
        if (!isConfigured()) {
            android.util.Log.e("ScanBarcode", "IGDB not configured (clientId/secret blank)")
            return emptyList()
        }
        // Le corps de requête IGDB est un mini-langage texte : on neutralise les caractères
        // qui casseraient la syntaxe (guillemets, antislash, point-virgule).
        val q = query.replace(Regex("[\"\\\\;]"), " ").trim()
        if (q.isBlank()) return emptyList()
        val body = buildString {
            append("search \"").append(q).append("\"; ")
            append("fields name, first_release_date, summary, cover.image_id, genres.name, platforms.name, ")
            append("involved_companies.company.name, involved_companies.publisher; ")
            // Filtre « Support » impératif quand une console est choisie : on ne garde que les
            // jeux sortis sur cette plateforme IGDB (en plus d'exclure les rééditions).
            append("where version_parent = null")
            if (platformId != null) append(" & platforms = (").append(platformId).append(")")
            append("; ")
            append("limit ").append(PAGE_SIZE).append("; offset ").append(page * PAGE_SIZE).append(";")
        }
        // Deux tentatives : si la première échoue (jeton expiré/révoqué entre-temps), on
        // repart avec un jeton neuf avant d'abandonner.
        repeat(2) { attempt ->
            val bearer = bearer() ?: return emptyList()
            try {
                return api.games(clientId, "Bearer $bearer", body.toRequestBody(TEXT))
                    .map { it.toGameInfo() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("ScanBarcode", "IGDB search failed (attempt $attempt): ${e.javaClass.simpleName}: ${e.message}", e)
                token = null
                if (attempt == 1) return emptyList()
            }
        }
        return emptyList()
    }

    /**
     * Fiche IGDB exacte pour l'identifiant [id] (pas une recherche floue) : utilisée quand
     * l'identifiant est déjà connu avec certitude (ex. [ScanDexApi], qui renvoie l'id IGDB déjà
     * apparié à un code-barres par leur propre base), pour récupérer la fiche complète sans
     * repasser par le scoring de confiance appliqué aux résultats de recherche par texte.
     */
    suspend fun byId(id: Long): GameInfo? {
        if (!isConfigured()) return null
        val body = buildString {
            append("fields name, first_release_date, summary, cover.image_id, genres.name, platforms.name, ")
            append("involved_companies.company.name, involved_companies.publisher; ")
            append("where id = ").append(id).append(";")
        }
        repeat(2) { attempt ->
            val bearer = bearer() ?: return null
            try {
                return api.games(clientId, "Bearer $bearer", body.toRequestBody(TEXT))
                    .firstOrNull()?.toGameInfo()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("ScanBarcode", "IGDB byId failed (attempt $attempt): ${e.javaClass.simpleName}: ${e.message}", e)
                token = null
                if (attempt == 1) return null
            }
        }
        return null
    }

    private fun normTitle(s: String): String =
        s.lowercase().replace(Regex("[^\\p{L}\\p{Nd}]+"), " ").trim()

    /**
     * Cherche la VRAIE jaquette (cover art « t_cover_big ») d'un jeu par son titre sur IGDB.
     * Sert à remplacer une image RAWG — qui est une capture de gameplay (`background_image`), pas
     * une jaquette — par la couverture de la boîte. On privilégie une correspondance de titre
     * exacte (et, si fournie, la bonne année), avec repli sur la première jaquette trouvée. Null
     * si non configuré, aucune correspondance ou aucune jaquette disponible.
     */
    suspend fun coverUrlFor(name: String, year: Int? = null): String? {
        if (!isConfigured() || name.isBlank()) return null
        val results = try {
            search(name, 0)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return null
        }
        val withCover = results.filter { !it.coverUrl.isNullOrBlank() }
        if (withCover.isEmpty()) return null
        val target = normTitle(name)
        val exact = withCover.filter { normTitle(it.name) == target }
        return (exact.firstOrNull { year != null && it.releaseYear == year }
            ?: exact.firstOrNull()
            ?: withCover.firstOrNull { year != null && it.releaseYear == year }
            ?: withCover.first()).coverUrl
    }

    /**
     * Complète une fiche IGDB choisie avec une description française (Wikipédia) quand c'est
     * possible — équivalent de ce que fait la fiche détaillée RAWG.
     */
    suspend fun frenchify(g: GameInfo): GameInfo {
        if (AppPrefs.language != "fr" || g.name.isBlank()) return g
        val fr = try {
            ImageSearch.frenchDescription(g.name)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
        return if (fr != null) g.copy(description = fr) else g
    }

    private fun IgdbGame.toGameInfo(): GameInfo {
        val year = first_release_date?.let {
            Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                .apply { timeInMillis = it * 1000L }
                .get(Calendar.YEAR)
        }
        val publisher = involved_companies.orEmpty().firstOrNull { it.publisher == true }?.company?.name
            ?: involved_companies.orEmpty().firstOrNull()?.company?.name
        return GameCatalog.localizeGenres(
            GameInfo(
                // Pas d'id RAWG : les fiches détaillées et vidéos RAWG ne s'appliquent pas ici,
                // toutes les infos utiles sont déjà dans la réponse de recherche.
                sourceId = null,
                name = name.orEmpty(),
                platforms = platforms.orEmpty().mapNotNull { it.name }.joinToString(", "),
                genres = genres.orEmpty().mapNotNull { it.name }.joinToString(", "),
                releaseYear = year,
                description = summary.orEmpty(),
                coverUrl = cover?.image_id?.let { "https://images.igdb.com/igdb/image/upload/t_cover_big/$it.jpg" },
                publisher = publisher,
                source = "igdb"
            )
        )
    }
}
