package com.example.macollection.ui.games.orchard

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
import kotlin.math.hypot
import kotlin.math.min
import kotlin.random.Random

/** Identifiant du jeu (high-score / déblocage) — voir GameShopCatalog pour l'intégration finale. */
const val ORCHARD_GAME_ID = "orchard"

private const val WIDTH = 11
private const val HEIGHT = 15
private const val LIVES_START = 3
private const val MAX_CONTINUES = 3
private const val FRUIT_SCORE = 10
private const val CRUSH_SCORE = 150
private const val POINTS_PER_LEVEL = 10
private const val FRANK_SPEED = 5f
private const val BUG_TUNNEL_SPEED = 4.2f  // vitesse dans une galerie déjà creusée
private const val BUG_DIG_SPEED = 2.1f     // plus lent quand l'insecte doit creuser lui-même
private const val APPLE_ROLL_STEP = 0.09f  // secondes entre deux cases pour une pomme qui roule (rapide)
private const val BALL_STEP = 0.045f       // secondes entre deux cases pour la bille lancée (très rapide)
private const val BALL_COOLDOWN = 4f
// Probabilité qu'un insecte choisisse une direction au hasard plutôt que la meilleure vers Frank :
// avec 0 (comportement d'origine), l'insecte fonçait TOUJOURS droit sur le joueur (trop prévisible/
// dirigé) ; ce taux garde une vraie menace (il continue globalement de se rapprocher) sans être
// un rabatteur parfait à chaque intersection.
private const val BUG_RANDOM_CHANCE = 0.4f

private val DirtColor = Color(0xFF2E5C3A)
private val DirtDark = Color(0xFF24492E)
private val WallColor = Color(0xFF5D4037)
private val FruitRed = Color(0xFFFF3B3B)
private val FruitPale = Color(0xFFFFE9A8)
private val OrangeColor = Color(0xFFFF8F1F)
private val OrangePale = Color(0xFFFFD199)
private val BananaColor = Color(0xFFFFD93B)
private val BananaDark = Color(0xFFE0B400)
private val CherryColor = Color(0xFFC62828)
private val CherryLeaf = Color(0xFF2E7D32)
private val AppleColor = Color(0xFFE0433B)
private val AppleLeaf = Color(0xFF2E7D32)
private val FrankColor = Color(0xFFFF9F1C)
private val BugColors = listOf(Color(0xFF8B5CFF), Color(0xFF7C4DFF), Color(0xFF9575CD))
private val BallColor = Color(0xFFFFE93B)

private enum class Dir(val dx: Int, val dy: Int) {
    UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0), NONE(0, 0);
    fun opposite() = when (this) { UP -> DOWN; DOWN -> UP; LEFT -> RIGHT; RIGHT -> LEFT; NONE -> NONE }
}

// FRUIT = pomme rouge d'origine ; les 3 autres sont purement cosmétiques (même score, même
// comportement) pour varier les couleurs, comme demandé. Voir FRUIT_CELLS pour les traiter en bloc.
private enum class Cell { EMPTY, DIRT, WALL, FRUIT, FRUIT_ORANGE, FRUIT_BANANA, FRUIT_CHERRY }
private val FRUIT_CELLS = setOf(Cell.FRUIT, Cell.FRUIT_ORANGE, Cell.FRUIT_BANANA, Cell.FRUIT_CHERRY)

private data class Mover(val col: Int, val row: Int, val dir: Dir, val progress: Float)
private data class Bug(val mover: Mover, val color: Color)
private data class Apple(val col: Int, val row: Int, val dir: Dir)
private data class Ball(val col: Int, val row: Int, val dir: Dir)

