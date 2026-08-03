package com.example.macollection.data

import android.content.Context
import android.net.Uri
import com.example.macollection.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// --- DTO Groq (API compatible OpenAI /chat/completions) ---
private data class GqImageUrl(val url: String)
private data class GqContent(val type: String, val text: String? = null, val image_url: GqImageUrl? = null)
private data class GqMessage(val role: String, val content: List<GqContent>)
private data class GqRequest(
    val model: String,
    val messages: List<GqMessage>,
    val temperature: Double = 0.2,
    val max_tokens: Int? = null
)
private data class GqRespMsg(val content: String?)
private data class GqChoice(val message: GqRespMsg?)
private data class GqResponse(val choices: List<GqChoice>?)

private interface GroqApi {
    @POST("openai/v1/chat/completions")
    suspend fun chat(@Header("Authorization") auth: String, @Body body: GqRequest): GqResponse
}

/**
 * Reconnaissance visuelle de secours via Groq (modèle Llama multimodal), utilisée quand Gemini
 * échoue — typiquement quota journalier gratuit atteint. Free tier bien plus large que Gemini.
 * Réutilise le prétraitement d'image et le parsing JSON de [GeminiVision] : mêmes prompts, mêmes
 * types de résultat (VisualResult / BatchItem), donc interchangeable avec Gemini sans rien changer
 * en aval. Ne lève jamais : renvoie null en cas d'échec.
 */
object GroqVision {

    // Modèle multimodal disponible en free tier. Llama 4 Scout a été décommissionné par Groq le
    // 17/07/2026 ; remplacé par Qwen3.6 27B, le seul autre modèle vision proposé par Groq à cette
    // date (GPT-OSS 120B, suggéré en parallèle par Groq, est texte seul et ne peut pas le remplacer
    // ici puisqu'on envoie une image). Modèle « preview » chez Groq (pas de SLA production), à
    // surveiller si Groq le fait évoluer à son tour.
    private const val MODEL = "qwen/qwen3.6-27b"
    // Modèle texte rapide pour la traduction (free tier généreux).
    private const val TEXT_MODEL = "llama-3.1-8b-instant"

    private fun languageName(code: String): String = when (code) {
        "fr" -> "French"; "de" -> "German"; "es" -> "Spanish"; "it" -> "Italian"
        "pt" -> "Portuguese"; "tr" -> "Turkish"; "ru" -> "Russian"; "el" -> "Greek"
        "zh" -> "Chinese"; "ja" -> "Japanese"; else -> "English"
    }

