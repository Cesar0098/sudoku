package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Registro individual de una partida completada (ganada). A diferencia de
 * [GameStatsEntity], que guarda solo acumulados por dificultad, esta tabla
 * guarda una fila por partida para poder mostrar tendencias (gráfico de
 * tiempos, rachas, etc.) en vez de solo totales.
 */
@Entity(tableName = "game_history")
data class GameHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val difficulty: String,
    val completedAt: Long,
    val timeSeconds: Long,
    val errors: Int,
    val hints: Int
) {
    val isPerfect: Boolean
        get() = errors == 0 && hints == 0
}
