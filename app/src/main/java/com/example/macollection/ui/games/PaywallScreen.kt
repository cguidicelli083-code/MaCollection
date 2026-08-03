package com.example.macollection.ui.games

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.billingclient.api.ProductDetails
import com.example.macollection.BuildConfig
import com.example.macollection.data.AppPrefs
import com.example.macollection.ui.FREE_COLLECTION_LIMIT
import com.example.macollection.ui.GameViewModel
import com.example.macollection.ui.GamerScreenBackground
import com.example.macollection.ui.ads.watchRewardedAd
import com.example.macollection.ui.billing.BillingManager
import com.example.macollection.ui.theme.CardGradient
import com.example.macollection.ui.theme.NeonCyan
import com.example.macollection.ui.theme.NeonPurple
import java.text.NumberFormat
import java.util.Currency
import kotlin.math.roundToInt

/**
 * Paywall Premium : 2 abonnements (mensuel, annuel) + 1 achat unique à vie. Les prix affichés
 * viennent TOUJOURS de [GameViewModel.premiumProductDetails] (Google Play), jamais en dur ici —
 * ce qui permet une offre de lancement à durée limitée sur l'abonnement annuel (configurée côté
 * Play Console) de s'afficher automatiquement sans toucher au code.
 */
@Composable
fun PaywallScreen(vm: GameViewModel, onClose: () -> Unit, modifier: Modifier = Modifier) {
    val isPremium by vm.isPremiumAllAccess.collectAsState()
    val hasLifetime by vm.hasLifetimePremium.collectAsState()
    val activeSubId by vm.activePremiumSubscriptionId.collectAsState()
    val willNotRenew by vm.premiumSubscriptionWillNotRenew.collectAsState()
    val availableIds by vm.availablePremiumProductIds.collectAsState()
    val productDetails by vm.premiumProductDetails.collectAsState()
    val context = LocalContext.current
    var restoring by remember { mutableStateOf(false) }
    var restoreMessage by remember { mutableStateOf<String?>(null) }
    var showTestBlocked by remember { mutableStateOf(false) }
    if (showTestBlocked) TestBlockedDialog { showTestBlocked = false }

    fun buy(productId: String) {
        if (BuildConfig.IS_TEST) { showTestBlocked = true; return }
        (context as? Activity)?.let { vm.launchRealPremiumPurchase(it, productId) }
    }

    GamerScreenBackground {
        Column(
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Fermer", tint = Color.White)
                }
            }
            Text("✨ Passe à l'illimité", color = Color.White, fontWeight = FontWeight.Black, fontSize = 26.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                "Profite de la collection sans aucune limite et soutiens le développement de l'appli.",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 14.sp
            )
            Spacer(Modifier.height(20.dp))

            BenefitRow("📦", "Stockage illimité (consoles, jeux, accessoires)")
            BenefitRow("🎮", "Accès complet à tous les mini-jeux et à tous les thèmes")
            BenefitRow("💹", "Cotes et informations à jour en temps réel, sans restriction")

            Spacer(Modifier.height(20.dp))

            if (isPremium) {
                PremiumActiveCard(hasLifetime = hasLifetime, activeSubId = activeSubId, willNotRenew = willNotRenew)
            } else {
                if (BuildConfig.ADS_ENABLED) {
                    RewardedAdBonusRow()
                    Spacer(Modifier.height(16.dp))
                }

                val monthlyDetails = productDetails[BillingManager.SUB_MONTHLY]
                val yearlyDetails = productDetails[BillingManager.SUB_YEARLY]
                val lifetimeDetails = productDetails[BillingManager.LIFETIME]

                PremiumOfferCard(
                    title = "Mensuel",
                    priceLabel = offerPriceLabel(monthlyDetails.bestSubscriptionOffer()),
                    available = BillingManager.SUB_MONTHLY in availableIds,
                    onBuy = { buy(BillingManager.SUB_MONTHLY) }
                )
                Spacer(Modifier.height(12.dp))
                PremiumOfferCard(
                    title = "Annuel",
                    priceLabel = offerPriceLabel(yearlyDetails.bestSubscriptionOffer()),
                    subLabel = yearlyMonthlyEquivalentLabel(yearlyDetails)?.let { "soit $it" },
                    badge = "🔥 Populaire" + (yearlySavingsPercent(monthlyDetails, yearlyDetails)?.let { "  •  -$it%" } ?: ""),
                    highlighted = true,
                    available = BillingManager.SUB_YEARLY in availableIds,
                    onBuy = { buy(BillingManager.SUB_YEARLY) }
                )
                Spacer(Modifier.height(12.dp))
                PremiumOfferCard(
                    title = "Pass à Vie",
                    priceLabel = lifetimeDetails?.oneTimePurchaseOfferDetails?.formattedPrice ?: "…",
                    subLabel = "Un seul paiement, pas d'abonnement !",
                    available = BillingManager.LIFETIME in availableIds,
                    onBuy = { buy(BillingManager.LIFETIME) }
                )
            }

            Spacer(Modifier.height(20.dp))
            OutlinedButton(
                onClick = {
                    restoring = true
                    restoreMessage = null
                    vm.restorePremiumPurchases { found ->
                        restoring = false
                        restoreMessage = if (found) "✅ Achat retrouvé et restauré." else "Aucun achat Premium trouvé pour ce compte Google."
                    }
                },
                enabled = !restoring,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (restoring) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text("Restaurer mes achats")
            }
            restoreMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

/**
 * Alternative gratuite au Premium pour repousser le quota de la collection : +5 objets par pub
 * récompensée visionnée EN ENTIER, cumulable sans limite (contrairement aux packs de sauvegardes
 * de la Boutique à points). [AppPrefs.addExtraCollectionSlots] n'est appelé que sur confirmation
 * réelle de visionnage complet (voir [watchRewardedAd]), jamais sur un simple clic.
 */
@Composable
private fun RewardedAdBonusRow() {
    val context = LocalContext.current
    val extraSlots by AppPrefs.extraCollectionSlots
    var watchingAd by remember { mutableStateOf(false) }
    var earnedMessage by remember { mutableStateOf<String?>(null) }

    Column {
        Text(
            "Quota actuel : ${FREE_COLLECTION_LIMIT + extraSlots} objets",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
        Spacer(Modifier.height(6.dp))
        OutlinedButton(
            onClick = {
                watchingAd = true
                earnedMessage = null
                watchRewardedAd(
                    context = context,
                    onRewarded = {
                        AppPrefs.addExtraCollectionSlots(context)
                        earnedMessage = "🎉 Félicitations ! Tu as débloqué 5 objets supplémentaires."
                    },
                    onClosed = { watchingAd = false }
                )
            },
            enabled = !watchingAd,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (watchingAd) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text("🎬 Regarder une pub (+5 objets gratuits)")
        }
        earnedMessage?.let {
            Spacer(Modifier.height(6.dp))
            Text(it, color = NeonCyan, fontSize = 13.sp)
        }
    }
}

@Composable
private fun BenefitRow(emoji: String, text: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 18.sp)
        Spacer(Modifier.width(10.dp))
        Text(text, color = Color.White, fontSize = 14.sp)
    }
}

