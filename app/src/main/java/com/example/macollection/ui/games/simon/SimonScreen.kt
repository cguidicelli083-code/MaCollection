package com.example.macollection.ui.games.simon

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.macollection.BuildConfig
import com.example.macollection.data.GameShopCatalog
import com.example.macollection.ui.GameViewModel
import com.example.macollection.ui.ads.watchRewardedAd
import com.example.macollection.ui.games.engine.ContinueOffer
import com.example.macollection.ui.games.engine.GameFx
import com.example.macollection.ui.games.engine.GameScaffold
import kotlinx.coroutines.delay
import kotlin.random.Random

// Une nouvelle chance sur la même manche (séquence conservée) offerte contre une pub, jusqu'à 3
// fois par partie.
private const val MAX_CONTINUES = 3

private val PadColors = listOf(
    Color(0xFF3DDC84), // vert
    Color(0xFFFF4D4D), // rouge
    Color(0xFFFFD400), // jaune
    Color(0xFF5C8DFF)  // bleu
)

/**
 * Simon (« Jean dit ») : une séquence de couleurs s'allonge d'un cran à chaque manche. Regarde-la
 * s'illuminer, puis reproduis-la en tapant les touches dans l'ordre. Une erreur = partie terminée.
 * Toutes les 3 manches réussies = +10 points de boutique. Jeu de mémoire, mécanique originale.
 */
@Composable
fun SimonScreen(vm: GameViewModel, onExit: () -> Unit) {
    var sequence by remember { mutableStateOf(listOf<Int>()) }
    var inputIndex by remember { mutableIntStateOf(0) }
    var showing by remember { mutableStateOf(true) }
    var litPad by remember { mutableStateOf<Int?>(null) }
    var gameOver by remember { mutableStateOf(false) }
    var scoreRecorded by remember { mutableStateOf(false) }
    var earnedTotal by remember { mutableIntStateOf(0) }
    var playToken by remember { mutableIntStateOf(0) }
    var flashKey by remember { mutableIntStateOf(0) }
    var continuesUsed by remember { mutableIntStateOf(0) }
    var continueOffer by remember { mutableStateOf<ContinueOffer?>(null) }
    var multiplierClaimed by remember { mutableStateOf(false) }
    val bestScores by vm.highScores.collectAsState()
    val context = LocalContext.current
    val isPremium by vm.isPremiumAllAccess.collectAsState()

    fun startGame() {
        sequence = listOf(Random.nextInt(4))
        inputIndex = 0
        earnedTotal = 0
        continuesUsed = 0
        continueOffer = null
        multiplierClaimed = false
        gameOver = false
        scoreRecorded = false
        litPad = null
        showing = true
        playToken++
    }

    /** Propose une pub pour retenter la même manche (séquence conservée), sinon termine la partie. */
    fun offerContinueOrEnd(onGranted: () -> Unit) {
        if (!BuildConfig.ADS_ENABLED || continuesUsed >= MAX_CONTINUES) {
            gameOver = true
            return
        }
        if (isPremium) {
            continuesUsed++
            onGranted()
            return
        }
        continueOffer = ContinueOffer(
            message = "Erreur ! Regarder une pub pour retenter cette manche (séquence conservée) ?",
            buttonLabel = "🎬 Regarder une pub pour continuer",
            onAccept = {
                continueOffer = null
                watchRewardedAd(context, onRewarded = { continuesUsed++; onGranted() }, onClosed = { gameOver = true })
            },
            onDecline = { continueOffer = null; gameOver = true }
        )
    }

    LaunchedEffect(Unit) { startGame() }

    // Rejoue la séquence courante (à chaque nouvelle manche).
    LaunchedEffect(playToken) {
        if (playToken == 0) return@LaunchedEffect
        showing = true
        delay(450)
        for (p in sequence) {
            litPad = p
            GameFx.simonPad(p)
            delay(400)
            litPad = null
            delay(180)
        }
        showing = false
        inputIndex = 0
    }

    // Éteint la touche après un bref flash lors d'un appui.
    LaunchedEffect(flashKey) {
        if (flashKey == 0) return@LaunchedEffect
        delay(200)
        if (!showing) litPad = null
    }

    LaunchedEffect(gameOver) {
        if (gameOver && !scoreRecorded) {
            scoreRecorded = true
            vm.recordHighScore(GameShopCatalog.SIMON, sequence.size)
            GameFx.gameOver(context)
        }
    }

    fun onPad(p: Int) {
        if (showing || gameOver || sequence.isEmpty() || continueOffer != null) return
        litPad = p
        flashKey++
        GameFx.simonPad(p)
        if (p == sequence[inputIndex]) {
            inputIndex++
            if (inputIndex == sequence.size) {
                if (sequence.size % 3 == 0) {
                    vm.earnPoints(10)
                    earnedTotal += 10
                }
                sequence = sequence + Random.nextInt(4)
                playToken++
            }
        } else {
            offerContinueOrEnd(onGranted = { inputIndex = 0; litPad = null; playToken++ })
        }
    }

    GameScaffold(
        title = if (showing) "Mémo — regarde bien" else "Mémo — à toi ! (manche ${sequence.size})",
        score = sequence.size,
        onExit = onExit,
        gameOver = gameOver,
        pointsEarned = earnedTotal,
        bestScore = bestScores[GameShopCatalog.SIMON] ?: 0,
        onRestart = { startGame() },
        continueOffer = continueOffer,
        onWatchAdForMultiplier = if (BuildConfig.ADS_ENABLED && !isPremium && !multiplierClaimed && earnedTotal > 0) {
            {
                watchRewardedAd(context, onRewarded = {
                    val bonus = earnedTotal * 2
                    vm.earnPoints(bonus)
                    earnedTotal += bonus
                    multiplierClaimed = true
                })
            }
        } else null
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(Modifier.fillMaxWidth().weight(1f)) {
                SimonPad(0, litPad == 0, ::onPad, Modifier.weight(1f).fillMaxHeight())
                SimonPad(1, litPad == 1, ::onPad, Modifier.weight(1f).fillMaxHeight())
            }
            Row(Modifier.fillMaxWidth().weight(1f)) {
                SimonPad(2, litPad == 2, ::onPad, Modifier.weight(1f).fillMaxHeight())
                SimonPad(3, litPad == 3, ::onPad, Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}

@Composable
private fun SimonPad(index: Int, lit: Boolean, onTap: (Int) -> Unit, modifier: Modifier) {
    val base = PadColors[index]
    Column(
        modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (lit) base else base.copy(alpha = 0.35f))
            .clickable { onTap(index) }
    ) {}
}
