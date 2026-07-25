package com.example.macollection.data

/** Résultat d'une recherche de cote + informations en ligne. */
data class PriceInfoResult(
    val priceCents: Int?,
    val info: String?
)

/**
 * Source des cotes et des informations (PriceCharting pour le prix,
 * Wikipédia ou un site spécialisé pour les infos).
 *
 * Pour l'instant c'est un « bouchon » : on branchera la vraie source plus tard,
 * sans rien changer au reste de l'application.
 */
interface PriceInfoService {
    suspend fun lookup(
        type: ItemType,
        brand: String,
        name: String,
        region: Region,
        hasBox: Boolean,
        hasManual: Boolean
    ): PriceInfoResult
}

/** Implémentation temporaire : ne va pas encore chercher sur Internet. */
class StubPriceInfoService : PriceInfoService {
    override suspend fun lookup(
        type: ItemType,
        brand: String,
        name: String,
        region: Region,
        hasBox: Boolean,
        hasManual: Boolean
    ): PriceInfoResult {
        // TODO: brancher PriceCharting (cote selon boîte/notice/état)
        //       et Wikipédia / site spécialisé (infos : date de sortie, genre...).
        return PriceInfoResult(priceCents = null, info = "Cote en ligne à venir")
    }
}