@Composable
private fun PremiumActiveCard(hasLifetime: Boolean, activeSubId: String?, willNotRenew: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.fillMaxWidth().background(CardGradient).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = NeonCyan)
                Spacer(Modifier.width(8.dp))
                Text(
                    when {
                        hasLifetime -> "Pass à Vie actif — merci pour ton soutien !"
                        activeSubId == BillingManager.SUB_MONTHLY -> "Abonnement mensuel actif"
                        activeSubId == BillingManager.SUB_YEARLY -> "Abonnement annuel actif"
                        else -> "Premium actif"
                    },
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
            }
            if (!hasLifetime && willNotRenew) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "⚠️ Ne se renouvellera pas automatiquement à la fin de la période en cours.",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun PremiumOfferCard(
    title: String,
    priceLabel: String,
    subLabel: String? = null,
    badge: String? = null,
    highlighted: Boolean = false,
    available: Boolean,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(CardGradient)
                .then(
                    if (highlighted) Modifier.padding(1.dp).background(NeonPurple.copy(alpha = 0.12f))
                    else Modifier
                )
                .padding(16.dp)
        ) {
            if (badge != null) {
                Box(
                    Modifier
                        .background(NeonPurple, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(badge, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
            }
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Spacer(Modifier.height(4.dp))
            Text(priceLabel, color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            subLabel?.let {
                Spacer(Modifier.height(2.dp))
                Text(it, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
            Spacer(Modifier.height(12.dp))
            if (available) {
                Button(
                    onClick = onBuy,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Choisir cette offre") }
            } else {
                OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                    Text(if (BuildConfig.IS_TEST) "Indisponible (version test)" else "Bientôt disponible")
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Lecture des prix réels renvoyés par Google Play (jamais de prix en dur).
// ---------------------------------------------------------------------------

/**
 * Offre éligible la moins chère en première phase : sélectionne automatiquement une promo de
 * lancement (prix réduit à durée limitée, configurée côté Play Console) si l'utilisateur y est
 * actuellement éligible, sinon l'offre de base — sans aucune logique de dates côté appli.
 */
private fun ProductDetails?.bestSubscriptionOffer(): ProductDetails.SubscriptionOfferDetails? =
    this?.subscriptionOfferDetails
        ?.minByOrNull { it.pricingPhases.pricingPhaseList.firstOrNull()?.priceAmountMicros ?: Long.MAX_VALUE }

private fun periodSuffix(isoPeriod: String): String = when (isoPeriod) {
    "P1W" -> "/semaine"
    "P1M" -> "/mois"
    "P3M" -> "/trimestre"
    "P1Y" -> "/an"
    else -> ""
}

/** Concatène les phases de prix d'une offre (ex. "9,99 €  puis  14,99 €/an" pour une promo). */
private fun offerPriceLabel(offer: ProductDetails.SubscriptionOfferDetails?): String {
    val phases = offer?.pricingPhases?.pricingPhaseList
    if (phases.isNullOrEmpty()) return "…"
    return phases.joinToString("  puis  ") { "${it.formattedPrice}${periodSuffix(it.billingPeriod)}" }
}

/** Dernière phase (prix "de croisière", après une éventuelle promo initiale) d'une offre. */
private fun ProductDetails?.steadyStateMicros(): Long? =
    this.bestSubscriptionOffer()?.pricingPhases?.pricingPhaseList?.lastOrNull()?.priceAmountMicros

/** Pourcentage économisé par l'annuel vs 12x le mensuel (prix de croisière des deux), ou null. */
private fun yearlySavingsPercent(monthly: ProductDetails?, yearly: ProductDetails?): Int? {
    val monthlyMicros = monthly.steadyStateMicros() ?: return null
    val yearlyMicros = yearly.steadyStateMicros() ?: return null
    if (monthlyMicros <= 0) return null
    val percent = ((1.0 - yearlyMicros.toDouble() / (monthlyMicros * 12)) * 100).roundToInt()
    return percent.takeIf { it > 0 }
}

/** Équivalent mensuel du prix annuel de croisière (ex. "1,25 €/mois"), ou null si indisponible. */
private fun yearlyMonthlyEquivalentLabel(yearly: ProductDetails?): String? {
    val phase = yearly.bestSubscriptionOffer()?.pricingPhases?.pricingPhaseList?.lastOrNull() ?: return null
    return runCatching {
        val format = NumberFormat.getCurrencyInstance()
        format.currency = Currency.getInstance(phase.priceCurrencyCode)
        "${format.format(phase.priceAmountMicros / 12 / 1_000_000.0)}/mois"
    }.getOrNull()
}
