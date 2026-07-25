package com.example.macollection.ui.games

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/** Message affiché en variante TEST quand on tente de débloquer un jeu ou une fonctionnalité. */
const val TEST_BLOCKED_MESSAGE =
    "En mode test, le déblocage des jeux et des fonctionnalités n'est pas accessible."

/** Pop-up d'information, réservé à la variante TEST, sur l'impossibilité de débloquer. */
@Composable
fun TestBlockedDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mode test") },
        text = { Text(TEST_BLOCKED_MESSAGE) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
    )
}
