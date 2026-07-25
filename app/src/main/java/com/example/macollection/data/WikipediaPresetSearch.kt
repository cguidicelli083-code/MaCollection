package com.example.macollection.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

// --- DTO Wikipédia (recherche large, pas seulement la meilleure correspondance) ---
private data class WikiPresetSummary(
    val extract: String?,
    // Courte description Wikidata (ex. "console de jeux vidéo de Sony..." vs "jeu vidéo de 2018")
    // utilisée uniquement pour écarter les jeux vidéo des résultats de cette recherche, qui ne
    // sert qu'à trouver des consoles/accessoires.
    val description: String?,
    val thumbnail: WikiPresetImage?,
    val originalimage: WikiPresetImage?
)
private data class WikiPresetImage(val source: String?)
private data class WikiPresetSearchResponse(val query: WikiPresetSearchQuery?)
private data class WikiPresetSearchQuery(val search: List<WikiPresetSearchItem>?)
private data class WikiPresetSearchItem(val title: String?)

private interface WikiPresetApi {
    @Headers("User-Agent: MaCollection/1.0 (app collection jeux video)")
    @GET("api/rest_v1/page/summary/{title}")
    suspend fun summary(@Path("title") title: String): WikiPresetSummary

    @Headers("User-Agent: MaCollection/1.0 (app collection jeux video)")
    @GET("w/api.php?action=query&list=search&format=json")
    suspend fun search(@Query("srsearch") query: String, @Query("srlimit") limit: Int = 20): WikiPresetSearchResponse
}

/** Résultat d'une recherche Wikipédia pour une console/un accessoire absent du catalogue intégré. */
data class WikiPresetResult(
    val title: String,
    val description: String,
    val photoUrl: String?
)

/**
 * Recherche libre sur Wikipédia (console/accessoire), pour quand le catalogue intégré et les
 * ajouts personnalisés existants ne suffisent pas. Best-effort, liste vide en cas d'échec.
 */
object WikipediaPresetSearch {

