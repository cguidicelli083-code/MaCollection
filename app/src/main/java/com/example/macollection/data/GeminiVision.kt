package com.example.macollection.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import com.example.macollection.BuildConfig
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

// --- DTO Gemini (generateContent) ---
private data class GemPart(val text: String? = null, val inline_data: GemInlineData? = null)
private data class GemInlineData(val mime_type: String, val data: String)
private data class GemContent(val parts: List<GemPart>)
// [tools] optionnel : n'active la recherche Google intégrée que pour estimatePrice (voir plus
// bas), inutile et potentiellement plus lent/coûteux pour la reconnaissance photo pure.
private class GemGoogleSearch
private data class GemTool(val google_search: GemGoogleSearch = GemGoogleSearch())
private data class GemRequest(val contents: List<GemContent>, val tools: List<GemTool>? = null)
private data class GemCandidate(val content: GemContent?)
private data class GemResponse(val candidates: List<GemCandidate>?)
// JSON structuré renvoyé par le prompt (produit identifié visuellement).
private data class GemProduct(val type: String?, val nom: String?, val console: String?)
// Une entrée du tableau JSON renvoyé par le scan multiple (lot / étagère).
private data class GemBatchEntry(val titre: String?, val type: String?, val console: String?)
// JSON structuré renvoyé par le prompt d'estimation de cote (voir [GeminiVision.estimatePrice]).
private data class GemPriceEstimate(val prix_eur: Double?)

private interface GeminiApi {
    // flash-lite : qualité suffisante pour lire titres/logos, et surtout quota gratuit
    // journalier bien plus élevé que gemini-2.5-flash (plafonné à 20 req/jour sur ce projet).
    @POST("v1beta/models/gemini-2.5-flash-lite:generateContent")
    suspend fun generate(
        @Query("key") key: String,
        @Body body: GemRequest
    ): GemResponse
}

/**
 * Reconnaissance visuelle par IA (Gemini) : palier principal du scan photo. On envoie
 * directement l'image à Gemini et on lui demande le NOM textuel précis de l'objet
 * (jeu / console / accessoire rétro) — ce nom alimente ensuite la recherche IGDB.
 *
 * Clé lue depuis local.properties via BuildConfig.GEMINI_API_KEY (repli sur la clé Vision).
 * Ne lève jamais : renvoie null en cas d'échec pour laisser l'OCR prendre le relais.
 */
object GeminiVision {

