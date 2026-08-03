package com.example.macollection.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macollection.BuildConfig
import com.example.macollection.data.AppPrefs
import com.example.macollection.data.GameShopCatalog
import com.example.macollection.data.ShopItem
import com.example.macollection.data.ShopItemCategory
import com.example.macollection.ui.GameViewModel
import com.example.macollection.ui.GamerScreenBackground
import com.example.macollection.ui.theme.CardGradient
import com.example.macollection.ui.theme.NeonCyan
import com.example.macollection.ui.theme.NeonPurple

/**
 * Boutique : thèmes/skins, export Excel, packs de sauvegardes supplémentaires, pubs réduites,
 * et statut Premium (accès total) — payable en points OU en argent réel via Google Play
 * (placeholder "Bientôt disponible" tant que le produit n'est pas configuré côté Play Console).
 */
@Composable
fun ShopScreen(vm: GameViewModel, onBack: () -> Unit, onOpenPaywall: () -> Unit, modifier: Modifier = Modifier) {
    val points by vm.points.collectAsState()
    val unlockedItemIds by vm.unlockedItemIds.collectAsState()
    val isPremium by vm.isPremiumAllAccess.collectAsState()
    val extraBackupPacks by AppPrefs.extraBackupPacks
    var showTestBlocked by remember { mutableStateOf(false) }
    if (showTestBlocked) TestBlockedDialog { showTestBlocked = false }

    val cosmeticAndFeatureItems = GameShopCatalog.shopItems.filter {
        it.id != GameShopCatalog.EXTRA_BACKUPS_5 && it.id != GameShopCatalog.PREMIUM_ALL_ACCESS
    }
    val premiumItem = GameShopCatalog.find(GameShopCatalog.PREMIUM_ALL_ACCESS)
    val extraBackupItem = GameShopCatalog.find(GameShopCatalog.EXTRA_BACKUPS_5)

    GamerScreenBackground {
        Column(
            modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                }
                Text("Boutique", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text("$points points disponibles", color = NeonCyan, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            cosmeticAndFeatureItems.forEach { item ->
                val owned = unlockedItemIds.contains(item.id) || isPremium
                ShopRow(
                    item = item,
                    points = points,
                    owned = owned,
                    blockedInTest = BuildConfig.IS_TEST && item.category == ShopItemCategory.SKIN,
                    onBlockedClick = { showTestBlocked = true },
                    onBuy = { vm.tryUnlock(item.id) }
                )
                Spacer(Modifier.height(10.dp))
            }

            if (extraBackupItem != null) {
                val totalBackups = GameShopCatalog.FREE_BACKUP_EXPORTS + extraBackupPacks * GameShopCatalog.BACKUP_PACK_SIZE
                ShopRow(
                    item = extraBackupItem,
                    points = points,
                    owned = false,
                    ownedLabel = null,
                    subtitle = "Quota actuel : $totalBackups exports au total (rachetable plusieurs fois).",
                    onBuy = { vm.buyExtraBackupPack() }
                )
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(8.dp))
            Text("Statut Premium (accès total)", color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            if (premiumItem != null) {
                ShopRow(
                    item = premiumItem,
                    points = points,
                    owned = isPremium,
                    blockedInTest = BuildConfig.IS_TEST,
                    onBlockedClick = { showTestBlocked = true },
                    onBuy = { vm.tryUnlock(premiumItem.id) }
                )
                Spacer(Modifier.height(10.dp))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.fillMaxWidth().background(CardGradient).padding(16.dp)) {
                    Text("Premium via Google Play (argent réel)", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Débloque tout instantanément sans dépenser de points : abonnement mensuel, annuel, ou pass à vie.",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    if (isPremium) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = NeonCyan)
                            Spacer(Modifier.width(6.dp))
                            Text("Déjà débloqué", color = NeonCyan)
                        }
                    } else {
                        Button(
                            onClick = onOpenPaywall,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                        ) { Text("Voir les offres Premium") }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ShopRow(
    item: ShopItem,
    points: Int,
    owned: Boolean,
    ownedLabel: String? = "Débloqué",
    subtitle: String? = null,
    blockedInTest: Boolean = false,
    onBlockedClick: () -> Unit = {},
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.fillMaxWidth().background(CardGradient).padding(16.dp)) {
            Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))
            Text(item.description, color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
            subtitle?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
            Spacer(Modifier.height(10.dp))
            if (owned && ownedLabel != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = NeonCyan)
                    Spacer(Modifier.width(6.dp))
                    Text(ownedLabel, color = NeonCyan)
                }
            } else if (blockedInTest) {
                OutlinedButton(onClick = onBlockedClick) {
                    Text("${item.cost} points 🔒")
                }
            } else {
                OutlinedButton(onClick = onBuy, enabled = points >= item.cost) {
                    Text("${item.cost} points")
                }
            }
        }
    }
}