    private fun apiFor(lang: String): WikiPresetApi =
        Retrofit.Builder()
            .baseUrl("https://$lang.wikipedia.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WikiPresetApi::class.java)

    /**
     * Vrai si la courte description Wikidata identifie clairement un JEU vidéo (et non une
     * console/un accessoire/un ordinateur) — ex. "jeu vidéo de 2018", "2018 video game". Cette
     * recherche ne sert qu'au catalogue console/accessoire : un jeu remonté par la recherche
     * texte libre de Wikipédia (ex. "Marvel's Spider-Man" pour la requête "ps4 pro spiderman")
     * doit être écarté plutôt qu'affiché comme un résultat de console.
     */
    private fun looksLikeVideoGame(description: String?): Boolean {
        if (description.isNullOrBlank()) return false
        val d = description.trim().lowercase()
        // Matériel (console/accessoire/périphérique...) : jamais écarté, MÊME si la description
        // commence par « video game » — les descriptions Wikidata anglaises de consoles et
        // d'accessoires commencent très souvent ainsi (« Video game console », « Video game
        // console accessories »...) et se faisaient jeter à tort par le filtre anti-jeux,
        // d'où des recherches d'accessoires (ex. « carry case N64 ») sans aucun résultat.
        val hardware = Regex(
            "console|accessor|p[ée]riph[ée]rique|peripheral|controller|manette|joystick|gamepad|hardware|handheld"
        )
        if (hardware.containsMatchIn(d)) return false
        return Regex("^jeux? vidéo\\b").containsMatchIn(d) ||
            Regex("^(\\d{4}\\s+)?video game\\b").containsMatchIn(d)
    }

    /**
     * Recherche libre de Wikipédia (texte intégral des articles) classe par pertinence sur TOUT
     * le contenu : une requête comme "ps4 pro spider-man" y remonte aussi bien "Tony Hawk's Pro
     * Skater" (qui contient juste le mot "Pro") que "Blade" ou "Deadpool" (qui mentionnent
     * Spider-Man en passant) — ce n'est PAS une recherche par mots-clés obligatoires. On
     * reclasse donc nous-mêmes les candidats par [ConsoleRecognition.matchScore] sur leur seul
     * TITRE (signal fiable de ce dont parle vraiment la page, contrairement au corps de
     * l'article) : ceux qui ne couvrent AUCUN mot-clé de la requête sont du bruit pur et
     * écartés ; les autres sont triés du plus complet au moins complet, le tri restant stable
     * sur les égalités pour garder l'ordre de pertinence d'origine de Wikipédia.
     */
    private data class Scored(val result: WikiPresetResult, val score: Int)

    /** Bonus de pertinence quand la console associée apparaît dans le titre/résumé du résultat. */
    private fun hintBoost(r: WikiPresetResult, hint: String?): Int {
        if (hint == null) return 0
        val h = hint.lowercase()
        return (if (r.title.lowercase().contains(h)) 2 else 0) +
            (if (r.description.lowercase().contains(h)) 1 else 0)
    }

    /**
     * Cherche et score les candidats d'UNE langue Wikipédia donnée (best-effort, jamais
     * d'exception). [queries] contient la requête telle que tapée + ses variantes (abréviations
     * développées) : toutes sont lancées en parallèle et leurs titres fusionnés sans doublon.
     * Le score, lui, reste calculé sur la requête D'ORIGINE ([scoreQuery]) — matchScore résout
     * déjà les alias de son côté.
     */
    private suspend fun searchLang(lang: String, queries: List<String>, scoreQuery: String): List<Scored> {
        val api = apiFor(lang)
        val titles = coroutineScope {
            queries.map { q ->
                async {
                    try {
                        api.search(q).query?.search?.mapNotNull { it.title } ?: emptyList()
                    } catch (e: CancellationException) {
                        throw e // recherche annulée (nouvelle requête lancée) : ne pas continuer
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
            }.awaitAll().flatten().distinct()
        }
        return coroutineScope {
            titles.map { title ->
                async {
                    try {
                        val s = api.summary(title)
                        if (looksLikeVideoGame(s.description)) return@async null
                        val score = ConsoleRecognition.matchScore(listOf(title), scoreQuery)
                        if (score == 0) return@async null
                        val extract = s.extract?.takeIf { it.isNotBlank() } ?: return@async null
                        Scored(
                            WikiPresetResult(
                                title = title,
                                description = extract,
                                photoUrl = s.thumbnail?.source ?: s.originalimage?.source
                            ),
                            score
                        )
                    } catch (e: CancellationException) {
                        throw e // recherche annulée : ne pas continuer
                    } catch (e: Exception) {
                        // Candidat inaccessible : on l'ignore plutôt que d'abandonner toute la recherche.
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }

    /**
     * Cherche TOUJOURS dans les deux langues de Wikipédia (celle de l'appli en priorité, puis
     * l'autre en complément), comme [GameCatalog.search] le fait déjà pour RAWG. Indispensable :
     * certaines fiches très spécifiques (modèles obscurs, éditions limitées) n'ont d'article
     * dédié que dans une seule langue — ex. "Game Boy Light" a un article anglais propre, mais
     * redirige vers la page générale "Game Boy" en français, donc une recherche FR seule ne la
     * trouve jamais. Le tri par score garde les meilleurs résultats des deux langues, en
     * favorisant la langue de l'appli à score égal (elle est cherchée — et donc triée — en
     * premier, et le tri est stable).
     */
    /**
     * [consoleHint] (facultatif) : console associée choisie dans le formulaire d'un accessoire.
     * On aligne alors la recherche sur celle des jeux (indice console) : une variante « requête +
     * console » est ajoutée pour préciser, et les résultats mentionnant la console sont remontés.
     */
    suspend fun search(query: String, consoleHint: String? = null): List<WikiPresetResult> = coroutineScope {
        if (query.isBlank()) return@coroutineScope emptyList()
        val hint = consoleHint?.trim()?.takeIf { it.isNotBlank() }
        // Requête telle que tapée + variante avec abréviations développées (« n64 » → « nintendo
        // 64 ») : Wikipédia ne connaît pas nos alias, la variante développée fait remonter les
        // pages qui n'emploient que le nom complet. Si une console est associée, on ajoute une
        // variante « requête console » pour cibler l'accessoire de CETTE console.
        val variants = (listOf(query.trim(), ConsoleRecognition.expandQueryAliases(query)) +
            listOfNotNull(hint?.let { "${query.trim()} $it" }))
            .filter { it.isNotBlank() }.distinct()
        val primaryLang = if (AppPrefs.language == "fr") "fr" else "en"
        val secondaryLang = if (primaryLang == "fr") "en" else "fr"
        // Wikidata en parallèle (par titre) : trouve les consoles/accessoires obscurs absents ou
        // introuvables par le plein-texte Wikipédia (ex. Sega SF-7000), avec description + photo.
        val wikidata = async { WikidataGameSearch.searchHardware(query) }
        val primary = searchLang(primaryLang, variants, query)
        val secondary = searchLang(secondaryLang, variants, query)
        val wiki = (primary + secondary).sortedByDescending { it.score + hintBoost(it.result, hint) }
            .distinctBy { it.result.title.lowercase() }
            .take(8).map { it.result }
        // Wikipédia (scoré, pertinent) d'abord, Wikidata en complément, dédoublonné par titre.
        val seen = HashSet<String>()
        (wiki + wikidata.await()).filter { seen.add(it.title.lowercase()) }.take(12)
    }

    /** Vrai si la description Wikidata identifie clairement un JEU vidéo (et non du matériel). */
    internal fun isVideoGame(description: String?): Boolean {
        if (description.isNullOrBlank()) return false
        val d = description.lowercase()
        if (Regex("console|accessor|manette|controller|ordinateur|computer").containsMatchIn(d)) return false
        return Regex("jeux? vid[ée]o|video game|arcade game|jeu d'arcade|shoot.?.?em.?up|shmup|platformer|beat.?.?em.?up").containsMatchIn(d)
    }

    internal fun parseYear(vararg texts: String?): Int? =
        texts.firstNotNullOfOrNull { t -> t?.let { Regex("\\b(19[5-9]\\d|20[0-4]\\d)\\b").find(it)?.value?.toIntOrNull() } }

    /**
     * Recherche de JEUX sur Wikipédia (FR + EN), en complément d'IGDB/RAWG : de nombreux jeux
     * rétro (arcade, micro-ordinateurs, titres japonais…) ont un article Wikipédia mais sont
     * absents de RAWG. On ne garde que les pages dont la description Wikidata dit « jeu vidéo ».
     * Best-effort, liste vide en cas d'échec. Renvoie des [GameInfo] (source = "wiki").
     */
    suspend fun searchGames(query: String): List<GameInfo> = coroutineScope {
        if (query.isBlank()) return@coroutineScope emptyList()
        val langs = if (AppPrefs.language == "fr") listOf("fr", "en") else listOf("en", "fr")
        val out = mutableListOf<GameInfo>()
        val seen = HashSet<String>()
        for (lang in langs) {
            val api = apiFor(lang)
            val titles = try {
                api.search(query, 12).query?.search?.mapNotNull { it.title } ?: emptyList()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emptyList()
            }
            val infos = titles.map { title ->
                async {
                    try {
                        val s = api.summary(title)
                        if (!isVideoGame(s.description)) return@async null
                        val extract = s.extract?.takeIf { it.isNotBlank() }
                        GameInfo(
                            sourceId = null,
                            name = title,
                            platforms = "",
                            genres = "",
                            releaseYear = parseYear(s.description, extract),
                            description = extract.orEmpty(),
                            coverUrl = s.thumbnail?.source ?: s.originalimage?.source,
                            source = "wiki"
                        )
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        null
                    }
                }
            }.awaitAll().filterNotNull()
            for (gi in infos) if (seen.add(gi.name.lowercase())) out += gi
        }
        out.take(10)
    }
}
