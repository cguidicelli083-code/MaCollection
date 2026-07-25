package com.example.macollection.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Recherche de jeux hybride : IGDB (via Twitch) en source principale, RAWG en complément.
 *
 * - [loadFirst] : uniquement la 1ʳᵉ page IGDB (résultats les plus pertinents). Si IGDB n'est
 *   pas configuré ou ne renvoie rien, bascule automatiquement sur RAWG (jamais d'écran vide
 *   à cause d'une seule API).
 * - [loadMore] (« Proposer plus de résultats ») : page IGDB suivante + recherche RAWG (une
 *   seule fois), lancées en parallèle puis fusionnées sans doublons de titre — IGDB garde la
 *   priorité d'affichage, RAWG ne fait qu'ajouter ce qui manque.
 *
 * Une instance = une saisie de recherche ; en créer une nouvelle si la requête change.
 */
class HybridGameSearch(
    private val query: String,
    private val platformId: Int? = null,
    consoleHint: String? = null
) {
    // Console visée (si connue) : sert à faire remonter en tête de chaque lot les jeux
    // effectivement sortis sur cette console, sans jamais écarter les autres.
    private val hint: String? = consoleHint?.trim()?.lowercase()?.takeIf { it.isNotBlank() }

    // Filtre « Support » côté IGDB (id plateforme IGDB dérivé de la console choisie). Conservé
    // pour toute la session, donc réappliqué automatiquement aux pages suivantes.
    private val igdbPlatformId: Int? = consoleHint?.let { ConsolePlatforms.igdbPlatformId(it) }

    private var igdbPage = 0
    private var igdbDone = !IgdbCatalog.isConfigured()
    private var rawgPage = 1
    private var rawgDone = !GameCatalog.isConfigured()
    // Wikipédia + Wikidata : une seule passe chacun (pas de pagination), complètent les jeux
    // rétro absents d'IGDB/RAWG. Wikidata couvre en plus les jeux SANS article Wikipédia.
    private var wikiDone = false
    private var wikidataDone = false

    private val seenTitles = HashSet<String>()
    private val all = mutableListOf<GameInfo>()

    /** Reste-t-il des résultats à aller chercher (affichage du bouton « plus ») ? */
    val hasMore: Boolean get() = !igdbDone || !rawgDone || !wikiDone || !wikidataDone

    /** Première recherche : IGDB/RAWG + Wikipédia + Wikidata lancés ensemble, fusionnés puis triés. */
    suspend fun loadFirst(): List<GameInfo> = coroutineScope {
        // Wikipédia + Wikidata TOUJOURS lancés en parallèle : beaucoup de jeux rétro n'existent
        // que là (Wikidata même sans article Wikipédia), et RAWG renvoie souvent des
        // correspondances floues qui, seules, masqueraient le bon jeu.
        val wiki = if (!wikiDone) async { WikipediaPresetSearch.searchGames(query) } else null
        val wikidata = if (!wikidataDone) async { WikidataGameSearch.search(query) } else null
        if (!igdbDone) consumeIgdb(IgdbCatalog.search(query, igdbPage, igdbPlatformId))
        if (all.isEmpty() && !rawgDone) consumeRawg(GameCatalog.search(query, platformId, rawgPage))
        wiki?.let { consumeWiki(it.await()) }
        wikidata?.let { consumeWikidata(it.await()) }
        snapshot()
    }

    /** Page suivante IGDB + RAWG + complément Wikipédia/Wikidata, en parallèle, sans doublons. */
    suspend fun loadMore(): List<GameInfo> = coroutineScope {
        val igdb = if (!igdbDone) async { IgdbCatalog.search(query, igdbPage, igdbPlatformId) } else null
        val rawg = if (!rawgDone) async { GameCatalog.search(query, platformId, rawgPage) } else null
        val wiki = if (!wikiDone) async { WikipediaPresetSearch.searchGames(query) } else null
        val wikidata = if (!wikidataDone) async { WikidataGameSearch.search(query) } else null
        igdb?.let { consumeIgdb(it.await()) }
        rawg?.let { consumeRawg(it.await()) }
        wiki?.let { consumeWiki(it.await()) }
        wikidata?.let { consumeWikidata(it.await()) }
        snapshot()
    }

    private fun consumeWiki(page: List<GameInfo>) {
        wikiDone = true // pas de pagination Wikipédia : une seule passe
        add(page)
    }

    private fun consumeWikidata(page: List<GameInfo>) {
        wikidataDone = true // pas de pagination Wikidata : une seule passe
        add(page)
    }

    private fun consumeIgdb(page: List<GameInfo>) {
        // Page incomplète = plus rien derrière : inutile de re-proposer le bouton pour IGDB.
        if (page.size < IgdbCatalog.PAGE_SIZE) igdbDone = true
        igdbPage++
        add(page)
    }

    private fun consumeRawg(page: List<GameInfo>) {
        // RAWG finit par renvoyer des pages vides ; on s'arrête là (et on plafonne à 5 pages).
        if (page.isEmpty() || rawgPage >= 5) rawgDone = true
        rawgPage++
        add(page)
    }

    private fun add(games: List<GameInfo>) {
        val batch = if (hint != null) {
            games.sortedByDescending { it.platforms.lowercase().contains(hint) }
        } else games
        for (g in batch) {
            if (g.name.isBlank()) continue
            if (seenTitles.add(normalizeTitle(g.name))) all += g
        }
    }

    // Copie défensive (Compose a besoin d'une nouvelle instance) + tri par pertinence : le titre
    // qui correspond exactement à la requête passe devant les correspondances floues (RAWG). Tri
    // stable : à pertinence égale, on garde l'ordre d'insertion (donc la priorité console/source).
    private fun snapshot(): List<GameInfo> {
        val qNorm = normalizeTitle(query)
        val qTokens = qNorm.split(' ').filter { it.isNotEmpty() }.toSet()
        return all.sortedByDescending { relevance(normalizeTitle(it.name), qNorm, qTokens) }
    }

    private fun relevance(name: String, qNorm: String, qTokens: Set<String>): Int = when {
        name == qNorm -> 3
        qNorm.isNotEmpty() && (name.startsWith(qNorm) || qNorm.startsWith(name)) -> 2
        qTokens.isNotEmpty() && qTokens.all { name.contains(it) } -> 1
        else -> 0
    }

    // Doublon = même titre à la casse/ponctuation près (« Zelda: A Link to the Past » RAWG
    // et « Zelda - A Link to the Past » IGDB ne doivent apparaître qu'une fois).
    private fun normalizeTitle(name: String): String =
        name.lowercase().replace(Regex("[^\\p{L}\\p{Nd}]+"), " ").trim()
}
