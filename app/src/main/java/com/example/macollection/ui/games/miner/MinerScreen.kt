package com.example.macollection.ui.games.miner

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
import androidx.compose.ui.graphics.Path
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
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/** Identifiant du jeu (high-score / déblocage) — voir GameShopCatalog pour l'intégration finale. */
const val MINER_GAME_ID = "miner"

private const val WIDTH = 11
private const val HEIGHT = 15
private const val LIVES_START = 3
private const val MAX_CONTINUES = 3
private const val DIAMOND_SCORE = 25
private const val POINTS_PER_LEVEL = 10
private const val MOVE_SPEED = 5f     // cases/seconde
private const val FALL_STEP = 0.22f   // secondes entre deux "chutes" d'un cran (façon Boulder Dash : pas fluide, par à-coups)

private val DirtColor = Color(0xFF6B4A2E)
private val DirtDark = Color(0xFF54381F)
private val WallColor = Color(0xFF4A4A55)
private val BoulderColor = Color(0xFF9AA0AE)
private val BoulderHighlight = Color(0xFFD8DCE4)
private val DiamondColor = Color(0xFF22E6FF)
private val ExitLocked = Color(0xFF5A2A2A)
private val ExitOpen = Color(0xFF39D98A)
private val MinerBody = Color(0xFFFFB347)
private val MinerLamp = Color(0xFFFFF3C4)

private enum class Dir(val dx: Int, val dy: Int) {
    UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0), NONE(0, 0)
}

private enum class Cell { EMPTY, DIRT, WALL, BOULDER, DIAMOND, EXIT }

private data class Mover(val col: Int, val row: Int, val dir: Dir, val progress: Float)

/**
 * Photo de l'état du jeu juste AVANT le dernier changement réel (mineur qui franchit une case,
 * rocher/diamant qui tombe, écrasement...), pour permettre d'« annuler le dernier coup » si ce
 * changement s'avère bloquant (ex. rocher tombé pile devant la seule sortie possible). Un seul
 * niveau d'annulation (pas d'historique complet) : chaque nouveau changement réel écrase l'ancien.
 */
private data class MinerSnapshot(
    val grid: Array<Array<Cell>>,
    val miner: Mover,
    val exitOpen: Boolean,
    val diamondsHeld: Int,
    val score: Int,
    val lives: Int,
    val fallingCells: Set<Int>
)

/**
 * Mineur : labyrinthe de terre à creuser. Glisser dans une direction pour orienter le mineur ;
 * il creuse la terre en avançant (aucun point) et ramasse les diamants (+25). Les rochers peuvent
 * être poussés horizontalement s'il y a de la place derrière, mais jamais verticalement. Sans
 * appui en dessous, rochers et diamants TOMBENT d'une case — se faire écraser par un ROCHER qui
 * tombe coûte une vie (un diamant qui tombe sur le mineur est ramassé automatiquement, sans
 * danger). Ramasse assez de diamants pour ouvrir la sortie (case verte) et terminer le niveau.
 */
