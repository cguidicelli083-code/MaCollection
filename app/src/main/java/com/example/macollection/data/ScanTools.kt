package com.example.macollection.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

/**
 * Outils de reconnaissance par photo (ML Kit).
 *
 * - [scanCamera] : ouvre le scanner de code-barres Google (aucune permission à gérer).
 * - [scanImage]  : analyse une image (code-barres + OCR du titre).
 */
object ScanTools {

    data class ScanResult(
        val barcode: String?,
        val suggestedName: String?,
        /** Nom de console reconnu dans le catalogue (sinon null). */
        val consolePresetName: String? = null,
        /** Nom d'accessoire reconnu (sinon null). */
        val accessoryName: String? = null,
        /** Jeu retrouvé en ligne (RAWG) à partir du texte lu (sinon null). */
        val gameMatch: GameInfo? = null,
        /** Console reconnue en même temps qu'un jeu (ex. cartouche) : à préremplir en "Console associée". */
        val gameConsoleHint: String? = null,
        /**
         * Autres pistes plausibles trouvées sur la même photo (autres lignes de texte), pour le
         * bouton « Réessayer » : permet d'essayer une autre détection sans recadrer à nouveau.
         */
        val alternatives: List<ScanCandidate> = emptyList()
    )

    /** Une piste de reconnaissance (texte/ligne) repérée sur la photo, pas encore résolue en détail. */
    data class ScanCandidate(
        val name: String,
        val type: ItemType,
        val gameMatch: GameInfo? = null,
        val consolePresetName: String? = null,
        val accessoryName: String? = null
    )

    /** Complète une piste alternative (détail RAWG si jeu) pour l'appliquer au formulaire. */
    suspend fun resolveCandidate(candidate: ScanCandidate): ScanResult {
        val detailed = candidate.gameMatch?.let { g -> g.sourceId?.let { id -> GameCatalog.detail(id) } ?: g }
        return ScanResult(
            barcode = null,
            suggestedName = candidate.name,
            consolePresetName = candidate.consolePresetName,
            accessoryName = candidate.accessoryName,
            gameMatch = detailed
        )
    }

    /** Scanner caméra : renvoie le code-barres lu, ou null si annulé / introuvable. */
    suspend fun scanCamera(context: Context): String? = try {
        GmsBarcodeScanning.getClient(context).startScan().await().rawValue
    } catch (e: Exception) {
        null
    }

