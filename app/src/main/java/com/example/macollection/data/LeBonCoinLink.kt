package com.example.macollection.data

import java.net.URLEncoder

/**
 * LeBonCoin n'a pas d'API publique officielle, et son API interne est protégée par Datadome :
 * une requête directe se heurte à un mur CAPTCHA dès le premier appel (vérifié en conditions
 * réelles), infranchissable sans navigateur complet. Impossible donc d'obtenir des annonces
 * structurées cliquables une par une comme pour eBay (voir [EbayPrices.offersFor]) — on se
 * contente d'un lien vers la page de résultats de recherche du site, filtrée sur l'objet, ouverte
 * dans le navigateur externe (même principe que [VintedLink] et que
 * [EbayPrices.findWorkingEbayQuery] pour les Souhaits).
 */
object LeBonCoinLink {
    fun searchUrl(brand: String, name: String, platform: String?): String {
        val brandParts = brand.split("/", ",").map { it.trim() }.filter { it.isNotBlank() }
        val base = if (brandParts.isEmpty() || brandParts.any { name.lowercase().contains(it.lowercase()) }) {
            name
        } else {
            "$brand $name"
        }
        val platformSuffix = if (!platform.isNullOrBlank() && !base.lowercase().contains(platform.lowercase())) " $platform" else ""
        val query = "$base$platformSuffix".trim()
        val encoded = URLEncoder.encode(query, "UTF-8")
        return "https://www.leboncoin.fr/recherche?text=$encoded"
    }
}
