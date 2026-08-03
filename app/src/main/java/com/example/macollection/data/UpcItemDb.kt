package com.example.macollection.data

import kotlinx.coroutines.CancellationException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private data class UpcItem(val title: String?)
private data class UpcLookupResponse(val code: String?, val items: List<UpcItem>?)

private interface UpcItemDbApi {
    @GET("prod/trial/lookup")
    suspend fun lookup(@Query("upc") upc: String): UpcLookupResponse
}

/**
 * Base de données de codes-barres produits (UPCitemdb), utilisée en PREMIER pour identifier un
 * objet scanné par code-barres : contrairement à une annonce eBay (titre rédigé librement par un
 * vendeur, plein de mentions d'état/complétude qui égarent la recherche, cf. [ScanTools]), cette
 * base renvoie un titre "propre" unique par code-barres (ex. "Mario Kart 8 Deluxe (Nintendo
 * Switch)"), bien plus fiable pour retrouver le bon jeu.
 *
 * Endpoint "trial" gratuit, sans clé API à configurer : limité à 100 requêtes/jour par adresse IP
 * (documenté par UPCitemdb), largement suffisant pour un usage personnel (scanner sa propre
 * collection, pas un usage commercial en masse). Si le quota est dépassé ou que le code-barres est
 * inconnu, on retombe simplement sur les annonces eBay (cf. [EbayPrices.titlesForBarcode]).
 */
object UpcItemDb {

    private val api: UpcItemDbApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.upcitemdb.com/")
            .client(NetworkClient.http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UpcItemDbApi::class.java)
    }

    suspend fun lookupTitle(barcode: String): String? {
        if (barcode.isBlank()) return null
        return try {
            val response = api.lookup(barcode)
            android.util.Log.d("ScanBarcode", "UPCitemdb response for $barcode: code=${response.code} items=${response.items}")
            response.items?.firstOrNull()?.title?.takeIf { it.isNotBlank() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.e("ScanBarcode", "UPCitemdb lookup failed for $barcode: ${e.javaClass.simpleName}: ${e.message}", e)
            null
        }
    }
}