/**
 * Croque-Fruits : jardin entièrement recouvert de terre à creuser (comme le Mineur), truffé de
 * fruits à ramasser. Pousser une pomme la fait ROULER en continu dans une galerie déjà creusée :
 * elle écrase tout insecte sur son passage (gros bonus), mais écrase aussi Frank s'il reste sur
 * sa trajectoire. Tape l'écran pour lancer une bille à portée limitée (une seule à la fois, il
 * faut attendre son retour). Les insectes creusent eux aussi pour te rejoindre à travers la terre.
 * Ramasse tous les fruits pour terminer le niveau.
 */
@Composable
fun OrchardScreen(vm: GameViewModel, onExit: () -> Unit) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    var grid by remember { mutableStateOf<Array<Array<Cell>>>(emptyArray()) }
    var frank by remember { mutableStateOf(Mover(1, 1, Dir.NONE, 0f)) }
    var facing by remember { mutableStateOf(Dir.DOWN) }
    var desiredDir by remember { mutableStateOf(Dir.NONE) }
    var bugs by remember { mutableStateOf<List<Bug>>(emptyList()) }
    var apples by remember { mutableStateOf<List<Apple>>(emptyList()) }
    var ball by remember { mutableStateOf<Ball?>(null) }
    var ballCooldown by remember { mutableFloatStateOf(0f) }
    var rollAccum by remember { mutableFloatStateOf(0f) }
    var ballAccum by remember { mutableFloatStateOf(0f) }
    var fruitLeft by remember { mutableIntStateOf(0) }
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
            message = "Écrasé ! Regarder une pub pour continuer avec une vie supplémentaire ?",
            buttonLabel = "🎬 Regarder une pub pour continuer",
            onAccept = {
                continueOffer = null
                watchRewardedAd(context, onRewarded = { continuesUsed++; onGranted() }, onClosed = { gameOver = true })
            },
            onDecline = { continueOffer = null; gameOver = true }
        )
    }

    fun generateGarden(lvl: Int): Array<Array<Cell>> {
        val g = Array(HEIGHT) { row -> Array(WIDTH) { col ->
            if (row == 0 || row == HEIGHT - 1 || col == 0 || col == WIDTH - 1) Cell.WALL else Cell.DIRT
        } }
        val rnd = Random(System.nanoTime())
        val wallCount = (8 + lvl).coerceAtMost(18)
        val fruitCount = (10 + lvl).coerceAtMost(18)
        fun randomInteriorCell(): Pair<Int, Int> = (1 until WIDTH - 1).random(rnd) to (1 until HEIGHT - 1).random(rnd)
        repeat(wallCount) {
            val (c, r) = randomInteriorCell()
            g[r][c] = Cell.WALL
        }
        repeat(fruitCount) {
            val (c, r) = randomInteriorCell()
            if (g[r][c] == Cell.DIRT) g[r][c] = FRUIT_CELLS.random(rnd)
        }
        for (dc in 0..2) for (dr in 0..2) {
            if (1 + dc < WIDTH - 1 && 1 + dr < HEIGHT - 1) g[1 + dr][1 + dc] = Cell.EMPTY
        }
        return g
    }

    fun spawnApplesAndBugs(lvl: Int, g: Array<Array<Cell>>) {
        val rnd = Random(System.nanoTime())
        val appleCount = (3 + lvl / 2).coerceAtMost(7)
        val newApples = ArrayList<Apple>()
        var guard = 0
        while (newApples.size < appleCount && guard < 200) {
            guard++
            val c = (1 until WIDTH - 1).random(rnd)
            val r = (1 until HEIGHT - 1).random(rnd)
            if (g[r][c] == Cell.DIRT && (c to r) != (1 to 1)) newApples.add(Apple(c, r, Dir.NONE))
        }
        apples = newApples
        val bugCount = (2 + lvl / 2).coerceAtMost(5)
        bugs = (0 until bugCount).map { i ->
            val corners = listOf(WIDTH - 2 to 1, 1 to HEIGHT - 2, WIDTH - 2 to HEIGHT - 2)
            val (bc, br) = corners[i % corners.size]
            Bug(Mover(bc, br, Dir.NONE, 0f), BugColors[i % BugColors.size])
        }
    }

    fun countFruit(g: Array<Array<Cell>>): Int {
        var n = 0
        for (row in g) for (c in row) if (c in FRUIT_CELLS) n++
        return n
    }

    fun resetGame(size: IntSize) {
        if (size.width <= 0 || size.height <= 0) return
        level = 1
        val g = generateGarden(level)
        grid = g
        fruitLeft = countFruit(g)
        spawnApplesAndBugs(level, g)
        frank = Mover(1, 1, Dir.NONE, 0f)
        facing = Dir.DOWN
        desiredDir = Dir.NONE
        ball = null
        ballCooldown = 0f
        rollAccum = 0f
        ballAccum = 0f
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
        countdownKey++
    }

    fun startNextLevel() {
        level++
        val g = generateGarden(level)
        grid = g
        fruitLeft = countFruit(g)
        spawnApplesAndBugs(level, g)
        frank = Mover(1, 1, Dir.NONE, 0f)
        facing = Dir.DOWN
        desiredDir = Dir.NONE
        ball = null
        ballCooldown = 0f
        rollAccum = 0f
        ballAccum = 0f
        countdownKey++
    }

    fun resumeAfterLifeLoss() {
        frank = Mover(1, 1, Dir.NONE, 0f)
        facing = Dir.DOWN
        desiredDir = Dir.NONE
        ball = null
        GameFx.loseLife(context)
        countdownKey++
    }

    LaunchedEffect(canvasSize) {
        if (!initialized) resetGame(canvasSize)
    }

    LaunchedEffect(gameOver) {
        if (gameOver && !scoreRecorded) {
            scoreRecorded = true
            vm.recordHighScore(ORCHARD_GAME_ID, score)
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
        val g = grid
        if (g.isEmpty()) return@GameLoop

        fun cellAt(col: Int, row: Int): Cell = if (col in 0 until WIDTH && row in 0 until HEIGHT) g[row][col] else Cell.WALL
        fun appleAt(col: Int, row: Int): Apple? = apples.find { it.col == col && it.row == row }

        // --- Frank : creuse la terre en avançant (galerie permanente), ramasse les fruits,
        // pousse les pommes immobiles (démarre leur roulement, sans avancer lui-même). ---
        fun canFrankEnter(col: Int, row: Int): Boolean {
            if (cellAt(col, row) == Cell.WALL) return false
            if (appleAt(col, row) != null) return false // toujours poussée, jamais traversée directement
            return true
        }

        if (frank.dir != Dir.NONE) facing = frank.dir
        if (frank.dir == Dir.NONE && desiredDir != Dir.NONE) {
            val tc = frank.col + desiredDir.dx
            val tr = frank.row + desiredDir.dy
            val blockingApple = appleAt(tc, tr)
            if (blockingApple != null) {
                val bc = tc + desiredDir.dx
                val br = tr + desiredDir.dy
                if (cellAt(bc, br) == Cell.EMPTY && appleAt(bc, br) == null) {
                    apples = apples.map { if (it == blockingApple) it.copy(dir = desiredDir) else it }
                    GameFx.hop()
                }
                desiredDir = Dir.NONE
            } else if (canFrankEnter(tc, tr)) {
                frank = frank.copy(dir = desiredDir)
            }
        }
        if (frank.dir != Dir.NONE) {
            var progress = frank.progress + FRANK_SPEED * dt
            var col = frank.col
            var row = frank.row
            var dir = frank.dir
            if (progress >= 1f) {
                progress -= 1f
                col += dir.dx
                row += dir.dy
                val entered = g[row][col]
                when {
                    entered == Cell.DIRT -> { g[row][col] = Cell.EMPTY; GameFx.chomp(true) }
                    entered in FRUIT_CELLS -> {
                        g[row][col] = Cell.EMPTY
                        score += FRUIT_SCORE
                        fruitLeft--
                        GameFx.eat()
                    }
                    else -> {}
                }
                val turning = desiredDir != Dir.NONE && desiredDir != dir && canFrankEnter(col + desiredDir.dx, row + desiredDir.dy)
                dir = when {
                    turning -> desiredDir
                    canFrankEnter(col + dir.dx, row + dir.dy) -> dir
                    else -> Dir.NONE
                }
                if (turning) desiredDir = Dir.NONE
                if (dir == Dir.NONE) progress = 0f
            }
            frank = Mover(col, row, dir, progress)
        }

        // --- Insectes : avancent vers Frank, creusent la terre eux-mêmes (plus lentement) s'il
        // le faut. Ne poussent jamais les pommes (bloquées comme un mur pour eux). ---
        val frankTile = frank.col to frank.row
        bugs = bugs.map { b ->
            var m = b.mover
            if (m.dir == Dir.NONE) {
                val opts = listOf(Dir.UP, Dir.DOWN, Dir.LEFT, Dir.RIGHT).filter {
                    cellAt(m.col + it.dx, m.row + it.dy) != Cell.WALL && appleAt(m.col + it.dx, m.row + it.dy) == null
                }
                if (opts.isNotEmpty()) {
                    m = m.copy(dir = if (Random.nextFloat() < BUG_RANDOM_CHANCE) opts.random()
                        else opts.minByOrNull { abs(m.col + it.dx - frankTile.first) + abs(m.row + it.dy - frankTile.second) }!!)
                }
            }
            val speed = if (cellAt(m.col + m.dir.dx, m.row + m.dir.dy) == Cell.DIRT) BUG_DIG_SPEED else BUG_TUNNEL_SPEED
            var progress = m.progress + speed * (1f + 0.05f * (level - 1)) * dt
            var col = m.col
            var row = m.row
            var dir = m.dir
            if (progress >= 1f) {
                progress -= 1f
                col += dir.dx
                row += dir.dy
                if (g[row][col] == Cell.DIRT) g[row][col] = Cell.EMPTY
                val opts = listOf(Dir.UP, Dir.DOWN, Dir.LEFT, Dir.RIGHT).filter {
                    it != dir.opposite() && cellAt(col + it.dx, row + it.dy) != Cell.WALL && appleAt(col + it.dx, row + it.dy) == null
                }
                val choices = if (opts.isNotEmpty()) opts
                else listOf(Dir.UP, Dir.DOWN, Dir.LEFT, Dir.RIGHT).filter { cellAt(col + it.dx, row + it.dy) != Cell.WALL && appleAt(col + it.dx, row + it.dy) == null }
                if (choices.isNotEmpty()) {
                    dir = if (Random.nextFloat() < BUG_RANDOM_CHANCE) choices.random()
                        else choices.minByOrNull { abs(col + it.dx - frankTile.first) + abs(row + it.dy - frankTile.second) }!!
                }
            }
            b.copy(mover = Mover(col, row, dir, progress))
        }

        // --- Collision Frank / insecte (contact direct = perte de vie, jamais d'écrasement à main nue). ---
        fun rpos(m: Mover) = (m.col + m.dir.dx * m.progress) to (m.row + m.dir.dy * m.progress)
        val (frx, fry) = rpos(frank)
        val touched = bugs.any { b -> val (brx, bry) = rpos(b.mover); hypot(frx - brx, fry - bry) < 0.6f }
        if (touched) {
            lives--
            if (lives <= 0) {
                offerContinueOrEnd(onGranted = { lives = 1; resumeAfterLifeLoss() })
                return@GameLoop
            }
            resumeAfterLifeLoss()
            return@GameLoop
        }

        // --- Pommes qui roulent : avancent d'une case à cadence fixe, écrasent tout ce qu'elles
        // traversent (insecte = bonus, Frank = perte de vie), s'arrêtent sur terre/mur/pomme. ---
        rollAccum += dt
        if (rollAccum >= APPLE_ROLL_STEP) {
            rollAccum -= APPLE_ROLL_STEP
            var lifeLostByApple = false
            apples = apples.map { a ->
                if (a.dir == Dir.NONE) return@map a
                val nc = a.col + a.dir.dx
                val nr = a.row + a.dir.dy
                if (cellAt(nc, nr) != Cell.EMPTY || appleAt(nc, nr) != null) return@map a.copy(dir = Dir.NONE)
                val hitBug = bugs.any { it.mover.col == nc && it.mover.row == nr }
                if (hitBug) {
                    bugs = bugs.filter { !(it.mover.col == nc && it.mover.row == nr) }
                    score += CRUSH_SCORE
                    GameFx.win()
                    return@map a.copy(col = nc, row = nr, dir = Dir.NONE)
                }
                if (nc == frank.col && nr == frank.row) {
                    lifeLostByApple = true
                    return@map a.copy(col = nc, row = nr, dir = Dir.NONE)
                }
                a.copy(col = nc, row = nr)
            }
            if (lifeLostByApple) {
                lives--
                if (lives <= 0) {
                    offerContinueOrEnd(onGranted = { lives = 1; resumeAfterLifeLoss() })
                    return@GameLoop
                }
                resumeAfterLifeLoss()
                return@GameLoop
            }
        }

        // --- Bille lancée : file tout droit, s'arrête/disparaît sur terre/mur, élimine un insecte. ---
        ballCooldown = (ballCooldown - dt).coerceAtLeast(0f)
        val currentBall = ball
        if (currentBall != null) {
            ballAccum += dt
            if (ballAccum >= BALL_STEP) {
                ballAccum -= BALL_STEP
                val nc = currentBall.col + currentBall.dir.dx
                val nr = currentBall.row + currentBall.dir.dy
                val hitBug = bugs.any { it.mover.col == nc && it.mover.row == nr }
                when {
                    hitBug -> {
                        bugs = bugs.filter { !(it.mover.col == nc && it.mover.row == nr) }
                        score += CRUSH_SCORE
                        GameFx.win()
                        ball = null
                        ballCooldown = BALL_COOLDOWN
                    }
                    cellAt(nc, nr) != Cell.EMPTY && cellAt(nc, nr) != Cell.FRUIT -> {
                        ball = null
                        ballCooldown = BALL_COOLDOWN
                    }
                    else -> ball = currentBall.copy(col = nc, row = nr)
                }
            }
        }

        // --- Niveau terminé : tous les fruits ramassés. ---
        if (fruitLeft <= 0) {
            levelsWon++
            vm.earnPoints(POINTS_PER_LEVEL)
            GameAds.onLevelCleared()
            startNextLevel()
        }
    }

    GameScaffold(
        title = "Croque-Fruits — Niv. $level — ❤ $lives",
        score = score,
        onExit = onExit,
        gameOver = gameOver,
        pointsEarned = levelsWon * POINTS_PER_LEVEL + multiplierBonus,
        bestScore = bestScores[ORCHARD_GAME_ID] ?: 0,
        onRestart = { resetGame(canvasSize) },
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
                .pointerInput(Unit) {
                    detectTapGestures {
                        if (ball == null && ballCooldown <= 0f) {
                            ball = Ball(frank.col + facing.dx, frank.row + facing.dy, facing)
                            ballAccum = 0f
                            GameFx.blip()
                        }
                    }
                }
        ) {
            drawRect(color = Color(0xFF0B140B))
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
                    Cell.FRUIT -> {
                        drawRect(DirtColor, Offset(left, top), Size(tile, tile))
                        val fc = cx(col.toFloat()); val fcy = cy(row.toFloat()); val r = tile * 0.3f
                        drawCircle(FruitRed, r, Offset(fc, fcy))
                        drawCircle(FruitPale, r * 0.55f, Offset(fc - r * 0.25f, fcy - r * 0.25f))
                    }
                    Cell.FRUIT_ORANGE -> {
                        drawRect(DirtColor, Offset(left, top), Size(tile, tile))
                        val fc = cx(col.toFloat()); val fcy = cy(row.toFloat()); val r = tile * 0.3f
                        drawCircle(OrangeColor, r, Offset(fc, fcy))
                        drawCircle(OrangePale, r * 0.5f, Offset(fc - r * 0.22f, fcy - r * 0.22f))
                    }
                    Cell.FRUIT_BANANA -> {
                        drawRect(DirtColor, Offset(left, top), Size(tile, tile))
                        val fc = cx(col.toFloat()); val fcy = cy(row.toFloat()); val r = tile * 0.3f
                        // Forme incurvée simplifiée : deux disques décalés en diagonale + un petit
                        // bout foncé, plus lisible qu'un arc dessiné sur une aussi petite case.
                        drawCircle(BananaColor, r * 0.62f, Offset(fc - r * 0.28f, fcy - r * 0.18f))
                        drawCircle(BananaColor, r * 0.62f, Offset(fc + r * 0.28f, fcy + r * 0.18f))
                        drawCircle(BananaDark, r * 0.12f, Offset(fc + r * 0.5f, fcy + r * 0.35f))
                    }
                    Cell.FRUIT_CHERRY -> {
                        drawRect(DirtColor, Offset(left, top), Size(tile, tile))
                        val fc = cx(col.toFloat()); val fcy = cy(row.toFloat()); val r = tile * 0.22f
                        drawCircle(CherryColor, r, Offset(fc - r * 0.65f, fcy + r * 0.4f))
                        drawCircle(CherryColor, r, Offset(fc + r * 0.65f, fcy + r * 0.4f))
                        drawCircle(CherryLeaf, r * 0.5f, Offset(fc, fcy - r * 0.9f))
                    }
                    Cell.EMPTY -> {}
                }
            }

            apples.forEach { a ->
                val acx = cx(a.col.toFloat()); val acy = cy(a.row.toFloat()); val r = tile * 0.38f
                drawCircle(AppleColor, r, Offset(acx, acy))
                drawCircle(FruitPale, r * 0.3f, Offset(acx - r * 0.3f, acy - r * 0.3f))
                drawRect(AppleLeaf, Offset(acx - r * 0.08f, acy - r * 1.05f), Size(r * 0.16f, r * 0.3f))
            }

            ball?.let { bl ->
                drawCircle(BallColor, tile * 0.16f, Offset(cx(bl.col.toFloat()), cy(bl.row.toFloat())))
            }

            // Frank (petit personnage simplifié : tête + corps, plus lisible qu'un disque façon Glouton).
            val (frx, fry) = (frank.col + frank.dir.dx * frank.progress) to (frank.row + frank.dir.dy * frank.progress)
            val fcx = cx(frx); val fcy2 = cy(fry); val fr = tile * 0.32f
            drawRect(FrankColor, Offset(fcx - fr * 0.5f, fcy2 - fr * 0.1f), Size(fr, fr * 1.1f))
            drawCircle(FrankColor, fr * 0.5f, Offset(fcx, fcy2 - fr * 0.55f))

            bugs.forEach { b ->
                val m = b.mover
                val bcx = cx(m.col + m.dir.dx * m.progress)
                val bcy = cy(m.row + m.dir.dy * m.progress)
                val br = tile * 0.36f
                drawCircle(b.color, br, Offset(bcx, bcy))
                drawCircle(Color.White, br * 0.22f, Offset(bcx - br * 0.35f, bcy - br * 0.1f))
                drawCircle(Color.White, br * 0.22f, Offset(bcx + br * 0.35f, bcy - br * 0.1f))
            }
        }
        CountdownOverlay(countdown)
    }
}
