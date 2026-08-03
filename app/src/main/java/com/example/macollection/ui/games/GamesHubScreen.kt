package com.example.macollection.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.macollection.BuildConfig
import com.example.macollection.data.AppPrefs
import com.example.macollection.data.GameShopCatalog
import com.example.macollection.ui.AD_REWARD_POINTS
import com.example.macollection.ui.GameViewModel
import com.example.macollection.ui.theme.AppTheme
import com.example.macollection.ui.GamerScreenBackground
import com.example.macollection.ui.ads.watchRewardedAdForPoints
import com.example.macollection.ui.theme.CardGradient
import com.example.macollection.ui.theme.NeonCyan
import com.example.macollection.ui.theme.NeonPurple

/**
 * Hub des mini-jeux : solde de points, Quiz + Pong (jouables tout de suite), les 4 jeux d'arcade
 * à débloquer avec des points (Arkanoid, Space Invaders, Centipede, Pacman), et l'accès à la
 * Boutique.
 */
/** Jeux d'arcade codés (tous, désormais) ; un id absent afficherait « bientôt disponible ». */
private val implementedArcadeGames = setOf(
    GameShopCatalog.ARKANOID,
    GameShopCatalog.SPACE_INVADERS,
    GameShopCatalog.CENTIPEDE,
    GameShopCatalog.PACMAN,
    GameShopCatalog.TETRIS,
    GameShopCatalog.SNAKE,
    GameShopCatalog.SIMON,
    GameShopCatalog.FROGGER,
    GameShopCatalog.MINER,
    GameShopCatalog.ORCHARD,
    GameShopCatalog.BOMB_HUNTER
)

