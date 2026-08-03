package com.example.macollection.data

import com.example.macollection.BuildConfig
import com.example.macollection.ui.billing.BillingManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Ids des items débloqués (jeux d'arcade, thèmes, fonctionnalités) dont l'empreinte anti-triche
 * est valide (voir [IntegrityGuard]). Extrait ici pour être partagé entre [AppViewModel]
 * (quota Premium de la collection) et [GameViewModel] (Boutique/Hub des jeux), qui ont chacun
 * accès à [UnlockedItemDao] mais ne doivent JAMAIS recalculer cette vérification différemment.
 */
fun verifiedUnlockedItemIds(dao: UnlockedItemDao): Flow<Set<String>> =
    dao.observeAll().map { list ->
        list.filter { IntegrityGuard.signUnlock(it.itemId, it.unlockedAt) == it.checksum }
            .map { it.itemId }.toSet()
    }

/**
 * Statut Premium unifié (accès total) de l'appli — SEULE ET UNIQUE définition, à ne jamais
 * dupliquer ailleurs pour éviter que la Collection et l'onglet Jeux se désynchronisent. Premium si :
 *  - la variante a tous les verrous levés d'origine (édition "noads"/V2SP, usage perso) ; ou
 *  - "Premium (accès total)" a été acheté avec des points dans la Boutique ; ou
 *  - un abonnement (mensuel/annuel) ou l'achat à vie est possédé côté Google Play (voir [BillingManager]).
 * Toujours false en variante TEST (achats bloqués, plafond de test incontournable).
 */
fun combinedPremiumFlow(unlockedItemIds: Flow<Set<String>>, billing: BillingManager): Flow<Boolean> =
    combine(unlockedItemIds, billing.premiumPurchased) { unlocked, purchased ->
        if (BuildConfig.IS_TEST) false
        else BuildConfig.UNLOCK_ALL || unlocked.contains(GameShopCatalog.PREMIUM_ALL_ACCESS) || purchased
    }
