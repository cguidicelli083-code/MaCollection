package com.example.macollection.ui.games.frogger

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.example.macollection.BuildConfig
import com.example.macollection.data.GameShopCatalog
import com.example.macollection.ui.GameViewModel
import com.example.macollection.ui.games.engine.CountdownOverlay
import com.example.macollection.ui.ads.GameAds
import com.example.macollection.ui.ads.watchRewardedAd
import com.example.macollection.ui.games.engine.ContinueOffer
import com.example.macollection.ui.games.engine.GameFx
import com.example.macollection.ui.games.engine.GameLoop
import com.example.macollection.ui.games.engine.GameScaffold
import kotlinx.coroutines.delay
import kotlin.random.Random

private const val COLS = 11
private const val ROWS = 13
private const val LIVES_START = 3
// Une vie supplémentaire offerte contre une pub, jusqu'à 3 fois par partie.
private const val MAX_CONTINUES = 3
// Rééquilibré le 2026-07-12 : à 10, une traversée toutes les 10-20 s donnait ~30-60 points/minute,
// bien plus que les autres jeux d'arcade (~10-20 pts/min).
private const val POINTS_PER_CROSSING = 5

private val FrogGreen = Color(0xFF3DDC84)
private val GoalGold = Color(0xFFFFC94D)
private val RoadColor = Color(0xFF1A1A26)
private val SafeColor = Color(0xFF163A1E)
private val CarColors = listOf(Color(0xFFFF4D4D), Color(0xFFFF8A1E), Color(0xFF5C8DFF), Color(0xFFFFD400))

// Voies générées ALÉATOIREMENT à chaque partie (sens, vitesse, espacement, phase de départ), pour
// que le trafic soit différent à chaque fois. Seules les lignes IMPAIRES portent des voitures ;
// les paires sont des rangées de repos sûres, et 0 = but, ROWS-1 = départ.

/**
 * Frogger : fais traverser la grenouille de bas en haut en esquivant les voitures qui défilent
 * dans chaque voie. Glisser dans une direction = un déplacement d'une case. Atteindre le haut =
 * traversée réussie (+100, +10 points de boutique) et ça accélère un peu. Une voiture te percute =
 * une vie en moins. Mécanique originale (voies défilantes), aucun asset d'origine.
 */
