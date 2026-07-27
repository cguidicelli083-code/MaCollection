package com.example.macollection.ui.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import com.example.macollection.BuildConfig
import com.example.macollection.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Unités publicitaires : IDs réels du compte AdMob personnel (créé 2026-07-12), utilisés dans
 * l'édition normale (« full »). L'édition TEST garde volontairement les IDs de TEST officiels
 * Google : le développeur charge/affiche des pubs très fréquemment dessus pendant ses propres
 * essais sur l'appareil, ce qui sur de VRAIES unités serait vu par Google comme du trafic
 * publicitaire invalide (clics/impressions de son propre compte) — risque de suspension du
 * compte AdMob. L'édition « noads » (V2SP) n'affiche jamais de pub (BuildConfig.ADS_ENABLED),
 * ces IDs n'y sont donc jamais utilisés.
 */
private object AdUnits {
    private const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"

    private const val REAL_INTERSTITIAL = "ca-app-pub-6018236504099673/3026438372"
    private const val REAL_REWARDED = "ca-app-pub-6018236504099673/3437501917"

    val INTERSTITIAL: String get() = if (BuildConfig.IS_TEST) TEST_INTERSTITIAL else REAL_INTERSTITIAL
    val REWARDED: String get() = if (BuildConfig.IS_TEST) TEST_REWARDED else REAL_REWARDED
}

private fun Context.findActivity(): Activity? {
    var ctx: Context = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

/**
 * Lance une pub récompensée pour gagner des points, aux points de friction où l'utilisateur est
 * déjà motivé (jeu d'arcade verrouillé, plafond de sauvegarde atteint...). [onEarned] n'est
 * appelé que sur confirmation réelle de visionnage complet (voir [showRewardedAd]) — jamais de
 * points crédités sur un simple clic. Ne fait rien dans l'édition sans pub (V2SP).
 */
fun watchRewardedAdForPoints(context: Context, onEarned: () -> Unit, onClosed: () -> Unit = {}) =
    watchRewardedAd(context, onRewarded = onEarned, onClosed = onClosed)

/**
 * Lance une pub récompensée générique (voir [watchRewardedAdForPoints] pour la variante à points).
 * Utilisée aussi pour la « continuation » de partie en fin de jeu (voir [GameContinue]). [onRewarded]
 * n'est appelé que sur confirmation réelle de visionnage complet ; [onClosed] est systématiquement
 * appelé ensuite (ou immédiatement si aucune pub n'a pu être proposée).
 */
fun watchRewardedAd(context: Context, onRewarded: () -> Unit, onClosed: () -> Unit = {}) {
    if (!BuildConfig.ADS_ENABLED) {
        onClosed()
        return
    }
    val activity = context.findActivity()
    if (activity == null) {
        onClosed()
        return
    }
    showRewardedAd(activity, onReward = onRewarded, onClosed = onClosed)
}

/**
 * Charge et affiche un interstitiel (fin de partie / entre deux manches). [onClosed] est
 * systématiquement appelé (pub fermée, échec de chargement/affichage) pour ne jamais bloquer
 * la suite du parcours de jeu.
 */
fun showInterstitial(activity: Activity, onClosed: () -> Unit) {
    InterstitialAd.load(
        activity,
        AdUnits.INTERSTITIAL,
        AdRequest.Builder().build(),
        object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() = onClosed()
                    override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) = onClosed()
                }
                ad.show(activity)
            }
            override fun onAdFailedToLoad(error: LoadAdError) = onClosed()
        }
    )
}

/**
 * Charge une pub récompensée. [onReward] n'est appelé qu'au callback réel de l'API confirmant
 * un visionnage complet — jamais de points crédités avant cette confirmation. [onClosed] est
 * toujours appelé ensuite (ou immédiatement en cas d'échec de chargement).
 */
fun showRewardedAd(activity: Activity, onReward: () -> Unit, onClosed: () -> Unit = {}) {
    fun notifyUnavailable() {
        Toast.makeText(activity, activity.getString(R.string.ad_unavailable), Toast.LENGTH_LONG).show()
    }
    RewardedAd.load(
        activity,
        AdUnits.REWARDED,
        AdRequest.Builder().build(),
        object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() = onClosed()
                    override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                        notifyUnavailable()
                        onClosed()
                    }
                }
                ad.show(activity) { onReward() }
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                notifyUnavailable()
                onClosed()
            }
        }
    )
}

/**
 * Interstitiel affiché à chaque ouverture de l'onglet Total (écran à forte visibilité). Ne fait
 * rien si [adsEnabled] est faux (Premium / pubs réduites) ou si aucune Activity n'est trouvée.
 * Best-effort : un échec de chargement est ignoré (l'utilisateur voit simplement son total, sans pub).
 */
fun showTotalScreenAd(context: Context, adsEnabled: Boolean) {
    if (!BuildConfig.ADS_ENABLED) return
    if (!adsEnabled) return
    val activity = context.findActivity() ?: return
    showInterstitial(activity) { /* simple consultation du total : rien à reprendre */ }
}

/**
 * Cadence des interstitiels : un compteur PARTAGÉ par tous les mini-jeux, incrémenté à chaque niveau
 * franchi. La pub n'est JAMAIS affichée en pleine partie — uniquement en fin de partie (game over),
 * où le jeu est déjà arrêté (rien à mettre en pause). Elle n'apparaît que si assez de niveaux ont
 * été franchis depuis la dernière (environ 3, aléatoirement 3 ou 4, pour rester peu intrusive).
 */
object GameAds {
    private const val MIN_LEVELS_BETWEEN_ADS = 5
    private var levelsSinceLastAd = 0
    private var nextThreshold = pickThreshold()

    private fun pickThreshold(): Int =
        MIN_LEVELS_BETWEEN_ADS + (0..1).random() // 5 ou 6 niveaux

    /**
     * À appeler à chaque niveau franchi (tous jeux confondus). N'affiche RIEN : incrémente juste le
     * compteur. L'éventuelle pub sera montrée plus tard, à la fin de la partie ([onGameOver]).
     */
    fun onLevelCleared() {
        levelsSinceLastAd++
    }

    /**
     * À appeler en fin de partie (game over). Affiche un interstitiel si assez de niveaux ont été
     * franchis depuis la dernière pub. Ne fait rien si [adsEnabled] est faux (Premium / pubs
     * réduites) ou si aucune Activity n'est trouvée. Best-effort : un échec de chargement est ignoré.
     */
    fun onGameOver(context: Context, adsEnabled: Boolean) {
        if (!BuildConfig.ADS_ENABLED) return // édition « V2SP » sans pub (R8 supprime le reste)
        if (!adsEnabled) return
        if (levelsSinceLastAd < nextThreshold) return
        levelsSinceLastAd = 0
        nextThreshold = pickThreshold()
        val activity = context.findActivity() ?: return
        showInterstitial(activity) { /* fin de partie : rien à reprendre */ }
    }
}
