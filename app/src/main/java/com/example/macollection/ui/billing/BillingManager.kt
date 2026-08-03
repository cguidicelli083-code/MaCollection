package com.example.macollection.ui.billing

import android.app.Activity
import android.content.Context
import android.util.Log
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

private const val TAG = "MaCollectionBilling"

/**
 * Google Play Billing pour le statut Premium (argent réel) : 2 abonnements (mensuel, annuel) +
 * 1 achat unique à vie. Un seul de ces 3 produits suffit à rendre [premiumPurchased] vrai — voir
 * [combinedPremiumFlow][com.example.macollection.data.combinedPremiumFlow] pour comment ça se
 * combine avec le Premium acheté en points.
 *
 * Singleton (voir [get]) : un seul [BillingClient] pour toute l'appli, partagé entre
 * [com.example.macollection.ui.GameViewModel] (onglet Jeux/Boutique) et
 * [com.example.macollection.ui.AppViewModel] (quota de la collection gratuite) — deux connexions
 * séparées au même service Play Store gaspilleraient des ressources et risqueraient un statut
 * temporairement incohérent entre les deux écrans.
 *
 * Reste sans effet tant que les produits ne sont pas configurés côté Play Console (Monétiser >
 * Produits) : [availableProductIds] ne contient alors aucun des 3 ids, et les cartes d'offre du
 * Paywall affichent "Bientôt disponible". Jamais de faux succès simulé : [premiumPurchased] ne
 * passe à true que sur confirmation réelle de Google Play (onPurchasesUpdated, ou achat déjà
 * possédé retrouvé au démarrage via [queryPurchasesAsync]).
 */
class BillingManager private constructor(context: Context) : PurchasesUpdatedListener {

    companion object {
        const val SUB_MONTHLY = "sub_monthly"
        const val SUB_YEARLY = "sub_yearly"
        const val LIFETIME = "inapp_lifetime"
        val SUBSCRIPTION_IDS = setOf(SUB_MONTHLY, SUB_YEARLY)

        @Volatile private var instance: BillingManager? = null

        fun get(context: Context): BillingManager = instance ?: synchronized(this) {
            instance ?: BillingManager(context.applicationContext).also { instance = it }
        }
    }

    private val appContext = context.applicationContext

    private val _premiumPurchased = MutableStateFlow(false)
    /** true si un abonnement actif (mensuel/annuel) OU l'achat à vie est possédé. */
    val premiumPurchased: StateFlow<Boolean> = _premiumPurchased

    private val _activeSubscriptionId = MutableStateFlow<String?>(null)
    /** [SUB_MONTHLY] ou [SUB_YEARLY] si un abonnement est actif, sinon null (voir [hasLifetime]). */
    val activeSubscriptionId: StateFlow<String?> = _activeSubscriptionId

    private val _hasLifetime = MutableStateFlow(false)
    val hasLifetime: StateFlow<Boolean> = _hasLifetime

    /** true dès qu'un abonnement actif existe mais n'est PAS en renouvellement auto (annulé mais
     *  encore valide jusqu'à la fin de la période déjà payée) — pour prévenir l'utilisateur dans
     *  le Paywall plutôt que de le laisser croire que son abonnement continuera indéfiniment. */
    private val _subscriptionWillNotRenew = MutableStateFlow(false)
    val subscriptionWillNotRenew: StateFlow<Boolean> = _subscriptionWillNotRenew

    /** Ids parmi [SUB_MONTHLY]/[SUB_YEARLY]/[LIFETIME] réellement configurés côté Play Console. */
    private val _availableProductIds = MutableStateFlow<Set<String>>(emptySet())
    val availableProductIds: StateFlow<Set<String>> = _availableProductIds

    private val _productDetails = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    /** Détails (dont le prix localisé réel) des produits trouvés côté Play Console, par id. */
    val productDetails: StateFlow<Map<String, ProductDetails>> = _productDetails