@Composable
fun FroggerScreen(vm: GameViewModel, onExit: () -> Unit) {
    var frogCol by remember { mutableIntStateOf(COLS / 2) }
    var frogRow by remember { mutableIntStateOf(ROWS - 1) }
    var laneOffsets by remember { mutableStateOf(FloatArray(ROWS)) }
    var lives by remember { mutableIntStateOf(LIVES_START) }
    var score by remember { mutableIntStateOf(0) }
    var crossings by remember { mutableIntStateOf(0) }
    var earnedTotal by remember { mutableIntStateOf(0) }
    var speedMult by remember { mutableFloatStateOf(1f) }
    var initialized by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    var scoreRecorded by remember { mutableStateOf(false) }
    val bestScores by vm.highScores.collectAsState()
    val context = LocalContext.current
    val isPremium by vm.isPremiumAllAccess.collectAsState()
    val adsEnabled = !vm.isAdsReduced.collectAsState().value && !isPremium
    var laneDir by remember { mutableStateOf(IntArray(ROWS)) }
    var laneSpeed by remember { mutableStateOf(FloatArray(ROWS)) }
    var laneSpacing by remember { mutableStateOf(IntArray(ROWS) { 4 }) }
    var countdown by remember { mutableIntStateOf(0) }
    var countdownKey by remember { mutableIntStateOf(0) }
    var continuesUsed by remember { mutableIntStateOf(0) }
    var continueOffer by remember { mutableStateOf<ContinueOffer?>(null) }
    var multiplierClaimed by remember { mutableStateOf(false) }

    /** Propose une pub pour une vie supplémentaire, sinon termine la partie. */
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
            message = "Plus de vies ! Regarder une pub pour continuer avec une vie supplémentaire ?",
            buttonLabel = "🎬 Regarder une pub pour continuer",
            onAccept = {
                continueOffer = null
                watchRewardedAd(context, onRewarded = { continuesUsed++; onGranted() }, onClosed = { gameOver = true })
            },
            onDecline = { continueOffer = null; gameOver = true }
        )
    }

    fun carAt(row: Int, col: Int): Boolean {
        if (row <= 0 || row >= ROWS - 1) return false
        if (row % 2 == 0) return false // rangées paires = repos sûr
        val s = laneSpacing[row]
        val m = (((col - laneOffsets[row]) % s) + s) % s
        return m < 1f
    }

    // Génère un parcours : sens, vitesse de base, espacement et phase de chaque voie, tirés au sort.
    // Utilisé au début de partie ET pour changer la disposition des obstacles (dès la 4e traversée).
    fun generateCourse() {
        val dir = IntArray(ROWS)
        val spd = FloatArray(ROWS)
        val sp = IntArray(ROWS)
        val off = FloatArray(ROWS)
        for (r in 0 until ROWS) {
            dir[r] = if (Random.nextBoolean()) 1 else -1
            spd[r] = 0.8f + Random.nextFloat() * 1.1f
            sp[r] = 4 + Random.nextInt(3)
            off[r] = Random.nextFloat() * sp[r]
        }
        laneDir = dir
        laneSpeed = spd
        laneSpacing = sp
        laneOffsets = off
    }

    fun resetGame() {
        frogCol = COLS / 2
        frogRow = ROWS - 1
        generateCourse()
        lives = LIVES_START
        score = 0
        crossings = 0
        earnedTotal = 0
        speedMult = 1f
        countdown = 0
        continuesUsed = 0
        continueOffer = null
        multiplierClaimed = false
        gameOver = false
        scoreRecorded = false
        initialized = true
        countdownKey++ // décompte 3-2-1 avant de (re)lancer la partie
    }

    LaunchedEffect(Unit) { if (!initialized) resetGame() }

    LaunchedEffect(gameOver) {
        if (gameOver && !scoreRecorded) {
            scoreRecorded = true
            vm.recordHighScore(GameShopCatalog.FROGGER, score)
            GameFx.gameOver(context)
            GameAds.onGameOver(context, adsEnabled)
        }
    }

    // Décompte 3-2-1-GO ! après une vie perdue, avant de reprendre.
    LaunchedEffect(countdownKey) {
        if (countdownKey == 0) return@LaunchedEffect
        countdown = 3; delay(600)
        countdown = 2; delay(600)
        countdown = 1; delay(600)
        countdown = -1; delay(450)
        countdown = 0
    }

    fun moveFrog(dc: Int, dr: Int) {
        if (gameOver || countdown != 0 || continueOffer != null) return
        frogCol = (frogCol + dc).coerceIn(0, COLS - 1)
        frogRow = (frogRow + dr).coerceIn(0, ROWS - 1)
        if (frogRow == 0) {
            crossings++
            score += 100
            vm.earnPoints(POINTS_PER_CROSSING)
            earnedTotal += POINTS_PER_CROSSING
            speedMult += 0.08f
            GameFx.win()
            GameAds.onLevelCleared()
            // Traversées 1-2-3 : même parcours, juste accéléré (speedMult). À partir de la 4e
            // traversée : nouvelle disposition d'obstacles (nouveau parcours), toujours plus rapide.
            if (crossings >= 3) generateCourse()
            frogCol = COLS / 2
            frogRow = ROWS - 1
        } else {
            GameFx.hop()
        }
    }

    GameLoop(running = initialized && !gameOver && countdown == 0 && continueOffer == null) { dt ->
        val no = laneOffsets.copyOf()
        for (r in 1 until ROWS - 1) no[r] += laneDir[r] * laneSpeed[r] * speedMult * dt
        laneOffsets = no
        if (carAt(frogRow, frogCol)) {
            lives--
            if (lives <= 0) {
                offerContinueOrEnd(onGranted = {
                    lives = 1
                    frogCol = COLS / 2
                    frogRow = ROWS - 1
                    GameFx.loseLife(context)
                    countdownKey++
                })
            } else {
                frogCol = COLS / 2
                frogRow = ROWS - 1
                GameFx.loseLife(context)
                countdownKey++
            }
        }
    }

    GameScaffold(
        title = "Grenouille — ❤ $lives — $crossings traversées",
        score = score,
        onExit = onExit,
        gameOver = gameOver,
        pointsEarned = earnedTotal,
        bestScore = bestScores[GameShopCatalog.FROGGER] ?: 0,
        onRestart = { resetGame() },
        continueOffer = continueOffer,
        onWatchAdForMultiplier = if (BuildConfig.ADS_ENABLED && adsEnabled && !multiplierClaimed && earnedTotal > 0) {
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
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { moveFrog(0, -1) },      // taper = avancer (vers le haut)
                        onDoubleTap = { moveFrog(0, 1) }  // double-taper = reculer
                    )
                }
        ) {
            drawRect(color = Color(0xFF0B0B14))
            val cell = minOf(size.width / COLS, size.height / ROWS)
            val ox = (size.width - cell * COLS) / 2f
            val oy = (size.height - cell * ROWS) / 2f

            // Fonds de lignes.
            for (r in 0 until ROWS) {
                val bg = when {
                    r == 0 -> GoalGold.copy(alpha = 0.25f)
                    r == ROWS - 1 || r % 2 == 0 -> SafeColor // départ + rangées de repos (sûres)
                    else -> RoadColor
                }
                drawRect(bg, topLeft = Offset(ox, oy + r * cell), size = Size(cell * COLS, cell))
            }
            // But (haut).
            drawRect(GoalGold, topLeft = Offset(ox, oy), size = Size(cell * COLS, cell * 0.12f))

            // Voitures.
            for (r in 1 until ROWS - 1) {
                for (c in 0 until COLS) {
                    if (carAt(r, c)) {
                        val pad = cell * 0.08f
                        drawRoundRect(
                            color = CarColors[r % CarColors.size],
                            topLeft = Offset(ox + c * cell + pad, oy + r * cell + pad),
                            size = Size(cell - 2 * pad, cell - 2 * pad)
                        )
                    }
                }
            }

            // Grenouille.
            val fx = ox + (frogCol + 0.5f) * cell
            val fy = oy + (frogRow + 0.5f) * cell
            drawCircle(FrogGreen, radius = cell * 0.4f, center = Offset(fx, fy))
            drawCircle(Color.Black, radius = cell * 0.08f, center = Offset(fx - cell * 0.15f, fy - cell * 0.12f))
            drawCircle(Color.Black, radius = cell * 0.08f, center = Offset(fx + cell * 0.15f, fy - cell * 0.12f))
        }
        CountdownOverlay(countdown)
    }
}
