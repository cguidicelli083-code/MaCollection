package com.example.macollection.ui.games.pong

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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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

private const val WINNING_SCORE_START = 10
// 3 pts par but marqué : un match gagné (10 buts) rapporte 30 points au lieu de 100, pour garder
// une progression lente des déblocages (récompenses gagnées petit à petit).
private const val POINTS_PER_PLAYER_GOAL = 3
// Quand l'IA gagne : jusqu'à 2 pubs pour repousser l'objectif de 5 points (10 -> 15 -> 20), puis
// fin de partie définitive à la 3e défaite.
private const val MAX_CONTINUES = 2
private const val SCORE_STEP = 5

/**
 * Pong solo contre une IA. La raquette du joueur se déplace librement (glisser au doigt) dans
 * toute sa moitié de terrain, jusqu'à la ligne médiane — avancer permet de smasher mais laisse
 * le but découvert. Un déplacement latéral rapide au moment de l'impact « lifte » la balle :
 * l'effet incurve sa trajectoire (comme un vrai lift au tennis de table).
 */
@Composable
fun PongScreen(vm: GameViewModel, onExit: () -> Unit) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    var ballX by remember { mutableFloatStateOf(0f) }
    var ballY by remember { mutableFloatStateOf(0f) }
    var ballVX by remember { mutableFloatStateOf(0f) }
    var ballVY by remember { mutableFloatStateOf(0f) }
    // Effet (lift) : courbe la trajectoire de la balle, s'estompe progressivement.
    var ballSpin by remember { mutableFloatStateOf(0f) }
    var playerX by remember { mutableFloatStateOf(0f) }
    var playerY by remember { mutableFloatStateOf(0f) }
    // Vitesse latérale de la raquette (pour transmettre l'effet à l'impact), décroît seule.
    var paddleVX by remember { mutableFloatStateOf(0f) }
    var aiX by remember { mutableFloatStateOf(0f) }
    // Position en profondeur de l'IA (comme playerY pour le joueur) : elle peut avancer/reculer.
    var aiY by remember { mutableFloatStateOf(0f) }
    var playerScore by remember { mutableIntStateOf(0) }
    var aiScore by remember { mutableIntStateOf(0) }
    var winningScore by remember { mutableIntStateOf(WINNING_SCORE_START) }
    var continuesUsed by remember { mutableIntStateOf(0) }
    var continueOffer by remember { mutableStateOf<ContinueOffer?>(null) }
    var initialized by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    var pointsAwarded by remember { mutableStateOf(false) }
    // Pause entre deux points : la balle est figée au centre 1 s, puis « GO ! », puis remise en jeu.
    var betweenPoints by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(0) } // 0 = rien, -1 = « GO ! »
    var serveKey by remember { mutableIntStateOf(0) }
    var serveTowardPlayer by remember { mutableStateOf(true) }
    val bestScores by vm.highScores.collectAsState()
    val isPremium by vm.isPremiumAllAccess.collectAsState()
    val context = LocalContext.current
    // Décalage du doigt sous la raquette (8 mm) : sans lui, le doigt se pose pile sur la raquette
    // et la cache complètement pendant qu'on la déplace.
    val density = LocalDensity.current.density
    val fingerOffsetPx = remember(density) { (8f / 25.4f) * 160f * density }

    /** Propose une pub pour repousser l'objectif de [SCORE_STEP] points, sinon termine la partie. */
    fun offerContinueOrEnd(message: String, onGranted: () -> Unit) {
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
            message = message,
            buttonLabel = "🎬 Regarder une pub pour continuer",
            onAccept = {
                continueOffer = null
                watchRewardedAd(context, onRewarded = { continuesUsed++; onGranted() }, onClosed = { gameOver = true })
            },
            onDecline = { continueOffer = null; gameOver = true }
        )
    }

    fun resetBall(size: IntSize, towardPlayer: Boolean) {
        ballX = size.width / 2f
        ballY = size.height / 2f
        ballSpin = 0f
        val speed = size.height * 0.5f
        val angleFactor = Random.nextFloat() * 0.6f - 0.3f
        ballVX = speed * angleFactor
        ballVY = if (towardPlayer) speed else -speed
    }

    // Prépare un service : balle figée au centre, puis la séquence pause + « GO ! » la relance.
    fun scheduleServe(size: IntSize, towardPlayer: Boolean) {
        ballX = size.width / 2f
        ballY = size.height / 2f
        ballVX = 0f; ballVY = 0f; ballSpin = 0f
        serveTowardPlayer = towardPlayer
        betweenPoints = true
        serveKey++
    }

    fun resetGame(size: IntSize) {
        if (size.width <= 0 || size.height <= 0) return
        playerX = size.width / 2f
        playerY = size.height * 0.92f
        aiX = size.width / 2f
        aiY = size.height * 0.08f
        paddleVX = 0f
        playerScore = 0
        aiScore = 0
        winningScore = WINNING_SCORE_START
        continuesUsed = 0
        continueOffer = null
        gameOver = false
        pointsAwarded = false
        scheduleServe(size, Random.nextBoolean())
        initialized = true
    }

    LaunchedEffect(canvasSize) {
        if (!initialized) resetGame(canvasSize)
    }

    // Séquence de service : 1 s de pause, puis « GO ! » (0,5 s), puis la balle est lancée.
    LaunchedEffect(serveKey) {
        if (serveKey == 0) return@LaunchedEffect
        delay(1000)
        countdown = -1
        delay(500)
        countdown = 0
        resetBall(canvasSize, serveTowardPlayer)
        betweenPoints = false
    }

    val pointsEarned = playerScore * POINTS_PER_PLAYER_GOAL

    LaunchedEffect(gameOver) {
        if (gameOver && !pointsAwarded) {
            pointsAwarded = true
            vm.earnPoints(pointsEarned)
            vm.recordHighScore(GameShopCatalog.PONG, playerScore)
            GameFx.gameOver(context)
        }
    }

    GameLoop(running = initialized && !gameOver && !betweenPoints && continueOffer == null) { dt ->
        val size = canvasSize
        // Raquettes réduites (12% de demi-largeur -> 9%) pour corser le jeu.
        val paddleHalfWidth = size.width * 0.09f
        val ballRadius = size.width * 0.02f
        val paddleHalfThickness = size.height * 0.015f

        // L'effet incurve la trajectoire, puis s'estompe (frottement de l'air).
        ballVX += ballSpin * dt
        ballSpin *= (1f - (1.2f * dt)).coerceAtLeast(0f)
        paddleVX *= (1f - (6f * dt)).coerceAtLeast(0f)

        ballX += ballVX * dt
        ballY += ballVY * dt

        if (ballX - ballRadius < 0f) { ballX = ballRadius; ballVX = abs(ballVX); ballSpin *= 0.5f; GameFx.wallBounce() }
        if (ballX + ballRadius > size.width) { ballX = size.width - ballRadius; ballVX = -abs(ballVX); ballSpin *= 0.5f; GameFx.wallBounce() }

        // IA : suit la balle latéralement (vitesse plafonnée, réduite pour rester accessible/battable).
        val aiMaxSpeed = size.width * 0.55f
        val aiDelta = (ballX - aiX).coerceIn(-aiMaxSpeed * dt, aiMaxSpeed * dt)
        aiX = (aiX + aiDelta).coerceIn(paddleHalfWidth, size.width - paddleHalfWidth)
        // ... et se déplace aussi en profondeur, comme le joueur : elle AVANCE vers la balle pour la
        // renvoyer plus tôt (smash) quand celle-ci vient vers elle, et RECULE garder son but sinon.
        val aiMinY = size.height * 0.04f
        val aiMaxY = size.height * 0.46f  // ne franchit pas la ligne médiane
        val aiGuardY = size.height * 0.08f
        val aiTargetY = if (ballVY < 0f) ballY.coerceIn(aiMinY, aiMaxY) else aiGuardY
        val aiMaxVSpeed = size.height * 0.5f
        val aiVDelta = (aiTargetY - aiY).coerceIn(-aiMaxVSpeed * dt, aiMaxVSpeed * dt)
        aiY = (aiY + aiVDelta).coerceIn(aiMinY, aiMaxY)

        // Collision raquette joueur (mobile dans toute la moitié basse).
        if (ballVY > 0 &&
            ballY + ballRadius >= playerY - paddleHalfThickness &&
            ballY - ballRadius <= playerY + paddleHalfThickness &&
            abs(ballX - playerX) <= paddleHalfWidth
        ) {
            ballY = playerY - paddleHalfThickness - ballRadius
            ballVY = -abs(ballVY) * 1.03f
            ballVX += (ballX - playerX) * 1.5f
            // Lift : la vitesse latérale de la raquette à l'impact devient de l'effet.
            ballSpin += paddleVX * 2.5f
            GameFx.pong()
        }

        // Collision raquette IA (mobile dans toute la moitié haute).
        if (ballVY < 0 &&
            ballY - ballRadius <= aiY + paddleHalfThickness &&
            ballY + ballRadius >= aiY - paddleHalfThickness &&
            abs(ballX - aiX) <= paddleHalfWidth
        ) {
            ballY = aiY + paddleHalfThickness + ballRadius
            ballVY = abs(ballVY) * 1.03f
            ballVX += (ballX - aiX) * 1.5f
            GameFx.pong()
        }

        // But marqué : pause d'1 s + « GO ! » avant la remise en jeu (voir scheduleServe).
        if (ballY - ballRadius > size.height) {
            aiScore++
            GameFx.losePoint()
            if (aiScore >= winningScore) {
                val nextTarget = winningScore + SCORE_STEP
                offerContinueOrEnd(
                    message = "L'IA mène $aiScore à $playerScore. Continuer jusqu'à $nextTarget points ?",
                    onGranted = { winningScore = nextTarget; scheduleServe(size, towardPlayer = false) }
                )
            } else scheduleServe(size, towardPlayer = false)
        } else if (ballY + ballRadius < 0f) {
            playerScore++
            GameFx.scorePoint()
            if (playerScore >= winningScore) gameOver = true else scheduleServe(size, towardPlayer = true)
        }
    }

    GameScaffold(
        title = "Tennis Rétro — Toi $playerScore · IA $aiScore",
        score = playerScore,
        onExit = onExit,
        gameOver = gameOver,
        pointsEarned = pointsEarned,
        bestScore = bestScores[GameShopCatalog.PONG] ?: 0,
        onRestart = { resetGame(canvasSize) },
        continueOffer = continueOffer
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = it }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        playerX = change.position.x.coerceIn(0f, size.width.toFloat())
                        // Peut avancer jusqu'à juste sous la ligne médiane.
                        playerY = (change.position.y - fingerOffsetPx).coerceIn(size.height * 0.52f, size.height * 0.96f)
                        // ~vitesse latérale instantanée (les événements arrivent à ~60-120 Hz).
                        paddleVX = dragAmount.x * 60f
                    }
                }
        ) {
            drawRect(color = Color(0xFF0B0B14))
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = 4f
            )
            // Grands scores façon Pong : IA en haut, joueur en bas (premier à 10 gagne).
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(55, 255, 255, 255)
                    textSize = size.width * 0.16f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                }
                drawText(aiScore.toString(), size.width / 2f, size.height * 0.30f, paint)
                drawText(playerScore.toString(), size.width / 2f, size.height * 0.74f, paint)
            }
            val paddleHalfWidth = size.width * 0.09f
            val paddleHeight = size.height * 0.02f
            drawRoundRect(
                color = NeonCyan,
                topLeft = Offset(playerX - paddleHalfWidth, playerY - paddleHeight / 2f),
                size = Size(paddleHalfWidth * 2f, paddleHeight)
            )
            drawRoundRect(
                color = NeonPink,
                topLeft = Offset(aiX - paddleHalfWidth, aiY - paddleHeight / 2f),
                size = Size(paddleHalfWidth * 2f, paddleHeight)
            )
            drawCircle(color = Color.White, radius = size.width * 0.02f, center = Offset(ballX, ballY))
        }
        CountdownOverlay(countdown)
    }
}
