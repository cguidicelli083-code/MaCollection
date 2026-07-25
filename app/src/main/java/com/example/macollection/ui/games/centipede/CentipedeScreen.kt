package com.example.macollection.ui.games.centipede

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
import com.example.macollection.ui.games.engine.CountdownOverlay
import com.example.macollection.ui.ads.GameAds
import com.example.macollection.ui.ads.watchRewardedAd
import com.example.macollection.ui.games.engine.ContinueOffer
import com.example.macollection.ui.games.engine.GameFx
import com.example.macollection.ui.games.engine.GameLoop
import com.example.macollection.ui.games.engine.GameScaffold
import com.example.macollection.ui.theme.NeonCyan
import com.example.macollection.ui.theme.NeonPink
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

private const val COLS = 10
private const val ROWS = 16
private const val LIVES_START = 3
// Une vie supplémentaire offerte contre une pub, jusqu'à 3 fois par partie.
private const val MAX_CONTINUES = 3
private const val MUSH_HP = 3
private const val SCORE_PER_SEGMENT = 10
private const val SCORE_PER_MUSHROOM = 5
private const val POINTS_PER_WAVE_WON = 10

// Bande basse où le joueur peut se déplacer (dernières lignes) — permet de monter d'un ou deux
// crans pour esquiver la chenille. Les champignons ne sont jamais placés dans cette bande.
private const val PLAYER_BAND_TOP = 0.78f
private const val PLAYER_BAND_BOTTOM = 0.95f

private val MushroomGreen = Color(0xFF3DDC84)
private val BodyColors = listOf(NeonCyan, MushroomGreen, Color(0xFF5C8DFF))

/** Une case de la grille (colonne, ligne). */
private data class Cell(val col: Int, val row: Int)

/** Un tronçon de mille-pattes : [cells].first() est la tête ; [dir] = sens horizontal, [vdir] = sens vertical (+1 descend, -1 remonte). */
private data class Train(val cells: List<Cell>, val dir: Int, val vdir: Int)

private data class Shot(val x: Float, val y: Float)

private fun key(col: Int, row: Int) = row * COLS + col

/**
 * Centipede : un mille-pattes serpente à travers un champ de champignons. Le canon suit le doigt
 * (glisser) et tire tout seul ; on peut aussi le **monter d'un ou deux crans** dans la bande basse
 * pour esquiver la chenille. Tirer un tronçon le coupe en deux et laisse un champignon. Quand la
 * chenille atteint le bas sans toucher le joueur, **elle remonte** (elle rebondit de haut en bas).
 * Toucher le canon coûte une vie et recommence la vague. Vague nettoyée = +10 points, la suivante
 * est plus longue et rapide.
 */
