package com.example.macollection.ui.games.invaders

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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
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
import com.example.macollection.ui.theme.NeonCyan
import com.example.macollection.ui.theme.NeonPink
import com.example.macollection.ui.theme.NeonPurple
import kotlinx.coroutines.delay
import kotlin.math.abs

private const val ALIEN_COLS = 6
private const val ALIEN_ROWS = 4
private const val LIVES_START = 3
// Une vie supplémentaire offerte contre une pub, jusqu'à 3 fois par partie.
private const val MAX_CONTINUES = 3
private const val SCORE_PER_ALIEN = 10
private const val SCORE_PER_BOSS_HIT = 20
private const val POINTS_PER_WAVE_WON = 10
private const val POINTS_PER_BOSS_WON = 30
private const val BOSS_EVERY = 5
// Le boss est une grille de blocs qui se désintègrent un par un sous les tirs.
private const val BOSS_COLS = 8
private const val BOSS_ROWS = 4

private data class Bullet(val x: Float, val y: Float)

/** Vague-boss toutes les [BOSS_EVERY] vagues (5, 10, 15...). */
private fun isBossWave(wave: Int) = wave % BOSS_EVERY == 0

/**
 * Space Invaders : le canon suit le doigt (glisser) et tire tout seul — accessible pour un
 * joueur occasionnel. Les aliens accélèrent en descendant, balaient jusqu'aux bords réels de la
 * formation (colonnes vivantes) et ripostent. Toutes les 5 vagues, un gros BOSS remplace la
 * formation : il se déplace, tire plusieurs projectiles à la fois, et se détruit petit à petit.
 * Perdre une vie recommence la vague en cours. Vague nettoyée = +10 pts (+30 pour un boss),
 * la suivante est un peu plus rapide (progressif, jouable).
 */
