package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_stats")
data class GameStatsEntity(
    @PrimaryKey val difficulty: String,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val bestTimeSeconds: Long = 0L,
    val totalTimeSeconds: Long = 0L,
    val totalErrors: Int = 0,
    val totalHintsUsed: Int = 0,
    // Racha de partidas ganadas consecutivas sin errores ni pistas ("perfectas").
    // Se corta apenas se gana una partida no perfecta; no se ve afectada por
    // partidas abandonadas (solo se actualiza en recordGameCompleted).
    val currentPerfectStreak: Int = 0,
    val bestPerfectStreak: Int = 0,
    // Conteo total (no racha) de partidas ganadas sin errores ni pistas.
    val perfectWins: Int = 0
) {
    val winRatePercent: Int
        get() = if (gamesPlayed > 0) ((gamesWon.toDouble() / gamesPlayed) * 100).toInt() else 0

    val averageTimeSeconds: Long
        get() = if (gamesWon > 0) totalTimeSeconds / gamesWon else 0L
}
