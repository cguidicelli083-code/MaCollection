package com.example.macollection.data

import kotlinx.coroutines.CancellationException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private data class UpcItem(val title: String?, val brand: String?, val category: String?)
private data class UpcLookupResponse(val code: String?, val items: List<UpcItem>?)

private interface UpcItemDbApi {
    @GET("prod/trial/lookup")
    suspend fun lookup(@Query("upc") upc: String): UpcLookupResponse
}

/**
 * Titre nettoyé (marque retirée) + catégorie BRUTE renvoyée par UPCitemdb (ex. "Electronics >
 * Video Game Consoles"). La catégorie est un champ STRUCTURÉ posé par UPCitemdb lui-même sur le
 * produit, indépendant du texte du titre : un JEU y est toujours classé "Video Games", jamais
 * "Video Game Consoles", quelle que soit la console mentionnée dans SON titre à lui (cf. usage
 * dans [ScanTools.identifyFromBarcode]).
 */
data class UpcLookupResult(val title: String, val category: String?)

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

    suspend fun lookup(barcode: String): UpcLookupResult? {
        if (barcode.isBlank()) return null
        return try {
            val response = api.lookup(barcode)
            android.util.Log.d("ScanBarcode", "UPCitemdb response for $barcode: code=${response.code} items=${response.items}")
            val item = response.items?.firstOrNull() ?: return null
            val title = item.title?.takeIf { it.isNotBlank() } ?: return null
            // UPCitemdb préfixe souvent le titre par l'éditeur/la marque (ex. "Capcom Resident Evil
            // 6") : IGDB fait une recherche par PHRASE si stricte qu'un SEUL mot en trop suffit à
            // faire tomber tout résultat à zéro (constaté en conditions réelles : "Capcom Resident
            // Evil 6" -> aucun résultat, "Resident Evil 6" seul -> trouvé immédiatement). On retire
            // ce préfixe quand on le connaît via le champ "brand" de la réponse, plutôt que de lister
            // à la main chaque éditeur possible (liste sans fin, contrairement aux marques/consoles
            // de [ScanTools.listingNoiseWords]).
            val brand = item.brand?.trim()?.takeIf { it.isNotBlank() }
            val cleanedTitle = if (brand != null && title.startsWith(brand, ignoreCase = true)) {
                title.substring(brand.length).trim().trimStart(':', '-').trim().ifBlank { title }
            } else {
                title
            }
            UpcLookupResult(cleanedTitle, item.category)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.e("ScanBarcode", "UPCitemdb lookup failed for $barcode: ${e.javaClass.simpleName}: ${e.message}", e)
            null
        }
    }
}