@Composable
fun SpaceInvadersScreen(vm: GameViewModel, onExit: () -> Unit) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    var aliens by remember { mutableStateOf(List(ALIEN_ROWS * ALIEN_COLS) { true }) }
    var formX by remember { mutableFloatStateOf(0f) }
    var formY by remember { mutableFloatStateOf(0f) }
    var formDir by remember { mutableIntStateOf(1) }
    var cannonX by remember { mutableFloatStateOf(0f) }
    var playerBullets by remember { mutableStateOf(listOf<Bullet>()) }
    var alienBullets by remember { mutableStateOf(listOf<Bullet>()) }
    var fireCooldown by remember { mutableFloatStateOf(0f) }
    var alienFireCooldown by remember { mutableFloatStateOf(1.5f) }
    var marchCd by remember { mutableFloatStateOf(0.5f) }
    var marchTick by remember { mutableStateOf(false) }
    // Boss : grille de blocs destructibles (vide hors vague-boss).
    var bossBlocks by remember { mutableStateOf(listOf<Boolean>()) }
    var bossX by remember { mutableFloatStateOf(0f) }
    var bossDir by remember { mutableIntStateOf(1) }
    var bossFireCd by remember { mutableFloatStateOf(1.5f) }
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(LIVES_START) }
    var wave by remember { mutableIntStateOf(1) }
    var wavesWon by remember { mutableIntStateOf(0) }
    var earnedTotal by remember { mutableIntStateOf(0) }
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
    // Canon remonté d'un cran fixe (8 mm) par rapport au bord bas : sans ça, le doigt qui le
    // déplace (axe horizontal uniquement) se pose naturellement dessus et le cache.
    val density = LocalDensity.current.density
    val cannonLift = remember(density) { (8f / 25.4f) * 160f * density }
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

    fun startWave(size: IntSize) {
        playerBullets = emptyList()
        alienBullets = emptyList()
        alienFireCooldown = 1.5f
        cannonX = size.width / 2f
        if (isBossWave(wave)) {
            aliens = List(ALIEN_ROWS * ALIEN_COLS) { false }
            bossBlocks = List(BOSS_ROWS * BOSS_COLS) { true }
            bossX = size.width / 2f
            bossDir = 1
            bossFireCd = 1.5f
        } else {
            aliens = List(ALIEN_ROWS * ALIEN_COLS) { true }
            formX = size.width * 0.08f
            formY = size.height * 0.10f
            formDir = 1
            bossBlocks = emptyList()
        }
    }

    fun resetGame(size: IntSize) {
        if (size.width <= 0 || size.height <= 0) return
        score = 0
        lives = LIVES_START
        wave = 1
        wavesWon = 0
        earnedTotal = 0
        continuesUsed = 0
        continueOffer = null
        multiplierClaimed = false
        gameOver = false
        scoreRecorded = false
        startWave(size)
        initialized = true
        countdownKey++ // décompte 3-2-1 avant de (re)lancer la partie
    }

    LaunchedEffect(canvasSize) {
        if (!initialized) resetGame(canvasSize)
    }

    LaunchedEffect(gameOver) {
        if (gameOver && !scoreRecorded) {
            scoreRecorded = true
            vm.recordHighScore(GameShopCatalog.SPACE_INVADERS, score)
            GameFx.gameOver(context)
            GameAds.onGameOver(context, adsEnabled)
        }
    }

    // Décompte 3-2-1-GO ! après une vie perdue, avant de reprendre la vague.
    LaunchedEffect(countdownKey) {
        if (countdownKey == 0) return@LaunchedEffect
        countdown = 3; delay(600)
        countdown = 2; delay(600)
        countdown = 1; delay(600)
        countdown = -1; delay(450)
        countdown = 0
    }

    GameLoop(running = initialized && !gameOver && countdown == 0 && continueOffer == null) { dt ->
        val size = canvasSize
        val w = size.width.toFloat()
        val h = size.height.toFloat()
        if (w <= 0f || h <= 0f) return@GameLoop
        val cellW = w * 0.13f
        val cellH = h * 0.055f
        val alienHalf = w * 0.045f
        val cannonY = h * 0.86f - cannonLift
        val cannonHalf = w * 0.05f

        // --- Tir automatique du canon (max 2 tirs à l'écran). ---
        fireCooldown -= dt
        if (fireCooldown <= 0f && playerBullets.size < 2) {
            playerBullets = playerBullets + Bullet(cannonX, cannonY - h * 0.02f)
            fireCooldown = 0.45f
        }

        if (isBossWave(wave)) {
            // ===================== BOSS (grille de blocs qui se désintègre) =====================
            val tier = wave / BOSS_EVERY
            val bossHalfW = w * 0.30f
            val bw = (bossHalfW * 2f) / BOSS_COLS
            val bossTop = h * 0.10f
            val bh = h * 0.04f

            // Déplacement latéral (rebond sur les bords).
            val bspeed = w * (0.09f + 0.02f * tier)
            bossX += bossDir * bspeed * dt
            if (bossX + bossHalfW > w - w * 0.04f) { bossX = w - w * 0.04f - bossHalfW; bossDir = -1 }
            if (bossX - bossHalfW < w * 0.04f) { bossX = w * 0.04f + bossHalfW; bossDir = 1 }

            val bossLeft = bossX - bossHalfW
            fun blockCenter(idx: Int): Offset {
                val r = idx / BOSS_COLS
                val c = idx % BOSS_COLS
                return Offset(bossLeft + (c + 0.5f) * bw, bossTop + (r + 0.5f) * bh)
            }

            // Salve : tirs depuis quelques blocs encore vivants, vers le bas.
            bossFireCd -= dt
            val aliveBlocks = bossBlocks.indices.filter { bossBlocks[it] }
            if (bossFireCd <= 0f && aliveBlocks.isNotEmpty()) {
                val n = (3 + tier).coerceAtMost(6)
                val spread = aliveBlocks.shuffled().take(n).map { idx ->
                    Bullet(blockCenter(idx).x, bossTop + BOSS_ROWS * bh)
                }
                alienBullets = alienBullets + spread
                bossFireCd = (1.4f - 0.1f * tier).coerceAtLeast(0.6f)
            }

            // Tirs du joueur : chaque bloc touché est désintégré (un bloc par tir).
            var mutBlocks: MutableList<Boolean>? = null
            val kept = mutableListOf<Bullet>()
            for (b in playerBullets) {
                val ny = b.y - h * 0.8f * dt
                if (ny < 0f) continue
                var hit = false
                val current = mutBlocks ?: bossBlocks
                for (idx in current.indices) {
                    if (!current[idx]) continue
                    val ctr = blockCenter(idx)
                    if (abs(b.x - ctr.x) <= bw / 2f && abs(ny - ctr.y) <= bh / 2f) {
                        val list = mutBlocks ?: bossBlocks.toMutableList().also { mutBlocks = it }
                        list[idx] = false
                        score += SCORE_PER_BOSS_HIT
                        hit = true
                        break
                    }
                }
                if (!hit) kept.add(Bullet(b.x, ny))
            }
            playerBullets = kept
            mutBlocks?.let { bossBlocks = it }
        } else {
            // ===================== ALIENS =====================
            val aliveCount = aliens.count { it }
            val aliveRatio = aliveCount / (ALIEN_ROWS * ALIEN_COLS).toFloat()

            // « Marche » des ennemis : un battement régulier qui accélère au fil des pertes.
            marchCd -= dt
            if (marchCd <= 0f) {
                GameFx.march(marchTick)
                marchTick = !marchTick
                marchCd = (0.5f - 0.35f * (1f - aliveRatio)).coerceAtLeast(0.14f)
            }

            // Déplacement de la formation (accélère quand il reste peu d'aliens + par vague).
            val baseSpeed = w * (0.06f + 0.012f * (wave - 1)).coerceAtMost(0.14f)
            val speed = baseSpeed * (1f + 1.6f * (1f - aliveRatio))
            formX += formDir * speed * dt

            // Rebond sur les extrémités RÉELLES : seules les colonnes encore vivantes comptent,
            // pour que les aliens restants balaient jusqu'aux bords de l'écran.
            val aliveCols = (0 until ALIEN_COLS).filter { col ->
                (0 until ALIEN_ROWS).any { row -> aliens[row * ALIEN_COLS + col] }
            }
            val minCol = aliveCols.minOrNull() ?: 0
            val maxCol = aliveCols.maxOrNull() ?: (ALIEN_COLS - 1)
            if (formDir > 0 && formX + maxCol * cellW > w - w * 0.08f) {
                formDir = -1
                formY += cellH * 0.6f
            } else if (formDir < 0 && formX + minCol * cellW < w * 0.08f) {
                formDir = 1
                formY += cellH * 0.6f
            }

            fun alienCenter(idx: Int): Offset {
                val row = idx / ALIEN_COLS
                val col = idx % ALIEN_COLS
                return Offset(formX + col * cellW, formY + row * cellH)
            }

            // Aliens arrivés au niveau du canon : perte d'une vie + reprise de la vague.
            var reached = false
            aliens.forEachIndexed { idx, alive ->
                if (alive && alienCenter(idx).y + alienHalf >= cannonY - h * 0.02f) reached = true
            }
            if (reached) {
                lives--
                if (lives <= 0) {
                    offerContinueOrEnd(onGranted = { lives = 1; startWave(size); GameFx.loseLife(context); countdownKey++ })
                } else {
                    startWave(size); GameFx.loseLife(context); countdownKey++
                }
                return@GameLoop
            }

            // Riposte alien : un alien vivant (le plus bas de sa colonne) tire vers le bas.
            alienFireCooldown -= dt
            if (alienFireCooldown <= 0f && aliveCount > 0) {
                val col = aliveCols.random()
                val row = (ALIEN_ROWS - 1 downTo 0).first { aliens[it * ALIEN_COLS + col] }
                val from = alienCenter(row * ALIEN_COLS + col)
                alienBullets = alienBullets + Bullet(from.x, from.y + alienHalf)
                alienFireCooldown = (1.4f - 0.12f * (wave - 1)).coerceAtLeast(0.55f)
            }

            // Tirs du joueur : montée + collision aliens.
            var mutAliens: MutableList<Boolean>? = null
            val keptPlayerBullets = mutableListOf<Bullet>()
            for (b in playerBullets) {
                val ny = b.y - h * 0.8f * dt
                if (ny < 0f) continue
                var hit = false
                val current = mutAliens ?: aliens
                for (idx in current.indices) {
                    if (!current[idx]) continue
                    val c = alienCenter(idx)
                    if (abs(b.x - c.x) <= alienHalf && abs(ny - c.y) <= alienHalf) {
                        val list = mutAliens ?: aliens.toMutableList().also { mutAliens = it }
                        list[idx] = false
                        score += SCORE_PER_ALIEN
                        hit = true
                        break
                    }
                }
                if (!hit) keptPlayerBullets.add(Bullet(b.x, ny))
            }
            playerBullets = keptPlayerBullets
            mutAliens?.let { aliens = it }
        }

        // --- Tirs ennemis (aliens OU boss) : descente + collision canon. ---
        val keptAlienBullets = mutableListOf<Bullet>()
        var playerHit = false
        for (b in alienBullets) {
            val ny = b.y + h * (0.35f + 0.03f * (wave - 1)).coerceAtMost(0.55f) * dt
            if (ny > h) continue
            if (!playerHit && ny >= cannonY - h * 0.015f && ny <= cannonY + h * 0.02f && abs(b.x - cannonX) <= cannonHalf) {
                playerHit = true
                continue
            }
            keptAlienBullets.add(Bullet(b.x, ny))
        }
        alienBullets = keptAlienBullets
        if (playerHit) {
            lives--
            if (lives <= 0) {
                offerContinueOrEnd(onGranted = { lives = 1; startWave(size); GameFx.loseLife(context); countdownKey++ })
            } else {
                startWave(size); GameFx.loseLife(context); countdownKey++
            }
            return@GameLoop
        }

        // --- Fin de vague : boss désintégré OU tous les aliens détruits. ---
        val waveCleared = if (isBossWave(wave)) bossBlocks.none { it } else aliens.none { it }
        if (waveCleared) {
            wavesWon++
            val reward = if (isBossWave(wave)) POINTS_PER_BOSS_WON else POINTS_PER_WAVE_WON
            vm.earnPoints(reward)
            earnedTotal += reward
            GameFx.win()
            wave++
            GameAds.onLevelCleared()
            startWave(size)
        }
    }

    GameScaffold(
        title = if (isBossWave(wave)) "Invasion — BOSS — ❤ $lives" else "Invasion — Vague $wave — ❤ $lives",
        score = score,
        onExit = onExit,
        gameOver = gameOver,
        pointsEarned = earnedTotal,
        bestScore = bestScores[GameShopCatalog.SPACE_INVADERS] ?: 0,
        onRestart = { resetGame(canvasSize) },
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
                    detectDragGestures { change, _ ->
                        change.consume()
                        cannonX = change.position.x.coerceIn(0f, size.width.toFloat())
                    }
                }
        ) {
            drawRect(color = Color(0xFF0B0B14))
            val w = size.width
            val h = size.height
            val cellW = w * 0.13f
            val cellH = h * 0.055f
            val alienHalf = w * 0.045f

            // Aliens.
            aliens.forEachIndexed { idx, alive ->
                if (alive) {
                    val row = idx / ALIEN_COLS
                    val col = idx % ALIEN_COLS
                    val cx = formX + col * cellW
                    val cy = formY + row * cellH
                    val color = when (row) {
                        0 -> NeonPink
                        1 -> NeonPurple
                        2 -> Color(0xFF5C8DFF)
                        else -> NeonCyan
                    }
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(cx - alienHalf, cy - alienHalf * 0.7f),
                        size = Size(alienHalf * 2f, alienHalf * 1.4f)
                    )
                    drawCircle(Color.Black, radius = alienHalf * 0.18f, center = Offset(cx - alienHalf * 0.4f, cy - alienHalf * 0.15f))
                    drawCircle(Color.Black, radius = alienHalf * 0.18f, center = Offset(cx + alienHalf * 0.4f, cy - alienHalf * 0.15f))
                }
            }

            // Boss : grille de blocs qui se désintègre au fur et à mesure des tirs.
            if (bossBlocks.isNotEmpty()) {
                val bossHalfW = w * 0.30f
                val bw = (bossHalfW * 2f) / BOSS_COLS
                val bossTop = h * 0.10f
                val bh = h * 0.04f
                val bossLeft = bossX - bossHalfW
                val pad = bw * 0.06f
                bossBlocks.forEachIndexed { idx, alive ->
                    if (alive) {
                        val r = idx / BOSS_COLS
                        val c = idx % BOSS_COLS
                        val color = when (r) {
                            0 -> NeonPink
                            1 -> NeonPurple
                            2 -> Color(0xFF5C8DFF)
                            else -> NeonCyan
                        }
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(bossLeft + c * bw + pad, bossTop + r * bh + pad),
                            size = Size(bw - 2 * pad, bh - 2 * pad)
                        )
                    }
                }
                // Deux « yeux » tant que leurs blocs tiennent, pour l'allure de boss.
                listOf(2, BOSS_COLS - 3).forEach { ec ->
                    val idx = 1 * BOSS_COLS + ec
                    if (idx < bossBlocks.size && bossBlocks[idx]) {
                        drawCircle(
                            Color.Black,
                            radius = bh * 0.3f,
                            center = Offset(bossLeft + (ec + 0.5f) * bw, bossTop + 1.5f * bh)
                        )
                    }
                }
            }

            playerBullets.forEach { b ->
                drawLine(NeonCyan, Offset(b.x, b.y), Offset(b.x, b.y + h * 0.018f), strokeWidth = 6f)
            }
            alienBullets.forEach { b ->
                drawLine(NeonPink, Offset(b.x, b.y), Offset(b.x, b.y - h * 0.018f), strokeWidth = 6f)
            }

            // Canon du joueur (base + fût).
            val cannonY = h * 0.86f - cannonLift
            val cannonHalf = w * 0.05f
            drawRoundRect(
                color = NeonCyan,
                topLeft = Offset(cannonX - cannonHalf, cannonY),
                size = Size(cannonHalf * 2f, h * 0.016f)
            )
            drawRoundRect(
                color = NeonCyan,
                topLeft = Offset(cannonX - w * 0.008f, cannonY - h * 0.018f),
                size = Size(w * 0.016f, h * 0.018f)
            )
        }
        CountdownOverlay(countdown)
    }
}
