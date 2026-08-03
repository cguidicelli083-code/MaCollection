package com.example.macollection.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import retrofit2.http.Url

// --- DTO Wikidata (wbsearchentities + wbgetentities) ---
private data class WdSearchEntity(val id: String?, val label: String?, val description: String?)
private data class WdSearchResponse(val search: List<WdSearchEntity>?)
private data class WdSitelink(val title: String?)
private data class WdEntity(val sitelinks: Map<String, WdSitelink>?)
private data class WdGetResponse(val entities: Map<String, WdEntity>?)
// Résumé REST d'un article Wikipédia (description en prose + vignette).
private data class WdSummaryImage(val source: String?)
private data class WdSummary(val extract: String?, val thumbnail: WdSummaryImage?, val originalimage: WdSummaryImage?)

private interface WikidataApi {
    @Headers("User-Agent: MaCollection/1.0 (app collection jeux video)")
    @GET("w/api.php?action=wbsearchentities&format=json&type=item&limit=10")
    suspend fun searchEntities(
        @Query("search") query: String,
        @Query("language") language: String,
        @Query("uselang") uselang: String
    ): WdSearchResponse

    @Headers("User-Agent: MaCollection/1.0 (app collection jeux video)")
    @GET("w/api.php?action=wbgetentities&format=json&props=sitelinks")
    suspend fun getEntities(@Query("ids") ids: String): WdGetResponse
}

private interface WikiSummaryApi {
    @Headers("User-Agent: MaCollection/1.0 (app collection jeux video)")
    @GET
    suspend fun summary(@Url url: String): WdSummary
}

/**
 * Recherche de JEUX sur Wikidata (par titre), en complément d'IGDB/RAWG/Wikipédia. Wikidata
 * répertorie des milliers de jeux rétro (arcade, micro-ordinateurs, titres japonais…) absents de
 * RAWG et introuvables par le plein-texte Wikipédia — ex. « Star Jacker » (Q107866681).
 *
 * La courte « description » Wikidata n'est qu'un libellé (« jeu vidéo de 1983 »). Pour une VRAIE
 * description + une jaquette, on suit le lien d'article de l'entité (sitelink) et on récupère le
 * résumé Wikipédia correspondant, en préférant la langue de l'appli, puis l'anglais, puis toute
 * autre langue disponible (certains jeux très obscurs n'ont d'article que dans une seule langue).
 * Best-effort, jamais d'exception. Résultats en [GameInfo] (source = "wikidata").
 */
object WikidataGameSearch {

