package com.example.macollection.ui.games.pacman

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
import androidx.compose.ui.geometry.CornerRadius
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
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private const val WIDTH = 11
private const val HEIGHT = 15
private const val LIVES_START = 3
// Une vie supplémentaire offerte contre une pub, jusqu'à 3 fois par partie.
private const val MAX_CONTINUES = 3
private const val DOT_SCORE = 10
private const val PELLET_SCORE = 50
private const val GHOST_SCORE = 200
private const val POINTS_PER_LEVEL = 10
private const val FRIGHT_TIME = 6f
private const val PAC_SPEED = 5.5f
private const val GHOST_SPEED = 4.6f
// Probabilité qu'un fantôme (hors mode apeuré) choisisse une direction au hasard plutôt que la
// meilleure vers Pacman : sans ça, il fonçait TOUJOURS droit dessus à chaque intersection (trop
// prévisible/dirigé). Garde une vraie menace (se rapproche globalement) sans être parfaitement
// dirigé à chaque choix.
private const val GHOST_RANDOM_CHANCE = 0.35f

private val WallBlue = Color(0xFF3B4CFF)
private val DotColor = Color(0xFFFFE08A)
private val PacYellow = Color(0xFFFFD400)
private val FrightBlue = Color(0xFF2A2AE0)
private val GhostRed = Color(0xFFFF4D4D)
private val GhostPink = Color(0xFFFF7DD1)
private val PupilColor = Color(0xFF1A1A3A)

/** Cases sans point au démarrage (départ Pacman + maison des fantômes). */
private val NoDotCells = setOf(5 to 13, 4 to 7, 5 to 7, 6 to 7)
private val PelletCells = setOf(1 to 1, 9 to 1, 1 to 13, 9 to 13)

private enum class Dir(val dx: Int, val dy: Int) {
    UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0), NONE(0, 0);

    fun opposite() = when (this) {
        UP -> DOWN; DOWN -> UP; LEFT -> RIGHT; RIGHT -> LEFT; NONE -> NONE
    }
}

private data class Mover(val col: Int, val row: Int, val dir: Dir, val progress: Float)
private data class Ghost(val mover: Mover, val color: Color)

/** Nombre de formes de labyrinthe différentes, parcourues au fil des niveaux. */
private const val MAZE_PATTERNS = 5

/**
 * Un pilier (case colonne ET ligne paires) est-il présent pour ce niveau ? On ne place JAMAIS de
 * mur sur une case de coordonnée impaire : quel que soit le motif choisi, le labyrinthe reste donc
 * toujours entièrement connecté (Pacman et les fantômes peuvent atteindre toutes les cases).
 * Le motif change à chaque niveau pour renouveler la forme du plateau.
 */
private fun pillarPresent(col: Int, row: Int, level: Int): Boolean {
    val ci = col / 2   // index de colonne de pilier (1..4)
    val ri = row / 2   // index de ligne de pilier (1..6)
    return when ((level - 1).mod(MAZE_PATTERNS)) {
        0 -> true                       // grille pleine (motif d'origine)
        1 -> (ci + ri) % 2 == 0         // damier
        2 -> ci % 2 == 1                // colonnes de piliers (couloirs verticaux)
        3 -> ri % 2 == 1                // rangées de piliers (couloirs horizontaux)
        else -> (ci + ri) % 3 != 0      // diagonales clairsemées
    }
}

/** Mur si bord du plateau, ou pilier intérieur présent pour ce niveau → labyrinthe à grille. */
private fun isWall(col: Int, row: Int, level: Int): Boolean {
    if (col <= 0 || col >= WIDTH - 1 || row <= 0 || row >= HEIGHT - 1) return true
    if (col % 2 == 0 && row % 2 == 0) return pillarPresent(col, row, level)
    return false
}

private fun manhattan(col: Int, row: Int, target: Pair<Int, Int>) =
    abs(col - target.first) + abs(row - target.second)

/**
 * Pacman : labyrinthe à grille (murs = bords + piliers). Glisser dans une direction pour
 * orienter Pacman (il tourne aux intersections). Manger tous les points termine le niveau
 * (+10 points), les pastilles rendent les fantômes vulnérables quelques secondes (on peut alors
 * les manger pour un bonus). Toucher un fantôme non vulnérable coûte une vie. IA de poursuite
 * simple mais lisible ; chaque niveau accélère un peu les fantômes.
 */