    private val client = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    init {
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Connexion Billing établie, vérification des achats existants et du catalogue.")
                    refreshPurchases()
                    queryProducts()
                } else {
                    Log.w(TAG, "Échec de connexion Billing : ${result.responseCode} ${result.debugMessage}")
                }
            }
            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Service Billing déconnecté (pas de retry agressif, revérifié au prochain lancement).")
            }
        })
    }

    private fun queryProducts() {
        // Deux appels séparés (SUBS puis INAPP) : les deux types de produits ne se mélangent pas
        // forcément bien dans une seule requête selon les versions de la librairie, et ça reste
        // lisible de garder un appel par type.
        val subsProducts = SUBSCRIPTION_IDS.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        client.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(subsProducts).build()
        ) { result, response ->
            Log.d(TAG, "Catalogue abonnements : ${result.responseCode}, ${response.productDetailsList.size} trouvé(s)")
            mergeProductDetails(response.productDetailsList)
        }

        val lifetimeProduct = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(LIFETIME)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        client.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(listOf(lifetimeProduct)).build()
        ) { result, response ->
            Log.d(TAG, "Catalogue achat à vie : ${result.responseCode}, ${response.productDetailsList.size} trouvé(s)")
            mergeProductDetails(response.productDetailsList)
        }
    }

    private fun mergeProductDetails(found: List<ProductDetails>) {
        if (found.isEmpty()) return
        _productDetails.value = _productDetails.value + found.associateBy { it.productId }
        _availableProductIds.value = _availableProductIds.value + found.map { it.productId }
    }

    /** Revérifie les achats/abonnements réels auprès de Google Play (démarrage, ou bouton "Restaurer mes achats"). */
    fun refreshPurchases(onDone: (foundAny: Boolean) -> Unit = {}) {
        var subsPurchases: List<Purchase>? = null
        var inappPurchases: List<Purchase>? = null

        fun tryFinish() {
            val subs = subsPurchases ?: return
            val inapp = inappPurchases ?: return
            val all = subs + inapp
            Log.d(TAG, "Achats retrouvés : ${all.size} (${subs.size} abonnement(s), ${inapp.size} achat(s) unique(s))")
            handlePurchases(all)
            onDone(all.isNotEmpty())
        }

        client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { _, purchases -> subsPurchases = purchases; tryFinish() }

        client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        ) { _, purchases -> inappPurchases = purchases; tryFinish() }
    }

    /**
     * Lance le flux d'achat réel pour [productId] ([SUB_MONTHLY], [SUB_YEARLY] ou [LIFETIME]).
     * Ne fait rien si le produit n'est pas disponible côté Play Console (voir [availableProductIds]).
     * Pour un abonnement avec plusieurs offres éligibles (ex. prix de lancement à durée limitée
     * puis tarif normal), Google Play ne renvoie QUE les offres pour lesquelles l'utilisateur est
     * réellement éligible : on prend celle avec la première phase de prix la plus basse, ce qui
     * choisit automatiquement une promo active sans aucune logique de dates côté appli.
     */
    fun launchPurchase(activity: Activity, productId: String) {
        val details = _productDetails.value[productId] ?: run {
            Log.w(TAG, "launchPurchase($productId) : produit non disponible côté Play Console.")
            return
        }
        val offerToken = if (productId in SUBSCRIPTION_IDS) {
            details.subscriptionOfferDetails
                ?.minByOrNull { it.pricingPhases.pricingPhaseList.firstOrNull()?.priceAmountMicros ?: Long.MAX_VALUE }
                ?.offerToken
        } else {
            details.oneTimePurchaseOfferDetails?.offerToken
        }
        if (offerToken == null) {
            Log.w(TAG, "launchPurchase($productId) : aucun offerToken disponible.")
            return
        }
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offerToken)
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()
        val result = client.launchBillingFlow(activity, flowParams)
        Log.d(TAG, "launchBillingFlow($productId) -> ${result.responseCode}")
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        Log.d(TAG, "onPurchasesUpdated : ${result.responseCode}, ${purchases?.size ?: 0} achat(s)")
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases?.let { handlePurchases(it) }
            BillingClient.BillingResponseCode.USER_CANCELED ->
                Log.d(TAG, "Achat annulé par l'utilisateur.")
            else ->
                Log.w(TAG, "Achat en échec : ${result.responseCode} ${result.debugMessage}")
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        val active = purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }

        val activeSub = active.firstOrNull { p -> p.products.any { it in SUBSCRIPTION_IDS } }
        _activeSubscriptionId.value = activeSub?.products?.firstOrNull { it in SUBSCRIPTION_IDS }
        _subscriptionWillNotRenew.value = activeSub?.isAutoRenewing == false
        _hasLifetime.value = active.any { it.products.contains(LIFETIME) }
        _premiumPurchased.value = activeSub != null || _hasLifetime.value

        // Un achat non reconnu (acknowledged) sous 3 jours est automatiquement remboursé par
        // Google : indispensable de le faire pour CHAQUE achat (abonnement ET à vie), pas
        // seulement celui qui vient de mettre premiumPurchased à true.
        active.filter { !it.isAcknowledged }.forEach { purchase ->
            val ackParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            client.acknowledgePurchase(ackParams) { ackResult ->
                Log.d(TAG, "acknowledgePurchase(${purchase.products}) -> ${ackResult.responseCode}")
            }
        }
    }
}