    /**
     * Identifie un objet à partir de son seul code-barres (EAN/UPC) : récupère le titre d'une
     * annonce eBay correspondante, puis applique les mêmes règles de reconnaissance que pour
     * une photo (console, accessoire, ou jeu confirmé sur RAWG).
     */
    suspend fun identifyFromBarcode(barcode: String): ScanResult {
        // UPCitemdb en premier, puis Barcode Lookup : contrairement à une annonce eBay (titre
        // rédigé librement par un vendeur, plein de mentions d'état/complétude qui égarent la
        // recherche), ces deux bases renvoient un titre "propre" unique par code-barres (ex.
        // "Mario Kart 8 Deluxe (Nintendo Switch)"), bien plus fiable pour retrouver le bon jeu —
        // et à elles deux couvrent des jeux que ni l'une ni l'autre seule ne trouvait (constaté en
        // conditions réelles). Les annonces eBay restent essayées ensuite (et en repli si aucun
        // code-barres n'est connu des deux bases).
        val upcTitle = runCatching { UpcItemDb.lookupTitle(barcode) }.getOrNull()
        val barcodeLookupTitle = runCatching { BarcodeLookupApi.lookupTitle(barcode) }.getOrNull()
        val ebayTitles = runCatching { EbayPrices.titlesForBarcode(barcode) }.getOrNull().orEmpty()
        val titles = listOfNotNull(upcTitle, barcodeLookupTitle) + ebayTitles
        Log.d("ScanBarcode", "barcode=$barcode upcTitle=$upcTitle barcodeLookupTitle=$barcodeLookupTitle ebayTitles=$ebayTitles")
        val title = titles.firstOrNull() ?: return ScanResult(barcode, null)

        // Une boîte/cartouche de JEU porte presque toujours aussi le nom de sa console (mention
        // obligatoire du fabricant) : reconnaître une console dans le titre ne suffit donc pas à
        // conclure que le produit scanné EST cette console (même piège que pour l'OCR photo, cf.
        // scanImage). On cherche donc d'abord un jeu (filtré sur la console repérée si elle existe),
        // et on ne retombe sur "c'est la console elle-même" que si aucun jeu fiable n'est trouvé.
        val consoleName = ConsoleRecognition.recognize(title)
        val platformId = consoleName?.let { ConsolePlatforms.platformId(it) }
        Log.d("ScanBarcode", "consoleName=$consoleName platformId=$platformId")

        // On essaie CHAQUE titre d'annonce renvoyé (pas seulement le premier) : eBay classe
        // parfois en tête une annonce pour un objet DIFFÉRENT qui partage le même GTIN (ex. un lot
        // "3 jeux" au lieu du jeu précis scanné) — s'arrêter au premier titre qui trouve NE
        // SERAIT-CE QU'un match faible (ex. juste le préfixe de franchise) empêchait de découvrir
        // le bien meilleur match d'un titre suivant, plus propre. On garde donc le MEILLEUR score
        // toutes annonces confondues, pas le premier trouvé. On utilise aussi IGDB en priorité
        // (meilleure couverture rétro que RAWG seul, cf. [firstGameMatch] déjà utilisé par le scan
        // photo), et on nettoie chaque titre du bruit typique d'une annonce (état, complétude,
        // marque/plateforme déjà identifiée à part, mentions d'édition) AVANT de chercher (cf.
        // [cleanListingTitle]).
        var best: Pair<GameInfo, Double>? = null
        var bestOriginalTitle: String? = null
        for (t in titles) {
            val cleaned = cleanListingTitle(t).ifBlank { t }
            val scored = firstGameMatchScored(cleaned, platformId)
            Log.d("ScanBarcode", "firstGameMatch(\"$cleaned\") -> ${scored?.first?.name} (score=${scored?.second})")
            if (scored != null && (best == null || scored.second > best!!.second)) {
                best = scored
                bestOriginalTitle = t
            }
        }
        val gameMatch = best?.let { (g, _) -> g.copy(name = GameCatalog.preserveEditionSuffix(bestOriginalTitle!!, g.name)) }

        var accessoryName: String? = null
        if (gameMatch == null) {
            accessoryName = titles.firstNotNullOfOrNull { AccessoryRecognition.recognize(it) }
        }
        val suggestedName = gameMatch?.name ?: accessoryName ?: consoleName ?: title
        val gameConsoleHint = if (gameMatch != null) consoleName else null
        Log.d("ScanBarcode", "RESULT suggestedName=$suggestedName gameMatch=${gameMatch?.name} console=$consoleName accessory=$accessoryName")
        return ScanResult(barcode, suggestedName, consoleName.takeIf { gameMatch == null }, accessoryName, gameMatch, gameConsoleHint)
    }

