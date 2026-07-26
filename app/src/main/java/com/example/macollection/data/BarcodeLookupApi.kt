package com.example.macollection.data

import com.example.macollection.BuildConfig
import kotlinx.coroutines.CancellationException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private data class BarcodeLookupProduct(val title: String?)
private data class BarcodeLookupResponse(val products: List<BarcodeLookupProduct>?)

private interface BarcodeLookupApiService {
    @GET("v3/products")
    suspend fun lookup(@Query("barcode") barcode: String, @Query("key") key: String): BarcodeLookupResponse
}

/**
 * Base de données de codes-barres produits (Barcode Lookup), utilisée en 2e position (après
 * [UpcItemDb], avant les annonces eBay) pour identifier un objet scanné : bonne couverture
 * jeux vidéo constatée en test (titres propres retrouvés pour des jeux absents d'UPCitemdb ET
 * d'eBay). Clé API gratuite fournie par l'utilisateur (compte personnel sur barcodelookup.com).
 */
object BarcodeLookupApi {

    private val api: BarcodeLookupApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.barcodelookup.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BarcodeLookupApiService::class.java)
    }

    private val apiKey: String get() = BuildConfig.BARCODE_LOOKUP_API_KEY

    fun isConfigured(): Boolean = apiKey.isNotBlank()

    suspend fun lookupTitle(barcode: String): String? {
        if (!isConfigured() || barcode.isBlank()) return null
        return try {
            api.lookup(barcode, apiKey).products?.firstOrNull()?.title?.takeIf { it.isNotBlank() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.e("ScanBarcode", "Barcode Lookup failed for $barcode: ${e.javaClass.simpleName}: ${e.message}", e)
            null
        }
    }
}
