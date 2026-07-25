package com.example.macollection.ui.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Google Play Billing pour le statut Premium payant (argent réel). Le code ci-dessous est
 * fonctionnel de bout en bout, mais reste sans effet tant que le produit in-app [PRODUCT_ID]
 * n'existe pas côté Play Console (Monétisation > Produits > Produits gérés) sur l'app publiée
 * sous l'applicationId `com.nawash.macollection`. Tant que ce produit n'est pas actif côté Play
 * Console, [productAvailable] reste à false et le bouton "Passer Premium" affiche "Bientôt
 * disponible" côté UI. Jamais de faux succès simulé : [premiumPurchased] ne passe à true que sur
 * confirmation réelle de Google Play (onPurchasesUpdated / purchase déjà possédé au démarrage).
 */
class BillingManager(context: Context) : PurchasesUpdatedListener {

    companion object {
        /** ID placeholder du produit in-app "accès total" — à créer côté Play Console plus tard. */
        const val PRODUCT_ID = "premium_all_access_iap"
    }

    private val appContext = context.applicationContext

    private val _premiumPurchased = MutableStateFlow(false)
    val premiumPurchased: StateFlow<Boolean> = _premiumPurchased

    /** true seulement si [PRODUCT_ID] est réellement configuré côté Play Console. */
    private val _productAvailable = MutableStateFlow(false)
    val productAvailable: StateFlow<Boolean> = _productAvailable

    private var productDetails: ProductDetails? = null

    private val client = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    init {
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    refreshPurchases()
                    queryProduct()
                }
            }
            override fun onBillingServiceDisconnected() {
                // Pas de retry agressif : ce chemin reste secondaire tant que le statut Premium
                // via points (GameViewModel) est le chemin fonctionnel principal.
            }
        })
    }

    private fun queryProduct() {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PRODUCT_ID)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val params = QueryProductDetailsParams.newBuilder().setProductList(listOf(product)).build()
        client.queryProductDetailsAsync(params) { _, result ->
            val details = result.productDetailsList.firstOrNull()
            productDetails = details
            _productAvailable.value = details != null
        }
    }

    private fun refreshPurchases() {
        val params = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        client.queryPurchasesAsync(params) { _, purchases -> handlePurchases(purchases) }
    }

    /** Lance le flux d'achat réel. Ne fait rien si le produit n'est pas disponible (voir [productAvailable]). */
    fun launchPurchase(activity: Activity) {
        val details = productDetails ?: return
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()
        client.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases)
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        val owned = purchases.any {
            it.products.contains(PRODUCT_ID) && it.purchaseState == Purchase.PurchaseState.PURCHASED
        }
        if (owned) _premiumPurchased.value = true
        purchases.filter {
            it.products.contains(PRODUCT_ID) && it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged
        }.forEach { purchase ->
            val ackParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            client.acknowledgePurchase(ackParams) {}
        }
    }
}
