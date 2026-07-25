package com.example.macollection.ui.games.arkanoid

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.example.macollection.ui.theme.NeonCyan
import com.example.macollection.ui.theme.NeonPink
import com.example.macollection.ui.theme.NeonPurple
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

private const val COLS = 7
private const val ROWS = 6
private const val LIVES_START = 3
private const val MAX_LIVES = 5
// Une vie supplémentaire offerte contre une pub, jusqu'à 3 fois par partie.
private const val MAX_CONTINUES = 3
private const val SCORE_PER_BRICK = 10
private const val POINTS_PER_LEVEL_WON = 10
private const val BONUS_DROP_CHANCE = 0.20f
private const val MAX_BALLS = 6

/** Couleurs des rangées de briques (palette néon du thème). */
private val rowColors = listOf(
    NeonPink, NeonPurple, Color(0xFF5C8DFF), NeonCyan, Color(0xFF3DDC84), Color(0xFFFFC94D)
)

/** Bonus qui tombent des briques détruites, à attraper avec la raquette (comme le vrai Arkanoid). */
private enum class BonusType(val label: String, val color: Color) {
    ENLARGE("E", Color(0xFF3DDC84)),   // raquette allongée
    GLUE("C", Color(0xFFFFC94D)),      // colle : la balle se fixe, taper pour relancer
    TRIPLE("D", NeonCyan),             // 3 balles en même temps
    LASER("L", NeonPink),              // tirs laser : taper pour tirer
    LIFE("P", Color(0xFFFF6D6D)),      // vie supplémentaire
    SLOW("S", Color(0xFF9A9AB5))       // ralentit les balles
}

private data class Ball(
    val x: Float, val y: Float,
    val vx: Float, val vy: Float,
    val stuck: Boolean = false,
    val stuckOffset: Float = 0f
)

private data class FallingBonus(val x: Float, val y: Float, val type: BonusType)

private data class LaserShot(val x: Float, val y: Float)

/**
 * Casse-briques avec bonus (raquette allongée, colle, multi-balles, laser, vie, ralenti).
 * La raquette est remontée (~1 cm au-dessus du bas) pour ne pas être cachée par le doigt.
 * Chaque tableau nettoyé = partie gagnée = +10 points, puis la difficulté monte (balle plus
 * rapide) en restant progressive. Taper l'écran relance une balle collée / tire au laser.
 */