    /**
     * Analyse une photo : code-barres + identification de la console (si reconnue dans
     * le catalogue) ou, à défaut, une ligne de texte « propre » (utile pour un titre de jeu).
     * Ne lève jamais d'exception.
     */
    suspend fun scanImage(context: Context, uri: Uri, onDeepScan: (() -> Unit)? = null): ScanResult {
        // Prétraitement local (contraste rehaussé) pour une meilleure lecture OCR ; repli sur
        // l'image d'origine si le traitement échoue.
        val image = try {
            ImagePreprocess.enhance(context, uri)?.let { InputImage.fromBitmap(it, 0) }
                ?: InputImage.fromFilePath(context, uri)
        } catch (e: Exception) {
            return ScanResult(null, null)
        }

        val barcodeScanner = BarcodeScanning.getClient()
        val barcode = try {
            barcodeScanner.process(image).await().firstOrNull()?.rawValue
        } catch (e: Exception) {
            null
        } finally {
            barcodeScanner.close()
        }

        var consoleName: String? = null
        var accessoryName: String? = null
        var suggestedName: String? = null
        var gameMatch: GameInfo? = null
        // Fermés dans le finally ci-dessous : ML Kit documente ces recognizers comme Closeable,
        // sans quoi chaque scan fuit un détecteur natif (scanImage est appelé à chaque photo).
        val latinRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val japaneseRecognizer = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
        try {
            // Le modèle latin par défaut ne lit pas le japonais (kanji/hiragana/katakana) :
            // on lance aussi le modèle dédié et on combine les deux lectures. Beaucoup de
            // boîtes/cartouches japonaises portent aussi le titre en lettres latines ailleurs
            // sur l'emballage, donc combiner les deux augmente les chances de tout capter.
            val textLatin = latinRecognizer.process(image).await()
            val textJapanese: Text? = runCatching {
                japaneseRecognizer.process(image).await()
            }.getOrNull()

            val combinedText = listOfNotNull(textLatin.text, textJapanese?.text).joinToString("\n")

            // 1) Reconnaissance d'une console connue à partir du texte complet. Attention :
            // une boîte/cartouche de JEU porte presque toujours aussi le nom de la console
            // (mention obligatoire du fabricant) — ce repérage seul ne suffit donc pas à
            // distinguer "photo de la console" de "photo d'un jeu pour cette console".
            consoleName = ConsoleRecognition.recognize(combinedText)

            // 2) Lignes « propres » (pas un numéro de série), triées par TAILLE DE TEXTE
            // décroissante (hauteur de la boîte de détection ML Kit) : un gros titre de logo a
            // beaucoup plus de chances d'être le vrai titre du jeu/produit qu'une mention écrite
            // en tout petit (mentions légales, numéro de série...). La longueur du texte ne
            // départage qu'en cas d'égalité de taille. Les blocs OCR (regroupement visuel) sont
            // ajoutés en plus des lignes individuelles : un titre stylisé (logo) est souvent
            // coupé sur 2-3 lignes séparées (ex. « STREET » / « FIGHTER » / « II ») et aucune
            // ligne seule ne contient alors assez de mots du titre pour passer le contrôle de
            // confiance — le bloc entier, lui, recolle le titre complet (on lui donne la hauteur
            // moyenne de ses lignes).
            fun cleanCandidate(raw: String) = raw.trim().replace(Regex("\\s+"), " ")
            data class TextCandidate(val text: String, val height: Int)

            val lineCandidates = textLatin.textBlocks.flatMap { block -> block.lines.map { TextCandidate(it.text, it.boundingBox?.height() ?: 0) } } +
                (textJapanese?.textBlocks?.flatMap { block -> block.lines.map { TextCandidate(it.text, it.boundingBox?.height() ?: 0) } } ?: emptyList())
            val blockCandidates = textLatin.textBlocks.map { block ->
                TextCandidate(block.text, block.lines.mapNotNull { it.boundingBox?.height() }.average().takeIf { !it.isNaN() }?.toInt() ?: 0)
            } + (textJapanese?.textBlocks?.map { block ->
                TextCandidate(block.text, block.lines.mapNotNull { it.boundingBox?.height() }.average().takeIf { !it.isNaN() }?.toInt() ?: 0)
            } ?: emptyList())

            val cleanLines = (lineCandidates + blockCandidates)
                .map { TextCandidate(cleanCandidate(it.text), it.height) }
                .filter { c ->
                    c.text.length in 3..50 &&
                        c.text.count { it.isDigit() }.toDouble() / c.text.length < 0.3
                }
                .distinctBy { it.text }
                .sortedByDescending { it.height }
                .map { it.text }
            val cleanLine = cleanLines.firstOrNull()

            // 3) On tente une recherche RAWG avec chacune de ces lignes (la plus grande taille de
            // texte d'abord), filtrée sur la console déjà reconnue si on en a une. RAWG renvoie
            // presque toujours « quelque chose », même pour une requête sans rapport : on ne
            // garde un résultat que s'il partage vraiment des mots avec la ligne lue. On exclut
            // aussi les lignes qui sont elles-mêmes une mention de console/accessoire (ex.
            // « Super Nintendo Entertainment System » sur l'étiquette) : ce n'est pas un titre
            // de jeu, et un mot générique comme « Super » suffirait sinon à matcher n'importe
            // quoi (ex. « Super Smash Bros » à la place du vrai jeu).
            // On ne s'arrête plus au premier match trouvé : on en garde jusqu'à 5, pour que le
            // bouton « Réessayer » puisse proposer une autre piste sans recadrer à nouveau.
            val platformId = consoleName?.let { ConsolePlatforms.platformId(it) }
            val gameCandidates = mutableListOf<GameInfo>()
            for (line in cleanLines.take(8)) {
                if (gameCandidates.size >= 5) break
                val matchedConsole = ConsoleRecognition.recognize(line)
                val matchedAccessory = AccessoryRecognition.recognize(line)
                if (matchedConsole != null || matchedAccessory != null) {
                    // Si la ligne/bloc ne contient QUE la mention console/accessoire (rien
                    // d'autre de significatif), c'est du pur texte de boîtier sans titre : on
                    // l'ignore. Mais un bloc OCR regroupe parfois le titre du jeu ET la mention
                    // console ensemble (ex. « SUPER FAMICOM STREET FIGHTER II TURBO ») — dans ce
                    // cas il reste d'autres mots significatifs après avoir retiré ceux de la
                    // mention reconnue, donc on tente quand même la recherche ; le contrôle de
                    // confiance plus bas filtrera un faux positif sur le seul mot de la console.
                    val matchedWords = significantWords(matchedConsole ?: matchedAccessory ?: "")
                    val remainingWords = significantWords(line) - matchedWords
                    if (remainingWords.size < 2) continue
                }
                if (isBoilerplateLine(line)) continue
                val candidate = runCatching { GameCatalog.search(line, platformId) }.getOrNull()?.firstOrNull()
                if (candidate != null && isConfidentGameMatch(line, candidate.name) &&
                    gameCandidates.none { it.name.equals(candidate.name, ignoreCase = true) }
                ) {
                    // Le catalogue (RAWG/IGDB) ne connaît que le titre de base : sans ça, une
                    // mention d'édition lue sur la jaquette (ex. "Platinum", "Collector")
                    // disparaissait silencieusement au profit du nom "propre" du catalogue.
                    gameCandidates += candidate.copy(name = GameCatalog.preserveEditionSuffix(line, candidate.name))
                }
            }
            gameMatch = gameCandidates.firstOrNull()

            // 4) Reconnaissance d'un accessoire connu — toujours essayée, même si une console a
            // été reconnue : une boîte d'accessoire (ex. lunettes 3D Sega) porte presque
            // toujours aussi le nom de la console compatible (« pour Master System »), donc la
            // détection de console seule ne doit pas empêcher de voir qu'il s'agit en réalité
            // d'un accessoire. Un mot-clé d'accessoire est très spécifique : s'il matche, il
            // prime sur la détection de console.
            if (gameMatch == null) {
                AccessoryRecognition.recognize(combinedText)?.let {
                    accessoryName = it
                    consoleName = null
                }
            }

            // Une fois un jeu confirmé, on récupère sa fiche détaillée (éditeur, description
            // complète) plutôt que les infos partielles de la recherche — ça remplit aussi la
            // marque automatiquement dans le formulaire.
            gameMatch?.sourceId?.let { id ->
                GameCatalog.detail(id)?.let { gameMatch = it }
            }

            val gameConsoleHint = if (gameMatch != null) consoleName else null
            suggestedName = gameMatch?.name ?: consoleName ?: accessoryName ?: suggestedName ?: cleanLine

            // Pistes alternatives pour le bouton « Réessayer » (sans le résultat déjà retenu) :
            // les autres titres de jeu trouvés, puis la console/l'accessoire reconnu s'il n'est
            // pas déjà le résultat principal.
            val alternatives = mutableListOf<ScanCandidate>()
            gameCandidates.drop(if (gameMatch != null) 1 else 0).forEach { g ->
                if (g.sourceId != gameMatch?.sourceId) alternatives += ScanCandidate(g.name, ItemType.JEU, gameMatch = g)
            }
            if (gameMatch != null) {
                consoleName?.let { alternatives += ScanCandidate(it, ItemType.CONSOLE, consolePresetName = it) }
            }
            accessoryName?.takeIf { it != suggestedName }?.let {
                alternatives += ScanCandidate(it, ItemType.ACCESSOIRE, accessoryName = it)
            }

            // NIVEAU 1 (OCR) concluant : correspondance console/accessoire/jeu trouvée -> on valide.
            if (gameMatch != null || consoleName != null || accessoryName != null) {
                return ScanResult(
                    barcode, suggestedName, consoleName.takeIf { gameMatch == null }, accessoryName, gameMatch, gameConsoleHint,
                    alternatives = alternatives
                )
            }
        } catch (e: Exception) {
            // ignore : on tente l'analyse approfondie ci-dessous
        } finally {
            latinRecognizer.close()
            japaneseRecognizer.close()
        }

        // NIVEAU 2 (analyse approfondie) : OCR ambigu ou infructueux -> reconnaissance visuelle
        // par IA (Gemini) sur toute la photo. Le loader « Analyse approfondie… » est déclenché ici.
        onDeepScan?.invoke()
        deepScanImage(context, uri, barcode)?.let { return it }
        return ScanResult(barcode, suggestedName, consoleName, accessoryName, gameMatch)
    }