    /**
     * Traduit [text] dans la langue [targetLangCode] (code appli, ex. "fr"). Si le texte est déjà
     * dans cette langue, le modèle le renvoie inchangé. Renvoie null en cas d'échec (Groq non
     * configuré, réseau…) — l'appelant garde alors le texte d'origine.
     */
    suspend fun translate(text: String, targetLangCode: String): String? {
        if (!isConfigured() || text.isBlank()) return null
        val lang = languageName(targetLangCode)
        val prompt = "Translate the following text into $lang. If it is already in $lang, return it " +
            "unchanged. Output ONLY the translation, with no preamble, notes or quotation marks.\n\n$text"
        val body = GqRequest(TEXT_MODEL, listOf(GqMessage("user", listOf(GqContent("text", text = prompt)))))
        return try {
            api.chat("Bearer $key", body).choices?.firstOrNull()?.message?.content?.trim()?.takeIf { it.isNotBlank() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Envoie un prompt texte brut à Groq (modèle rapide, quota séparé de la vision) et renvoie sa
     * réponse telle quelle, ou null en cas d'échec. Utilisé par [TavilyPriceEstimate] pour extraire
     * un prix des résultats de recherche web Tavily, à la même façon que [GeminiVision.estimatePrice].
     */
    suspend fun completeText(prompt: String): String? {
        if (!isConfigured()) return null
        val body = GqRequest(TEXT_MODEL, listOf(GqMessage("user", listOf(GqContent("text", text = prompt)))))
        return try {
            api.chat("Bearer $key", body).choices?.firstOrNull()?.message?.content
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    private val api: GroqApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.groq.com/")
            .client(NetworkClient.vision)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GroqApi::class.java)
    }

    private val key: String get() = BuildConfig.GROQ_API_KEY

    fun isConfigured(): Boolean = key.isNotBlank()

    /** Équivalent Groq de [GeminiVision.identify]. */
    suspend fun identify(context: Context, uri: Uri): GeminiVision.VisualResult? =
        request(context, uri, GeminiVision.PROMPT)?.let { GeminiVision.parseProduct(it) }

    /** Équivalent Groq de [GeminiVision.identifyBatch] (scan multiple). */
    suspend fun identifyBatch(context: Context, uri: Uri): List<GeminiVision.BatchItem>? =
        request(context, uri, GeminiVision.BATCH_PROMPT)?.let { GeminiVision.parseBatch(it) }

    // Envoie l'image (prétraitée par ImagePreprocess via GeminiVision) + le prompt, renvoie le
    // texte brut de la réponse (JSON éventuellement entre balises, nettoyé par le parseur partagé).
    private suspend fun request(context: Context, uri: Uri, prompt: String): String? {
        if (!isConfigured()) return null
        val base64 = withContext(Dispatchers.IO) { GeminiVision.encodeImage(context, uri) } ?: return null
        // "/no_think" : commande native Qwen3 (indépendante de Groq) qui désactive le mode
        // "raisonnement" du modèle — sans ça, Qwen3.6 consomme TOUT son budget de tokens en
        // réflexion interne (balise <think>) sans jamais atteindre le JSON demandé, quelle que soit
        // la taille du lot (vérifié : même longueur de réponse, toujours coupée en pleine réflexion,
        // sur une photo à 14 ARTICLES COMME à 6). Effet attendu : réponse directe, sans <think>.
        val body = GqRequest(
            model = MODEL,
            messages = listOf(GqMessage("user", listOf(
                GqContent("text", text = "$prompt /no_think"),
                GqContent("image_url", image_url = GqImageUrl("data:image/jpeg;base64,$base64"))
            ))),
            // Modèle "thinking" (voir commentaire sur MODEL) : sans plafond explicite, la réponse
            // peut être coupée par le défaut de Groq EN PLEIN raisonnement (balise <think> jamais
            // refermée) avant d'atteindre le JSON demandé, surtout sur un scan de lot à plusieurs
            // articles (vérifié en conditions réelles : réponse tronquée à 6269 caractères, encore
            // dans le bloc <think>). Un plafond trop haut (8192) casse à son tour la requête : le
            // free tier Groq limite à 8000 tokens/minute TOTAL (prompt + image + max_tokens) pour ce
            // modèle, or l'image + le prompt de lot valent déjà ~2700 tokens (vérifié : 413 avec
            // max_tokens=8192 -> "Requested 10921" pour une limite de 8000). Reste sous ce plafond
            // avec une marge de sécurité.
            max_tokens = 5000
        )
        return try {
            val content = api.chat("Bearer $key", body).choices?.firstOrNull()?.message?.content
            android.util.Log.d("ScanBarcode", "Groq raw response (${content?.length ?: 0} chars): ${content?.take(500)}")
            content
        } catch (e: CancellationException) {
            throw e
        } catch (e: retrofit2.HttpException) {
            val body2 = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
            // 429 "tokens per minute" (voir commentaire max_tokens ci-dessus) : purement temporaire,
            // le budget se libère tout seul quelques secondes plus tard — Groq indique lui-même le
            // délai dans le message ("...try again in 17.235s..."). Une seule retentative après ce
            // délai (pas de boucle : si ça échoue encore, autant laisser Gemini reprendre la main
            // au quota suivant plutôt que de faire attendre l'utilisateur indéfiniment).
            if (e.code() == 429) {
                val waitMs = parseRetryDelayMs(body2)
                android.util.Log.w("ScanBarcode", "Groq 429, retry in ${waitMs}ms: $body2")
                delay(waitMs)
                return try {
                    val content = api.chat("Bearer $key", body).choices?.firstOrNull()?.message?.content
                    android.util.Log.d("ScanBarcode", "Groq retry response (${content?.length ?: 0} chars): ${content?.take(500)}")
                    content
                } catch (e2: CancellationException) {
                    throw e2
                } catch (e2: Exception) {
                    android.util.Log.e("ScanBarcode", "Groq retry failed: ${e2.javaClass.simpleName}: ${e2.message}")
                    null
                }
            }
            android.util.Log.e("ScanBarcode", "Groq request HTTP ${e.code()}: $body2")
            null
        } catch (e: Exception) {
            android.util.Log.e("ScanBarcode", "Groq request failed: ${e.javaClass.simpleName}: ${e.message}", e)
            null
        }
    }

    // Extrait le délai d'attente suggéré par Groq dans son message d'erreur 429 ("...try again in
    // 17.235s..."). 20s de repli si absent/imprévu ; plafonné à 30s pour ne jamais faire attendre
    // l'utilisateur au-delà d'une durée raisonnable pour un scan qu'il attend activement.
    private fun parseRetryDelayMs(body: String?): Long {
        val seconds = body?.let { Regex("in ([0-9]+(?:\\.[0-9]+)?)s").find(it) }
            ?.groupValues?.get(1)?.toDoubleOrNull()
        return (((seconds ?: 20.0) + 0.5) * 1000).toLong().coerceIn(1000L, 30_000L)
    }
}
