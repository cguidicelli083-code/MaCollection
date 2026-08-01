package com.example.macollection.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

// --- DTO Wikipédia ---
private data class WikiSummary(
    val extract: String?,
    val description: String?,
    val thumbnail: WikiImage?,
    val originalimage: WikiImage?
)
private data class WikiImage(val source: String?)
private data class WikiSearchResponse(val query: WikiSearchQuery?)
private data class WikiSearchQuery(val search: List<WikiSearchItem>?)
private data class WikiSearchItem(val title: String?)

private interface WikiApi {
    @Headers("User-Agent: MaCollection/1.0 (app collection jeux video)")
    @GET("api/rest_v1/page/summary/{title}")
    suspend fun summary(@Path("title") title: String): WikiSummary

    @Headers("User-Agent: MaCollection/1.0 (app collection jeux video)")
    @GET("w/api.php?action=query&list=search&srlimit=1&format=json")
    suspend fun search(@Query("srsearch") query: String): WikiSearchResponse
}

/**
 * Accès best-effort à Wikipédia :
 * - [consoleImage] : une vraie photo de console (filtre les logos / SVG).
 * - [frenchDescription] : un résumé en français (jeux), avec repli sur une recherche
 *   si la page au nom exact n'existe pas.
 */
object ImageSearch {

    private fun apiFor(lang: String): WikiApi =
        Retrofit.Builder()
            .baseUrl("https://$lang.wikipedia.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WikiApi::class.java)

    private val enApi: WikiApi by lazy { apiFor("en") }
    private val frApi: WikiApi by lazy { apiFor("fr") }

    suspend fun consoleImage(brand: String, name: String): String? {
        val cleaned = name.replace(Regex("\\(.*?\\)"), "").trim()
        val candidates = listOf(
            "$brand $cleaned".trim(),
            cleaned,
            "$brand $name".trim()
        ).distinct().filter { it.isNotBlank() }

        for (title in candidates) {
            val src = try {
                val r = enApi.summary(title)
                r.thumbnail?.source ?: r.originalimage?.source
            } catch (e: Exception) {
                null
            }
            if (src != null && isLikelyPhoto(src)) return src
        }
        return null
    }

    /** Résumé en français d'un jeu (ou null). */
    suspend fun frenchDescription(title: String): String? {
        // 1. Essai direct par le titre.
        summaryExtract(title)?.let { return it }
        // 2. Repli : on cherche le bon titre français puis on récupère son résumé. La recherche
        // plein-texte peut tomber sur la page d'une AUTRE entité que le jeu lui-même — le plus
        // souvent la société qui l'a développé/édité, quand le jeu n'a pas sa propre page dédiée
        // (ex. nom de jeu ambigu, article inexistant) — d'où le filtre [looksLikeGameDescription]
        // avant d'accepter ce résultat.
        val found = try {
            frApi.search(title).query?.search?.firstOrNull()?.title
        } catch (e: Exception) {
            null
        }
        if (found != null && !found.equals(title, ignoreCase = true)) {
            summaryExtract(found)?.let { return it }
        }
        return null
    }

    private suspend fun summaryExtract(title: String): String? = try {
        val s = frApi.summary(title)
        s.extract?.takeIf { it.isNotBlank() && looksLikeGameDescription(s.description) }
    } catch (e: Exception) {
        null
    }

    // Le court descriptif Wikipédia ("description", ex. « jeu vidéo de plate-forme sorti en
    // 1991 ») distingue fiablement un article de JEU d'un article sur la société qui l'a créé
    // (« société japonaise de jeux vidéo », « éditeur »...). Absent (null), on ne bloque pas :
    // beaucoup d'articles de jeux n'ont pas ce descriptif renseigné.
    private val nonGameDescriptors = Regex(
        "soci[ée]t[ée]|entreprise|[ée]diteur|d[ée]veloppeur|studio|compagnie|constructeur|fabricant"
    )
    private fun looksLikeGameDescription(description: String?): Boolean =
        description.isNullOrBlank() || !nonGameDescriptors.containsMatchIn(description.lowercase())

    private fun isLikelyPhoto(url: String): Boolean {
        val u = url.lowercase()
        return !u.contains("logo") && !u.contains(".svg")
    }
}
