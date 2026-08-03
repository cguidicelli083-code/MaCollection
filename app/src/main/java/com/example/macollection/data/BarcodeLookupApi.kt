package com.example.macollection.data

import com.example.macollection.BuildConfig
import kotlinx.coroutines.CancellationException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private data class BarcodeLookupProduct(val title: String?, val features: List<String>? = null)
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
            .client(NetworkClient.http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BarcodeLookupApiService::class.java)
    }

    private val apiKey: String get() = BuildConfig.BARCODE_LOOKUP_API_KEY

    fun isConfigured(): Boolean = apiKey.isNotBlank()

    suspend fun lookupTitle(barcode: String): String? {
        if (!isConfigured() || barcode.isBlank()) return null
        return try {
            val product = api.lookup(barcode, apiKey).products?.firstOrNull() ?: return null
            // Certaines fiches (constaté sur des produits japonais, ex. NEC PC Engine CoreGrafx
            // II) ont un titre corrompu À LA SOURCE côté Barcode Lookup (encodage cassé, ex.
            // "?????????2?? ?pc?????" au lieu du vrai titre) : un titre réel ne contient jamais de
            // "?" littéral, donc sa présence signale un titre inexploitable. Le champ "features"
            // reste souvent propre dans ce cas (ex. "Core Grafx 2") et sert alors de repli.
            val title = product.title?.takeIf { it.isNotBlank() && !it.contains('?') }
            title ?: product.features?.filter { it.isNotBlank() }?.joinToString(" ")?.takeIf { it.isNotBlank() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.e("ScanBarcode", "Barcode Lookup failed for $barcode: ${e.javaClass.simpleName}: ${e.message}", e)
            null
        }
    }
}
