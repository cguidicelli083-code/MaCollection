package com.example.macollection.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Outils de reconnaissance par photo (ML Kit).
 *
 * - [scanImage] : analyse une image (code-barres + OCR du titre). Le scan caméra en direct est
 *   géré séparément par [com.example.macollection.ui.NativeBarcodeScannerScreen] (CameraX), qui
 *   réutilise le même modèle ML Kit "barcode" ci-dessous — voir sa documentation pour l'historique
 *   (a remplacé le scanner hébergé par Google Play Services, dont le module séparé pouvait échouer
 *   à se télécharger indépendamment de l'app).
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
        val gameConsoleHint: String? = null
    )

    /**
     * Codes-barres de CONSOLES connus en dur, vérifiés manuellement (barcode → nom de preset) :
     * les consoles sont beaucoup moins bien indexées que les jeux dans UPCitemdb/Barcode
     * Lookup/eBay (constaté en conditions réelles : zéro résultat des 3 sources, même pour un
     * modèle grand public comme la Switch OLED), donc pas d'autre choix que de les recenser à la
     * main au fur et à mesure qu'un utilisateur en signale un manquant.
     */
    private val knownConsoleBarcodes = mapOf(
        "0718910896254" to "Switch OLED",
        "3328170307079" to "Switch 2"
    )

    /**
     * Identifie un objet à partir de son seul code-barres (EAN/UPC) : récupère le titre d'une
     * annonce eBay correspondante, puis applique les mêmes règles de reconnaissance que pour
     * une photo (console, accessoire, ou jeu confirmé sur RAWG).
     */
    /**
     * @param allowQuotaLimitedSources Si faux, n'interroge PAS UPCitemdb/Barcode Lookup/Barcode
     * Spider (quota partagé par toute l'appli, ~100 requêtes/jour CHACUNE, cf. [knownConsoleBarcodes]) —
     * seulement ScanDex (spécialisé jeux, pas de quota connu) et eBay (quota bien plus large,
     * ~5000/jour par clé + clé de secours). Utilisé par l'appelant pour un premier essai
     * totalement gratuit avant de proposer une pub récompensée si rien n'est trouvé (cf.
     * [MainActivity]) : la plupart des JEUX se résolvent déjà via ScanDex seul, donc la majorité
     * des scans n'ont jamais besoin de toucher au quota partagé.
     */
    suspend fun identifyFromBarcode(barcode: String, allowQuotaLimitedSources: Boolean = true): ScanResult {
        knownConsoleBarcodes[barcode]?.let { return ScanResult(barcode, it, consolePresetName = it) }

        // ScanDex : base spécialisée JEUX VIDÉO, essayée en tout premier — un code-barres qui y
        // est connu est déjà apparié à une fiche IGDB précise par LEUR base (pas un titre
        // d'annonce à deviner), donc plus fiable que le scoring flou appliqué aux autres sources
        // ci-dessous.
        val scanDexMatch = runCatching { ScanDexApi.lookupGame(barcode) }.getOrNull()
        Log.d("ScanBarcode", "ScanDex match for $barcode: $scanDexMatch")

        // Dès que ScanDex trouve une correspondance, on la renvoie IMMÉDIATEMENT : c'est déjà une
        // correspondance fiable (base dédiée par code-barres), toujours prioritaire sur tout ce que
        // les autres sources pourraient trouver ensuite. Continuer à interroger UPCitemdb/Barcode
        // Lookup/Barcode Spider/eBay puis IGDB/RAWG sur chaque titre pour un résultat qui serait de
        // toute façon jeté à la poubelle ne servait qu'à faire attendre l'utilisateur pour rien
        // (jusqu'à une bonne minute) alors que le bon résultat était déjà disponible en ~1 seconde.
        if (scanDexMatch != null) {
            val game = (runCatching { IgdbCatalog.byId(scanDexMatch.igdbId) }.getOrNull()?.let { IgdbCatalog.frenchify(it) })
                // Repli si la fiche détaillée échoue (réseau) : au moins le nom/plateforme déjà
                // fournis par ScanDex, plutôt que de perdre une correspondance qu'on a pourtant.
                ?: GameInfo(sourceId = null, name = scanDexMatch.name, platforms = scanDexMatch.platformName.orEmpty(), genres = "", releaseYear = null, description = "", coverUrl = null, source = "igdb")
            val consoleName = scanDexMatch.platformName?.let { ConsoleRecognition.recognize(it) }
            Log.d("ScanBarcode", "RESULT (ScanDex direct) suggestedName=${game.name} console=$consoleName")
            return ScanResult(barcode, game.name, gameMatch = game, gameConsoleHint = consoleName)
        }

        // UPCitemdb en premier, puis Barcode Lookup et Barcode Spider : contrairement à une
        // annonce eBay (titre rédigé librement par un vendeur, plein de mentions d'état/
        // complétude qui égarent la recherche), ces bases renvoient un titre "propre" unique par
        // code-barres (ex. "Mario Kart 8 Deluxe (Nintendo Switch)"), bien plus fiable pour
        // retrouver le bon jeu — et à elles trois couvrent des objets qu'aucune seule ne trouvait
        // (constaté en conditions réelles). Sautées si [allowQuotaLimitedSources] est faux (cf.
        // doc ci-dessus). Les annonces eBay restent essayées dans tous les cas (et en repli si
        // aucun code-barres n'est connu des bases précédentes).
        // Les 4 sources ci-dessous sont indépendantes (services différents, aucune ne dépend du
        // résultat d'une autre) : lancées en parallèle plutôt que l'une après l'autre, pour ne
        // jamais attendre plus longtemps que la plus lente des quatre (au lieu de la somme des
        // quatre, potentiellement plus de 20s si l'une d'elles est au ralenti).
        var upcResult: UpcLookupResult? = null
        val titles = coroutineScope {
            val upcDeferred = if (allowQuotaLimitedSources) async { runCatching { UpcItemDb.lookup(barcode) }.getOrNull() } else null
            val barcodeLookupDeferred = if (allowQuotaLimitedSources) async { runCatching { BarcodeLookupApi.lookupTitle(barcode) }.getOrNull() } else null
            val barcodeSpiderDeferred = if (allowQuotaLimitedSources) async { runCatching { BarcodeSpiderApi.lookupTitle(barcode) }.getOrNull() } else null
            val ebayDeferred = async { runCatching { EbayPrices.titlesForBarcode(barcode) }.getOrNull().orEmpty() }
            upcResult = upcDeferred?.await()
            val upcTitle = upcResult?.title
            val barcodeLookupTitle = barcodeLookupDeferred?.await()
            val barcodeSpiderTitle = barcodeSpiderDeferred?.await()
            val ebayTitles = ebayDeferred.await()
            Log.d("ScanBarcode", "barcode=$barcode allowQuotaLimited=$allowQuotaLimitedSources upcTitle=$upcTitle upcCategory=${upcResult?.category} barcodeLookupTitle=$barcodeLookupTitle barcodeSpiderTitle=$barcodeSpiderTitle ebayTitles=$ebayTitles")
            listOfNotNull(upcTitle, barcodeLookupTitle, barcodeSpiderTitle) + ebayTitles
        }
        if (titles.isEmpty()) return ScanResult(barcode, null)

        // Signal STRUCTUREL de console (pas un deviné depuis le texte du titre, contrairement au
        // piège documenté plus bas avec [consoleName]) : UPCitemdb classe lui-même le produit
        // "Electronics > Video Game Consoles" quand c'est une vraie console — un JEU y est
        // TOUJOURS classé "Video Games", jamais "Consoles", quel que soit le nom de console
        // mentionné dans SON titre à lui (ex. "Resident Evil 6 (Xbox 360)" reste catégorie "Video
        // Games"). Contrairement à la reconnaissance de nom de console dans le texte du titre
        // (qui matchait à tort presque tous les jeux, cf. plus bas), ce champ dédié à la catégorie
        // ne se déclenche jamais sur un jeu. Vérifié en conditions réelles (console Game Boy
        // Advance blanche, code-barres 045496712112).
        upcResult?.let { r ->
            if (r.category?.contains("console", ignoreCase = true) == true) {
                matchConsolePreset(r.title)?.let { preset ->
                    Log.d("ScanBarcode", "RESULT (UPCitemdb catégorie console) preset=${preset.name}")
                    return ScanResult(barcode, preset.name, consolePresetName = preset.name)
                }
            }
        }

        val title = titles.firstOrNull()

        // Une boîte/cartouche de JEU porte presque toujours aussi le nom de sa console (mention
        // obligatoire du fabricant) : reconnaître une console dans le titre ne suffit donc pas à
        // conclure que le produit scanné EST cette console (même piège que pour l'OCR photo, cf.
        // scanImage). On cherche donc d'abord un jeu (filtré sur la console repérée si elle existe),
        // et on ne retombe sur "c'est la console elle-même" que si aucun jeu fiable n'est trouvé.
        val consoleName = title?.let { ConsoleRecognition.recognize(it) }
        val platformId = consoleName?.let { ConsolePlatforms.platformId(it) }
        Log.d("ScanBarcode", "consoleName=$consoleName platformId=$platformId")

        // On essaie CHAQUE titre d'annonce renvoyé (pas seulement le premier) : eBay classe
        // parfois en tête une annonce pour un objet DIFFÉRENT qui partage le même GTIN (ex. un lot
        // "3 jeux" au lieu du jeu précis scanné) — un seul titre ne suffit donc pas toujours. Le
        // premier titre à trouver une correspondance FIABLE (score de confiance suffisant, cf.
        // [gameMatchScore]) l'emporte (voir plus bas) ; les autres recherches encore en cours sont
        // alors annulées plutôt que d'attendre un éventuel meilleur score, pour rester réactif.
        // On utilise aussi IGDB en priorité (meilleure couverture rétro que RAWG seul, cf.
        // [firstGameMatch] déjà utilisé par le scan photo), et on nettoie chaque titre du bruit
        // typique d'une annonce (état, complétude, marque/plateforme déjà identifiée à part,
        // mentions d'édition) AVANT de chercher (cf. [cleanListingTitle]).
        // Titres testés en parallèle (pas l'un après l'autre) : avec jusqu'à une dizaine de titres
        // d'annonces différents, les tester en série pouvait à elle seule étirer le scan à plus
        // d'une minute quand IGDB/RAWG sont lents. Surtout, dès qu'UN titre trouve une correspondance
        // fiable, on l'utilise IMMÉDIATEMENT et on annule les recherches encore en cours pour les
        // autres titres (ex. celles bloquées sur un appel RAWG lent) — sans ça, un résultat déjà
        // trouvé en 1s attendait quand même que tous les autres titres finissent, jusqu'à leur
        // plafond de 7s chacun.
        // Traductions FR -> EN reconnues dans les titres d'annonces (ex. "Pokémon Version Blanche"
        // -> "Pokemon White Version") : IGDB ne connaît que les titres anglais/internationaux,
        // jamais les titres français officiels utilisés par la quasi-totalité des annonces PAL
        // françaises — sans repérer cette traduction, une requête 100% correcte mais en français
        // ne matchait JAMAIS rien (cf. [frenchToEnglishTitles]). Essayées EN PLUS des titres
        // nettoyés normalement, jamais à la place (un titre déjà en anglais n'a besoin de rien).
        val translatedQueries = titles.mapNotNull { translateFrenchTitle(it) }.distinct()

        val gameMatch = coroutineScope {
            val firstMatch = CompletableDeferred<Pair<GameInfo, Double>?>()
            val cleanedJobs = titles.map { t ->
                launch {
                    val cleaned = cleanListingTitle(t).ifBlank { t }
                    var scored = firstGameMatchScored(cleaned, platformId)
                    // Requête la plus réduite déjà tentée (met à jour au fil des replis ci-dessous) :
                    // le repli suivant part toujours de la dernière version essayée, jamais de la
                    // requête d'origine, sinon un chiffre isolé retiré par le 1er repli (ex. le "1"
                    // de "Saints Row 1 EX") réapparaissait dans le 2e repli en repartant de zéro
                    // (ex. "Saints Row EX" -> enlève "EX" -> "Saints Row 1", le "1" isolé qui bloquait
                    // déjà tout repose problème).
                    var reduced = cleaned
                    if (scored == null) {
                        // Repli : IGDB échoue parfois sur un seul chiffre/tiret/lettre isolée resté
                        // après nettoyage (ex. un "2" résiduel de "Playstation 2", ou un "U" résiduel
                        // de "Wii U" une fois "Wii" filtré — alors que le vrai titre du jeu n'a ni
                        // numéro ni lettre de modèle) — on ne retente SANS eux qu'en dernier recours,
                        // pour ne jamais perdre les titres qui ont VRAIMENT besoin de leur numéro
                        // (ex. "Mario Kart 8", essayé en premier avec son "8").
                        val stripped = cleaned.split(Regex("\\s+"))
                            .filterNot { it.matches(Regex("^-+$")) || it.matches(Regex("^\\d{1,2}$")) || it.matches(Regex("^[A-Za-zÀ-ÿ]$")) }
                            .joinToString(" ").trim()
                        if (stripped.isNotBlank() && stripped != cleaned) {
                            // minQueryWords abaissé à 2 : cette requête est déjà purgée des
                            // chiffres/tirets isolés, donc le risque de faux positif générique
                            // ("2 pc"...) qui justifiait le seuil de 3 ne s'applique plus ici.
                            scored = firstGameMatchScored(stripped, platformId, minQueryWords = 2)
                            reduced = stripped
                        }
                    }
                    if (scored == null) {
                        // Dernier repli : sous-titre régional absent du nom IGDB (ex. l'édition PAL
                        // "Split/Second: Velocity" n'existe sur IGDB que sous "Split/Second", ou un
                        // numéro de seller ajouté pour distinguer un opus non numéroté officiellement
                        // — ex. "Saints Row 1 EX" : IGDB ne connaît que "Saints Row" tout court, sans
                        // "1". IGDB fait une recherche par PHRASE si stricte qu'UN SEUL mot en trop
                        // fait tomber TOUS les résultats à zéro (constaté en conditions réelles). On
                        // ne retire que le DERNIER mot de la requête déjà la plus réduite ([reduced]),
                        // et seulement si les tentatives précédentes ont échoué : les seuils de score
                        // (minQueryWords=2 + ratios élevés dans gameMatchScore) restent la seule
                        // protection contre un faux positif sur un titre plus court mais différent.
                        val withoutLastWord = reduced.split(Regex("\\s+")).let { if (it.size > 2) it.dropLast(1) else it }.joinToString(" ").trim()
                        if (withoutLastWord.isNotBlank() && withoutLastWord != reduced) {
                            scored = firstGameMatchScored(withoutLastWord, platformId, minQueryWords = 2)
                        }
                    }
                    Log.d("ScanBarcode", "firstGameMatch(\"$cleaned\") -> ${scored?.first?.name} (score=${scored?.second})")
                    if (scored != null) {
                        val named = scored.first.copy(name = GameCatalog.preserveEditionSuffix(t, scored.first.name)) to scored.second
                        firstMatch.complete(named) // no-op si un autre titre a déjà gagné la course
                    }
                }
            }
            val translatedJobs = translatedQueries.map { translated ->
                launch {
                    val scored = firstGameMatchScored(translated, platformId)
                    Log.d("ScanBarcode", "firstGameMatch (traduit FR->EN) (\"$translated\") -> ${scored?.first?.name} (score=${scored?.second})")
                    if (scored != null) firstMatch.complete(scored)
                }
            }
            val jobs = cleanedJobs + translatedJobs
            // Si aucun titre ne trouve rien, il faut quand même débloquer firstMatch une fois que
            // TOUS ont fini (sinon on attendrait indéfiniment un résultat qui ne viendra jamais).
            launch { jobs.joinAll(); firstMatch.complete(null) }
            val result = firstMatch.await()
            jobs.forEach { it.cancel() }
            result?.first
        }

        var accessoryName: String? = null
        if (gameMatch == null) {
            accessoryName = titles.firstNotNullOfOrNull { AccessoryRecognition.recognize(it) }
        }
        // On ne propose JAMAIS la console comme résultat par défaut quand ni un jeu ni un
        // accessoire n'a été identifié avec confiance : une boîte de JEU mentionne presque
        // toujours sa console (mention obligatoire du fabricant), donc reconnaître une console
        // dans un titre d'annonce ne veut PAS dire que l'objet scanné EST cette console — l'avoir
        // proposée comme résultat par défaut affichait "PlayStation Vita" pour un jeu PS Vita non
        // identifié, comme si une console avait été scannée. Sans correspondance fiable (jeu ou
        // accessoire), mieux vaut ne rien proposer (saisie manuelle) qu'un résultat sûrement faux.
        // Un VRAI code-barres de console reste identifié correctement plus haut, via
        // [knownConsoleBarcodes] ou la catégorie UPCitemdb (liste/champ dédiés, jamais par ce repli).
        val suggestedName = gameMatch?.name ?: accessoryName
        val gameConsoleHint = if (gameMatch != null) consoleName else null

        Log.d("ScanBarcode", "RESULT suggestedName=$suggestedName gameMatch=${gameMatch?.name} console=$consoleName accessory=$accessoryName")
        return ScanResult(barcode, suggestedName, accessoryName = accessoryName, gameMatch = gameMatch, gameConsoleHint = gameConsoleHint)
    }

    /**
     * Associe un titre déjà classé "console" par UPCitemdb (cf. ci-dessus) à une fiche du
     * catalogue [consolePresets] : tous les mots du nom du preset doivent apparaître dans le
     * titre, et le preset avec le PLUS de mots l'emporte (le plus spécifique disponible, ex. un
     * coloris précis plutôt que le modèle générique) — sans correspondance, on ne force rien
     * (repli sur la suite normale : jeu/accessoire/saisie manuelle).
     */
    private fun matchConsolePreset(title: String): ConsolePreset? {
        fun words(s: String) = s.lowercase()
            .replace(Regex("[^a-z0-9à-ÿ]+"), " ")
            .trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .toSet()
        val titleWords = words(title)
        if (titleWords.isEmpty()) return null
        return consolePresets
            .filter { preset -> words(preset.name).all { it in titleWords } }
            .maxByOrNull { words(it.name).size }
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
            Log.d("ScanBarcode", "OCR consoleName=$consoleName cleanLines=${cleanLines.take(8)}")

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
                // IGDB en premier, RAWG en repli (comme le scan code-barres, cf. [firstGameMatchScored])
                // : auparavant l'OCR n'interrogeait QUE RAWG, donc une panne RAWG bloquait toute
                // reconnaissance par photo sans aucun filet, alors que le scan code-barres restait
                // opérationnel grâce à IGDB (vérifié en conditions réelles : RAWG en panne un soir,
                // IGDB toujours disponible).
                val scored = runCatching { firstGameMatchScored(line, platformId) }.getOrNull()
                val candidate = scored?.first
                Log.d("ScanBarcode", "OCR line=\"$line\" -> candidate=${candidate?.name} score=${scored?.second}")
                if (candidate != null &&
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
            Log.d("ScanBarcode", "OCR RESULT gameMatch=${gameMatch?.name} consoleName=$consoleName accessoryName=$accessoryName")

            // NIVEAU 1 (OCR) concluant : correspondance console/accessoire/jeu trouvée -> on valide.
            if (gameMatch != null || consoleName != null || accessoryName != null) {
                return ScanResult(
                    barcode, suggestedName, consoleName.takeIf { gameMatch == null }, accessoryName, gameMatch, gameConsoleHint
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
     * (cf. [identifyFromBarcode], qui essaie plusieurs titres d'annonce pour un même code-barres).
     * [minQueryWords] : voir [gameMatchScore] — abaissé à 2 uniquement pour la tentative de repli
     * "sans chiffres/tirets isolés" (déjà purgée des résidus numériques les plus à risque de faux
     * positif, cf. [identifyFromBarcode]), jamais pour la requête normale. */
    private suspend fun firstGameMatchScored(query: String, platformId: Int?, minQueryWords: Int = 3): Pair<GameInfo, Double>? {
        val igdbResults = runCatching { IgdbCatalog.search(query, 0) }.getOrNull().orEmpty()
        Log.d("ScanBarcode", "  IGDB candidates for \"$query\": ${igdbResults.take(5).map { it.name }}")
        val igdbBest = igdbResults.mapNotNull { g -> gameMatchScore(query, g.name, minQueryWords)?.let { it to g } }.maxByOrNull { it.first }
        if (igdbBest != null) return IgdbCatalog.frenchify(igdbBest.second) to igdbBest.first
        val rawgResults = runCatching { GameCatalog.search(query, platformId) }.getOrNull().orEmpty()
        Log.d("ScanBarcode", "  RAWG candidates for \"$query\": ${rawgResults.take(5).map { it.name }}")
        val rawgBest = rawgResults.mapNotNull { g -> gameMatchScore(query, g.name, minQueryWords)?.let { it to g } }.maxByOrNull { it.first }
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
            // \p{L} (toute lettre Unicode, accents compris) au lieu de a-z (ASCII seul) : sans
            // ça, un mot accentué ("Pokémon") était tronqué en fragments ("pok"/"mon") au lieu
            // d'être gardé intact, ce qui faisait chuter le score de tout candidat IGDB accentué
            // (constaté : "Pokemon White Version" ne matchait plus "Pokémon White Version").
            .replace(Regex("[^\\p{L}\\p{Nd} ]"), " ")
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
    private fun gameMatchScore(query: String, candidate: String, minQueryWords: Int = 3): Double? {
        // Les mentions d'édition ("Deluxe", "Definitive Edition"...) sont retirées des DEUX côtés
        // avant de comparer : un candidat catalogue les inclut parfois dans son nom officiel (ex.
        // "CastleStorm - Definitive Edition"), et les compter faussait la comparaison dans les deux
        // sens (cf. [GameCatalog.stripEditionKeywords]).
        val qWords = significantWords(GameCatalog.stripEditionKeywords(query))
        val cWords = significantWords(GameCatalog.stripEditionKeywords(candidate))
        if (cWords.isEmpty()) return null
        // Correspondance EXACTE (mêmes mots significatifs des deux côtés) : toujours acceptée,
        // même sous le seuil minimal de mots ci-dessous — ce seuil n'existe que pour écarter les
        // chevauchements PARTIELS ambigus (un résidu de nettoyage qui matche par hasard), une
        // ambiguïté qui n'existe pas ici. Sans ce cas particulier, un titre légitimement réduit à
        // un seul mot significatif après nettoyage (ex. "Zathura") ne pouvait jamais matcher.
        if (qWords == cWords) return 1.0
        // En dessous de [minQueryWords] mots significatifs (3 par défaut), la requête est trop
        // pauvre pour qu'un chevauchement veuille dire quoi que ce soit : un résidu de nettoyage à
        // 2 mots très génériques (ex. "2 pc", tout ce qu'il restait d'un titre étranger mal encodé
        // après avoir retiré le bruit) matchait n'importe quel candidat qui contient ces 2 mots par
        // hasard (ex. "Disgaea 2 PC" pour la boîte d'une console PC Engine CoreGrafx sans aucun
        // rapport). Abaissable à 2 uniquement pour la tentative de repli déjà purgée des chiffres
        // isolés (cf. [firstGameMatchScored]) : beaucoup de vrais titres courts ("Sega Superstars
        // Tennis" une fois "Sega" écarté comme marque, "Call of Duty" une fois "of" filtré comme
        // mot-outil...) ne laissent que 2 mots significatifs, et étaient injustement rejetés.
        if (qWords.size < minQueryWords) return null
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

    /** Marques/plateformes et vocabulaire d'état très fréquents sur une annonce eBay (jeu vidéo) :
     * jamais des mots du titre du jeu lui-même, mais leur présence isolée dans le texte peut
     * fausser le classement des catalogues (cf. [cleanListingTitle]). Les marques/plateformes
     * sont redondantes ici : la console est déjà identifiée séparément par [ConsoleRecognition].
     */
    private val listingNoiseWords = setOf(
        "complet", "complete", "complète", "neuf", "occasion", "occas", "occaz", "boite", "boîte", "boitier", "boîtier",
        "vide", "notice", "teste", "testé", "testee", "testée", "fonctionnel", "fonctionne",
        "authentique", "original", "originale", "pal", "ntsc", "fra", "fr", "francais", "français",
        "francaise", "française", "vf", "vo", "vosf", "cib", "ttbe", "tbe", "beg", "bne", "meh",
        "blister", "sous", "jaquette", "cartouche",
        "disque", "cd", "dvd", "avec", "sans", "tres", "très", "bon", "etat", "état", "jeu", "jeux",
        "game", "games", "sony", "nintendo", "microsoft", "sega", "playstation", "xbox", "switch",
        "wii", "gamecube", "atari", "oled", "lite", "console",
        // Connecteurs français très courants dans une annonce, jamais porteurs de sens pour le
        // titre lui-même — cf. commentaire ci-dessous sur la sensibilité extrême d'IGDB au moindre
        // mot en trop (constaté : même "sur" ou "cd" isolé suffit à faire échouer un match).
        "sur", "de", "du", "des", "en", "et",
        // Abréviations de région/zone très fréquentes sur les annonces (redondantes avec la
        // console déjà identifiée à part, cf. doc ci-dessus).
        "eur", "eu", "usa", "us", "uk", "jap", "jpn", "jp",
        // Modèles de console abrégés : IGDB fait une recherche par PHRASE assez stricte (constaté
        // en conditions réelles : "LEGO MARVEL SUPER HEROES" seul trouve le jeu, mais un seul
        // token en trop comme "VITA" ou "TBE" suffit à faire tomber le résultat à zéro), donc
        // chaque abréviation non nettoyée ici coûte potentiellement un match raté en entier.
        "vita", "psvita", "psone", "ps", "ps1", "ps2", "ps3", "ps4", "ps5", "psp",
        "3ds", "2ds", "ds", "dsi", "dsl", "nds", "gba", "gbc", "gbp", "gb", "n64",
        "snes", "nes", "megadrive", "md", "dreamcast", "dc", "saturn", "gc",
        "x360", "xb1", "xbone", "series",
        // "x-box" (avec tiret) : le tiret n'est pas séparé en mots par noSymbols (conservé comme
        // caractère de mot), donc ce variant de graphie de "xbox" ne matchait pas l'entrée ci-dessus
        // et restait comme bruit résiduel (constaté : "Kingdom Under Fire ... X-box 360 In French
        // Language" ne retrouvait RIEN, un seul mot en trop suffisant à faire échouer IGDB).
        "x-box", "360",
        // Mentions de langue/version très fréquentes sur les fiches UPCitemdb/Barcode Lookup
        // anglophones (ex. "In French Language" sur une release PAL FR), jamais un mot du titre.
        "french", "language", "version"
    )

    /**
     * Titre français reconnu (motif) -> nom IGDB (anglais). Se concentre sur Pokémon, seule grande
     * franchise Nintendo dont les titres sont systématiquement traduits en français sur le
     * boîtier/l'annonce ("Pokémon Version Blanche"), contrairement à la quasi-totalité des autres
     * séries (le titre reste identique en anglais même sur une boîte PAL française) : IGDB ne
     * connaît que le nom anglais/international, donc sans cette table une requête pourtant
     * parfaitement propre ne matchait jamais rien pour ces jeux (cf. [translateFrenchTitle]).
     * Motifs les plus spécifiques d'abord (ex. "rouge feu" avant "rouge" seul), l'ordre de la
     * liste fait foi.
     */
    private val frenchToEnglishTitles: List<Pair<Regex, String>> = listOf(
        Regex("(?i)pok[ée]mon\\s+(?:version\\s+)?rouge\\s*feu") to "Pokemon FireRed Version",
        Regex("(?i)pok[ée]mon\\s+(?:version\\s+)?vert.?\\s*feuille") to "Pokemon LeafGreen Version",
        Regex("(?i)pok[ée]mon\\s+(?:version\\s+)?noir\\s*2") to "Pokemon Black Version 2",
        Regex("(?i)pok[ée]mon\\s+(?:version\\s+)?blanc(?:he)?\\s*2") to "Pokemon White Version 2",
        Regex("(?i)pok[ée]mon\\s+(?:version\\s+)?rouge\\b") to "Pokemon Red Version",
        Regex("(?i)pok[ée]mon\\s+(?:version\\s+)?bleu(?:e)?\\b") to "Pokemon Blue Version",
        Regex("(?i)pok[ée]mon\\s+(?:version\\s+)?jaune\\b") to "Pokemon Yellow Version",
        Regex("(?i)pok[ée]mon\\s+(?:version\\s+)?or\\b") to "Pokemon Gold Version",
        Regex("(?i)pok[ée]mon\\s+(?:version\\s+)?argent\\b") to "Pokemon Silver Version",
        Regex("(?i)pok[ée]mon\\s+(?:version\\s+)?cristal\\b") to "Pokemon Crystal Version",
        Regex("(?i)pok[ée]mon\\s+(?:version\\s+)?rubis\\b") to "Pokemon Ruby Version",
        Regex("(?i)pok[ée]mon\\s+(?:version\\s+)?saphir\\b") to "Pokemon Sapphire Version",
        Regex("(?i)pok[ée]mon\\s+(?:version\\s+)?[ée]meraude\\b") to "Pokemon Emerald Version",
        Regex("(?i)pok[ée]mon\\s+(?:version\\s+)?diamant\\b") to "Pokemon Diamond Version",
        Regex("(?i)pok[ée]mon\\s+(?:version\\s+)?perle\\b") to "Pokemon Pearl Version",
        Regex("(?i)pok[ée]mon\\s+(?:version\\s+)?platine\\b") to "Pokemon Platinum Version",
        Regex("(?i)pok[ée]mon\\s+(?:version\\s+)?noir(?:e)?\\b") to "Pokemon Black Version",
        Regex("(?i)pok[ée]mon\\s+(?:version\\s+)?blanc(?:he)?\\b") to "Pokemon White Version",
        Regex("(?i)pok[ée]mon\\s+ultra.?soleil\\b") to "Pokemon Ultra Sun",
        Regex("(?i)pok[ée]mon\\s+ultra.?lune\\b") to "Pokemon Ultra Moon",
        Regex("(?i)pok[ée]mon\\s+soleil\\b") to "Pokemon Sun",
        Regex("(?i)pok[ée]mon\\s+lune\\b") to "Pokemon Moon",
        Regex("(?i)pok[ée]mon\\s+[ée]p[ée]e\\b") to "Pokemon Sword",
        Regex("(?i)pok[ée]mon\\s+bouclier\\b") to "Pokemon Shield",
        Regex("(?i)pok[ée]mon\\s+[ée]carlate\\b") to "Pokemon Scarlet"
    )

    /** Renvoie le nom IGDB (anglais) si [title] contient un titre français reconnu, sinon null. */
    private fun translateFrenchTitle(title: String): String? =
        frenchToEnglishTitles.firstOrNull { (pattern, _) -> pattern.containsMatchIn(title) }?.second

    /**
     * Nettoie un titre d'annonce eBay du bruit qui égare la recherche dans les catalogues : état/
     * complétude ("Complet", "Testé Fonctionnel"...), marque/plateforme déjà identifiée à part,
     * mentions entre crochets ("[FRA]"), et mentions d'édition (retirées puis réinjectées après
     * coup via [GameCatalog.preserveEditionSuffix]). Un titre brut trop bruyant fait échouer IGDB
     * (recherche stricte, contrairement à RAWG) ou fausse le classement de RAWG (ex. chercher
     * "Rayman Legends Definitive Edition" ne retrouve même pas "Rayman Legends" dans les 5 premiers
     * résultats, noyé par d'autres jeux "Definitive Edition" sans rapport).
     */
    internal fun cleanListingTitle(title: String): String {
        val noBrackets = title.replace(Regex("\\[[^]]*]"), " ")
        // Une parenthèse contenant au moins un chiffre est quasi toujours un repère technique
        // (référence vendeur "(n°6854S)", modèle de console "(Microsoft X-box 360)", ASIN...),
        // jamais un mot du titre du jeu lui-même — contrairement à une parenthèse SANS chiffre
        // (ex. juste "(Nintendo Switch)"), laissée à noSymbols/listingNoiseWords ci-dessous qui la
        // nettoie déjà correctement mot par mot. IGDB fait une recherche par PHRASE très stricte :
        // un seul token résiduel de ce genre suffit à faire tomber tout résultat à zéro (constaté
        // en conditions réelles : "Resident Evil 6 n 6854S" -> aucun résultat, "Resident Evil 6"
        // seul -> trouvé immédiatement).
        val noRefParens = Regex("\\([^)]*\\d[^)]*\\)").replace(noBrackets, " ")
        // UPCitemdb formate souvent ses titres "Nom Du Jeu by Éditeur (CODE-SKU)" : tout ce qui suit
        // " by " est du bruit (nom d'éditeur + code produit alphanumérique type ASIN), qui allonge
        // la requête sans rien apporter et fait chuter le ratio de couverture sous le seuil de
        // confiance même pour un match par ailleurs parfait (ex. "Lara Croft Temple of Osiris PS4
        // by Square Enix (B00TPWPDHO)" ne retrouvait pas "Lara Croft and the Temple of Osiris").
        val noPublisherSuffix = Regex("(?i)\\bby\\b.*$").replace(noRefParens, "")
        val noEdition = GameCatalog.stripEditionKeywords(noPublisherSuffix)
        val noSymbols = noEdition.replace(Regex("[^\\p{L}\\p{Nd}' -]"), " ")
        val tokens = noSymbols.split(Regex("\\s+")).filter { it.isNotBlank() }
        val kept = tokens.filterNot { it.lowercase().trim('\'', '-') in listingNoiseWords }
        return kept.joinToString(" ").trim()
    }
}