    /** NIVEAU 2 : identification visuelle IA (Gemini) structurée quand l'OCR est ambigu. */
    private suspend fun deepScanImage(context: Context, uri: Uri, barcode: String?): ScanResult? {
        // Gemini d'abord : meilleure qualité de reconnaissance que Groq (Qwen3.6, vérifié en
        // conditions réelles) et ne coûte qu'UN appel Gemini par scan, donc l'inverser pour
        // économiser le quota Gemini n'apportait quasiment rien face à la perte de qualité.
        // Repli sur Groq si Gemini échoue (quota atteint, réseau…).
        val v = GeminiVision.identify(context, uri) ?: GroqVision.identify(context, uri) ?: return null
        // Console/accessoire reconnu en BDD -> fiche exacte.
        ConsoleRecognition.recognize(v.name)?.let { return ScanResult(barcode, it, consolePresetName = it) }
        AccessoryRecognition.recognize(v.name)?.let { return ScanResult(barcode, it, accessoryName = it) }
        // Type matériel identifié par l'IA mais absent de la BDD : on garde juste le nom.
        if (v.type == "console" || v.type == "accessoire") return ScanResult(barcode, v.name)
        // Jeu : recherche IGDB/RAWG filtrée sur la console détectée par l'IA si disponible.
        val platformId = v.console?.let { ConsolePlatforms.platformId(it) }
        val game = firstGameMatch(v.name, platformId)
        // v.name (IA) garde déjà la mention d'édition (Platinum, Collector...) depuis le prompt
        // Gemini/Groq ; le catalogue (RAWG/IGDB) ne connaît que le titre de base, donc on la
        // réinjecte pour ne pas la perdre en préférant le nom "propre" du catalogue.
        val finalName = game?.name?.takeIf { it.isNotBlank() }?.let { GameCatalog.preserveEditionSuffix(v.name, it) } ?: v.name
        return ScanResult(barcode, finalName, gameMatch = game?.copy(name = finalName), gameConsoleHint = v.console)
    }

