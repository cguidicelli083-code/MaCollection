package com.example.macollection.ui.games.tetris

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
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
import kotlinx.coroutines.delay
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
import com.example.macollection.ui.ads.GameAds
import com.example.macollection.ui.ads.watchRewardedAd
import com.example.macollection.ui.games.engine.ContinueOffer
import com.example.macollection.ui.games.engine.CountdownOverlay
import com.example.macollection.ui.games.engine.GameFx
import com.example.macollection.ui.games.engine.GameLoop
import com.example.macollection.ui.games.engine.GameScaffold
import kotlin.random.Random

private const val COLS = 10
private const val ROWS = 20
private const val POINTS_PER_LEVEL = 10
// Un plateau vidé (score/niveau conservés) offert contre une pub, jusqu'à 3 fois par partie.
private const val MAX_CONTINUES = 3

/** Une pièce (tétromino) : ses 4 orientations (chacune = 4 cases col/ligne) et sa couleur. */
private class Piece(val rotations: List<List<Pair<Int, Int>>>, val color: Color)

// Les 7 tétrominos classiques, dans leurs 4 orientations (repère 0..3 en colonne/ligne).
private val PIECES = listOf(
    // I
    Piece(
        listOf(
            listOf(0 to 1, 1 to 1, 2 to 1, 3 to 1),
            listOf(2 to 0, 2 to 1, 2 to 2, 2 to 3),
            listOf(0 to 2, 1 to 2, 2 to 2, 3 to 2),
            listOf(1 to 0, 1 to 1, 1 to 2, 1 to 3)
        ), Color(0xFF22E6FF)
    ),
    // O
    Piece(
        List(4) { listOf(1 to 0, 2 to 0, 1 to 1, 2 to 1) }, Color(0xFFFFD400)
    ),
    // T
    Piece(
        listOf(
            listOf(1 to 0, 0 to 1, 1 to 1, 2 to 1),
            listOf(1 to 0, 1 to 1, 2 to 1, 1 to 2),
            listOf(0 to 1, 1 to 1, 2 to 1, 1 to 2),
            listOf(1 to 0, 0 to 1, 1 to 1, 1 to 2)
        ), Color(0xFF8B5CFF)
    ),
    // S
    Piece(
        listOf(
            listOf(1 to 0, 2 to 0, 0 to 1, 1 to 1),
            listOf(1 to 0, 1 to 1, 2 to 1, 2 to 2),
            listOf(1 to 1, 2 to 1, 0 to 2, 1 to 2),
            listOf(0 to 0, 0 to 1, 1 to 1, 1 to 2)
        ), Color(0xFF3DDC84)
    ),
    // Z
    Piece(
        listOf(
            listOf(0 to 0, 1 to 0, 1 to 1, 2 to 1),
            listOf(2 to 0, 1 to 1, 2 to 1, 1 to 2),
            listOf(0 to 1, 1 to 1, 1 to 2, 2 to 2),
            listOf(1 to 0, 0 to 1, 1 to 1, 0 to 2)
        ), Color(0xFFFF4D4D)
    ),
    // J
    Piece(
        listOf(
            listOf(0 to 0, 0 to 1, 1 to 1, 2 to 1),
            listOf(1 to 0, 2 to 0, 1 to 1, 1 to 2),
            listOf(0 to 1, 1 to 1, 2 to 1, 2 to 2),
            listOf(1 to 0, 1 to 1, 0 to 2, 1 to 2)
        ), Color(0xFF5C8DFF)
    ),
    // L
    Piece(
        listOf(
            listOf(2 to 0, 0 to 1, 1 to 1, 2 to 1),
            listOf(1 to 0, 1 to 1, 1 to 2, 2 to 2),
            listOf(0 to 1, 1 to 1, 2 to 1, 0 to 2),
            listOf(0 to 0, 1 to 0, 1 to 1, 1 to 2)
        ), Color(0xFFFF8A1E)
    )
)

private fun lineScore(lines: Int) = when (lines) { 1 -> 100; 2 -> 300; 3 -> 500; else -> 800 }
private fun dropInterval(level: Int) = (0.8f - 0.06f * (level - 1)).coerceAtLeast(0.08f)

