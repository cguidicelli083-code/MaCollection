package com.example.macollection.data

import android.content.Context
import android.net.Uri
import com.example.macollection.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
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
private data class GqRequest(val model: String, val messages: List<GqMessage>, val temperature: Double = 0.2)
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
        val body = GqRequest(
            model = MODEL,
            messages = listOf(GqMessage("user", listOf(
                GqContent("text", text = prompt),
                GqContent("image_url", image_url = GqImageUrl("data:image/jpeg;base64,$base64"))
            )))
        )
        return try {
            api.chat("Bearer $key", body).choices?.firstOrNull()?.message?.content
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }
}
