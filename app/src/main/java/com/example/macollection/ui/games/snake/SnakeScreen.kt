package com.example.macollection.ui.games.snake

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import com.example.macollection.BuildConfig
import com.example.macollection.data.GameShopCatalog
import com.example.macollection.ui.GameViewModel
import com.example.macollection.ui.ads.watchRewardedAd
import com.example.macollection.ui.games.engine.ContinueOffer
import com.example.macollection.ui.games.engine.CountdownOverlay
import com.example.macollection.ui.games.engine.GameFx
import com.example.macollection.ui.games.engine.GameLoop
import com.example.macollection.ui.games.engine.GameScaffold
import com.example.macollection.ui.theme.NeonCyan
import com.example.macollection.ui.theme.NeonPink
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

private const val COLS = 14
private const val ROWS = 18
private const val SCORE_PER_APPLE = 10
private const val POINTS_PER_MILESTONE = 10
// Rééquilibré le 2026-07-12 : à 5, Serpent donnait ~40-60 points/minute (une pomme toutes les
// 2-3 s), bien plus que les autres jeux d'arcade (~10-20 pts/min) — écart repéré par l'utilisateur.
private const val APPLES_PER_MILESTONE = 10
// Un nouveau serpent (taille de départ, score/fruits conservés) offert contre une pub, jusqu'à 3
// fois par partie.
private const val MAX_CONTINUES = 3

private val SnakeGreen = Color(0xFF3DDC84)

/**
 * Snake : le serpent avance tout seul, glisser dans une direction le fait tourner (impossible de
 * faire demi-tour sur soi). Manger une pomme le fait grandir et accélérer un peu. Toucher un mur
 * ou son propre corps = partie terminée. Tous les 5 fruits = +10 points de boutique.
 */
