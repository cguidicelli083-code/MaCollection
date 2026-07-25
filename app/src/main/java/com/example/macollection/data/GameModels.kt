package com.example.macollection.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Progression du joueur (ligne unique, id toujours 0). [checksum] (voir [IntegrityGuard]) permet
 * de détecter une modification directe de la base (root + éditeur SQLite) : toute ligne dont
 * l'empreinte ne correspond pas à son contenu est ignorée par [com.example.macollection.ui.GameViewModel].
 */
@Entity(tableName = "player_progress")
data class PlayerProgress(
    @PrimaryKey val id: Int = 0,
    val points: Int = 0,
    val lifetimePoints: Int = 0,
    val checksum: String = ""
)

/**
 * Un jeu/skin/fonctionnalité débloqué par le joueur (via points ou achat Premium). [checksum]
 * (voir [IntegrityGuard]) permet de rejeter une ligne insérée directement dans la base sans
 * passer par l'appli (ex. une fausse ligne "premium_all_access").
 */
@Entity(tableName = "unlocked_items")
data class UnlockedItem(
    @PrimaryKey val itemId: String,
    val unlockedAt: Long,
    val checksum: String = ""
)

/** Meilleur score enregistré pour un mini-jeu (une ligne par jeu). */
@Entity(tableName = "game_high_scores")
data class GameHighScore(
    @PrimaryKey val gameId: String,
    val bestScore: Int,
    val playedAt: Long
)
