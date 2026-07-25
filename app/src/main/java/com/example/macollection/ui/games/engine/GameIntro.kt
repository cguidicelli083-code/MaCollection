package com.example.macollection.ui.games.engine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macollection.data.GameGuide
import com.example.macollection.ui.theme.themedGradient

/**
 * Écran d'explication d'un mini-jeu (comment jouer + règles), affiché une fois au premier lancement.
 * Un bouton « Jouer » lance réellement la partie ; la flèche retour renvoie à la liste des jeux.
 */
@Composable
fun GameIntroScreen(guide: GameGuide.Guide, onStart: () -> Unit, onExit: () -> Unit) {
    Box(Modifier.fillMaxSize().background(themedGradient())) {
        IconButton(onClick = onExit, modifier = Modifier.padding(8.dp)) {
            Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White)
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(guide.emoji, fontSize = 64.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                guide.title,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Comment jouer",
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(20.dp))
            guide.rules.forEach { rule ->
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Text("•", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(rule, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onStart,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Jouer ▶", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
