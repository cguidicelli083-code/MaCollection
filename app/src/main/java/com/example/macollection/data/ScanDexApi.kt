package com.example.macollection.data

import com.example.macollection.BuildConfig
import kotlinx.coroutines.CancellationException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

private data class ScanDexPlatform(val name: String?)
private data class ScanDexIgdbMetadata(val id: Long?, val name: String?, val platform: ScanDexPlatform?)
private data class ScanDexResponse(val igdb_metadata: ScanDexIgdbMetadata?)

private interface ScanDexApiService {
    @GET("api/v2/lookup")
    suspend fun lookup(@Query("value") value: String, @Header("Authorization") token: String): ScanDexResponse
}

/** Correspondance code-barres -> jeu déjà identifiée par ScanDex (id IGDB, nom, plateforme). */
data class ScanDexMatch(val igdbId: Long, val name: String, val platformName: String?)

/**
 * Base de codes-barres spécialisée JEUX VIDÉO (scandex.gamery.app), utilisée en TOUT PREMIER pour
 * identifier un jeu scanné : contrairement à UPCitemdb/Barcode Lookup/Barcode Spider (bases
 * produits généralistes, dont le titre doit être nettoyé puis comparé par score de confiance),
 * ScanDex fait déjà correspondre le code-barres à une fiche IGDB précise dans SA PROPRE base —
 * une correspondance directement fiable, à privilégier sur le scoring flou des autres sources.
 * Gratuite (compte personnel de l'utilisateur sur scandex.gamery.app), quota non communiqué.
 */
object ScanDexApi {

    private val api: ScanDexApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://scandex.gamery.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ScanDexApiService::class.java)
    }

    private val apiKey: String get() = BuildConfig.SCANDEX_API_KEY

    fun isConfigured(): Boolean = apiKey.isNotBlank()

    suspend fun lookupGame(barcode: String): ScanDexMatch? {
        if (!isConfigured() || barcode.isBlank()) return null
        return try {
            val meta = api.lookup(barcode, apiKey).igdb_metadata ?: return null
            val id = meta.id ?: return null
            val name = meta.name?.takeIf { it.isNotBlank() } ?: return null
            ScanDexMatch(id, name, meta.platform?.name)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.e("ScanBarcode", "ScanDex lookup failed for $barcode: ${e.javaClass.simpleName}: ${e.message}", e)
            null
        }
    }
}
