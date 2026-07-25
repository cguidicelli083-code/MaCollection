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
        // 2. Repli : on cherche le bon titre français puis on récupère son résumé.
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
        frApi.summary(title).extract?.takeIf { it.isNotBlank() }
    } catch (e: Exception) {
        null
    }

    private fun isLikelyPhoto(url: String): Boolean {
        val u = url.lowercase()
        return !u.contains("logo") && !u.contains(".svg")
    }
}