@Composable
fun CentipedeScreen(vm: GameViewModel, onExit: () -> Unit) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    var mushrooms by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var trains by remember { mutableStateOf<List<Train>>(emptyList()) }
    var shots by remember { mutableStateOf<List<Shot>>(emptyList()) }
    var playerX by remember { mutableFloatStateOf(0f) }
    var playerY by remember { mutableFloatStateOf(0f) }
    var fireCooldown by remember { mutableFloatStateOf(0f) }
    var stepAccum by remember { mutableFloatStateOf(0f) }
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(LIVES_START) }
    var wave by remember { mutableIntStateOf(1) }
    var wavesWon by remember { mutableIntStateOf(0) }
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

    fun randomMushrooms(count: Int, existing: Map<Int, Int>): Map<Int, Int> {
        val m = HashMap(existing)
        var placed = 0
        var guard = 0
        while (placed < count && guard < count * 12) {
            guard++
            val col = Random.nextInt(0, COLS)
            val row = Random.nextInt(2, ROWS - 4) // jamais dans la bande basse du joueur
            val k = key(col, row)
            if (!m.containsKey(k)) { m[k] = MUSH_HP; placed++ }
        }
        return m
    }

    fun spawnTrain(wave: Int): Train {
        val len = (5 + wave).coerceAtMost(COLS)
        val cells = (0 until len).map { Cell(len - 1 - it, 0) }
        return Train(cells, dir = 1, vdir = 1)
    }

    fun stepInterval(wave: Int) = (0.26f - 0.02f * (wave - 1)).coerceAtLeast(0.09f)

    fun loadWave(size: IntSize, resetField: Boolean) {
        mushrooms = if (resetField) randomMushrooms(10 + wave, emptyMap())
        else randomMushrooms(4, mushrooms)
        trains = listOf(spawnTrain(wave))
        shots = emptyList()
        playerX = size.width / 2f
        playerY = size.height * 0.90f
        stepAccum = 0f
        fireCooldown = 0f
    }

    fun resetGame(size: IntSize) {
        if (size.width <= 0 || size.height <= 0) return
        score = 0
        lives = LIVES_START
        wave = 1
        wavesWon = 0
        continuesUsed = 0
        continueOffer = null
        multiplierClaimed = false
        multiplierBonus = 0
        gameOver = false
        scoreRecorded = false
        loadWave(size, resetField = true)
        initialized = true
        countdownKey++ // décompte 3-2-1 avant de (re)lancer la partie
    }

    LaunchedEffect(canvasSize) {
        if (!initialized) resetGame(canvasSize)
    }

    LaunchedEffect(gameOver) {
        if (gameOver && !scoreRecorded) {
            scoreRecorded = true
            vm.recordHighScore(GameShopCatalog.CENTIPEDE, score)
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

    fun stepTrain(train: Train, mush: Map<Int, Int>): Train {
        val head = train.cells.first()
        var dir = train.dir
        var vdir = train.vdir
        val fwd = Cell(head.col + dir, head.row)
        val canFwd = fwd.col in 0 until COLS && !mush.containsKey(key(fwd.col, fwd.row))
        val target: Cell = if (canFwd) {
            fwd
        } else {
            dir = -dir
            var nrow = head.row + vdir
            if (nrow > ROWS - 1) { vdir = -1; nrow = head.row + vdir } // au fond → la chenille remonte
            if (nrow < 0) { vdir = 1; nrow = head.row + vdir }         // en haut → elle redescend
            Cell(head.col, nrow.coerceIn(0, ROWS - 1))
        }
        val newCells = ArrayList<Cell>(train.cells.size)
        newCells.add(target)
        for (i in 0 until train.cells.size - 1) newCells.add(train.cells[i])
        return Train(newCells, dir, vdir)
    }

    GameLoop(running = initialized && !gameOver && countdown == 0 && continueOffer == null) { dt ->
        val size = canvasSize
        val w = size.width.toFloat()
        val h = size.height.toFloat()
        if (w <= 0f || h <= 0f) return@GameLoop
        val cellW = w / COLS
        val cellH = h / ROWS

        // --- Déplacement discret du mille-pattes (au rythme de la vague). ---
        stepAccum += dt
        val interval = stepInterval(wave)
        if (stepAccum >= interval) {
            stepAccum -= interval
            trains = trains.map { stepTrain(it, mushrooms) }
        }

        // --- Collision chenille / canon (chaque frame : le joueur bouge en continu). ---
        var hitPlayer = false
        for (t in trains) for (c in t.cells) {
            val segX = (c.col + 0.5f) * cellW
            val segY = (c.row + 0.5f) * cellH
            if (abs(segX - playerX) < cellW * 0.6f && abs(segY - playerY) < cellH * 0.7f) {
                hitPlayer = true
            }
        }
        if (hitPlayer) {
            lives--
            if (lives <= 0) {
                offerContinueOrEnd(onGranted = {
                    lives = 1
                    loadWave(size, resetField = true)
                    GameFx.loseLife(context)
                    countdownKey++
                })
                return@GameLoop
            }
            loadWave(size, resetField = true) // perdre une vie recommence la vague
            GameFx.loseLife(context)
            countdownKey++
            return@GameLoop
        }

        // --- Tir automatique du canon (max 2 tirs à l'écran). ---
        fireCooldown -= dt
        if (fireCooldown <= 0f && shots.size < 2) {
            shots = shots + Shot(playerX, playerY - cellH * 0.4f)
            fireCooldown = 0.16f
        }

        // --- Montée des tirs + collisions (tronçon prioritaire, sinon champignon). ---
        val keptShots = ArrayList<Shot>()
        var curTrains = trains
        var curMush = mushrooms
        var addScore = 0
        for (s in shots) {
            val ny = s.y - h * 0.9f * dt
            if (ny < 0f) continue
            val col = (s.x / cellW).toInt().coerceIn(0, COLS - 1)
            val row = (ny / cellH).toInt().coerceIn(0, ROWS - 1)

            var hit = false
            loop@ for (ti in curTrains.indices) {
                val cells = curTrains[ti].cells
                for (si in cells.indices) {
                    if (cells[si].col == col && cells[si].row == row) {
                        addScore += SCORE_PER_SEGMENT
                        curMush = curMush + (key(col, row) to MUSH_HP)
                        val old = curTrains[ti]
                        val front = old.cells.subList(0, si).toList()
                        val back = old.cells.subList(si + 1, old.cells.size).toList()
                        curTrains = buildList {
                            for (i in curTrains.indices) if (i != ti) add(curTrains[i])
                            if (front.isNotEmpty()) add(Train(front, old.dir, old.vdir))
                            if (back.isNotEmpty()) add(Train(back, old.dir, old.vdir))
                        }
                        hit = true
                        break@loop
                    }
                }
            }
            if (hit) continue

            val mk = key(col, row)
            val hp = curMush[mk]
            if (hp != null) {
                if (hp <= 1) {
                    curMush = curMush - mk
                    addScore += SCORE_PER_MUSHROOM
                } else {
                    curMush = curMush + (mk to hp - 1)
                }
                continue
            }
            keptShots.add(Shot(s.x, ny))
        }
        shots = keptShots
        trains = curTrains
        mushrooms = curMush
        score += addScore

        // --- Vague nettoyée : +10 points, vague suivante plus longue/rapide, champ densifié. ---
        if (trains.isEmpty()) {
            wavesWon++
            vm.earnPoints(POINTS_PER_WAVE_WON)
            GameFx.win()
            wave++
            GameAds.onLevelCleared()
            loadWave(size, resetField = false)
        }
    }

    GameScaffold(
        title = "Mille-Pattes — Vague $wave — ❤ $lives",
        score = score,
        onExit = onExit,
        gameOver = gameOver,
        pointsEarned = wavesWon * POINTS_PER_WAVE_WON + multiplierBonus,
        bestScore = bestScores[GameShopCatalog.CENTIPEDE] ?: 0,
        onRestart = { resetGame(canvasSize) },
        continueOffer = continueOffer,
        onWatchAdForMultiplier = if (BuildConfig.ADS_ENABLED && adsEnabled && !multiplierClaimed && wavesWon * POINTS_PER_WAVE_WON > 0) {
            {
                watchRewardedAd(context, onRewarded = {
                    val bonus = wavesWon * POINTS_PER_WAVE_WON * 2
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
                        playerX = change.position.x.coerceIn(0f, size.width.toFloat())
                        // Déplacement vertical limité à la bande basse (monter d'un ou deux crans).
                        playerY = change.position.y.coerceIn(
                            size.height * PLAYER_BAND_TOP,
                            size.height * PLAYER_BAND_BOTTOM
                        )
                    }
                }
        ) {
            drawRect(color = Color(0xFF0B0B14))
            val w = size.width
            val h = size.height
            val cellW = w / COLS
            val cellH = h / ROWS

            // Champignons (dôme + pied, opacité selon les PV restants).
            mushrooms.forEach { (k, hp) ->
                val col = k % COLS
                val row = k / COLS
                val cx = (col + 0.5f) * cellW
                val cy = (row + 0.5f) * cellH
                val alpha = 0.45f + 0.55f * (hp.toFloat() / MUSH_HP)
                drawCircle(MushroomGreen.copy(alpha = alpha), radius = cellW * 0.33f, center = Offset(cx, cy - cellH * 0.05f))
                drawRoundRect(
                    color = MushroomGreen.copy(alpha = alpha * 0.7f),
                    topLeft = Offset(cx - cellW * 0.08f, cy),
                    size = Size(cellW * 0.16f, cellH * 0.28f)
                )
            }

            // Mille-pattes : tête rose, corps en alternance.
            trains.forEach { train ->
                train.cells.forEachIndexed { idx, c ->
                    val cx = (c.col + 0.5f) * cellW
                    val cy = (c.row + 0.5f) * cellH
                    val isHead = idx == 0
                    val color = if (isHead) NeonPink else BodyColors[c.col % BodyColors.size]
                    drawCircle(color, radius = cellW * 0.42f, center = Offset(cx, cy))
                    if (isHead) {
                        val eye = cellW * 0.09f
                        drawCircle(Color.Black, eye, Offset(cx - cellW * 0.15f, cy - cellH * 0.08f))
                        drawCircle(Color.Black, eye, Offset(cx + cellW * 0.15f, cy - cellH * 0.08f))
                    }
                }
            }

            // Tirs.
            shots.forEach { s ->
                drawLine(NeonPink, Offset(s.x, s.y), Offset(s.x, s.y + h * 0.02f), strokeWidth = 6f)
            }

            // Canon du joueur (base + fût) à sa position courante dans la bande basse.
            val half = cellW * 0.5f
            drawRoundRect(
                color = NeonCyan,
                topLeft = Offset(playerX - half, playerY),
                size = Size(half * 2f, cellH * 0.4f)
            )
            drawRoundRect(
                color = NeonCyan,
                topLeft = Offset(playerX - cellW * 0.08f, playerY - cellH * 0.3f),
                size = Size(cellW * 0.16f, cellH * 0.3f)
            )
        }
        CountdownOverlay(countdown)
    }
}