    /**
     * Meilleure fiche jeu correspondant à [query] : IGDB d'abord (source principale), RAWG en
     * secours. Une fiche IGDB est déjà complète ; une fiche RAWG est enrichie via son détail.
     * Renvoie null si aucune correspondance fiable (mêmes règles de confiance que l'OCR).
     *
     * On garde le candidat au MEILLEUR score (pas le premier qui passe le seuil) : RAWG/IGDB
     * renvoient les résultats par pertinence de LEUR moteur, pas par proximité avec notre requête
     * nettoyée — un résultat plus loin dans la liste correspond parfois bien mieux qu'un résultat
     * plus haut qui ne partage qu'un mot générique.
     */
    private suspend fun firstGameMatch(query: String, platformId: Int?): GameInfo? =
        firstGameMatchScored(query, platformId)?.first

    /** Variante de [firstGameMatch] qui renvoie aussi le score, pour comparer entre plusieurs requêtes
     * (cf. [identifyFromBarcode], qui essaie plusieurs titres d'annonce pour un même code-barres). */
    private suspend fun firstGameMatchScored(query: String, platformId: Int?): Pair<GameInfo, Double>? {
        val igdbResults = runCatching { IgdbCatalog.search(query, 0) }.getOrNull().orEmpty()
        Log.d("ScanBarcode", "  IGDB candidates for \"$query\": ${igdbResults.take(5).map { it.name }}")
        val igdbBest = igdbResults.mapNotNull { g -> gameMatchScore(query, g.name)?.let { it to g } }.maxByOrNull { it.first }
        if (igdbBest != null) return IgdbCatalog.frenchify(igdbBest.second) to igdbBest.first
        val rawgResults = runCatching { GameCatalog.search(query, platformId) }.getOrNull().orEmpty()
        Log.d("ScanBarcode", "  RAWG candidates for \"$query\": ${rawgResults.take(5).map { it.name }}")
        val rawgBest = rawgResults.mapNotNull { g -> gameMatchScore(query, g.name)?.let { it to g } }.maxByOrNull { it.first }
            ?: return null
        val detailed = rawgBest.second.sourceId?.let { id -> GameCatalog.detail(id) } ?: rawgBest.second
        return detailed to rawgBest.first
    }

