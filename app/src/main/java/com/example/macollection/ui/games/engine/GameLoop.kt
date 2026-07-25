package com.example.macollection.ui.games.engine

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos

/**
 * Boucle de jeu à delta-time (une frame d'affichage = un appel à [onFrame]), tant que
 * [running] est vrai. Le delta est plafonné à 50ms pour éviter les gros bonds de simulation
 * (changement d'onglet, débogueur, reprise après pause).
 */
@Composable
fun GameLoop(running: Boolean, onFrame: (deltaSeconds: Float) -> Unit) {
    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        var lastFrameNanos = -1L
        while (true) {
            withFrameNanos { frameNanos ->
                if (lastFrameNanos >= 0) {
                    val deltaSeconds = ((frameNanos - lastFrameNanos) / 1_000_000_000.0).toFloat()
                    onFrame(deltaSeconds.coerceAtMost(0.05f))
                }
                lastFrameNanos = frameNanos
            }
        }
    }
}
