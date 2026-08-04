package com.example.macollection.ui.games.engine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macollection.ui.theme.themedGradient

/**
 * Proposition de « continuer la partie » via une pub récompensée, affichée à la place de l'écran
 * de fin de partie tant qu'il reste des continuations disponibles (voir chaque mini-jeu pour le
 * nombre maximal et l'effet exact — vie supplémentaire, objectif repoussé...). [onAccept] lance la
 * pub (ou continue directement en Premium) ; [onDecline] termine la partie pour de bon.
 */
data class ContinueOffer(
    val message: String,
    val buttonLabel: String,
    val onAccept: () -> Unit,
    val onDecline: () -> Unit
)

/**
 * Scaffold commun à tous les mini-jeux : fond dégradé néon, en-tête (retour + titre + score),
 * zone de jeu ([content], généralement un [androidx.compose.foundation.Canvas]), et un écran de
 * fin de partie superposé quand [gameOver] est vrai (score, points gagnés, rejouer/retour). Si
 * [continueOffer] est non nul, il est affiché à la place (proposition de pub pour continuer).
 *
 * [onWatchAdForMultiplier], si non nul, affiche un bouton « tripler mes points » sur l'écran de
 * fin de partie (Multiplicateur de Score) : l'appelant lance la pub récompensée et crédite le
 * bonus lui-même, puis repasse ce paramètre à null (ex. via un état "déjà réclamé" pour cette
 * partie) pour faire disparaître le bouton une fois utilisé.
 *
 * [onResetLevel], si non nul, affiche une icône dans l'en-tête (à côté du retour), utilisable
 * PENDANT la partie (contrairement à [onRestart], réservé à l'écran de fin) : pour les jeux où un
 * élément physique peut immobiliser le joueur sans issue (ex. un rocher tombé dans le Mineur,
 * bloquant l'accès sans pouvoir être poussé ni détruit), c'est la porte de sortie si la partie en
 * cours est bloquée. Ouvre un choix entre recommencer la partie (perte du score en cours) et, si
 * [onUndoLastAction] est également non nul, annuler seulement le dernier changement (rocher/
 * diamant qui vient de tomber, dernier déplacement...) sans tout reperdre — utile quand seule la
 * toute dernière action a bloqué la partie.
 */
@Composable
fun GameScaffold(
    title: String,
    score: Int,
    onExit: () -> Unit,
    gameOver: Boolean,
    pointsEarned: Int,
    bestScore: Int,
    onRestart: () -> Unit,
    continueOffer: ContinueOffer? = null,
    onWatchAdForMultiplier: (() -> Unit)? = null,
    onResetLevel: (() -> Unit)? = null,
    onUndoLastAction: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    var showResetConfirm by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(themedGradient())) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onExit) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                    if (onResetLevel != null) {
                        IconButton(onClick = { showResetConfirm = true }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Bloqué ? Recommencer ou annuler le dernier coup", tint = Color.White)
                        }
                    }
                }
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Score : $score", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Box(Modifier.weight(1f).fillMaxWidth()) {
                content()
            }
        }
        if (showResetConfirm) {
            AlertDialog(
                onDismissRequest = { showResetConfirm = false },
                title = { Text("Bloqué ?") },
                text = {
                    Text(
                        if (onUndoLastAction != null)
                            "Annulez juste le dernier changement (ex. un rocher qui vient de tomber), ou recommencez la partie entière (score et progression actuels perdus)."
                        else
                            "Recommencer la partie ? Le score et la progression actuels seront perdus."
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showResetConfirm = false; onResetLevel?.invoke() }) { Text("Recommencer la partie") }
                },
                dismissButton = {
                    Row {
                        if (onUndoLastAction != null) {
                            TextButton(onClick = { showResetConfirm = false; onUndoLastAction.invoke() }) { Text("Revenir en arrière") }
                        }
                        TextButton(onClick = { showResetConfirm = false }) { Text("Annuler") }
                    }
                }
            )
        }
        if (continueOffer != null) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Partie terminée ?", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(continueOffer.message, color = Color.White)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = continueOffer.onAccept, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                        Text(continueOffer.buttonLabel)
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = continueOffer.onDecline) {
                        Text("Non merci")
                    }
                }
            }
        } else if (gameOver) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Partie terminée", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    val recordSuffix = if (score > bestScore && score > 0) "  🏆 Nouveau record !" else ""
                    Text("Score : $score$recordSuffix", color = Color.White)
                    Spacer(Modifier.height(4.dp))
                    Text("+$pointsEarned points", color = MaterialTheme.colorScheme.secondary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    if (onWatchAdForMultiplier != null && pointsEarned > 0) {
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = onWatchAdForMultiplier,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("🎬 Tripler mes points (pub)")
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = onRestart, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                        Text("Rejouer")
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = onExit) {
                        Text("Retour aux jeux")
                    }
                }
            }
        }
    }
}

/**
 * Décompte plein écran « 3 · 2 · 1 · GO ! » superposé au jeu (met la partie en pause après avoir
 * perdu une vie). [countdown] : 3/2/1 affiche le chiffre, valeur négative affiche « GO ! »,
 * 0 = rien (le jeu tourne normalement).
 */
@Composable
fun BoxScope.CountdownOverlay(countdown: Int) {
    if (countdown == 0) return
    Box(
        Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (countdown < 0) "GO !" else countdown.toString(),
            color = Color.White,
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