@Composable
fun GamesHubScreen(
    vm: GameViewModel,
    onOpenQuiz: () -> Unit,
    onOpenPong: () -> Unit,
    onOpenArcade: (String) -> Unit,
    onOpenShop: () -> Unit,
    onOpenPaywall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val points by vm.points.collectAsState()
    val unlockedGames by vm.unlockedGameIds.collectAsState()
    val isPremium by vm.isPremiumAllAccess.collectAsState()
    val context = LocalContext.current
    var comingSoonMessage by remember { mutableStateOf<String?>(null) }
    var showTestBlocked by remember { mutableStateOf(false) }
    if (showTestBlocked) TestBlockedDialog { showTestBlocked = false }

    // Rafraîchit le compte à rebours du Coffre Quotidien toutes les 30s (pas besoin de plus
    // précis) : sans ce tick, le bouton resterait grisé après expiration du cooldown tant que
    // rien d'autre ne force une recomposition de cet écran.
    var chestNowTick by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(30_000)
            chestNowTick = System.currentTimeMillis()
        }
    }
    val chestLastOpened = AppPrefs.dailyChestLastOpened.value
    val chestRemainingMs = (24 * 60 * 60 * 1000L - (chestNowTick - chestLastOpened)).coerceAtLeast(0L)
    val chestAvailable = chestRemainingMs <= 0L
    var chestMessage by remember { mutableStateOf<String?>(null) }

    GamerScreenBackground {
        Column(modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(AppTheme.byId(AppPrefs.selectedTheme.value).emoji, fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(8.dp))
                    Text("$points points", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))

                if (BuildConfig.ADS_ENABLED && !isPremium) {
                    DailyChestCard(
                        available = chestAvailable,
                        remainingMs = chestRemainingMs,
                        onOpen = {
                            watchRewardedAdForPoints(context, onEarned = {
                                val reward = (5..30).random()
                                vm.earnPoints(reward)
                                AppPrefs.recordDailyChestOpened(context)
                                chestMessage = "🎁 Coffre ouvert : +$reward points !"
                            })
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                GameCard(
                    title = "Quiz Encyclopédie",
                    description = "Réponds à des questions sur les consoles de ta collection et gagne des points.",
                    actionLabel = "Jouer",
                    onAction = onOpenQuiz
                )
                Spacer(Modifier.height(12.dp))
                GameCard(
                    title = "Tennis Rétro",
                    description = "Affronte l'IA raquette en main. Chaque point marqué te rapporte des points.",
                    actionLabel = "Jouer",
                    onAction = onOpenPong
                )

                if (!isPremium) {
                    Spacer(Modifier.height(20.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth().clickableCard(onOpenPaywall),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().background(CardGradient).padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✨", fontSize = 22.sp)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Débloque tout instantanément avec Premium", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Tous les mini-jeux et tous les thèmes, sans passer par les points.", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                Text("Jeux d'arcade à débloquer", color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                GameShopCatalog.lockedGames.forEach { item ->
                    val unlocked = isPremium || unlockedGames.contains(item.id)
                    Spacer(Modifier.height(12.dp))
                    LockedGameCard(
                        title = item.title,
                        description = item.description,
                        cost = item.cost,
                        points = points,
                        unlocked = unlocked,
                        showWatchAd = BuildConfig.ADS_ENABLED,
                        canWatchAd = AppPrefs.canWatchAdForPoints(context),
                        onUnlock = {
                            if (BuildConfig.IS_TEST) showTestBlocked = true
                            else vm.tryUnlock(item.id)
                        },
                        onPlay = {
                            if (item.id in implementedArcadeGames) onOpenArcade(item.id)
                            else comingSoonMessage = "${item.title} arrive dans une prochaine mise à jour !"
                        },
                        onWatchAd = {
                            watchRewardedAdForPoints(context, onEarned = {
                                vm.earnPoints(AD_REWARD_POINTS)
                                AppPrefs.recordAdRewardWatched(context)
                            })
                        }
                    )
                }

                Spacer(Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().clickableCard(onOpenShop),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().background(CardGradient).padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Storefront, contentDescription = null, tint = NeonPurple)
                        Spacer(Modifier.width(12.dp))
                        Text("Boutique", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                comingSoonMessage?.let { message ->
                    Spacer(Modifier.height(12.dp))
                    Snackbar { Text(message) }
                    LaunchedEffect(message) {
                        kotlinx.coroutines.delay(2500)
                        comingSoonMessage = null
                    }
                }
                chestMessage?.let { message ->
                    Spacer(Modifier.height(12.dp))
                    Snackbar { Text(message) }
                    LaunchedEffect(message) {
                        kotlinx.coroutines.delay(2500)
                        chestMessage = null
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

/**
 * Coffre Quotidien : bonus de points aléatoire (5 à 30) contre une pub, disponible une fois
 * toutes les 24h (cooldown glissant, voir [AppPrefs.canOpenDailyChest]) — jamais visible en
 * Premium (déjà sans pub) ni dans l'édition sans pub (V2SP, filtrée par l'appelant).
 */
@Composable
private fun DailyChestCard(available: Boolean, remainingMs: Long, onOpen: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.fillMaxWidth().background(CardGradient).padding(16.dp)) {
            Text("🎁 Coffre Quotidien", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                "Regarde une pub pour ouvrir le coffre et gagner un bonus de points aléatoire.",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 13.sp
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onOpen, enabled = available) {
                Text(if (available) "🎬 Ouvrir le coffre (regarder une pub)" else "🔒 Prochain coffre dans ${formatChestCountdown(remainingMs)}")
            }
        }
    }
}

private fun formatChestCountdown(remainingMs: Long): String {
    val totalMinutes = (remainingMs / 60_000L).coerceAtLeast(1L)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h${minutes.toString().padStart(2, '0')}" else "${minutes}min"
}

@Composable
private fun GameCard(title: String, description: String, actionLabel: String, onAction: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.fillMaxWidth().background(CardGradient).padding(16.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(6.dp))
            Text(description, color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun LockedGameCard(
    title: String,
    description: String,
    cost: Int,
    points: Int,
    unlocked: Boolean,
    showWatchAd: Boolean,
    canWatchAd: Boolean,
    onUnlock: () -> Unit,
    onPlay: () -> Unit,
    onWatchAd: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.fillMaxWidth().background(CardGradient).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!unlocked) Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
                Spacer(Modifier.width(6.dp))
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text(description, color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            if (unlocked) {
                Button(onClick = onPlay, colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Jouer")
                }
            } else {
                OutlinedButton(onClick = onUnlock, enabled = points >= cost) {
                    Text("Débloquer — $cost points")
                }
                if (showWatchAd) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = onWatchAd, enabled = canWatchAd) {
                        Text(
                            if (canWatchAd) "🎬 Regarder une pub (+$AD_REWARD_POINTS points)"
                            else "🎬 Limite quotidienne atteinte (revenez demain)"
                        )
                    }
                }
            }
        }
    }
}

/** Ouvre la Boutique en cliquant n'importe où sur la carte. */
private fun Modifier.clickableCard(onClick: () -> Unit): Modifier = this.clickable(onClick = onClick)