@Composable
fun ArkanoidScreen(vm: GameViewModel, onExit: () -> Unit) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    var balls by remember { mutableStateOf(listOf<Ball>()) }
    var bonuses by remember { mutableStateOf(listOf<FallingBonus>()) }
    var lasers by remember { mutableStateOf(listOf<LaserShot>()) }
    var paddleX by remember { mutableFloatStateOf(0f) }
    var bricks by remember { mutableStateOf(List(ROWS * COLS) { true }) }
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(LIVES_START) }
    var levelNum by remember { mutableIntStateOf(1) }
    var levelsWon by remember { mutableIntStateOf(0) }
    // Durées restantes des bonus temporaires (secondes).
    var enlargeTimer by remember { mutableFloatStateOf(0f) }
    var glueTimer by remember { mutableFloatStateOf(0f) }
    var laserTimer by remember { mutableFloatStateOf(0f) }
    var initialized by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    var scoreRecorded by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(0) }
    var countdownKey by remember { mutableIntStateOf(0) }
    var continuesUsed by remember { mutableIntStateOf(0) }
    var continueOffer by remember { mutableStateOf<ContinueOffer?>(null) }
    var multiplierClaimed by remember { mutableStateOf(false) }
    var multiplierBonus by remember { mutableIntStateOf(0) }
    val bestScores by vm.highScores.collectAsState()
    val context = LocalContext.current
    // Raquette remontée d'un cran fixe (8 mm) par rapport au bord bas : sans ça, le doigt qui la
    // déplace (axe horizontal uniquement) se pose naturellement dessus et la cache.
    val density = LocalDensity.current.density
    val paddleLift = remember(density) { (8f / 25.4f) * 160f * density }
    val isPremium by vm.isPremiumAllAccess.collectAsState()
    val adsEnabled = !vm.isAdsReduced.collectAsState().value && !isPremium

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

    fun ballSpeed(size: IntSize): Float =
        (size.height * (0.40f + 0.035f * (levelNum - 1))).coerceAtMost(size.height * 0.62f)

    /** Nouvelle balle collée sur la raquette (relâchée d'un tap). */
    fun spawnStuckBall(size: IntSize) {
        balls = listOf(Ball(paddleX, size.height * 0.86f - paddleLift - size.width * 0.03f, 0f, 0f, stuck = true))
    }

    fun startLevel(size: IntSize) {
        bricks = List(ROWS * COLS) { true }
        bonuses = emptyList()
        lasers = emptyList()
        enlargeTimer = 0f; glueTimer = 0f; laserTimer = 0f
        spawnStuckBall(size)
    }

    fun resetGame(size: IntSize) {
        if (size.width <= 0 || size.height <= 0) return
        paddleX = size.width / 2f
        score = 0
        lives = LIVES_START
        levelNum = 1
        levelsWon = 0
        continuesUsed = 0
        continueOffer = null
        multiplierClaimed = false
        multiplierBonus = 0
        gameOver = false
        scoreRecorded = false
        startLevel(size)
        initialized = true
        countdownKey++ // décompte 3-2-1 avant de (re)lancer la partie
    }

    LaunchedEffect(canvasSize) {
        if (!initialized) resetGame(canvasSize)
    }

    LaunchedEffect(gameOver) {
        if (gameOver && !scoreRecorded) {
            scoreRecorded = true
            vm.recordHighScore(GameShopCatalog.ARKANOID, score)
            GameFx.gameOver(context)
            GameAds.onGameOver(context, adsEnabled)
        }
    }

    // Décompte 3-2-1-GO ! après une vie perdue, avant de reprendre le niveau.
    LaunchedEffect(countdownKey) {
        if (countdownKey == 0) return@LaunchedEffect
        countdown = 3; delay(600)
        countdown = 2; delay(600)
        countdown = 1; delay(600)
        countdown = -1; delay(450)
        countdown = 0
    }

    fun releaseStuckBalls(size: IntSize) {
        val speed = ballSpeed(size)
        balls = balls.map { b ->
            if (b.stuck) {
                val angle = (b.stuckOffset / (size.width * 0.18f)).coerceIn(-0.6f, 0.6f)
                Ball(b.x, b.y, speed * angle, -speed, stuck = false)
            } else b
        }
    }

    fun applyBonus(type: BonusType, size: IntSize) {
        when (type) {
            BonusType.ENLARGE -> enlargeTimer = 20f
            BonusType.GLUE -> glueTimer = 15f
            BonusType.LASER -> laserTimer = 12f
            BonusType.LIFE -> lives = (lives + 1).coerceAtMost(MAX_LIVES)
            BonusType.SLOW -> balls = balls.map {
                if (it.stuck) it else it.copy(vx = it.vx * 0.72f, vy = it.vy * 0.72f)
            }
            BonusType.TRIPLE -> {
                val source = balls.firstOrNull { !it.stuck } ?: return
                val extra = listOf(-0.45f, 0.45f).map { spread ->
                    // Rotation approximative du vecteur vitesse pour écarter les balles.
                    source.copy(vx = source.vx + source.vy * spread, vy = source.vy - source.vx * spread)
                }
                balls = (balls + extra).take(MAX_BALLS)
            }
        }
    }

    GameLoop(running = initialized && !gameOver && countdown == 0 && continueOffer == null) { dt ->
        val size = canvasSize
        val w = size.width.toFloat()
        val h = size.height.toFloat()
        val ballRadius = w * 0.018f
        // Raquette remontée (~1 cm au-dessus du bord bas selon les écrans, + le décalage fixe).
        val paddleY = h * 0.86f - paddleLift
        val paddleHalfWidth = w * (if (enlargeTimer > 0f) 0.18f else 0.12f)
        val paddleHalfThickness = h * 0.012f
        val brickTop = h * 0.06f
        val brickAreaHeight = h * 0.28f
        val brickW = w / COLS
        val brickH = brickAreaHeight / ROWS

        enlargeTimer = (enlargeTimer - dt).coerceAtLeast(0f)
        glueTimer = (glueTimer - dt).coerceAtLeast(0f)
        laserTimer = (laserTimer - dt).coerceAtLeast(0f)

        var newScore = score
        var mutBricks: MutableList<Boolean>? = null
        var newBonuses = bonuses

        fun destroyBrick(idx: Int, brickCol: Int, brickRow: Int) {
            val list = mutBricks ?: bricks.toMutableList().also { mutBricks = it }
            if (!list[idx]) return
            list[idx] = false
            newScore += SCORE_PER_BRICK
            GameFx.brick()
            if (Random.nextFloat() < BONUS_DROP_CHANCE) {
                newBonuses = newBonuses + FallingBonus(
                    x = brickCol * brickW + brickW / 2f,
                    y = brickTop + brickRow * brickH + brickH / 2f,
                    type = BonusType.entries.random()
                )
            }
        }

        // --- Balles ---
        val survivors = mutableListOf<Ball>()
        for (b in balls) {
            if (b.stuck) {
                survivors.add(b.copy(x = paddleX + b.stuckOffset, y = paddleY - paddleHalfThickness - ballRadius))
                continue
            }
            var x = b.x + b.vx * dt
            var y = b.y + b.vy * dt
            var vx = b.vx
            var vy = b.vy
            var stuck = false
            var stuckOffset = 0f

            if (x - ballRadius < 0f) { x = ballRadius; vx = abs(vx) }
            if (x + ballRadius > w) { x = w - ballRadius; vx = -abs(vx) }
            if (y - ballRadius < 0f) { y = ballRadius; vy = abs(vy) }

            // Raquette.
            if (vy > 0 &&
                y + ballRadius >= paddleY - paddleHalfThickness &&
                y - ballRadius <= paddleY + paddleHalfThickness &&
                abs(x - paddleX) <= paddleHalfWidth
            ) {
                y = paddleY - paddleHalfThickness - ballRadius
                if (glueTimer > 0f) {
                    stuck = true
                    stuckOffset = x - paddleX
                    vx = 0f; vy = 0f
                } else {
                    vy = -abs(vy)
                    vx += (x - paddleX) * 2.2f
                    GameFx.pong()
                }
            }

            // Briques.
            if (y - ballRadius < brickTop + brickAreaHeight && y + ballRadius > brickTop) {
                val col = (x / brickW).toInt().coerceIn(0, COLS - 1)
                val row = ((y - brickTop) / brickH).toInt()
                if (row in 0 until ROWS) {
                    val idx = row * COLS + col
                    val current = mutBricks ?: bricks
                    if (current[idx]) {
                        destroyBrick(idx, col, row)
                        vy = -vy
                    }
                }
            }

            if (y - ballRadius <= h) {
                survivors.add(Ball(x, y, vx, vy, stuck, stuckOffset))
            }
        }
        balls = survivors

        // --- Lasers ---
        if (lasers.isNotEmpty()) {
            val remaining = mutableListOf<LaserShot>()
            for (shot in lasers) {
                val ny = shot.y - h * 0.9f * dt
                if (ny < 0f) continue
                var absorbed = false
                if (ny < brickTop + brickAreaHeight && ny > brickTop) {
                    val col = (shot.x / brickW).toInt().coerceIn(0, COLS - 1)
                    val row = ((ny - brickTop) / brickH).toInt()
                    if (row in 0 until ROWS) {
                        val idx = row * COLS + col
                        val current = mutBricks ?: bricks
                        if (current[idx]) {
                            destroyBrick(idx, col, row)
                            absorbed = true
                        }
                    }
                }
                if (!absorbed) remaining.add(LaserShot(shot.x, ny))
            }
            lasers = remaining
        }

        // --- Bonus qui tombent ---
        if (newBonuses.isNotEmpty()) {
            val kept = mutableListOf<FallingBonus>()
            for (bonus in newBonuses) {
                val ny = bonus.y + h * 0.25f * dt
                val caught = ny >= paddleY - paddleHalfThickness - h * 0.012f &&
                    ny <= paddleY + paddleHalfThickness + h * 0.012f &&
                    abs(bonus.x - paddleX) <= paddleHalfWidth
                when {
                    caught -> { applyBonus(bonus.type, size); GameFx.eat() }
                    ny < h -> kept.add(bonus.copy(y = ny))
                }
            }
            newBonuses = kept
        }
        bonuses = newBonuses
        mutBricks?.let { bricks = it }
        score = newScore

        // Tableau nettoyé = partie gagnée : +10 points, difficulté suivante.
        if (bricks.none { it }) {
            levelsWon++
            vm.earnPoints(POINTS_PER_LEVEL_WON)
            GameFx.win()
            levelNum++
            GameAds.onLevelCleared()
            startLevel(size)
        }

        // Toutes les balles perdues : perdre une vie recommence le niveau en cours (briques
        // remises à plein, bonus/timers réinitialisés).
        if (balls.isEmpty()) {
            lives--
            if (lives <= 0) {
                offerContinueOrEnd(onGranted = { lives = 1; startLevel(size); GameFx.loseLife(context); countdownKey++ })
            } else {
                startLevel(size); GameFx.loseLife(context); countdownKey++
            }
        }
    }

    GameScaffold(
        title = "Casse-Briques — Niv. $levelNum — ❤ $lives",
        score = score,
        onExit = onExit,
        gameOver = gameOver,
        pointsEarned = levelsWon * POINTS_PER_LEVEL_WON + multiplierBonus,
        bestScore = bestScores[GameShopCatalog.ARKANOID] ?: 0,
        onRestart = { resetGame(canvasSize) },
        continueOffer = continueOffer,
        onWatchAdForMultiplier = if (BuildConfig.ADS_ENABLED && adsEnabled && !multiplierClaimed && levelsWon * POINTS_PER_LEVEL_WON > 0) {
            {
                watchRewardedAd(context, onRewarded = {
                    val bonus = levelsWon * POINTS_PER_LEVEL_WON * 2
                    vm.earnPoints(bonus)
                    multiplierBonus += bonus
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
                    detectDragGestures { change, _ ->
                        change.consume()
                        paddleX = change.position.x.coerceIn(0f, size.width.toFloat())
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures {
                        val s = canvasSize
                        if (balls.any { it.stuck }) releaseStuckBalls(s)
                        else if (laserTimer > 0f) {
                            val halfW = s.width * (if (enlargeTimer > 0f) 0.18f else 0.12f)
                            val y = s.height * 0.86f - paddleLift
                            lasers = lasers + LaserShot(paddleX - halfW * 0.8f, y) + LaserShot(paddleX + halfW * 0.8f, y)
                        }
                    }
                }
        ) {
            drawRect(color = Color(0xFF0B0B14))
            val w = size.width
            val h = size.height
            val brickTop = h * 0.06f
            val brickAreaHeight = h * 0.28f
            val brickW = w / COLS
            val brickH = brickAreaHeight / ROWS
            val gap = 3f
            bricks.forEachIndexed { idx, alive ->
                if (alive) {
                    val row = idx / COLS
                    val col = idx % COLS
                    drawRoundRect(
                        color = rowColors[row % rowColors.size],
                        topLeft = Offset(col * brickW + gap, brickTop + row * brickH + gap),
                        size = Size(brickW - 2 * gap, brickH - 2 * gap)
                    )
                }
            }
            // Bonus : capsules colorées avec leur lettre (comme le vrai Arkanoid).
            bonuses.forEach { bonus ->
                drawCircle(color = bonus.type.color, radius = w * 0.028f, center = Offset(bonus.x, bonus.y))
                drawCircle(color = Color.Black.copy(alpha = 0.35f), radius = w * 0.017f, center = Offset(bonus.x, bonus.y))
            }
            lasers.forEach { shot ->
                drawLine(
                    color = NeonPink,
                    start = Offset(shot.x, shot.y),
                    end = Offset(shot.x, shot.y + h * 0.02f),
                    strokeWidth = 6f
                )
            }
            val paddleHalfWidth = w * (if (enlargeTimer > 0f) 0.18f else 0.12f)
            val paddleHeight = h * 0.018f
            val paddleY = h * 0.86f - paddleLift
            drawRoundRect(
                color = if (laserTimer > 0f) NeonPink else NeonCyan,
                topLeft = Offset(paddleX - paddleHalfWidth, paddleY - paddleHeight / 2f),
                size = Size(paddleHalfWidth * 2f, paddleHeight)
            )
            balls.forEach { b ->
                drawCircle(color = Color.White, radius = w * 0.018f, center = Offset(b.x, b.y))
            }
        }
        CountdownOverlay(countdown)
    }
}
