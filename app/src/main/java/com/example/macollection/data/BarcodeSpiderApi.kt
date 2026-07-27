package com.example.macollection.data

import com.example.macollection.BuildConfig
import kotlinx.coroutines.CancellationException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private data class BarcodeSpiderAttributes(val title: String?)
private data class BarcodeSpiderResponse(val item_attributes: BarcodeSpiderAttributes?)

private interface BarcodeSpiderApiService {
    // v1 : endpoint marqué "deprecated" par Barcode Spider mais dont la coupure n'est prévue
    // qu'en 2027 (constaté sur une réponse réelle) ; v2 n'existe pas encore à une URL stable.
    @GET("v1/lookup")
    suspend fun lookup(@Query("upc") upc: String, @Query("token") token: String): BarcodeSpiderResponse
}

/**
 * Base de données de codes-barres produits (Barcode Spider), utilisée en 3e position (après
 * [UpcItemDb] et [BarcodeLookupApi], avant les annonces eBay) : quota GRATUIT distinct des deux
 * autres sources (100 requêtes/jour, propre compte/jeton), donc un vrai quota supplémentaire et
 * pas juste un doublon des deux premières. Clé API gratuite fournie par l'utilisateur (compte
 * personnel sur barcodespider.com).
 */
object BarcodeSpiderApi {

    private val api: BarcodeSpiderApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.barcodespider.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BarcodeSpiderApiService::class.java)
    }

    private val apiKey: String get() = BuildConfig.BARCODE_SPIDER_API_KEY

    fun isConfigured(): Boolean = apiKey.isNotBlank()

    suspend fun lookupTitle(barcode: String): String? {
        if (!isConfigured() || barcode.isBlank()) return null
        return try {
            api.lookup(barcode, apiKey).item_attributes?.title?.takeIf { it.isNotBlank() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.e("ScanBarcode", "Barcode Spider lookup failed for $barcode: ${e.javaClass.simpleName}: ${e.message}", e)
            null
        }
    }
}