    private val api: WikidataApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.wikidata.org/")
            .client(NetworkClient.http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WikidataApi::class.java)
    }

    // baseUrl requis par Retrofit mais inutilisé : summary() prend une URL absolue (@Url).
    private val wiki: WikiSummaryApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.wikidata.org/")
            .client(NetworkClient.http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WikiSummaryApi::class.java)
    }

    /** Un jeu ou matériel résolu : libellé, texte (extrait d'article ou libellé court), jaquette. */
    private data class Resolved(val label: String, val text: String, val cover: String?, val shortDesc: String)

    /** Recherche de JEUX (Wikidata + article lié). */
    suspend fun search(query: String): List<GameInfo> =
        resolve(query) { WikipediaPresetSearch.isVideoGame(it) }.map { r ->
            GameInfo(
                sourceId = null, name = r.label, platforms = "", genres = "",
                releaseYear = WikipediaPresetSearch.parseYear(r.text, r.shortDesc),
                description = r.text, coverUrl = r.cover, source = "wikidata"
            )
        }

    /** Recherche de CONSOLES/ACCESSOIRES (Wikidata + article lié) : même stratégie que les jeux. */
    suspend fun searchHardware(query: String): List<WikiPresetResult> =
        resolve(query) { isHardware(it) }.map { r -> WikiPresetResult(r.label, r.text, r.cover) }

    /** Vrai si la description Wikidata décrit du matériel (console/ordinateur/accessoire…). */
    private fun isHardware(description: String?): Boolean {
        if (description.isNullOrBlank()) return false
        val d = description.lowercase()
        return Regex(
            "console|ordinateur|computer|handheld|game system|home computer|accessor|accessoire|" +
                "p[ée]riph[ée]rique|peripheral|manette|controller|joystick|gamepad|clavier|keyboard|" +
                "disk drive|lecteur|module|adapter|adaptor|add-?on"
        ).containsMatchIn(d)
    }

    /**
     * Cœur partagé : cherche les entités Wikidata par titre (FR+EN), garde celles dont la
     * description passe [keep], puis suit leur article Wikipédia lié (langue de l'appli > anglais
     * > toute langue) pour récupérer une vraie description en prose + une jaquette.
     */
    private suspend fun resolve(query: String, keep: (String?) -> Boolean): List<Resolved> = coroutineScope {
        if (query.isBlank()) return@coroutineScope emptyList()
        val langs = if (AppPrefs.language == "fr") listOf("fr", "en") else listOf("en", "fr")
        val found = langs.map { lang ->
            async {
                try {
                    api.searchEntities(query.trim(), lang, lang).search ?: emptyList()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }.awaitAll().flatten()

        val seen = HashSet<String>()
        val entities = mutableListOf<WdSearchEntity>()
        for (e in found) {
            val id = e.id ?: continue
            if (!seen.add(id)) continue
            // La recherche Wikidata est un plein-texte permissif (comme Wikipédia, cf.
            // [WikipediaPresetSearch.titleMatchesQuery]) : sans ce filtre, une entité au nom sans
            // rapport pouvait remonter juste parce qu'elle partage un mot avec la requête
            // (ex. le nom de la franchise) tout en passant le test de catégorie [keep].
            if (e.label.isNullOrBlank() || !keep(e.description) || !WikipediaPresetSearch.titleMatchesQuery(e.label, query)) continue
            entities += e
            if (entities.size >= 8) break
        }
        if (entities.isEmpty()) return@coroutineScope emptyList()

        val sitelinksById = try {
            api.getEntities(entities.mapNotNull { it.id }.joinToString("|")).entities
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        } ?: emptyMap()

        entities.map { e ->
            async {
                val pick = pickSitelink(sitelinksById[e.id]?.sitelinks)
                var extract: String? = null
                var cover: String? = null
                if (pick != null) {
                    val (lang, title) = pick
                    val enc = java.net.URLEncoder.encode(title, "UTF-8").replace("+", "%20")
                    val s = try {
                        wiki.summary("https://$lang.wikipedia.org/api/rest_v1/page/summary/$enc")
                    } catch (ex: CancellationException) {
                        throw ex
                    } catch (ex: Exception) {
                        null
                    }
                    extract = s?.extract?.takeIf { it.isNotBlank() }
                    cover = s?.thumbnail?.source ?: s?.originalimage?.source
                }
                Resolved(e.label!!.trim(), extract ?: e.description.orEmpty(), cover, e.description.orEmpty())
            }
        }.awaitAll()
    }

    /**
     * Choisit le meilleur article Wikipédia lié : langue de l'appli d'abord, puis anglais, puis
     * français, puis n'importe quelle édition linguistique (code à 2-3 lettres). Renvoie
     * (code langue, titre de l'article) ou null si aucun article.
     */
    private fun pickSitelink(sl: Map<String, WdSitelink>?): Pair<String, String>? {
        if (sl.isNullOrEmpty()) return null
        val appLang = if (AppPrefs.language.isBlank()) "en" else AppPrefs.language
        for (p in listOf(appLang, "en", "fr")) {
            sl["${p}wiki"]?.title?.let { return p to it }
        }
        val entry = sl.entries.firstOrNull {
            Regex("^[a-z]{2,3}wiki$").matches(it.key) && !it.value.title.isNullOrBlank()
        } ?: return null
        return entry.key.removeSuffix("wiki") to entry.value.title!!
    }
}
