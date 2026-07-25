package com.example.macollection

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.android.gms.ads.MobileAds
import okhttp3.OkHttpClient

/**
 * Wikimedia (upload.wikimedia.org) exige un en-tête User-Agent descriptif et bloque (403)
 * les requêtes avec un agent générique/vide — ce qui empêchait beaucoup de photos de
 * consoles/accessoires (issues de Wikimedia Commons) de charger. Coil utilise sinon
 * l'agent par défaut d'OkHttp ; on en force un explicite pour toutes les images de l'app.
 */
class MaCollectionApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        // Unités publicitaires de TEST uniquement (voir ui/ads/AdsManager.kt).
        MobileAds.initialize(this)
    }

    override fun newImageLoader(): ImageLoader {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "MaCollectionApp/1.0 (Android app personnelle de collection)")
                    .build()
                chain.proceed(request)
            }
            .build()
        return ImageLoader.Builder(this)
            .okHttpClient(client)
            .build()
    }
}