    /** Mentions légales/standard très fréquentes sur boîtes et cartouches : jamais un titre de jeu. */
    private val boilerplatePhrases = listOf(
        "made in japan", "made in china", "made in usa", "made in malaysia", "made in mexico",
        "fabrique au japon", "fabriqué au japon", "fabrique en chine", "fabriqué en chine",
        "licensed by", "all rights reserved", "tous droits reserves", "tous droits réservés",
        "nintendo seal", "official nintendo seal", "patent pending", "pat pend",
        "compatible with", "for use with", "ne convient pas", "not for resale"
    )

    private fun isBoilerplateLine(line: String): Boolean {
        val t = line.lowercase()
        return boilerplatePhrases.any { t.contains(it) }
    }

    // Mots-outils très courants (FR/EN) à ignorer même à 2-3 lettres : sans cette liste, les
    // garder à ce seuil (cf. ci-dessous) les ferait compter comme un "mot commun" entre deux
    // titres totalement différents qui n'ont en réalité que ça en commun.
    private val stopWords = setOf(
        "le", "la", "les", "un", "une", "des", "du", "de", "et", "ou", "où", "est", "sur", "pour",
        "avec", "dans", "par", "au", "aux", "ce", "ces", "the", "and", "of", "in", "on", "to",
        "for", "with", "vs", "by", "is", "as", "at", "from"
    )

