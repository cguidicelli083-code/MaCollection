package com.example.macollection.ui.games.bombhunter

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import com.example.macollection.BuildConfig
import com.example.macollection.ui.GameViewModel
import com.example.macollection.ui.ads.GameAds
import com.example.macollection.ui.ads.watchRewardedAd
import com.example.macollection.ui.games.engine.ContinueOffer
import com.example.macollection.ui.games.engine.CountdownOverlay
import com.example.macollection.ui.games.engine.GameFx
import com.example.macollection.ui.games.engine.GameLoop
import com.example.macollection.ui.games.engine.GameScaffold
import kotlinx.coroutines.delay
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

/** Identifiant du jeu (high-score / déblocage) — voir GameShopCatalog pour l'intégration finale. */
const val BOMB_HUNTER_GAME_ID = "bomb_hunter"

private const val LIVES_START = 3
private const val MAX_CONTINUES = 3
private const val BOMB_SCORE = 30
private const val POINTS_PER_LEVEL = 10
// Toutes les valeurs de vitesse/physique sont exprimées en fractions de la largeur/hauteur du
// Canvas par seconde (comme Pong/Casse-Briques) : indépendant de la résolution de l'écran.
private const val GRAVITY = 1.15f          // hauteurs d'écran / s²
private const val JUMP_IMPULSE = 0.58f     // hauteur d'écran / s (vers le haut)
private const val MAX_FALL_SPEED = 0.62f   // vitesse de chute plafonnée (vol plané façon Artificier)
private const val MOVE_SPEED = 0.42f       // largeurs d'écran / s
private const val PLAYER_RADIUS = 0.032f   // fraction de la largeur
private const val DOUBLE_TAP_WINDOW_MS = 300L // délai max entre 2 frappes pour déclencher un saut

private val PlatformColor = Color(0xFF3B3B55)
private val BombColor = Color(0xFFECEFF4)
private val BombFuse = Color(0xFFFF4D4D)
private val PlayerColor = Color(0xFFFFD400)
private val PlayerVisor = Color(0xFF1A1A3A)
private val EnemyColors = listOf(Color(0xFFFF3D9A), Color(0xFF22E6FF), Color(0xFF8B5CFF))

private enum class HDir { LEFT, RIGHT, NONE }

/** Plateforme fixe (fractions 0..1 du Canvas) : même agencement à chaque niveau (pyramide de
 *  paliers), pour garantir que tout est toujours atteignable en voletant (tapoter = petite
 *  impulsion vers le haut, répétable en l'air à volonté — vol plané façon Artificier). */
private data class Platform(val xMin: Float, val xMax: Float, val y: Float)

private val PLATFORMS = listOf(
    Platform(0.05f, 0.95f, 0.92f),
    Platform(0.05f, 0.32f, 0.74f),
    Platform(0.68f, 0.95f, 0.74f),
    Platform(0.36f, 0.64f, 0.57f),
    Platform(0.08f, 0.34f, 0.38f),
    Platform(0.66f, 0.92f, 0.38f),
    Platform(0.40f, 0.60f, 0.20f)
)

private data class Bomb(val x: Float, val y: Float, val id: Int)
private data class Enemy(var x: Float, val y: Float, val speed: Float, val phase: Float, val color: Color)

/**
 * Artificier : plateformes fixes disposées en pyramide, semées de bombes à désamorcer. Chaque
 * frappe sur l'écran donne une petite impulsion vers le haut (répétable à volonté en l'air, façon
 * vol plané) et oriente le déplacement horizontal vers le côté touché tant que le doigt reste posé.
 * Ramasse toutes les bombes pour passer au niveau suivant ; les gardiens qui patrouillent en l'air
 * font perdre une vie au contact (aucun moyen de les éliminer, seulement de les éviter). Sortir
 * d'un bord de l'écran fait réapparaître de l'autre côté.
 */
