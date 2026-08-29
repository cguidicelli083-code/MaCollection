package com.example.macollection.data

/**
 * Une annonce individuelle, prête à afficher dans la section "Offres" d'une fiche
 * Console/Accessoire/Jeu de l'Encyclopédie. [source] identifie la marketplace d'origine (ex.
 * "eBay") — affiché dans [com.example.macollection.ui.OfferRow] pour que l'utilisateur sache où
 * cliquer l'emmène. Extrait de [EbayPrices] (ex-`EbayPrices.Offer`) en type top-level pour rester
 * prêt si une autre marketplace avec une vraie API structurée s'ajoute un jour — LeBonCoin et
 * Vinted, essayés, n'en ont pas (voir [LeBonCoinLink]/[VintedLink]) et se contentent d'un lien de
 * recherche externe, pas de ce type.
 */
data class Offer(
    val source: String,
    val title: String,
    val priceCents: Int?,
    val condition: String?,
    val imageUrl: String?,
    val url: String?
)