    /**
     * Seuil abaissé à 2 lettres (au lieu de 3) : un suffixe de plateforme court ("DS", "Wii", "HD",
     * "DX"...) est souvent LE mot qui distingue un jeu d'un autre de la même franchise (ex. "Mario
     * Kart DS" vs "Mario Kart 8") — le perdre réduisait ces titres à seulement 2 mots génériques
     * ("mario", "kart"), qui matchaient alors n'importe quel autre "Mario Kart" par la règle plus
     * permissive réservée aux noms courts. Un chiffre seul (le "8", le "4" d'un "IV" normalisé...)
     * reste gardé même à 1 caractère, pour la même raison.
     */
    private fun significantWords(s: String): Set<String> =
        s.lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .split(" ")
            .filter { it.isNotEmpty() && it !in stopWords && (it.length >= 2 || it.all(Char::isDigit)) }
            .toSet()

    /**
     * Score de confiance (0 si aucune correspondance fiable, sinon >0 — plus haut = meilleur) entre
     * [query] et [candidate], ou null si aucune correspondance fiable. Un seul mot en commun ne
     * suffit JAMAIS (même un mot rare) : un logo d'éditeur omniprésent comme « CAPCOM » sur la
     * cartouche matchait n'importe quel jeu Capcom au hasard (ex. « Capcom's MVP Football » pour
     * une cartouche Street Fighter II) avant ce durcissement.
     *
     * Renvoyer un SCORE (pas juste un booléen) permet, quand plusieurs candidats/titres passent
     * le seuil, de garder le MEILLEUR plutôt que le premier trouvé — important pour le scan
     * code-barres, où une annonce eBay mal choisie peut faire passer un match faible (ex. juste
     * le préfixe de franchise « Tom Clancy's ») avant le bon match, beaucoup plus fort, trouvé
     * dans une autre annonce du même code-barres (cf. [firstGameMatch] et [identifyFromBarcode]).
     */
    private fun gameMatchScore(query: String, candidate: String): Double? {
        // Les mentions d'édition ("Deluxe", "Definitive Edition"...) sont retirées des DEUX côtés
        // avant de comparer : un candidat catalogue les inclut parfois dans son nom officiel (ex.
        // "CastleStorm - Definitive Edition"), et les compter faussait la comparaison dans les deux
        // sens (cf. [GameCatalog.stripEditionKeywords]).
        val qWords = significantWords(GameCatalog.stripEditionKeywords(query))
        val cWords = significantWords(GameCatalog.stripEditionKeywords(candidate))
        if (qWords.size < 2 || cWords.isEmpty()) return null
        val overlap = qWords.intersect(cWords)
        return if (cWords.size <= 2) {
            // Nom de jeu court (ex. "Rayman Legends") : les mots RESTANTS après nettoyage sont
            // rares/spécifiques, donc on exige qu'ILS SOIENT TOUS retrouvés (pas de ratio permissif
            // possible avec seulement 1-2 mots). Le plancher est plus haut que pour les noms longs
            // (0.5 au lieu de tolérer un ratio bas) : un nom réduit à 1-2 mots très génériques par le
            // nettoyage (ex. "Tom Clancy's H.A.W.X." réduit à "tom"/"clancy", le vrai titre distinctif
            // ayant disparu dans les points/majuscules) ne doit PAS matcher juste sur ce préfixe de
            // franchise partagé par des dizaines d'autres jeux.
            val ratioQ = overlap.size.toDouble() / qWords.size
            if (overlap.size == cWords.size && ratioQ >= 0.5) ratioQ else null
        } else {
            // Nom de jeu plus long : exige une bonne couverture des DEUX côtés. Un seul mot en
            // commun (même un mot rare) ne suffit JAMAIS — un logo d'éditeur omniprésent comme
            // « CAPCOM » matchait n'importe quel jeu Capcom au hasard (ex. « Capcom's MVP Football »
            // pour une cartouche Street Fighter II), et un sous-titre générique partagé (ex.
            // "Super"/"Bros" entre deux jeux Nintendo totalement différents) ne doit pas suffire
            // non plus : la couverture doit être bonne des deux côtés à la fois, pas d'un seul.
            val ratioQ = overlap.size.toDouble() / qWords.size
            val ratioC = overlap.size.toDouble() / cWords.size
            if (overlap.size >= 2 && ratioQ >= 0.6 && ratioC >= 0.6) minOf(ratioQ, ratioC) else null
        }
    }

