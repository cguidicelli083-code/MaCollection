package com.example.macollection.data

import java.net.URLEncoder

/**
 * Vinted n'a pas d'API publique officielle. Une première tentative d'intégration via son API
 * interne (`api/v2/catalog/items`, cookie de session anonyme) a été essayée mais s'est révélée
 * non fiable en conditions réelles : elle fonctionne une fois puis échoue de façon constante
 * ensuite ("Jeton d'authentification invalide") — Vinted exige un jeton que son site web obtient
 * via du JavaScript, qu'un simple client HTTP ne peut pas reproduire. Comme pour [LeBonCoinLink],
 * on se contente donc d'un lien vers la page de résultats de recherche du site, filtrée sur
 * l'objet, ouverte dans le navigateur externe.
 */
object VintedLink {
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
        return "https://www.vinted.fr/catalog?search_text=$encoded"
    }
}
