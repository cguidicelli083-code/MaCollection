package com.example.macollection.ui.games.engine

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.sin

/**
 * Effets sonores et haptiques des mini-jeux. Les sons sont SYNTHÉTISÉS à la volée (tonalités
 * carrées/sinus façon chiptune) — aucun fichier audio n'est embarqué, donc rien de protégé.
 * Chaque son joue sur un thread démon éphémère (évènements ponctuels : manger, perdre une vie,
 * fin de partie, touches de Simon...), jamais à chaque frame.
 */
object GameFx {
    private const val SAMPLE_RATE = 44100

    /** 4 hauteurs distinctes pour Simon (do/mi/sol/aigu), pour reconnaître les couleurs à l'oreille. */
    private val SIMON_FREQS = doubleArrayOf(392.0, 494.0, 587.0, 784.0)

    /** Joue une tonalité de [durationMs] ms à [freqHz] Hz (carrée par défaut, sinus si [square]=false). */
    fun tone(freqHz: Double, durationMs: Int, volume: Float = 0.45f, square: Boolean = true) {
        thread(isDaemon = true) {
            try {
                val count = (SAMPLE_RATE * durationMs / 1000).coerceAtLeast(1)
                val buf = ShortArray(count)
                val amp = Short.MAX_VALUE * volume
                val fade = (count / 10).coerceAtLeast(1) // fondu entrée/sortie pour éviter les clics
                for (i in 0 until count) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val s = sin(2 * PI * freqHz * t)
                    val wave = if (square) (if (s >= 0) 1.0 else -1.0) else s
                    val env = when {
                        i < fade -> i.toDouble() / fade
                        i > count - fade -> (count - i).toDouble() / fade
                        else -> 1.0
                    }
                    buf[i] = (wave * amp * env).toInt().toShort()
                }
                @Suppress("DEPRECATION")
                val track = AudioTrack(
                    AudioManager.STREAM_MUSIC, SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    count * 2, AudioTrack.MODE_STATIC
                )
                track.write(buf, 0, count)
                track.play()
                Thread.sleep((durationMs + 60).toLong())
                track.release()
            } catch (_: Exception) {
                // Audio indisponible : on ignore, le jeu continue sans son.
            }
        }
    }

    fun vibrate(context: Context, ms: Long) {
        try {
            val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION") vib.vibrate(ms)
            }
        } catch (_: Exception) {
        }
    }

    // --- Effets prêts à l'emploi ---
    fun eat() = tone(880.0, 80)
    fun blip() = tone(1046.0, 70)
    fun win() = tone(784.0, 140)
    fun hop() = tone(660.0, 45)
    fun loseLife(context: Context) { vibrate(context, 120); tone(160.0, 220) }
    fun gameOver(context: Context) { vibrate(context, 260); tone(110.0, 340) }
    fun simonPad(index: Int) = tone(SIMON_FREQS[index.coerceIn(0, 3)], 340, 0.5f, square = false)

    // Space Invaders
    fun shoot() = tone(720.0, 28, 0.30f)             // tir du joueur (aigu)
    fun enemyShoot() = tone(300.0, 34, 0.30f)        // tir ennemi (grave)
    fun march(low: Boolean) = tone(if (low) 96.0 else 128.0, 45, 0.30f) // « marche » des ennemis

    // Pacman
    fun chomp(open: Boolean) = tone(if (open) 420.0 else 300.0, 38, 0.22f)      // « waka » en avançant
    fun frighten() = tone(180.0, 260, 0.35f, square = false)                    // fantômes vulnérables
    fun frightWarn() = tone(700.0, 130, 0.35f)                                  // alerte : bientôt de nouveau dangereux

    // Arkanoid
    fun brick() = tone(520.0, 26, 0.22f)             // brique cassée (bref, discret)

    // Tetris
    fun rotate() = tone(440.0, 20, 0.16f)            // rotation d'une pièce
    fun lock() = tone(180.0, 45, 0.20f)              // pièce posée au sol

    // Pong
    fun pong() = tone(560.0, 30, 0.40f)              // balle touchée (raquette)
    fun wallBounce() = tone(400.0, 26, 0.35f)        // rebond sur un mur
    fun scorePoint() = tone(880.0, 150, 0.45f)       // point gagné
    fun losePoint() = tone(200.0, 180, 0.40f)        // point encaissé
}