    private fun isConfidentGameMatch(query: String, candidate: String): Boolean =
        gameMatchScore(query, candidate) != null

    /** Marques/plateformes et vocabulaire d'état très fréquents sur une annonce eBay (jeu vidéo) :
     * jamais des mots du titre du jeu lui-même, mais leur présence isolée dans le texte peut
     * fausser le classement des catalogues (cf. [cleanListingTitle]). Les marques/plateformes
     * sont redondantes ici : la console est déjà identifiée séparément par [ConsoleRecognition].
     */
    private val listingNoiseWords = setOf(
        "complet", "complete", "complète", "neuf", "occasion", "boite", "boîte", "boitier", "boîtier",
        "vide", "notice", "teste", "testé", "testee", "testée", "fonctionnel", "fonctionne",
        "authentique", "original", "originale", "pal", "ntsc", "fra", "francais", "français",
        "francaise", "française", "vf", "cib", "ttbe", "blister", "sous", "jaquette", "cartouche",
        "disque", "avec", "sans", "tres", "très", "bon", "etat", "état", "jeu", "jeux",
        "game", "games", "sony", "nintendo", "microsoft", "sega", "playstation", "xbox", "switch",
        "wii", "gamecube", "atari", "oled", "lite", "console"
    )

    /**
     * Nettoie un titre d'annonce eBay du bruit qui égare la recherche dans les catalogues : état/
     * complétude ("Complet", "Testé Fonctionnel"...), marque/plateforme déjà identifiée à part,
     * mentions entre crochets ("[FRA]"), et mentions d'édition (retirées puis réinjectées après
     * coup via [GameCatalog.preserveEditionSuffix]). Un titre brut trop bruyant fait échouer IGDB
     * (recherche stricte, contrairement à RAWG) ou fausse le classement de RAWG (ex. chercher
     * "Rayman Legends Definitive Edition" ne retrouve même pas "Rayman Legends" dans les 5 premiers
     * résultats, noyé par d'autres jeux "Definitive Edition" sans rapport).
     */
    private fun cleanListingTitle(title: String): String {
        val noBrackets = title.replace(Regex("\\[[^]]*]"), " ")
        // UPCitemdb formate souvent ses titres "Nom Du Jeu by Éditeur (CODE-SKU)" : tout ce qui suit
        // " by " est du bruit (nom d'éditeur + code produit alphanumérique type ASIN), qui allonge
        // la requête sans rien apporter et fait chuter le ratio de couverture sous le seuil de
        // confiance même pour un match par ailleurs parfait (ex. "Lara Croft Temple of Osiris PS4
        // by Square Enix (B00TPWPDHO)" ne retrouvait pas "Lara Croft and the Temple of Osiris").
        val noPublisherSuffix = Regex("(?i)\\bby\\b.*$").replace(noBrackets, "")
        val noEdition = GameCatalog.stripEditionKeywords(noPublisherSuffix)
        val noSymbols = noEdition.replace(Regex("[^\\p{L}\\p{Nd}' -]"), " ")
        val tokens = noSymbols.split(Regex("\\s+")).filter { it.isNotBlank() }
        val kept = tokens.filterNot { it.lowercase().trim('\'', '-') in listingNoiseWords }
        return kept.joinToString(" ").trim()
    }
}
