package com.example.macollection.data

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Client HTTP partagé par toutes les API réseau de l'appli (eBay, RAWG, IGDB, ScanDex...).
 * Sans délais explicites, OkHttp attend par défaut jusqu'à 10s pour se connecter PUIS jusqu'à
 * 10s pour lire la réponse (20s au total) avant d'abandonner un seul appel — avec plusieurs
 * sources consultées à la suite (ex. un scan code-barres qui replie sur IGDB puis RAWG pour
 * chaque titre essayé), un seul serveur lent ou injoignable suffit à faire paraître le scan
 * bloqué "indéfiniment" alors qu'il finit par aboutir, juste très lentement.
 */
object NetworkClient {
    val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        // Plafond dur sur la durée TOTALE d'un appel (connexion + écriture + lecture + retries) :
        // contrairement à connectTimeout/readTimeout, ce délai n'est jamais réinitialisé en cours
        // de route (ex. un serveur qui renvoie la réponse par petits paquets espacés de moins de 8s
        // à chaque fois ne redéclenchait jamais readTimeout, et l'appel pouvait quand même traîner
        // largement au-delà des délais fixés ci-dessus).
        .callTimeout(7, TimeUnit.SECONDS)
        .build()

    // Client dédié aux appels de reconnaissance visuelle IA (Gemini/Groq) : un modèle "thinking"
    // (Groq/Qwen3.6, voir GroqVision) peut légitimement mettre plus de 7s à répondre sur une photo
    // de lot à plusieurs articles (vérifié en conditions réelles : timeout client déclenché EN PLEIN
    // milieu d'une génération encore valide côté serveur, alors qu'un délai plus long aurait laissé
    // la réponse aboutir). Les appels réseau "légers" (IGDB/RAWG/eBay/barcode) gardent le timeout
    // court ci-dessus, qui reste pertinent pour eux.
    val vision: OkHttpClient = http.newBuilder()
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(35, TimeUnit.SECONDS)
        .callTimeout(40, TimeUnit.SECONDS)
        .build()
}
