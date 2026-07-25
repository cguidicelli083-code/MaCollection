package com.example.macollection.data

import com.example.macollection.BuildConfig
import kotlinx.coroutines.CancellationException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import kotlin.math.roundToInt

// --- DTO Tavily (recherche web) ---
private data class TavilySearchRequest(
    val api_key: String,
    val query: String,
    val search_depth: String = "basic",
    val max_results: Int = 5
)
private data class TavilyResult(val title: String?, val content: String?)
private data class TavilySearchResponse(val results: List<TavilyResult>?)

private interface TavilyApi {
    @POST("search")
    suspend fun search(@Body body: TavilySearchRequest): TavilySearchResponse
}

/**
 * 3e recours pour l'estimation de cote, après eBay (annonces réelles) puis Gemini (IA + recherche
 * intégrée). Nécessaire car la clé Gemini de ce projet n'a qu'un quota gratuit de 20 requêtes/JOUR
 * (vérifié en conditions réelles) : dès qu'un scan par lot ou une grosse session d'ajouts le
 * dépasse, Gemini échoue systématiquement pour le reste de la journée. Tavily (recherche web, quota
 * gratuit séparé et bien plus généreux) trouve des annonces/prix, puis Groq (modèle texte, quota
 * lui aussi séparé de la reconnaissance photo) en extrait un prix — même format JSON que
 * [GeminiVision.estimatePrice], donc réutilise son parsing ([GeminiVision.extractJsonPrice] etc.).
 */
object TavilyPriceEstimate {

    private val api: TavilyApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.tavily.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TavilyApi::class.java)
    }

    private val key: String get() = BuildConfig.TAVILY_API_KEY

    fun isConfigured(): Boolean = key.isNotBlank() && GroqVision.isConfigured()

    // Quota Tavily gratuit MENSUEL (1000 crédits/mois, cf. tavily.com/pricing) : une fois épuisé,
    // chaque appel renvoie un 429 pour rien jusqu'à la fin du mois — inutile de continuer à
    // solliciter l'API (et donc Groq juste après) pour un échec certain. Court-circuit en mémoire,
    // même principe que [GeminiVision.isQuotaExhausted] mais avec une échéance mensuelle plutôt que
    // quotidienne. Comme pour Gemini, ce drapeau est oublié si l'appli redémarre avant l'échéance :
    // au pire, un seul appel supplémentaire échoue avant d'être remémorisé pour la session en cours.
    @Volatile
    private var quotaExhaustedUntil: Long = 0L

    private fun isQuotaExhausted(): Boolean = System.currentTimeMillis() < quotaExhaustedUntil

    private fun markQuotaExhausted() {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.MONTH, 1)
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        quotaExhaustedUntil = cal.timeInMillis
    }

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
        // La plateforme change souvent beaucoup la cote (même raison que GeminiVision.estimatePrice).
        val platformNote = if (!platform.isNullOrBlank() && !itemName.lowercase().contains(platform.lowercase())) " $platform" else ""

        val results = try {
            api.search(TavilySearchRequest(key, "prix occasion $itemName$platformNote ${condition.label} France vente")).results.orEmpty()
        } catch (e: CancellationException) {
            throw e
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 429) markQuotaExhausted()
            return null
        } catch (e: Exception) {
            return null
        }
        if (results.isEmpty()) return null

        val snippets = results.take(5).joinToString("\n\n") { r ->
            "${r.title.orEmpty()} : ${r.content.orEmpty().take(400)}"
        }
        val prompt = "Voici des résultats de recherche web pour ce $label d'occasion \"$itemName\"$platformNote, " +
            "état ${condition.label}, $completeness :\n\n$snippets\n\nÀ partir de CES résultats " +
            "UNIQUEMENT (n'invente rien d'autre), donne une estimation de prix en euros. Réponds " +
            "UNIQUEMENT en JSON strict, sans texte ni balise autour : {\"prix_eur\": nombre en " +
            "euros, ou null si ces résultats ne permettent pas d'estimer un prix fiable}."

        val text = runCatching { GroqVision.completeText(prompt) }.getOrNull() ?: return null
        val price = GeminiVision.extractJsonPrice(text) ?: GeminiVision.extractPriceFromFreeText(text) ?: return null
        return if (price <= 0) null else (price * 100).roundToInt()
    }
}
