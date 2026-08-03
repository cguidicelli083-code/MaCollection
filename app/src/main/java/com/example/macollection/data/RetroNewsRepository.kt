package com.example.macollection.data

import android.util.Log
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

/** Traduction d'un titre+résumé pour une langue donnée (voir `translate_entry()` dans le scraper). */
private data class RetroNewsTranslationDto(
    val title: String?,
    val summary: String?
)

/** Reflet JSON exact d'une entrée de `retro_news.json` (voir `scripts/scrape_retro_news.py`). */
private data class RetroNewsDto(
    val id: String,
    val title: String,
    val summary: String?,
    val category: String,
    val sourceName: String,
    val sourceUrl: String,
    val imageUrl: String?,
    val publishedAt: String,
    val scrapedAt: String,
    /** Clé = code langue (11 langues, mêmes codes que MaCollection : en/fr/es/it/de/pt/ru/el/tr/ja/zh). */
    val translations: Map<String, RetroNewsTranslationDto>?
)

private interface RetroNewsApi {
    @GET
    suspend fun fetchNews(@Url url: String): List<RetroNewsDto>
}

/**
 * Récupère les actus retrogaming à venir publiées par le scraper (`scripts/scrape_retro_news.py`,
 * exécuté chaque nuit par le workflow GitHub Actions `.github/workflows/scrape.yml`) sous forme
 * de JSON statique hébergé sur GitHub Pages. Best-effort : en cas d'échec réseau, on garde les
 * dernières actus connues en base plutôt que de vider l'écran ou planter.
 */
object RetroNewsRepository {
    private const val FEED_URL = "https://cguidicelli083-code.github.io/MaCollection/retro_news.json"
    private val gson = Gson()

    private val api: RetroNewsApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://example.invalid/") // non utilisée, l'URL complète est passée à @Url
            .client(NetworkClient.http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetroNewsApi::class.java)
    }

    suspend fun fetchLatest(): List<RetroNewsEntry>? = try {
        api.fetchNews(FEED_URL).map {
            RetroNewsEntry(
                id = it.id,
                title = it.title,
                summary = it.summary ?: "",
                category = it.category,
                sourceName = it.sourceName,
                sourceUrl = it.sourceUrl,
                imageUrl = it.imageUrl ?: "",
                publishedAt = it.publishedAt,
                scrapedAt = it.scrapedAt,
                translationsJson = gson.toJson(it.translations ?: emptyMap<String, RetroNewsTranslationDto>())
            )
        }
    } catch (e: Exception) {
        Log.w("RetroNewsRepository", "Échec récupération actus retrogaming : ${e::class.simpleName}: ${e.message}")
        null
    }
}