@Composable
fun SnakeScreen(vm: GameViewModel, onExit: () -> Unit) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    var snake by remember { mutableStateOf(listOf(7 to 9, 6 to 9, 5 to 9)) }
    var dir by remember { mutableStateOf(1 to 0) }
    var pendingDir by remember { mutableStateOf(1 to 0) }
    var food by remember { mutableStateOf(10 to 9) }
    var score by remember { mutableIntStateOf(0) }
    var apples by remember { mutableIntStateOf(0) }
    var earnedTotal by remember { mutableIntStateOf(0) }
    var stepAccum by remember { mutableFloatStateOf(0f) }
    var initialized by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    var scoreRecorded by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(0) }
    var countdownKey by remember { mutableIntStateOf(0) }
    var continuesUsed by remember { mutableIntStateOf(0) }
    var continueOffer by remember { mutableStateOf<ContinueOffer?>(null) }
    var multiplierClaimed by remember { mutableStateOf(false) }
    val bestScores by vm.highScores.collectAsState()
    val context = LocalContext.current
    val isPremium by vm.isPremiumAllAccess.collectAsState()

    /** Propose une pub pour un nouveau serpent (score et fruits conservés), sinon termine la partie. */
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
            message = "Partie terminée ! Regarder une pub pour repartir avec un nouveau serpent (score conservé) ?",
            buttonLabel = "🎬 Regarder une pub pour continuer",
            onAccept = {
                continueOffer = null
                watchRewardedAd(context, onRewarded = { continuesUsed++; onGranted() }, onClosed = { gameOver = true })
            },
            onDecline = { continueOffer = null; gameOver = true }
        )
    }

    fun placeFood(body: List<Pair<Int, Int>>) {
        var c: Pair<Int, Int>
        do {
            c = Random.nextInt(COLS) to Random.nextInt(ROWS)
        } while (body.contains(c))
        food = c
    }

    fun resetGame() {
        snake = listOf(7 to 9, 6 to 9, 5 to 9)
        dir = 1 to 0
        pendingDir = 1 to 0
        score = 0
        apples = 0
        earnedTotal = 0
        stepAccum = 0f
        continuesUsed = 0
        continueOffer = null
        multiplierClaimed = false
        gameOver = false
        scoreRecorded = false
        placeFood(snake)
        initialized = true
        countdownKey++ // décompte 3-2-1 avant de (re)lancer la partie
    }

    LaunchedEffect(Unit) { if (!initialized) resetGame() }

    LaunchedEffect(gameOver) {
        if (gameOver && !scoreRecorded) {
            scoreRecorded = true
            vm.recordHighScore(GameShopCatalog.SNAKE, score)
            GameFx.gameOver(context)
        }
    }

    // Décompte 3-2-1-GO ! avant de (re)lancer la partie.
    LaunchedEffect(countdownKey) {
        if (countdownKey == 0) return@LaunchedEffect
        countdown = 3; delay(600)
        countdown = 2; delay(600)
        countdown = 1; delay(600)
        countdown = -1; delay(450)
        countdown = 0
    }

    GameLoop(running = initialized && !gameOver && countdown == 0 && continueOffer == null) { dt ->
        stepAccum += dt
        val interval = (0.16f - 0.006f * apples).coerceAtLeast(0.06f)
        if (stepAccum < interval) return@GameLoop
        stepAccum -= interval

        // Applique la direction demandée si elle n'est pas un demi-tour.
        if (pendingDir.first != -dir.first || pendingDir.second != -dir.second) dir = pendingDir

        val head = snake.first()
        val newHead = (head.first + dir.first) to (head.second + dir.second)

        // Corps qui compte pour la collision : si ce coup ne mange pas, la queue est libérée au
        // même tick (voir plus bas, snake.dropLast(1)) donc avancer dans la case de la queue
        // actuelle est un mouvement légal, pas une auto-collision.
        val bodyForCollision = if (newHead == food) snake else snake.dropLast(1)

        // Mur ou corps -> mort.
        if (newHead.first < 0 || newHead.first >= COLS || newHead.second < 0 || newHead.second >= ROWS ||
            bodyForCollision.contains(newHead)
        ) {
            offerContinueOrEnd(onGranted = {
                snake = listOf(7 to 9, 6 to 9, 5 to 9)
                dir = 1 to 0
                pendingDir = 1 to 0
                stepAccum = 0f
                placeFood(snake)
                GameFx.loseLife(context)
                countdownKey++
            })
            return@GameLoop
        }

        if (newHead == food) {
            snake = listOf(newHead) + snake
            score += SCORE_PER_APPLE
            apples++
            if (apples % APPLES_PER_MILESTONE == 0) {
                vm.earnPoints(POINTS_PER_MILESTONE)
                earnedTotal += POINTS_PER_MILESTONE
                GameFx.win()
            } else {
                GameFx.eat()
            }
            placeFood(snake)
        } else {
            snake = listOf(newHead) + snake.dropLast(1)
        }
    }

    GameScaffold(
        title = "Serpent — $apples fruits",
        score = score,
        onExit = onExit,
        gameOver = gameOver,
        pointsEarned = earnedTotal,
        bestScore = bestScores[GameShopCatalog.SNAKE] ?: 0,
        onRestart = { resetGame() },
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
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = it }
                .pointerInput(Unit) {
                    detectDragGestures { change, drag ->
                        change.consume()
                        pendingDir = if (abs(drag.x) > abs(drag.y)) {
                            (if (drag.x > 0) 1 else -1) to 0
                        } else {
                            0 to (if (drag.y > 0) 1 else -1)
                        }
                    }
                }
        ) {
            drawRect(color = Color(0xFF0B0B14))
            val cell = minOf(size.width / COLS, size.height / ROWS)
            val ox = (size.width - cell * COLS) / 2f
            val oy = (size.height - cell * ROWS) / 2f
            val fieldW = cell * COLS
            val fieldH = cell * ROWS
            drawRect(color = Color(0xFF171728), topLeft = Offset(ox, oy), size = Size(fieldW, fieldH))

            // Grille légère : on voit clairement les cases de la zone de jeu.
            for (c in 1 until COLS) {
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(ox + c * cell, oy),
                    end = Offset(ox + c * cell, oy + fieldH),
                    strokeWidth = 1f
                )
            }
            for (r in 1 until ROWS) {
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(ox, oy + r * cell),
                    end = Offset(ox + fieldW, oy + r * cell),
                    strokeWidth = 1f
                )
            }
            // Bordure néon : délimite nettement les murs (mortels au contact).
            drawRect(
                color = NeonCyan,
                topLeft = Offset(ox, oy),
                size = Size(fieldW, fieldH),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f)
            )

            // Pomme.
            drawCircle(
                NeonPink,
                radius = cell * 0.4f,
                center = Offset(ox + (food.first + 0.5f) * cell, oy + (food.second + 0.5f) * cell)
            )
            // Serpent (tête plus claire).
            snake.forEachIndexed { i, (c, r) ->
                val pad = cell * 0.08f
                drawRoundRect(
                    color = if (i == 0) NeonCyan else SnakeGreen,
                    topLeft = Offset(ox + c * cell + pad, oy + r * cell + pad),
                    size = Size(cell - 2 * pad, cell - 2 * pad)
                )
            }
        }
        CountdownOverlay(countdown)
    }
}
