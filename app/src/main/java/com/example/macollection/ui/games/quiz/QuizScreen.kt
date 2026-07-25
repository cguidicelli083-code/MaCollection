package com.example.macollection.ui.games.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macollection.data.AppPrefs
import com.example.macollection.data.GameShopCatalog
import com.example.macollection.data.QuizGenerator
import com.example.macollection.ui.GameViewModel
import com.example.macollection.ui.games.engine.GameScaffold
import com.example.macollection.ui.theme.NeonCyan
import com.example.macollection.ui.theme.NeonPink
import com.example.macollection.ui.theme.NeonPurple

private const val QUESTIONS_PER_SESSION = 10

/**
 * Quiz à choix multiples généré depuis l'encyclopédie (voir QuizGenerator). La difficulté
 * monte automatiquement au fil des sessions terminées (niveau 1 → 3), et les bonnes réponses
 * rapportent d'autant plus de points que le niveau est élevé.
 */
@Composable
fun QuizScreen(vm: GameViewModel, onExit: () -> Unit) {
    val context = LocalContext.current
    var level by remember { mutableIntStateOf(AppPrefs.quizLevel.value) }
    var questions by remember { mutableStateOf(QuizGenerator.generateSession(level, QUESTIONS_PER_SESSION)) }
    var index by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var pointsAwarded by remember { mutableStateOf(false) }
    val bestScores by vm.highScores.collectAsState()

    val gameOver = index >= questions.size
    val pointsEarned = correctCount * QuizGenerator.POINTS_PER_CORRECT
    val leveledUp = correctCount >= QuizGenerator.PASS_THRESHOLD && level < QuizGenerator.MAX_LEVEL

    LaunchedEffect(gameOver) {
        if (gameOver && !pointsAwarded) {
            pointsAwarded = true
            vm.earnPoints(pointsEarned)
            vm.recordHighScore(GameShopCatalog.QUIZ, correctCount)
            AppPrefs.recordQuizCompleted(context)
            // 8 bonnes réponses ou plus : on débloque et passe au niveau supérieur (max 10).
            if (leveledUp) AppPrefs.setQuizLevel(context, level + 1)
        }
    }

    GameScaffold(
        title = if (gameOver && leveledUp) "Quiz — Niveau ${level + 1} débloqué ! 🎉"
        else "Quiz — Niv. $level · ${QuizGenerator.levelName(level)}",
        score = correctCount,
        onExit = onExit,
        gameOver = gameOver,
        pointsEarned = pointsEarned,
        bestScore = bestScores[GameShopCatalog.QUIZ] ?: 0,
        onRestart = {
            level = AppPrefs.quizLevel.value
            questions = QuizGenerator.generateSession(level, QUESTIONS_PER_SESSION)
            index = 0
            correctCount = 0
            selectedIndex = null
            pointsAwarded = false
        }
    ) {
        if (!gameOver) {
            val question = questions[index]
            Column(
                Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Question ${index + 1} / ${questions.size}", color = Color.White.copy(alpha = 0.7f))
                    Text(
                        "+${QuizGenerator.POINTS_PER_CORRECT} pts • ${correctCount}/${QuizGenerator.PASS_THRESHOLD} pour le niveau sup.",
                        color = NeonPink.copy(alpha = 0.9f), fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(question.text, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))
                question.choices.forEachIndexed { i, choice ->
                    val selected = selectedIndex
                    val containerColor = when {
                        selected == null -> NeonPurple.copy(alpha = 0.35f)
                        i == question.correctIndex -> Color(0xFF1E8E3E)
                        i == selected -> Color(0xFFB3261E)
                        else -> NeonPurple.copy(alpha = 0.15f)
                    }
                    Button(
                        onClick = {
                            if (selected == null) {
                                selectedIndex = i
                                if (i == question.correctIndex) correctCount++
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = containerColor)
                    ) { Text(choice, color = Color.White) }
                }
                Spacer(Modifier.height(20.dp))
                if (selectedIndex != null) {
                    Button(
                        onClick = { index++; selectedIndex = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        val label = if (index == questions.size - 1) "Voir le résultat" else "Question suivante"
                        Text(label, color = Color.Black)
                    }
                }
            }
        }
    }
}