    private val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(NetworkClient.vision)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }

    private val key: String get() = BuildConfig.GEMINI_API_KEY

    fun isConfigured(): Boolean = key.isNotBlank()

    // Mémorise jusqu'à quand (minuit) le quota quotidien gratuit est épuisé (vérifié en conditions
    // réelles : 20 requêtes/JOUR sur ce modèle). Sans ça, un scan par lot retentait Gemini (avec ses
    // 3 tentatives à 700ms) pour CHAQUE jeu alors que le quota était déjà mort pour la journée —
    // ralentissant tout le lot pour rien. Une fois le 429 "PerDay" vu une fois, on saute directement
    // au repli (Groq pour la reconnaissance, Tavily pour le prix) pour le reste de la session.
    @Volatile
    private var quotaExhaustedUntil: Long = 0L

    private fun isQuotaExhausted(): Boolean = System.currentTimeMillis() < quotaExhaustedUntil

    private fun markQuotaExhausted() {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        quotaExhaustedUntil = cal.timeInMillis
    }

    /** true si un 429 "quota quotidien" Gemini a déjà été détecté aujourd'hui (voir [markQuotaExhausted]). */
    private fun isDailyQuotaError(e: retrofit2.HttpException): Boolean {
        if (e.code() != 429) return false
        val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull().orEmpty()
        return body.contains("PerDay")
    }

    internal const val PROMPT =
        "Tu es un expert du jeu vidéo rétro. Analyse cette photo (jaquette, boîte, cartouche, " +
        "console, accessoire) et identifie l'objet principal via ses logos, artworks et éléments " +
        "matériels. Réponds UNIQUEMENT en JSON strict, sans texte ni balise autour : " +
        "{\"type\":\"jeu|console|accessoire\",\"nom\":\"nom commercial exact\"," +
        "\"console\":\"console associée ou null\"}. Le nom sans plateforme entre parenthèses, MAIS " +
        "en gardant impérativement toute mention de réédition/édition spéciale visible sur la " +
        "jaquette (Platinum, Collector, Greatest Hits, Player's Choice, Director's Cut, Deluxe, " +
        "GOTY, Essentials, Classics...) : une réédition ne vaut pas le même prix que l'originale, " +
        "cette mention ne doit jamais être omise si elle est visible. " +
        "Si tu ne reconnais rien de fiable : {\"type\":null,\"nom\":null,\"console\":null}."

    /** Objet identifié visuellement par l'IA (type + nom + console associée). */
    data class VisualResult(val type: String, val name: String, val console: String?)

    /**
     * Identification visuelle structurée de l'objet, ou null (image illisible, IA non configurée,
     * rien de reconnu ou échec réseau). L'image traitée (contraste rehaussé) est envoyée à l'IA.
     */
    suspend fun identify(context: Context, uri: Uri): VisualResult? {
        if (!isConfigured() || isQuotaExhausted()) return null
        val base64 = withContext(Dispatchers.IO) { encodeImage(context, uri) } ?: return null
        val request = GemRequest(
            listOf(
                GemContent(
                    listOf(
                        GemPart(text = PROMPT),
                        GemPart(inline_data = GemInlineData("image/jpeg", base64))
                    )
                )
            )
        )
        return try {
            val text = api.generate(key, request)
                .candidates?.firstOrNull()?.content?.parts?.firstOrNull { it.text != null }?.text
                ?: return null
            parseProduct(text)
        } catch (e: CancellationException) {
            throw e
        } catch (e: retrofit2.HttpException) {
            val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
            android.util.Log.e("ScanBarcode", "Gemini identify HTTP ${e.code()}: $body")
            if (isDailyQuotaError(e)) markQuotaExhausted()
            null
        } catch (e: Exception) {
            android.util.Log.e("ScanBarcode", "Gemini identify failed: ${e.javaClass.simpleName}: ${e.message}", e)
            null
        }
    }

    /**
     * Estimation de cote par IA (recherche Google intégrée), utilisée en dernier recours par
     * [AppViewModel.saveCollectionItem] quand [EbayPrices.lookup] ne trouve aucune annonce
     * comparable. Moins fiable qu'une vraie annonce eBay : l'appelant marque le résultat
     * (CollectionItem.priceIsAiEstimate) pour que l'interface affiche « (IA) » à côté du prix.
     * Renvoie le prix en centimes d'euro, ou null (IA non configurée, rien trouvé, échec réseau).
     */
    suspend fun estimatePrice(
        type: ItemType, brand: String, name: String,
        condition: Condition, hasBox: Boolean, hasManual: Boolean, platform: String? = null
    ): Int? {
        if (!isConfigured() || isQuotaExhausted()) return null
        val label = when (type) {
            ItemType.CONSOLE -> "console de jeu vidéo"
            ItemType.JEU -> "jeu vidéo"
            ItemType.ACCESSOIRE -> "accessoire de jeu vidéo"
        }
        val completeness = when {
            hasBox && hasManual -> "complet en boîte avec notice"
            hasBox -> "en boîte sans notice"
            else -> "en loose (sans boîte ni notice)"
        }
        val itemName = if (brand.isBlank() || name.lowercase().contains(brand.lowercase())) name else "$brand $name"
        // La plateforme change souvent beaucoup la cote (un même jeu peut exister sur plusieurs
        // consoles à des prix très différents) : sans la préciser ici, la requête ne changeait
        // jamais quand on modifiait la plateforme associée dans la fiche.
        val platformNote = if (!platform.isNullOrBlank() && !itemName.lowercase().contains(platform.lowercase())) " sur $platform" else ""
        // On nomme explicitement PriceCharting/Vinted/LeBoncoin/Rakuten en plus d'eBay : sans ça, le
        // search grounding de Gemini a tendance à ne formuler que des requêtes génériques ("annonce
        // vente X occasion France") qui retombent surtout sur des résultats eBay/Google Shopping —
        // pour les objets déjà sans annonce eBay comparable, élargir aux autres sources donne une
        // chance de plus de trouver un prix fiable. PriceCharting cité en premier (référence de cote
        // pour les jeux vidéo) à la demande explicite de l'utilisateur.
        val prompt = "Cherche sur internet des annonces ou ventes récentes en France pour ce $label " +
            "d'occasion : \"$itemName\"$platformNote, état ${condition.label}, $completeness. Regarde en " +
            "priorité sur PriceCharting.com (référence de cote pour les jeux vidéo), puis sur Vinted, " +
            "LeBoncoin et Rakuten (Priceminister) en plus d'eBay. Réponds UNIQUEMENT en JSON strict, " +
            "sans texte ni balise autour : {\"prix_eur\": nombre en euros, ou null si tu ne trouves " +
            "vraiment rien de fiable}."
        val request = GemRequest(
            contents = listOf(GemContent(listOf(GemPart(text = prompt)))),
            tools = listOf(GemTool())
        )
        // L'API Gemini renvoie de temps en temps une erreur transitoire (503 "high demand") qu'un
        // simple retry immédiat suffit à passer. En revanche un 429 "RESOURCE_EXHAUSTED" avec
        // quotaId contenant "PerDay" est un quota QUOTIDIEN épuisé (vérifié : ce projet n'a que 20
        // requêtes/jour en gratuit sur ce modèle) — le retenter est inutile (il ne reviendra pas
        // avant le lendemain) et ne fait que ralentir tout un lot pour rien : on abandonne direct.
        repeat(3) { attempt ->
            try {
                // Avec le search grounding activé, Gemini renvoie parfois PLUSIEURS blocs de texte
                // (un raisonnement en prose, PUIS le JSON demandé, PUIS parfois une reformulation) —
                // vérifié en conditions réelles. Ne garder que le premier bloc (l'ancien code) prend
                // souvent la prose de réflexion (qui peut citer un chiffre spéculatif) au lieu de la
                // vraie conclusion du modèle : on concatène donc tous les blocs avant d'en extraire
                // le prix, pour retomber sur le JSON final quelle que soit sa position.
                val parts = api.generate(key, request)
                    .candidates?.firstOrNull()?.content?.parts?.mapNotNull { it.text } ?: return null
                if (parts.isEmpty()) return null
                val fullText = parts.joinToString("\n")
                val price = extractJsonPrice(fullText) ?: extractPriceFromFreeText(fullText) ?: return null
                return if (price <= 0) null else (price * 100).roundToInt()
            } catch (e: CancellationException) {
                throw e
            } catch (e: retrofit2.HttpException) {
                if (isDailyQuotaError(e)) {
                    markQuotaExhausted() // évite de retenter Gemini pour le reste du lot/de la session
                    return null
                }
                if (attempt == 2) return null
                delay(700L)
            } catch (e: Exception) {
                if (attempt == 2) return null
                delay(700L)
            }
        }
        return null
    }

    /**
     * Cherche le JSON `{"prix_eur": ...}` demandé n'importe où dans un texte (voir [estimatePrice]).
     * Internal (pas private) : réutilisé par [TavilyPriceEstimate], 3e recours de prix qui redemande
     * le même format JSON à Groq à partir des résultats de recherche web Tavily.
     */
    internal fun extractJsonPrice(text: String): Double? {
        // Le "}" final DOIT être échappé : accepté tel quel par java.util.regex (JVM), mais rejeté
        // par le moteur ICU d'Android (PatternSyntaxException), jamais détecté avant la correction
        // des règles ProGuard ci-dessus qui empêchait cette fonction de s'exécuter sur un appareil réel.
        val match = Regex("\\{[^{}]*\"prix_eur\"[^{}]*\\}").find(text) ?: return null
        return runCatching { Gson().fromJson(match.value, GemPriceEstimate::class.java) }.getOrNull()?.prix_eur
    }

    // Parsing partagé (réutilisé par [GroqVision]) : nettoie les balises ```json et valide le JSON.
    // Le modèle Groq (Qwen3.6, un modèle "thinking") enveloppe sa réponse dans un bloc
    // <think>...</think> AVANT le JSON demandé — vérifié en conditions réelles. Sans ce nettoyage,
    // le JSON derrière échouait systématiquement à parser (le bloc de réflexion n'est pas du JSON),
    // faisant échouer silencieusement TOUTE reconnaissance Groq dès que le modèle "réfléchit".
    internal fun stripFences(text: String): String =
        text.trim()
            .replace(Regex("(?s)<think>.*?</think>"), "")
            .trim()
            .removePrefix("```json").removePrefix("```").removeSuffix("```").trim()

    /**
     * Dernier recours si le JSON demandé est introuvable ([extractJsonPrice]) : récupère un prix
     * donné en toutes lettres, dans les deux sens ("70 €"/"70 euros" ou "€70"/"environ €70-150").
     */
    internal fun extractPriceFromFreeText(text: String): Double? {
        val after = Regex("(\\d+(?:[.,]\\d+)?)\\s*(?:€|euros?)", RegexOption.IGNORE_CASE).find(text)
        if (after != null) return after.groupValues[1].replace(',', '.').toDoubleOrNull()
        val before = Regex("€\\s*(\\d+(?:[.,]\\d+)?)", RegexOption.IGNORE_CASE).find(text) ?: return null
        return before.groupValues[1].replace(',', '.').toDoubleOrNull()
    }

    internal fun parseProduct(rawText: String): VisualResult? {
        val p = runCatching { Gson().fromJson(stripFences(rawText), GemProduct::class.java) }.getOrNull() ?: return null
        val name = p.nom?.trim().orEmpty()
        val type = p.type?.trim()?.lowercase().orEmpty()
        if (name.isBlank() || name.length > 80 || type.isBlank() || type == "null") return null
        return VisualResult(type, name, p.console?.trim()?.takeIf { it.isNotBlank() && !it.equals("null", true) })
    }

    internal fun parseBatch(rawText: String): List<BatchItem>? {
        val cleaned = stripFences(rawText)
        val arr = runCatching { Gson().fromJson(cleaned, Array<GemBatchEntry>::class.java) }
            .onFailure { android.util.Log.e("ScanBarcode", "parseBatch JSON error: ${it.message} | cleaned=${cleaned.take(500)}") }
            .getOrNull()
            ?: return null
        // PAS de dédoublonnage par titre : une étagère peut légitimement contenir plusieurs
        // exemplaires physiques du même jeu (vérifié : 3 boîtiers "Les Sims" identiques sur une
        // même photo ne remontaient qu'une seule fois avant ce correctif). L'utilisateur valide de
        // toute façon chaque article un par un via des cases à cocher avant l'ajout, donc un doublon
        // accidentel (angle de prise de vue, reflet) reste sans conséquence à décocher soi-même.
        return arr.mapNotNull { e ->
            e.titre?.trim()?.takeIf { it.isNotBlank() && it.length in 2..80 }?.let { title ->
                val type = e.type?.trim()?.lowercase()?.takeIf { it in setOf("jeu", "console", "accessoire") }
                BatchItem(title, type, e.console?.trim()?.takeIf { it.isNotBlank() && !it.equals("null", true) })
            }
        }
    }

    /**
     * Un article détecté dans un lot par le scan multiple. [type] vaut "jeu", "console" ou
     * "accessoire" (null si non reconnu par l'IA -> traité comme un jeu par défaut, le cas le
     * plus fréquent). Avant l'ajout de ce champ, TOUT article de lot était traité comme un jeu,
     * y compris les consoles/accessoires détectés par le prompt — une console scannée par lot
     * (ex. une 3DS XL en édition spéciale) finissait alors avec une recherche jeu vidéo (IGDB/RAWG)
     * qui ne trouve rien de pertinent, au lieu d'une reconnaissance console/accessoire.
     */
    data class BatchItem(val title: String, val type: String?, val console: String?)

    internal const val BATCH_PROMPT =
        "Tu es un expert du jeu vidéo rétro. Cette photo montre PLUSIEURS articles (un lot, une " +
        "étagère…). Isole visuellement chaque jeu, console ou accessoire distinct via ses logos, " +
        "jaquettes et tranches. Réponds UNIQUEMENT par un tableau JSON strict, sans texte ni balise " +
        "autour : [{\"titre\":\"nom exact\",\"type\":\"jeu, console ou accessoire\",\"console\":" +
        "\"console associée ou null\"}]. Le champ \"type\" doit refléter la nature réelle de " +
        "l'article (une console ou un accessoire physique n'est PAS un jeu, même si son nom " +
        "contient une référence à un jeu, ex. une console \"3DS XL Pokémon\" est de type " +
        "\"console\", pas \"jeu\"). Un objet par boîtier/appareil physiquement visible, MÊME si " +
        "plusieurs exemplaires identiques du même jeu sont présents (ex. 3 boîtiers \"Les Sims\" " +
        "côte à côte = 3 entrées distinctes dans le tableau, pas une seule) : ne fusionne jamais " +
        "deux titres identiques en une seule entrée. Un objet par article réellement visible et " +
        "lisible ; ne devine pas. Le titre doit impérativement garder toute mention de " +
        "réédition/édition spéciale visible sur la jaquette (Platinum, Collector, Greatest Hits, " +
        "Player's Choice, Director's Cut, Deluxe, GOTY, Essentials, Classics...) : une réédition " +
        "ne vaut pas le même prix que l'originale, cette mention ne doit jamais être omise si " +
        "elle est visible. Si rien de fiable : []."

    /**
     * Scan multiple : identifie tous les articles présents sur une photo de lot. L'image traitée
     * (contraste rehaussé, sans recadrage) est envoyée à l'IA. Null si non configuré / échec ;
     * liste (éventuellement vide) des articles détectés sinon.
     */
    suspend fun identifyBatch(context: Context, uri: Uri): List<BatchItem>? {
        if (!isConfigured() || isQuotaExhausted()) return null
        val base64 = withContext(Dispatchers.IO) { encodeImage(context, uri) } ?: return null
        val request = GemRequest(
            listOf(GemContent(listOf(
                GemPart(text = BATCH_PROMPT),
                GemPart(inline_data = GemInlineData("image/jpeg", base64))
            )))
        )
        return try {
            val text = api.generate(key, request)
                .candidates?.firstOrNull()?.content?.parts?.firstOrNull { it.text != null }?.text
                ?: return null
            parseBatch(text)
        } catch (e: CancellationException) {
            throw e
        } catch (e: retrofit2.HttpException) {
            val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
            android.util.Log.e("ScanBarcode", "Gemini identifyBatch HTTP ${e.code()}: $body")
            if (isDailyQuotaError(e)) markQuotaExhausted()
            null
        } catch (e: Exception) {
            android.util.Log.e("ScanBarcode", "Gemini identifyBatch failed: ${e.javaClass.simpleName}: ${e.message}", e)
            null
        }
    }

    // Prétraite l'image (sous-échantillonnage + contraste, voir [ImagePreprocess]) puis la
    // ré-encode en JPEG base64 : requête légère et texte/logos plus lisibles pour l'IA.
    internal fun encodeImage(context: Context, uri: Uri): String? = try {
        ImagePreprocess.enhance(context, uri)?.let { bmp ->
            val out = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, out)
            bmp.recycle()
            Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        }
    } catch (e: Exception) {
        null
    }
}
