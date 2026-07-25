package com.example.macollection.data

/** Une question de quiz à choix multiples générée depuis l'encyclopédie des consoles. */
data class QuizQuestion(
    val text: String,
    val choices: List<String>,
    val correctIndex: Int
)

/**
 * Génère des questions à choix multiples à partir des données réelles de l'app : le catalogue
 * de consoles ([consolePresets], 165 fiches), les listes de jeux vérifiées ([CuratedGames]) et
 * les faits créateurs vérifiés ([ConsoleTrivia]). Jamais de données inventées — les mauvaises
 * réponses sont toujours tirées d'autres entrées réelles.
 *
 * 10 niveaux de difficulté progressifs (voir [levelName]), du plus simple (marque, salon/portable)
 * au plus pointu (CPU/mémoire, créateurs, chronologie, années à ±1 an). On passe au niveau suivant
 * en obtenant au moins [PASS_THRESHOLD] bonnes réponses dans une session.
 */
object QuizGenerator {

    const val MAX_LEVEL = 10

    /** Points fixes gagnés par bonne réponse (barème uniforme, quel que soit le niveau). */
    const val POINTS_PER_CORRECT = 2

    /** Bonnes réponses minimales pour débloquer le niveau suivant. */
    const val PASS_THRESHOLD = 8

    /** Nom de chaque niveau (1 → 10). */
    private val LEVEL_NAMES = listOf(
        "Débutant", "Novice", "Amateur", "Connaisseur", "Confirmé",
        "Passionné", "Expert", "Vétéran", "Maître", "Expert ultime"
    )

    fun levelName(level: Int): String = LEVEL_NAMES.getOrElse(level - 1) { "Niveau $level" }

    /** Génère une session de [count] questions du niveau [level], mélangées. */
    fun generateSession(level: Int, count: Int = 10): List<QuizQuestion> {
        val pool = consolePresets.shuffled()
        val presets = pool.take(count)
        return presets.mapIndexed { i, preset -> randomQuestionFor(preset, pool, level, i) }.shuffled()
    }

    private fun randomQuestionFor(preset: ConsolePreset, pool: List<ConsolePreset>, level: Int, index: Int): QuizQuestion {
        // Créateurs/chronologie (les plus pointues) à partir du niveau 7, glissées régulièrement.
        if (level >= 7 && index % 3 == 0) {
            return if (index % 2 == 0) creatorQuestion() else chronologyQuestion(pool)
        }
        // Types disponibles selon le niveau : simples en bas (marque, salon/portable), techniques
        // en haut (CPU/mémoire), avec pondération croissante des types difficiles.
        val types = buildList {
            add("brand")
            // « Salon ou portable ? » seulement pour ces deux catégories : un ordinateur (MSX, C64,
            // Amiga...) n'est ni l'un ni l'autre, la question n'aurait pas de bonne réponse.
            if (level <= 6 && (preset.kind == "Salon" || preset.kind == "Portable")) add("kind")
            if (level >= 2) add("year")
            if (level >= 3 && CuratedGames.gamesFor(preset.name) != null) {
                add("game"); if (level >= 4) add("game")
            }
            if (level >= 5 && preset.cpu.isNotBlank()) { add("cpu"); if (level >= 8) add("cpu") }
            if (level >= 6 && preset.memory.isNotBlank()) { add("memory"); if (level >= 9) add("memory") }
            if (isEmpty()) add("brand")
        }
        return when (types.random()) {
            "cpu" -> cpuQuestion(preset, pool)
            "memory" -> memoryQuestion(preset, pool)
            "game" -> gameQuestion(preset, pool)
            "brand" -> brandQuestion(preset, pool)
            "kind" -> kindQuestion(preset)
            else -> yearQuestion(preset, pool, level)
        }
    }

    private fun build(text: String, correct: String, distractors: List<String>): QuizQuestion {
        val choices = (distractors.distinct().filter { it != correct }.take(3) + correct).shuffled()
        return QuizQuestion(text, choices, choices.indexOf(correct))
    }

