package com.example.macollection.data

import android.content.Context
import android.net.Uri
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
        val title = runCatching { EbayPrices.titleForBarcode(barcode) }.getOrNull()
            ?: return ScanResult(barcode, null)

        val consoleName = ConsoleRecognition.recognize(title)
        var accessoryName: String? = null
        var gameMatch: GameInfo? = null
        if (consoleName == null) {
            accessoryName = AccessoryRecognition.recognize(title)
            if (accessoryName == null) {
                val candidate = runCatching { GameCatalog.search(title) }.getOrNull()?.firstOrNull()
                if (candidate != null && isConfidentGameMatch(title, candidate.name)) {
                    val detailed = candidate.sourceId?.let { id -> GameCatalog.detail(id) } ?: candidate
                    // Le catalogue (RAWG/IGDB) ne connaît que le titre de base : sans ça, une
                    // mention d'édition lue sur l'annonce eBay (ex. "Platinum") disparaissait
                    // silencieusement au profit du nom "propre" du catalogue.
                    gameMatch = detailed.copy(name = GameCatalog.preserveEditionSuffix(title, detailed.name))
                }
            }
        }
        val suggestedName = gameMatch?.name ?: consoleName ?: accessoryName ?: title
        val gameConsoleHint = if (gameMatch != null) consoleName else null
        return ScanResult(barcode, suggestedName, consoleName, accessoryName, gameMatch, gameConsoleHint)
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
     */
    private suspend fun firstGameMatch(query: String, platformId: Int?): GameInfo? {
        runCatching { IgdbCatalog.search(query, 0) }.getOrNull().orEmpty()
            .firstOrNull { isConfidentGameMatch(query, it.name) }
            ?.let { return IgdbCatalog.frenchify(it) }
        val rawg = runCatching { GameCatalog.search(query, platformId) }.getOrNull().orEmpty()
            .firstOrNull { isConfidentGameMatch(query, it.name) } ?: return null
        return rawg.sourceId?.let { id -> GameCatalog.detail(id) } ?: rawg
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

    private fun significantWords(s: String): Set<String> =
        s.lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .split(" ")
            .filter { it.length >= 3 }
            .toSet()

    /**
     * Vrai si [query] et [candidate] partagent vraiment plusieurs mots. Un seul mot en commun
     * ne suffit JAMAIS (même un mot rare) : un logo d'éditeur omniprésent comme « CAPCOM » sur
     * la cartouche matchait n'importe quel jeu Capcom au hasard (ex. « Capcom's MVP Football »
     * pour une cartouche Street Fighter II) avant ce durcissement.
     */
    private fun isConfidentGameMatch(query: String, candidate: String): Boolean {
        val qWords = significantWords(query)
        if (qWords.size < 2) return false
        val overlap = qWords.intersect(significantWords(candidate))
        return overlap.size >= 2 && overlap.size.toDouble() / qWords.size >= 0.6
    }
}
