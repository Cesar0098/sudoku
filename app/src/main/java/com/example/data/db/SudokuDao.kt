package com.example.data.db

import androidx.room.Transaction
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SudokuDao {

    @Query("SELECT * FROM active_game WHERE id = 1 LIMIT 1")
    fun getActiveGame(): Flow<ActiveGameEntity?>

    @Query("SELECT * FROM active_game WHERE id = 1 LIMIT 1")
    suspend fun getActiveGameDirect(): ActiveGameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveActiveGame(game: ActiveGameEntity)

    @Query("DELETE FROM active_game WHERE id = 1")
    suspend fun clearActiveGame()

    @Query("SELECT * FROM game_stats WHERE difficulty = :difficulty LIMIT 1")
    fun getStats(difficulty: String): Flow<GameStatsEntity?>

    @Query("SELECT * FROM game_stats WHERE difficulty = :difficulty LIMIT 1")
    suspend fun getStatsDirect(difficulty: String): GameStatsEntity?

    @Query("SELECT * FROM game_stats")
    fun getAllStats(): Flow<List<GameStatsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: GameStatsEntity)

    @Insert
    suspend fun insertGameHistory(entry: GameHistoryEntity)

    // Se trae un límite acotado en total (no por dificultad) para no cargar
    // en memoria un historial que crece indefinidamente con los años de uso;
    // 500 filas alcanzan de sobra para los gráficos de progreso (se agrupan
    // y se recortan a las últimas N por dificultad en el repositorio/UI).
    @Query("SELECT * FROM game_history ORDER BY id DESC LIMIT 500")
    fun getRecentHistory(): Flow<List<GameHistoryEntity>>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getUserSettings(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getUserSettingsDirect(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserSettings(settings: UserSettingsEntity)
    @Transaction
    suspend fun finishGame(sessionId: String, difficulty: String, elapsedSeconds: Long, errors: Int, hints: Int) {
        if (getActiveGameDirect()?.sessionId != sessionId) return
        val existing = getStatsDirect(difficulty) ?: GameStatsEntity(difficulty = difficulty)
        val newGamesWon = existing.gamesWon + 1
        val newBestTime = if (existing.bestTimeSeconds == 0L || elapsedSeconds < existing.bestTimeSeconds) {
            elapsedSeconds
        } else {
            existing.bestTimeSeconds
        }
        val newTotalTime = existing.totalTimeSeconds + elapsedSeconds
        val newErrors = existing.totalErrors + errors
        val newHints = existing.totalHintsUsed + hints

        // Partida "perfecta" = ganada sin errores ni pistas. La racha actual
        // se corta (vuelve a 0) apenas se gana una partida que no es perfecta;
        // el récord se actualiza si la racha actual lo supera. perfectWins es
        // el conteo total acumulado (no se corta nunca, a diferencia de la racha).
        val isPerfect = errors == 0 && hints == 0
        val newCurrentStreak = if (isPerfect) existing.currentPerfectStreak + 1 else 0
        val newBestStreak = maxOf(existing.bestPerfectStreak, newCurrentStreak)
        val newPerfectWins = if (isPerfect) existing.perfectWins + 1 else existing.perfectWins

        val updated = existing.copy(
            gamesWon = newGamesWon,
            bestTimeSeconds = newBestTime,
            totalTimeSeconds = newTotalTime,
            totalErrors = newErrors,
            totalHintsUsed = newHints,
            currentPerfectStreak = newCurrentStreak,
            bestPerfectStreak = newBestStreak,
            perfectWins = newPerfectWins
        )
        insertOrUpdateStats(updated)

        insertGameHistory(
            GameHistoryEntity(
                difficulty = difficulty,
                completedAt = System.currentTimeMillis(),
                timeSeconds = elapsedSeconds,
                errors = errors,
                hints = hints
            )
        )
        clearActiveGame()
    }

}