@Composable
fun MinerScreen(vm: GameViewModel, onExit: () -> Unit) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    var grid by remember { mutableStateOf<Array<Array<Cell>>>(emptyArray()) }
    var miner by remember { mutableStateOf(Mover(1, 1, Dir.NONE, 0f)) }
    var desiredDir by remember { mutableStateOf(Dir.NONE) }
    var exitPos by remember { mutableStateOf(1 to 1) }
    var exitOpen by remember { mutableStateOf(false) }
    var diamondsHeld by remember { mutableIntStateOf(0) }
    var diamondsNeeded by remember { mutableIntStateOf(6) }
    var fallAccum by remember { mutableFloatStateOf(0f) }
    // Cases actuellement "instables" (en train de tomber OU sur le point de tomber au prochain
    // tic) : sert à la fois à donner un tic de préavis avant la 1ère chute (voir la boucle de jeu)
    // et à faire trembler visuellement le rocher/diamant concerné.
    var fallingCells by remember { mutableStateOf<Set<Int>>(emptySet()) }
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
    var undoSnapshot by remember { mutableStateOf<MinerSnapshot?>(null) }
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
            message = "Écrasé par un rocher ! Regarder une pub pour continuer avec une vie supplémentaire ?",
            buttonLabel = "🎬 Regarder une pub pour continuer",
            onAccept = {
                continueOffer = null
                watchRewardedAd(context, onRewarded = { continuesUsed++; onGranted() }, onClosed = { gameOver = true })
            },
            onDecline = { continueOffer = null; gameOver = true }
        )
    }

    /** Génère une nouvelle grotte : bordure de roc indestructible, terre partout ailleurs, quelques
     *  murs, rochers et diamants semés aléatoirement, une poche de départ dégagée, une sortie fixe. */
    fun generateCave(lvl: Int): Array<Array<Cell>> {
        val g = Array(HEIGHT) { row -> Array(WIDTH) { col ->
            if (row == 0 || row == HEIGHT - 1 || col == 0 || col == WIDTH - 1) Cell.WALL else Cell.DIRT
        } }
        val rnd = Random(System.nanoTime())
        val wallCount = (10 + lvl).coerceAtMost(22)
        val boulderCount = (8 + lvl).coerceAtMost(18)
        val diamondCount = (10 + lvl / 2).coerceAtMost(16)
        fun randomInteriorCell(): Pair<Int, Int> = (1 until WIDTH - 1).random(rnd) to (1 until HEIGHT - 1).random(rnd)
        repeat(wallCount) {
            val (c, r) = randomInteriorCell()
            g[r][c] = Cell.WALL
        }
        repeat(boulderCount) {
            val (c, r) = randomInteriorCell()
            if (g[r][c] == Cell.DIRT) g[r][c] = Cell.BOULDER
        }
        repeat(diamondCount) {
            val (c, r) = randomInteriorCell()
            if (g[r][c] == Cell.DIRT) g[r][c] = Cell.DIAMOND
        }
        // Un diamant totalement encerclé de murs indestructibles (aucun côté creusable/franchissable)
        // ne pourra jamais être atteint : on le retire plutôt que de compter un objectif impossible
        // (voir aussi diamondCount(), qui recalcule diamondsNeeded d'après le résultat final ici).
        for (row in 1 until HEIGHT - 1) for (col in 1 until WIDTH - 1) {
            if (g[row][col] != Cell.DIAMOND) continue
            val fullyWalled = listOf(col - 1 to row, col + 1 to row, col to row - 1, col to row + 1)
                .all { (c, r) -> g[r][c] == Cell.WALL }
            if (fullyWalled) g[row][col] = Cell.WALL
        }
        // Poche de départ toujours dégagée (jamais de rocher/mur piégeant le mineur au lancement).
        for (dc in 0..2) for (dr in 0..2) {
            if (1 + dc < WIDTH - 1 && 1 + dr < HEIGHT - 1) g[1 + dr][1 + dc] = Cell.EMPTY
        }
        // Sortie décalée d'un cran par rapport au coin (au lieu de collée aux 2 murs de bordure) :
        // garantit 4 angles d'approche possibles plutôt que 2 seulement. Ses 4 cases voisines sont
        // forcées en terre normale (jamais un mur ni un rocher/diamant déjà posé), pour qu'un rocher
        // tombé plus tard ne puisse bloquer qu'UN SEUL des 4 accès, jamais tous à la fois comme dans
        // un coin à 2 accès (voir signalement utilisateur : sortie/diamant bloqués sans recours).
        val ex = WIDTH - 3
        val ey = HEIGHT - 3
        g[ey][ex] = Cell.EXIT
        exitPos = ex to ey
        listOf(ex - 1 to ey, ex + 1 to ey, ex to ey - 1, ex to ey + 1).forEach { (c, r) -> g[r][c] = Cell.DIRT }
        return g
    }

    /**
     * Nombre de diamants RÉELLEMENT présents sur la grille générée. Les murs/rochers déjà posés
     * (collisions aléatoires), la poche de départ dégagée après-coup et la case de sortie peuvent
     * tous supprimer des diamants tirés au sort par [generateCave] : sans ce recomptage, l'objectif
     * [diamondsNeeded] pouvait dépasser le nombre de diamants réellement atteignables, rendant le
     * niveau impossible à terminer (sortie qui ne s'ouvre jamais malgré tous les diamants ramassés).
     */
    fun Array<Array<Cell>>.diamondCount(): Int = sumOf { row -> row.count { it == Cell.DIAMOND } }

    fun resetGame(size: IntSize) {
        if (size.width <= 0 || size.height <= 0) return
        level = 1
        grid = generateCave(level)
        diamondsNeeded = 6.coerceAtMost(grid.diamondCount()).coerceAtLeast(1)
        miner = Mover(1, 1, Dir.NONE, 0f)
        desiredDir = Dir.NONE
        exitOpen = false
        diamondsHeld = 0
        fallAccum = 0f
        fallingCells = emptySet()
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
        initialized = true
        undoSnapshot = null
        countdownKey++
    }

    fun startNextLevel() {
        level++
        grid = generateCave(level)
        diamondsNeeded = (5 + level).coerceAtMost(12).coerceAtMost(grid.diamondCount()).coerceAtLeast(1)
        miner = Mover(1, 1, Dir.NONE, 0f)
        desiredDir = Dir.NONE
        exitOpen = false
        diamondsHeld = 0
        fallAccum = 0f
        fallingCells = emptySet()
        undoSnapshot = null
        countdownKey++
    }

    /**
     * Restaure l'état juste avant le dernier changement réel (voir [MinerSnapshot]) : utile quand
     * ce changement bloque la partie (ex. rocher tombé sans issue devant le mineur). Un seul niveau
     * d'annulation disponible à la fois.
     */
    fun undoLastAction() {
        val snap = undoSnapshot ?: return
        grid = snap.grid
        miner = snap.miner
        desiredDir = Dir.NONE
        exitOpen = snap.exitOpen
        diamondsHeld = snap.diamondsHeld
        score = snap.score
        lives = snap.lives
        fallingCells = snap.fallingCells
        undoSnapshot = null
    }

    LaunchedEffect(canvasSize) {
        if (!initialized) resetGame(canvasSize)
    }

    LaunchedEffect(gameOver) {
        if (gameOver && !scoreRecorded) {
            scoreRecorded = true
            vm.recordHighScore(MINER_GAME_ID, score)
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
        val g = grid
        if (g.isEmpty()) return@GameLoop

        // Photo de l'état AVANT toute mutation de cette frame, pour "annuler la dernière action"
        // (voir undoLastAction) : conservée dans undoSnapshot seulement si [changed] passe à vrai
        // (un vrai changement a lieu cette frame), pas à chaque frame sans effet.
        val preGrid = Array(HEIGHT) { r -> g[r].copyOf() }
        val preMiner = miner
        val preExitOpen = exitOpen
        val preDiamondsHeld = diamondsHeld
        val preScore = score
        val preLives = lives
        val preFallingCells = fallingCells
        var changed = false

        fun cellAt(col: Int, row: Int): Cell =
            if (col in 0 until WIDTH && row in 0 until HEIGHT) g[row][col] else Cell.WALL

        /** Peut-on s'engager vers (col,row) en venant de la direction [dir] ? Ne mute rien : les
         *  rochers ne sont "poussables" que si la case encore derrière eux est libre. */
        fun isPassable(col: Int, row: Int, dir: Dir): Boolean {
            return when (cellAt(col, row)) {
                Cell.WALL -> false
                Cell.EXIT -> exitOpen
                Cell.BOULDER -> (dir == Dir.LEFT || dir == Dir.RIGHT) && cellAt(col + dir.dx, row + dir.dy) == Cell.EMPTY
                else -> true
            }
        }

        /** Applique les effets d'entrée dans (col,row) : creuse la terre, ramasse un diamant,
         *  pousse un rocher. Appelé une seule fois, au moment où la case est réellement franchie. */
        fun applyEntry(col: Int, row: Int, dir: Dir) {
            when (cellAt(col, row)) {
                Cell.DIRT -> { g[row][col] = Cell.EMPTY; GameFx.hop(); changed = true }
                Cell.DIAMOND -> {
                    g[row][col] = Cell.EMPTY
                    diamondsHeld++
                    score += DIAMOND_SCORE
                    GameFx.eat()
                    if (diamondsHeld >= diamondsNeeded && !exitOpen) {
                        exitOpen = true
                        GameFx.win()
                    }
                    changed = true
                }
                Cell.BOULDER -> {
                    g[row][col] = Cell.EMPTY
                    g[row + dir.dy][col + dir.dx] = Cell.BOULDER
                    changed = true
                }
                Cell.EXIT -> { /* ouverte : simple passage, la victoire est gérée après le déplacement */ }
                else -> {}
            }
        }

        // --- Déplacement du mineur (case par case, interpolé pour un rendu fluide). ---
        if (miner.dir == Dir.NONE && desiredDir != Dir.NONE && isPassable(miner.col + desiredDir.dx, miner.row + desiredDir.dy, desiredDir)) {
            miner = miner.copy(dir = desiredDir)
        }
        if (miner.dir != Dir.NONE) {
            var progress = miner.progress + MOVE_SPEED * dt
            var col = miner.col
            var row = miner.row
            var dir = miner.dir
            if (progress >= 1f) {
                progress -= 1f
                applyEntry(col + dir.dx, row + dir.dy, dir)
                col += dir.dx
                row += dir.dy
                val turning = desiredDir != Dir.NONE && desiredDir != dir && isPassable(col + desiredDir.dx, row + desiredDir.dy, desiredDir)
                dir = when {
                    turning -> desiredDir
                    isPassable(col + dir.dx, row + dir.dy, dir) -> dir
                    else -> Dir.NONE
                }
                if (turning) desiredDir = Dir.NONE
                if (dir == Dir.NONE) progress = 0f
            }
            miner = Mover(col, row, dir, progress)
        }

        // Sortie atteinte (case franchie ET ouverte) : niveau terminé.
        if (exitOpen && miner.col == exitPos.first && miner.row == exitPos.second) {
            levelsWon++
            vm.earnPoints(POINTS_PER_LEVEL)
            GameAds.onLevelCleared()
            startNextLevel()
            return@GameLoop
        }

        // --- Gravité : rochers/diamants tombent d'une case, par à-coups (pas d'appui = chute). ---
        // Un rocher qui vient tout juste de perdre son appui ne tombe PAS immédiatement : il est
        // d'abord marqué "instable" pendant un tic entier (préavis, ~FALL_STEP secondes) avant de
        // réellement bouger — ça laisse au mineur le temps de s'écarter s'il vient de creuser
        // dessous. Une fois en chute, en revanche, il tombe à chaque tic sans interruption.
        fallAccum += dt
        if (fallAccum >= FALL_STEP) {
            fallAccum -= FALL_STEP
            var crushed = false
            val stillUnstable = HashSet<Int>()
            for (row in HEIGHT - 2 downTo 1) {
                for (col in 1 until WIDTH - 1) {
                    val c = g[row][col]
                    if (c != Cell.BOULDER && c != Cell.DIAMOND) continue
                    if (g[row + 1][col] != Cell.EMPTY) continue // posé sur un appui : stable
                    val key = row * WIDTH + col
                    if (key !in fallingCells) {
                        // Vient de perdre son appui ce tic : simple préavis, ne tombe pas encore.
                        stillUnstable.add(key)
                        continue
                    }
                    // Déjà instable depuis au moins un tic : chute réelle.
                    val landsOnMiner = row + 1 == miner.row && col == miner.col
                    if (landsOnMiner && c == Cell.DIAMOND) {
                        // Un diamant qui tombe sur le mineur est ramassé automatiquement au lieu
                        // de l'écraser : seuls les rochers sont mortels en tombant.
                        g[row][col] = Cell.EMPTY
                        diamondsHeld++
                        score += DIAMOND_SCORE
                        GameFx.eat()
                        if (diamondsHeld >= diamondsNeeded && !exitOpen) {
                            exitOpen = true
                            GameFx.win()
                        }
                        changed = true
                        continue
                    }
                    if (landsOnMiner) {
                        crushed = true
                    }
                    g[row + 1][col] = c
                    g[row][col] = Cell.EMPTY
                    stillUnstable.add((row + 1) * WIDTH + col)
                    changed = true
                }
            }
            fallingCells = stillUnstable
            if (crushed) {
                GameFx.loseLife(context)
                lives--
                if (lives <= 0) {
                    offerContinueOrEnd(onGranted = {
                        lives = 1
                        miner = Mover(1, 1, Dir.NONE, 0f)
                        desiredDir = Dir.NONE
                        countdownKey++
                    })
                } else {
                    miner = Mover(1, 1, Dir.NONE, 0f)
                    desiredDir = Dir.NONE
                    countdownKey++
                }
            }
        }

        if (changed) {
            undoSnapshot = MinerSnapshot(preGrid, preMiner, preExitOpen, preDiamondsHeld, preScore, preLives, preFallingCells)
        }
    }

    GameScaffold(
        title = "Mineur — Niv. $level — ❤ $lives",
        score = score,
        onExit = onExit,
        gameOver = gameOver,
        pointsEarned = levelsWon * POINTS_PER_LEVEL + multiplierBonus,
        bestScore = bestScores[MINER_GAME_ID] ?: 0,
        onRestart = { resetGame(canvasSize) },
        onResetLevel = { resetGame(canvasSize) },
        onUndoLastAction = if (undoSnapshot != null) { { undoLastAction() } } else null,
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
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        desiredDir = if (abs(dragAmount.x) > abs(dragAmount.y)) {
                            if (dragAmount.x > 0f) Dir.RIGHT else Dir.LEFT
                        } else {
                            if (dragAmount.y > 0f) Dir.DOWN else Dir.UP
                        }
                    }
                }
        ) {
            drawRect(color = Color(0xFF0B0B14))
            if (grid.isEmpty()) return@Canvas
            val w = size.width
            val h = size.height
            val tile = min(w / WIDTH, h / HEIGHT)
            val ox = (w - tile * WIDTH) / 2f
            val oy = (h - tile * HEIGHT) / 2f
            fun cx(c: Float) = ox + (c + 0.5f) * tile
            fun cy(r: Float) = oy + (r + 0.5f) * tile

            for (row in 0 until HEIGHT) for (col in 0 until WIDTH) {
                val left = ox + col * tile
                val top = oy + row * tile
                when (grid[row][col]) {
                    Cell.WALL -> drawRect(WallColor, Offset(left, top), Size(tile, tile))
                    Cell.DIRT -> {
                        drawRect(DirtColor, Offset(left, top), Size(tile, tile))
                        drawCircle(DirtDark, tile * 0.05f, Offset(left + tile * 0.3f, top + tile * 0.35f))
                        drawCircle(DirtDark, tile * 0.05f, Offset(left + tile * 0.68f, top + tile * 0.7f))
                    }
                    Cell.BOULDER -> {
                        val wobble = if (row * WIDTH + col in fallingCells) sin(animTime * 40f) * tile * 0.05f else 0f
                        drawCircle(BoulderColor, tile * 0.4f, Offset(cx(col.toFloat()) + wobble, cy(row.toFloat())))
                        drawCircle(BoulderHighlight, tile * 0.14f, Offset(cx(col.toFloat()) - tile * 0.12f + wobble, cy(row.toFloat()) - tile * 0.12f))
                    }
                    Cell.DIAMOND -> {
                        val wobble = if (row * WIDTH + col in fallingCells) sin(animTime * 40f) * tile * 0.05f else 0f
                        val cxp = cx(col.toFloat()) + wobble; val cyp = cy(row.toFloat()); val r = tile * 0.32f
                        val path = Path().apply {
                            moveTo(cxp, cyp - r); lineTo(cxp + r, cyp); lineTo(cxp, cyp + r); lineTo(cxp - r, cyp); close()
                        }
                        drawPath(path, DiamondColor)
                    }
                    Cell.EXIT -> {
                        val color = if (exitOpen) ExitOpen else ExitLocked
                        drawRect(color, Offset(left + tile * 0.1f, top + tile * 0.1f), Size(tile * 0.8f, tile * 0.8f))
                    }
                    Cell.EMPTY -> {}
                }
            }

            // Mineur (disque + lampe frontale orientée dans la direction courante).
            val mrx = miner.col + miner.dir.dx * miner.progress
            val mry = miner.row + miner.dir.dy * miner.progress
            val mcx = cx(mrx); val mcy = cy(mry); val mr = tile * 0.38f
            drawCircle(MinerBody, mr, Offset(mcx, mcy))
            val lampOffset = when (miner.dir) {
                Dir.UP -> Offset(0f, -mr * 0.8f)
                Dir.DOWN -> Offset(0f, mr * 0.8f)
                Dir.LEFT -> Offset(-mr * 0.8f, 0f)
                Dir.RIGHT -> Offset(mr * 0.8f, 0f)
                Dir.NONE -> Offset(0f, -mr * 0.8f)
            }
            drawCircle(MinerLamp, tile * 0.1f, Offset(mcx + lampOffset.x, mcy + lampOffset.y))
        }
        CountdownOverlay(countdown)
    }
}
