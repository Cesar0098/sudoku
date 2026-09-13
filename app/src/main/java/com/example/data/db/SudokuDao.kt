package com.example.data.db

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
}