@Composable
fun PacmanScreen(vm: GameViewModel, onExit: () -> Unit) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    var pac by remember { mutableStateOf(Mover(5, 13, Dir.NONE, 0f)) }
    var ghosts by remember { mutableStateOf<List<Ghost>>(emptyList()) }
    var dots by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var pellets by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var desiredDir by remember { mutableStateOf(Dir.NONE) }
    var frightenedTimer by remember { mutableFloatStateOf(0f) }
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
    var chompTick by remember { mutableStateOf(false) }
    var frightWarned by remember { mutableStateOf(false) }
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

    fun freshDots(): Pair<Set<Int>, Set<Int>> {
        val d = HashSet<Int>()
        val p = HashSet<Int>()
        for (row in 1 until HEIGHT - 1) for (col in 1 until WIDTH - 1) {
            if (isWall(col, row, level)) continue
            val k = row * WIDTH + col
            when {
                (col to row) in PelletCells -> p.add(k)
                (col to row) !in NoDotCells -> d.add(k)
            }
        }
        return d to p
    }

    fun startMovers(): Pair<Mover, List<Ghost>> {
        val pacStart = Mover(5, 13, Dir.NONE, 0f)
        val g = listOf(
            Ghost(Mover(4, 7, Dir.LEFT, 0f), GhostRed),
            Ghost(Mover(5, 7, Dir.UP, 0f), GhostPink),
            Ghost(Mover(6, 7, Dir.RIGHT, 0f), NeonCyan)
        )
        return pacStart to g
    }

    fun resetGame(size: IntSize) {
        if (size.width <= 0 || size.height <= 0) return
        val (d, p) = freshDots()
        dots = d
        pellets = p
        val (np, ng) = startMovers()
        pac = np
        ghosts = ng
        desiredDir = Dir.NONE
        frightenedTimer = 0f
        animTime = 0f
        score = 0
        lives = LIVES_START
        level = 1
        levelsWon = 0
        continuesUsed = 0
        continueOffer = null
        multiplierClaimed = false
        multiplierBonus = 0
        gameOver = false
        scoreRecorded = false
        initialized = true
        countdownKey++ // décompte 3-2-1 avant de (re)lancer la partie
    }

    LaunchedEffect(canvasSize) {
        if (!initialized) resetGame(canvasSize)
    }

    LaunchedEffect(gameOver) {
        if (gameOver && !scoreRecorded) {
            scoreRecorded = true
            vm.recordHighScore(GameShopCatalog.PACMAN, score)
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

    GameLoop(running = initialized && !gameOver && countdown == 0 && continueOffer == null) { dt ->
        animTime += dt
        val prevFright = frightenedTimer
        frightenedTimer = (frightenedTimer - dt).coerceAtLeast(0f)
        // Alerte ~2 s avant que les fantômes redeviennent menaçants (son + clignotement).
        if (prevFright > 2f && frightenedTimer <= 2f && frightenedTimer > 0f && !frightWarned) {
            frightWarned = true
            GameFx.frightWarn()
        }

        fun isOpen(col: Int, row: Int) = !isWall(col, row, level)
        fun rpos(m: Mover) = (m.col + m.dir.dx * m.progress) to (m.row + m.dir.dy * m.progress)

        // --- Pacman : démarre à l'arrêt, demi-tour instantané, sinon tourne aux centres. ---
        if (pac.dir == Dir.NONE && desiredDir != Dir.NONE && isOpen(pac.col + desiredDir.dx, pac.row + desiredDir.dy)) {
            pac = pac.copy(dir = desiredDir)
        } else if (pac.dir != Dir.NONE && desiredDir == pac.dir.opposite()) {
            // Demi-tour immédiat, sans attendre le centre de case (réactif comme l'arcade).
            pac = Mover(pac.col + pac.dir.dx, pac.row + pac.dir.dy, desiredDir, 1f - pac.progress)
            desiredDir = Dir.NONE
        }
        if (pac.dir != Dir.NONE) {
            var progress = pac.progress + PAC_SPEED * dt
            var col = pac.col
            var row = pac.row
            var dir = pac.dir
            if (progress >= 1f) {
                progress -= 1f
                col += dir.dx
                row += dir.dy
                val k = row * WIDTH + col
                if (k in dots) {
                    dots = dots - k
                    score += DOT_SCORE
                    GameFx.chomp(chompTick)
                    chompTick = !chompTick
                }
                if (k in pellets) {
                    pellets = pellets - k
                    score += PELLET_SCORE
                    frightenedTimer = FRIGHT_TIME
                    frightWarned = false
                    GameFx.frighten()
                }
                val turning = desiredDir != Dir.NONE && desiredDir != dir &&
                    isOpen(col + desiredDir.dx, row + desiredDir.dy)
                dir = when {
                    turning -> desiredDir
                    isOpen(col + dir.dx, row + dir.dy) -> dir
                    else -> Dir.NONE
                }
                // Virage pris → on oublie l'intention, pour ne plus tourner tout seul dans le
                // prochain couloir latéral (cause du déplacement "buggé" signalé).
                if (turning) desiredDir = Dir.NONE
                if (dir == Dir.NONE) progress = 0f
            }
            pac = Mover(col, row, dir, progress)
        }

        val pacTile = pac.col to pac.row
        val fright = frightenedTimer > 0f
        val gspeed = (GHOST_SPEED * (1f + 0.08f * (level - 1))).let { if (fright) it * 0.55f else it }

        // --- Fantômes : avancent, choisissent leur direction aux centres (poursuite / fuite). ---
        ghosts = ghosts.map { g ->
            var m = g.mover
            if (m.dir == Dir.NONE) m = m.copy(dir = Dir.UP)
            var progress = m.progress + gspeed * dt
            var col = m.col
            var row = m.row
            var dir = m.dir
            if (progress >= 1f) {
                progress -= 1f
                col += dir.dx
                row += dir.dy
                val opts = listOf(Dir.UP, Dir.DOWN, Dir.LEFT, Dir.RIGHT).filter {
                    it != dir.opposite() && isOpen(col + it.dx, row + it.dy)
                }
                val choices = if (opts.isNotEmpty()) opts
                else listOf(dir.opposite()).filter { isOpen(col + it.dx, row + it.dy) }
                if (choices.isNotEmpty()) {
                    dir = when {
                        fright -> choices.maxByOrNull { manhattan(col + it.dx, row + it.dy, pacTile) }!!
                        Random.nextFloat() < GHOST_RANDOM_CHANCE -> choices.random()
                        else -> choices.minByOrNull { manhattan(col + it.dx, row + it.dy, pacTile) }!!
                    }
                }
            }
            g.copy(mover = Mover(col, row, dir, progress))
        }

        // --- Collisions Pacman / fantômes. ---
        val (prx, pry) = rpos(pac)
        var died = false
        val rebuilt = ArrayList<Ghost>(ghosts.size)
        for (g in ghosts) {
            if (died) {
                rebuilt.add(g)
                continue
            }
            val (grx, gry) = rpos(g.mover)
            if (hypot(prx - grx, pry - gry) < 0.6f) {
                if (frightenedTimer > 0f) {
                    score += GHOST_SCORE
                    rebuilt.add(g.copy(mover = Mover(5, 7, Dir.UP, 0f)))
                } else {
                    died = true
                    rebuilt.add(g)
                }
            } else {
                rebuilt.add(g)
            }
        }
        // Perdre une vie recommence le niveau en cours (points/pastilles + positions).
        fun resumeAfterLifeLoss() {
            val (d, p) = freshDots()
            dots = d
            pellets = p
            val (np, ng) = startMovers()
            pac = np
            ghosts = ng
            desiredDir = Dir.NONE
            frightenedTimer = 0f
            GameFx.loseLife(context)
            countdownKey++
        }
        if (died) {
            lives--
            if (lives <= 0) {
                offerContinueOrEnd(onGranted = { lives = 1; resumeAfterLifeLoss() })
                return@GameLoop
            }
            resumeAfterLifeLoss()
        } else {
            ghosts = rebuilt
        }

        // --- Niveau terminé : +10 points, on régénère et on accélère. ---
        if (dots.isEmpty() && pellets.isEmpty()) {
            levelsWon++
            vm.earnPoints(POINTS_PER_LEVEL)
            GameFx.win()
            level++
            GameAds.onLevelCleared()
            val (d, p) = freshDots()
            dots = d
            pellets = p
            val (np, ng) = startMovers()
            pac = np
            ghosts = ng
            desiredDir = Dir.NONE
            frightenedTimer = 0f
        }
    }

    GameScaffold(
        title = "Glouton — Niv. $level — ❤ $lives",
        score = score,
        onExit = onExit,
        gameOver = gameOver,
        pointsEarned = levelsWon * POINTS_PER_LEVEL + multiplierBonus,
        bestScore = bestScores[GameShopCatalog.PACMAN] ?: 0,
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
                        if (abs(dragAmount.x) > abs(dragAmount.y)) {
                            desiredDir = if (dragAmount.x > 0f) Dir.RIGHT else Dir.LEFT
                        } else if (dragAmount.y != 0f) {
                            desiredDir = if (dragAmount.y > 0f) Dir.DOWN else Dir.UP
                        }
                    }
                }
        ) {
            drawRect(color = Color(0xFF0B0B14))
            val w = size.width
            val h = size.height
            val tile = min(w / WIDTH, h / HEIGHT)
            val ox = (w - tile * WIDTH) / 2f
            val oy = (h - tile * HEIGHT) / 2f
            fun cx(c: Float) = ox + (c + 0.5f) * tile
            fun cy(r: Float) = oy + (r + 0.5f) * tile

            // Murs.
            val inset = tile * 0.08f
            for (row in 0 until HEIGHT) for (col in 0 until WIDTH) {
                if (isWall(col, row, level)) {
                    drawRoundRect(
                        color = WallBlue,
                        topLeft = Offset(ox + col * tile + inset, oy + row * tile + inset),
                        size = Size(tile - 2 * inset, tile - 2 * inset),
                        cornerRadius = CornerRadius(tile * 0.22f, tile * 0.22f)
                    )
                }
            }

            // Points et pastilles.
            dots.forEach { k ->
                drawCircle(DotColor, tile * 0.08f, Offset(cx((k % WIDTH).toFloat()), cy((k / WIDTH).toFloat())))
            }
            val pelletPulse = 0.8f + 0.25f * sin(animTime * 6f)
            pellets.forEach { k ->
                drawCircle(DotColor, tile * 0.2f * pelletPulse, Offset(cx((k % WIDTH).toFloat()), cy((k / WIDTH).toFloat())))
            }

            // Pacman (disque avec bouche animée orientée selon la direction).
            val (prx, pry) = (pac.col + pac.dir.dx * pac.progress) to (pac.row + pac.dir.dy * pac.progress)
            val pcx = cx(prx)
            val pcy = cy(pry)
            val pr = tile * 0.42f
            val mouth = 8f + 32f * abs(sin(animTime * 10f))
            val centerAngle = when (pac.dir) {
                Dir.LEFT -> 180f
                Dir.UP -> 270f
                Dir.DOWN -> 90f
                else -> 0f
            }
            drawArc(
                color = PacYellow,
                startAngle = centerAngle + mouth,
                sweepAngle = 360f - 2f * mouth,
                useCenter = true,
                topLeft = Offset(pcx - pr, pcy - pr),
                size = Size(pr * 2f, pr * 2f)
            )

            // Fantômes.
            ghosts.forEach { g ->
                val m = g.mover
                val gcx = cx(m.col + m.dir.dx * m.progress)
                val gcy = cy(m.row + m.dir.dy * m.progress)
                val gr = tile * 0.42f
                // Fantômes vulnérables : bleus, puis clignotent blanc/bleu dans les 2 dernières
                // secondes pour prévenir qu'ils vont redevenir menaçants.
                val body = when {
                    frightenedTimer <= 0f -> g.color
                    frightenedTimer <= 2f && (frightenedTimer * 6f).toInt() % 2 == 0 -> Color.White
                    else -> FrightBlue
                }
                drawRoundRect(
                    color = body,
                    topLeft = Offset(gcx - gr, gcy - gr),
                    size = Size(gr * 2f, gr * 1.9f),
                    cornerRadius = CornerRadius(gr, gr)
                )
                val eyeR = gr * 0.28f
                val ex = gr * 0.38f
                val ey = -gr * 0.15f
                val pdx = m.dir.dx * eyeR * 0.4f
                val pdy = m.dir.dy * eyeR * 0.4f
                drawCircle(Color.White, eyeR, Offset(gcx - ex, gcy + ey))
                drawCircle(Color.White, eyeR, Offset(gcx + ex, gcy + ey))
                drawCircle(PupilColor, eyeR * 0.5f, Offset(gcx - ex + pdx, gcy + ey + pdy))
                drawCircle(PupilColor, eyeR * 0.5f, Offset(gcx + ex + pdx, gcy + ey + pdy))
            }
        }
        CountdownOverlay(countdown)
    }
}