@Composable
fun BombHunterScreen(vm: GameViewModel, onExit: () -> Unit) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    var px by remember { mutableFloatStateOf(0.5f) }
    var py by remember { mutableFloatStateOf(0.85f) }
    var vy by remember { mutableFloatStateOf(0f) }
    var moveDir by remember { mutableStateOf(HDir.NONE) }
    var bombs by remember { mutableStateOf<List<Bomb>>(emptyList()) }
    var enemies by remember { mutableStateOf<List<Enemy>>(emptyList()) }
    var animTime by remember { mutableFloatStateOf(0f) }
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(LIVES_START) }
    var level by remember { mutableIntStateOf(1) }
    var levelsWon by remember { mutableIntStateOf(0) }
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
    val isPremium by vm.isPremiumAllAccess.collectAsState()
    val adsEnabled = !vm.isAdsReduced.collectAsState().value && !isPremium

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
            message = "Touché par un gardien ! Regarder une pub pour continuer avec une vie supplémentaire ?",
            buttonLabel = "🎬 Regarder une pub pour continuer",
            onAccept = {
                continueOffer = null
                watchRewardedAd(context, onRewarded = { continuesUsed++; onGranted() }, onClosed = { gameOver = true })
            },
            onDecline = { continueOffer = null; gameOver = true }
        )
    }

    fun spawnLevel(lvl: Int) {
        val rnd = Random(System.nanoTime())
        val bombList = ArrayList<Bomb>()
        var id = 0
        PLATFORMS.forEach { plat ->
            val count = 2 + (lvl % 2)
            for (i in 0 until count) {
                val t = (i + 1f) / (count + 1f)
                bombList.add(Bomb(plat.xMin + (plat.xMax - plat.xMin) * t, plat.y - 0.05f, id++))
            }
        }
        // Bombes flottantes entre les paliers, accessibles en voletant.
        repeat((4 + lvl / 2).coerceAtMost(10)) {
            bombList.add(Bomb(0.15f + rnd.nextFloat() * 0.7f, 0.28f + rnd.nextFloat() * 0.55f, id++))
        }
        bombs = bombList
        val enemyCount = (2 + lvl).coerceAtMost(6)
        val bands = listOf(0.30f, 0.48f, 0.66f, 0.84f)
        enemies = (0 until enemyCount).map { i ->
            Enemy(
                x = rnd.nextFloat(),
                y = bands[i % bands.size],
                speed = (0.12f + 0.02f * lvl) * (if (i % 2 == 0) 1f else -1f),
                phase = rnd.nextFloat() * 6.28f,
                color = EnemyColors[i % EnemyColors.size]
            )
        }
    }

    fun resetGame() {
        level = 1
        px = 0.5f; py = 0.85f; vy = 0f
        moveDir = HDir.NONE
        animTime = 0f
        score = 0
        lives = LIVES_START
        levelsWon = 0
        continuesUsed = 0
        continueOffer = null
        multiplierClaimed = false
        multiplierBonus = 0
        gameOver = false
        scoreRecorded = false
        spawnLevel(level)
        initialized = true
        countdownKey++
    }

    fun respawnAfterHit() {
        px = 0.5f; py = 0.85f; vy = 0f
        moveDir = HDir.NONE
        GameFx.loseLife(context)
        countdownKey++
    }

    LaunchedEffect(canvasSize) {
        if (!initialized && canvasSize.width > 0) resetGame()
    }

    LaunchedEffect(gameOver) {
        if (gameOver && !scoreRecorded) {
            scoreRecorded = true
            vm.recordHighScore(BOMB_HUNTER_GAME_ID, score)
            GameFx.gameOver(context)
            GameAds.onGameOver(context, adsEnabled)
        }
    }

    LaunchedEffect(countdownKey) {
        if (countdownKey == 0) return@LaunchedEffect
        countdown = 3; delay(600)
        countdown = 2; delay(600)
        countdown = 1; delay(600)
        countdown = -1; delay(450)
        countdown = 0
    }

    GameLoop(running = initialized && !gameOver && countdown == 0 && continueOffer == null) { dt ->
        animTime += dt

        // --- Déplacement horizontal + réapparition de l'autre côté en sortant de l'écran. ---
        when (moveDir) {
            HDir.LEFT -> px -= MOVE_SPEED * dt
            HDir.RIGHT -> px += MOVE_SPEED * dt
            HDir.NONE -> {}
        }
        if (px < 0f) px += 1f
        if (px > 1f) px -= 1f

        // --- Gravité plafonnée (vol plané) + collision avec le dessus des plateformes. ---
        val prevBottom = py + PLAYER_RADIUS
        vy = (vy + GRAVITY * dt).coerceAtMost(MAX_FALL_SPEED)
        py += vy * dt
        val newBottom = py + PLAYER_RADIUS
        if (vy > 0f) {
            for (plat in PLATFORMS) {
                if (px in plat.xMin..plat.xMax && prevBottom <= plat.y && newBottom >= plat.y) {
                    py = plat.y - PLAYER_RADIUS
                    vy = 0f
                    break
                }
            }
        }
        if (py - PLAYER_RADIUS < 0f) { py = PLAYER_RADIUS; vy = 0f }
        // Filet de sécurité : la plateforme du bas laisse une marge de 5% de chaque côté (xMin=0.05,
        // xMax=0.95) sans rien en dessous. Sans cette limite, un joueur qui tombe dans cette marge
        // (ou pile après un franchissement de bord) continuait de chuter sous l'écran indéfiniment
        // (invisible, remontable seulement en sautant à l'aveugle) faute de toute autre plateforme
        // pour l'arrêter.
        if (py + PLAYER_RADIUS > 1f) { py = 1f - PLAYER_RADIUS; vy = 0f }

        // --- Gardiens : patrouille horizontale avec un léger flottement vertical sinusoïdal. ---
        enemies = enemies.map { e ->
            var nx = e.x + e.speed * dt
            if (nx < -0.05f) nx = 1.05f
            if (nx > 1.05f) nx = -0.05f
            e.copy(x = nx)
        }

        // --- Bombes ramassées. ---
        val playerR2 = PLAYER_RADIUS * 1.6f
        val remaining = bombs.filter { b -> hypot((b.x - px).toDouble(), (b.y - py).toDouble()) > playerR2 }
        if (remaining.size != bombs.size) {
            score += (bombs.size - remaining.size) * BOMB_SCORE
            GameFx.eat()
            bombs = remaining
        }

        // --- Collision avec un gardien. ---
        val hitBy = enemies.firstOrNull { e ->
            val ey = e.y + 0.02f * sin(animTime * 3f + e.phase)
            hypot((e.x - px).toDouble(), (ey - py).toDouble()) < PLAYER_RADIUS * 1.5
        }
        if (hitBy != null) {
            lives--
            if (lives <= 0) {
                offerContinueOrEnd(onGranted = { lives = 1; respawnAfterHit() })
                return@GameLoop
            }
            respawnAfterHit()
            return@GameLoop
        }

        // --- Niveau terminé : toutes les bombes désamorcées. ---
        if (bombs.isEmpty()) {
            levelsWon++
            vm.earnPoints(POINTS_PER_LEVEL)
            GameFx.win()
            level++
            GameAds.onLevelCleared()
            px = 0.5f; py = 0.85f; vy = 0f
            moveDir = HDir.NONE
            spawnLevel(level)
        }
    }

    GameScaffold(
        title = "Artificier — Niv. $level — ❤ $lives",
        score = score,
        onExit = onExit,
        gameOver = gameOver,
        pointsEarned = levelsWon * POINTS_PER_LEVEL + multiplierBonus,
        bestScore = bestScores[BOMB_HUNTER_GAME_ID] ?: 0,
        onRestart = { resetGame() },
        continueOffer = continueOffer,
        onWatchAdForMultiplier = if (BuildConfig.ADS_ENABLED && adsEnabled && !multiplierClaimed && levelsWon * POINTS_PER_LEVEL > 0) {
            {
                watchRewardedAd(context, onRewarded = {
                    val bonus = levelsWon * POINTS_PER_LEVEL * 2
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
                    // Seul un DOUBLE tap (2 appuis rapprochés de moins de DOUBLE_TAP_WINDOW_MS)
                    // déclenche un saut ; un simple appui ne fait qu'orienter le déplacement
                    // horizontal (comme avant). `lastTapUptime` vit dans cette coroutine (pas un
                    // état Compose) : awaitEachGesture boucle indéfiniment sur de nouveaux gestes
                    // sans jamais quitter ce bloc, donc la variable survit d'un appui à l'autre.
                    var lastTapUptime = 0L
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        if (down.uptimeMillis - lastTapUptime <= DOUBLE_TAP_WINDOW_MS) {
                            vy = -JUMP_IMPULSE
                            GameFx.hop()
                            lastTapUptime = 0L
                        } else {
                            lastTapUptime = down.uptimeMillis
                        }
                        moveDir = if (down.position.x < size.width / 2f) HDir.LEFT else HDir.RIGHT
                        drag(down.id) { change ->
                            moveDir = if (change.position.x < size.width / 2f) HDir.LEFT else HDir.RIGHT
                            change.consume()
                        }
                        moveDir = HDir.NONE
                    }
                }
        ) {
            drawRect(color = Color(0xFF0B0B18))
            val w = size.width
            val h = size.height

            PLATFORMS.forEach { plat ->
                drawRoundRect(
                    color = PlatformColor,
                    topLeft = Offset(plat.xMin * w, plat.y * h),
                    size = Size((plat.xMax - plat.xMin) * w, h * 0.025f),
                    cornerRadius = CornerRadius(h * 0.01f, h * 0.01f)
                )
            }

            bombs.forEach { b ->
                val bx = b.x * w; val by = b.y * h; val r = w * 0.024f
                drawCircle(BombColor, r, Offset(bx, by))
                drawCircle(BombFuse, r * 0.35f, Offset(bx, by - r * 0.9f))
            }

            enemies.forEach { e ->
                val ex = e.x * w
                val ey = (e.y + 0.02f * sin(animTime * 3f + e.phase)) * h
                val er = w * 0.03f
                drawCircle(e.color, er, Offset(ex, ey))
                drawCircle(Color.White, er * 0.35f, Offset(ex - er * 0.3f, ey - er * 0.2f))
                drawCircle(PlayerVisor, er * 0.18f, Offset(ex - er * 0.3f, ey - er * 0.2f))
            }

            // Joueur (casque + visière), léger indicateur de direction.
            val pxPix = px * w; val pyPix = py * h; val pr = w * PLAYER_RADIUS
            drawCircle(PlayerColor, pr, Offset(pxPix, pyPix))
            val visorDx = when (moveDir) { HDir.LEFT -> -pr * 0.3f; HDir.RIGHT -> pr * 0.3f; HDir.NONE -> 0f }
            drawRoundRect(
                color = PlayerVisor,
                topLeft = Offset(pxPix - pr * 0.35f + visorDx, pyPix - pr * 0.15f),
                size = Size(pr * 0.7f, pr * 0.5f),
                cornerRadius = CornerRadius(pr * 0.15f, pr * 0.15f)
            )
        }
        CountdownOverlay(countdown)
    }
}