/**
 * Puzzle de blocs qui tombent (façon Tetris). Glisser à gauche/droite déplace la pièce, glisser
 * vers le bas la fait descendre plus vite, taper la fait tourner. Compléter une ligne la supprime
 * et rapporte des points ; tous les 10 lignes, le niveau monte (chute plus rapide) et rapporte
 * +10 points de boutique. Partie terminée quand une nouvelle pièce ne peut plus apparaître.
 * Implémentation 100 % originale (mécanique de blocs, aucun asset d'origine).
 */
@Composable
fun TetrisScreen(vm: GameViewModel, onExit: () -> Unit) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    var board by remember { mutableStateOf(Array(ROWS) { IntArray(COLS) }) }
    var curType by remember { mutableIntStateOf(0) }
    var curRot by remember { mutableIntStateOf(0) }
    var curX by remember { mutableIntStateOf(3) }
    var curY by remember { mutableIntStateOf(0) }
    var nextType by remember { mutableIntStateOf(Random.nextInt(PIECES.size)) }
    var score by remember { mutableIntStateOf(0) }
    var totalLines by remember { mutableIntStateOf(0) }
    var level by remember { mutableIntStateOf(1) }
    var earnedTotal by remember { mutableIntStateOf(0) }
    var dropAccum by remember { mutableFloatStateOf(0f) }
    var dragAccumX by remember { mutableFloatStateOf(0f) }
    var dragAccumY by remember { mutableFloatStateOf(0f) }
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
    val adsEnabled = !vm.isAdsReduced.collectAsState().value && !isPremium

    /** Propose une pub pour un plateau vidé (score/niveau conservés), sinon termine la partie. */
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
            message = "Plateau plein ! Regarder une pub pour repartir avec un plateau vidé (score conservé) ?",
            buttonLabel = "🎬 Regarder une pub pour continuer",
            onAccept = {
                continueOffer = null
                watchRewardedAd(context, onRewarded = { continuesUsed++; onGranted() }, onClosed = { gameOver = true })
            },
            onDecline = { continueOffer = null; gameOver = true }
        )
    }

    fun cells(type: Int, rot: Int) = PIECES[type].rotations[rot]

    fun collides(cells: List<Pair<Int, Int>>, x: Int, y: Int): Boolean {
        for ((cx, cy) in cells) {
            val col = x + cx
            val row = y + cy
            if (col < 0 || col >= COLS || row >= ROWS) return true
            if (row >= 0 && board[row][col] != 0) return true
        }
        return false
    }

    fun spawnPiece() {
        curType = nextType
        nextType = Random.nextInt(PIECES.size)
        curRot = 0
        curX = 3
        curY = 0
        if (collides(cells(curType, 0), curX, curY)) {
            offerContinueOrEnd(onGranted = {
                board = Array(ROWS) { IntArray(COLS) } // plateau vidé, score/niveau conservés
                spawnPiece()
            })
        }
    }

    fun resetGame() {
        board = Array(ROWS) { IntArray(COLS) }
        score = 0
        totalLines = 0
        level = 1
        earnedTotal = 0
        dropAccum = 0f
        dragAccumX = 0f
        dragAccumY = 0f
        continuesUsed = 0
        continueOffer = null
        multiplierClaimed = false
        gameOver = false
        scoreRecorded = false
        nextType = Random.nextInt(PIECES.size)
        spawnPiece()
        initialized = true
        countdownKey++ // décompte 3-2-1 avant de (re)lancer la partie
    }

    fun lockPiece() {
        GameFx.lock()
        val nb = Array(ROWS) { r -> board[r].copyOf() }
        for ((cx, cy) in cells(curType, curRot)) {
            val col = curX + cx
            val row = curY + cy
            if (row in 0 until ROWS && col in 0 until COLS) nb[row][col] = curType + 1
        }
        // Supprime les lignes complètes (celles sans aucune case vide).
        val kept = nb.filter { row -> row.any { it == 0 } }
        val cleared = ROWS - kept.size
        val newBoard = Array(ROWS) { IntArray(COLS) }
        val offset = ROWS - kept.size
        for (i in kept.indices) newBoard[offset + i] = kept[i]
        board = newBoard
        if (cleared > 0) {
            GameFx.blip()
            totalLines += cleared
            score += lineScore(cleared) * level
            val newLevel = 1 + totalLines / 10
            if (newLevel > level) {
                repeat(newLevel - level) {
                    vm.earnPoints(POINTS_PER_LEVEL)
                    earnedTotal += POINTS_PER_LEVEL
                }
                level = newLevel
                GameFx.win()
                GameAds.onLevelCleared()
            }
        }
        spawnPiece()
    }

    fun tryMove(dx: Int, dy: Int): Boolean {
        if (!collides(cells(curType, curRot), curX + dx, curY + dy)) {
            curX += dx
            curY += dy
            return true
        }
        return false
    }

    fun softDropTick() {
        if (!tryMove(0, 1)) lockPiece()
    }

    fun rotate() {
        val nr = (curRot + 1) % 4
        for (k in listOf(0, -1, 1, -2, 2)) {
            if (!collides(cells(curType, nr), curX + k, curY)) {
                curRot = nr
                curX += k
                GameFx.rotate()
                return
            }
        }
    }

    LaunchedEffect(canvasSize) {
        if (!initialized && canvasSize.width > 0) resetGame()
    }

    LaunchedEffect(gameOver) {
        if (gameOver && !scoreRecorded) {
            scoreRecorded = true
            vm.recordHighScore(GameShopCatalog.TETRIS, score)
            GameFx.gameOver(context)
            GameAds.onGameOver(context, adsEnabled)
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
        dropAccum += dt
        val interval = dropInterval(level)
        if (dropAccum >= interval) {
            dropAccum -= interval
            softDropTick()
        }
    }

    GameScaffold(
        title = "Blocs — Niv. $level — $totalLines lignes",
        score = score,
        onExit = onExit,
        gameOver = gameOver,
        pointsEarned = earnedTotal,
        bestScore = bestScores[GameShopCatalog.TETRIS] ?: 0,
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
                .onSizeChanged { canvasSize = it }
                .pointerInput(Unit) {
                    val step = minOf(size.width / COLS, size.height / ROWS).toFloat()
                    detectDragGestures(
                        onDragEnd = { dragAccumX = 0f; dragAccumY = 0f }
                    ) { change, drag ->
                        change.consume()
                        dragAccumX += drag.x
                        dragAccumY += drag.y
                        while (dragAccumX >= step) { tryMove(1, 0); dragAccumX -= step }
                        while (dragAccumX <= -step) { tryMove(-1, 0); dragAccumX += step }
                        while (dragAccumY >= step) { softDropTick(); dragAccumY -= step }
                        if (dragAccumY < 0f) dragAccumY = 0f
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { rotate() }
                }
        ) {
            drawRect(color = Color(0xFF0B0B14))
            val cell = minOf(size.width / COLS, size.height / ROWS)
            val boardW = cell * COLS
            val boardH = cell * ROWS
            val ox = (size.width - boardW) / 2f
            val oy = (size.height - boardH) / 2f

            // Fond du plateau + cadre.
            drawRect(color = Color(0xFF12121F), topLeft = Offset(ox, oy), size = Size(boardW, boardH))

            fun drawCell(col: Int, row: Int, color: Color) {
                if (row < 0) return
                val x = ox + col * cell
                val y = oy + row * cell
                val pad = cell * 0.06f
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x + pad, y + pad),
                    size = Size(cell - 2 * pad, cell - 2 * pad)
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.18f),
                    topLeft = Offset(x + pad, y + pad),
                    size = Size(cell - 2 * pad, (cell - 2 * pad) * 0.35f)
                )
            }

            // Cases posées.
            for (r in 0 until ROWS) for (c in 0 until COLS) {
                val v = board[r][c]
                if (v != 0) drawCell(c, r, PIECES[v - 1].color)
            }
            // Pièce en cours.
            val color = PIECES[curType].color
            for ((cx, cy) in cells(curType, curRot)) drawCell(curX + cx, curY + cy, color)

            // Cadre lumineux du plateau.
            drawRect(
                color = Color.White.copy(alpha = 0.15f),
                topLeft = Offset(ox, oy),
                size = Size(boardW, boardH),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )
        }
        CountdownOverlay(countdown)
    }
}