    private fun yearQuestion(preset: ConsolePreset, pool: List<ConsolePreset>, level: Int): QuizQuestion {
        // Plus le niveau monte, plus les mauvaises années sont proches de la vraie (± 1 an au max).
        val maxGap = when {
            level <= 2 -> Int.MAX_VALUE
            level <= 4 -> 8
            level <= 6 -> 5
            level <= 8 -> 3
            level == 9 -> 2
            else -> 1
        }
        val near = pool.map { it.year }.distinct()
            .filter { it != preset.year && kotlin.math.abs(it - preset.year) <= maxGap }
            .shuffled()
        val far = pool.map { it.year }.distinct().filter { it != preset.year }.shuffled()
        val distractors = (near + far).distinct().take(3).map { it.toString() }
        return build(
            "En quelle année est sortie la ${preset.brand} ${preset.name} ?",
            preset.year.toString(), distractors
        )
    }

    private fun brandQuestion(preset: ConsolePreset, pool: List<ConsolePreset>): QuizQuestion =
        build(
            "Quelle marque a fabriqué la console « ${preset.name} » ?",
            preset.brand,
            pool.map { it.brand }.distinct().filter { it != preset.brand }.shuffled()
        )

    private fun kindQuestion(preset: ConsolePreset): QuizQuestion {
        val choices = listOf("Salon", "Portable").shuffled()
        return QuizQuestion(
            "La ${preset.brand} ${preset.name} est-elle une console de salon ou portable ?",
            choices, choices.indexOf(preset.kind)
        )
    }

    private fun cpuQuestion(preset: ConsolePreset, pool: List<ConsolePreset>): QuizQuestion =
        build(
            "Quel processeur équipe la ${preset.brand} ${preset.name} ?",
            preset.cpu,
            pool.map { it.cpu }.distinct().filter { it.isNotBlank() && it != preset.cpu }.shuffled()
        )

    private fun memoryQuestion(preset: ConsolePreset, pool: List<ConsolePreset>): QuizQuestion =
        build(
            "Quelle quantité de mémoire embarque la ${preset.brand} ${preset.name} ?",
            preset.memory,
            pool.map { it.memory }.distinct().filter { it.isNotBlank() && it != preset.memory }.shuffled()
        )

    /** « Lequel de ces jeux est sorti sur X ? » — bonne réponse et leurres tirés de [CuratedGames]. */
    private fun gameQuestion(preset: ConsolePreset, pool: List<ConsolePreset>): QuizQuestion {
        val ownGames = CuratedGames.gamesFor(preset.name).orEmpty()
        val correct = ownGames.random()
        val otherGames = CuratedGames.byConsole
            .filterKeys { it != preset.name }
            .values.flatten()
            .filter { it !in ownGames }
            .shuffled()
        return build(
            "Lequel de ces jeux est sorti sur ${preset.brand} ${preset.name} ?",
            correct, otherGames
        )
    }

    /** Question créateur (faits vérifiés uniquement, voir [ConsoleTrivia]). */
    private fun creatorQuestion(): QuizQuestion {
        val fact = ConsoleTrivia.creatorFacts.random()
        return build(
            "Qui a créé ${fact.consoleLabel} ?",
            fact.creator,
            ConsoleTrivia.distractorPeople.filter { it != fact.creator }.shuffled()
        )
    }

    /** « Laquelle de ces consoles est sortie en premier ? » — 4 consoles d'années distinctes. */
    private fun chronologyQuestion(pool: List<ConsolePreset>): QuizQuestion {
        val picks = pool.distinctBy { it.year }.shuffled().take(4)
        val earliest = picks.minBy { it.year }
        val labels = picks.map { "${it.brand} ${it.name}" }.shuffled()
        return QuizQuestion(
            "Laquelle de ces consoles est sortie en premier ?",
            labels, labels.indexOf("${earliest.brand} ${earliest.name}")
        )
    }
}
