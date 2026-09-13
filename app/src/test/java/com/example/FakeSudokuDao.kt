package com.example

import com.example.data.db.ActiveGameEntity
import com.example.data.db.GameHistoryEntity
import com.example.data.db.GameStatsEntity
import com.example.data.db.SudokuDao
import com.example.data.db.UserSettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory [SudokuDao] double for tests. Backed by [MutableStateFlow] so the
 * reactive queries (getActiveGame, getAllStats, getUserSettings) behave like
 * Room's would: they emit again whenever the underlying data changes.
 *
 * This lets SudokuRepository/SudokuViewModel tests exercise real logic
 * (restoreBoard, the game-over/resume flow, settings propagation) without
 * pulling in Room + Robolectric, since nothing under test here depends on
 * actual SQL persistence.
 */
class FakeSudokuDao : SudokuDao {

    private val activeGameState = MutableStateFlow<ActiveGameEntity?>(null)
    private val userSettingsState = MutableStateFlow<UserSettingsEntity?>(null)
    private val statsState = MutableStateFlow<Map<String, GameStatsEntity>>(emptyMap())
    private val historyState = MutableStateFlow<List<GameHistoryEntity>>(emptyList())

    override fun getActiveGame(): Flow<ActiveGameEntity?> = activeGameState

    override suspend fun getActiveGameDirect(): ActiveGameEntity? = activeGameState.value

    override suspend fun saveActiveGame(game: ActiveGameEntity) {
        activeGameState.value = game
    }

    override suspend fun clearActiveGame() {
        activeGameState.value = null
    }

    override fun getStats(difficulty: String): Flow<GameStatsEntity?> =
        statsState.map { it[difficulty] }

    override suspend fun getStatsDirect(difficulty: String): GameStatsEntity? =
        statsState.value[difficulty]

    override fun getAllStats(): Flow<List<GameStatsEntity>> =
        statsState.map { it.values.toList() }

    override suspend fun insertOrUpdateStats(stats: GameStatsEntity) {
        statsState.value = statsState.value + (stats.difficulty to stats)
    }

    override suspend fun insertGameHistory(entry: GameHistoryEntity) {
        // Emula el autoIncrement de Room para que el orden por id se mantenga.
        val nextId = (historyState.value.maxOfOrNull { it.id } ?: 0L) + 1
        historyState.value = historyState.value + entry.copy(id = nextId)
    }

    override fun getRecentHistory(): Flow<List<GameHistoryEntity>> =
        historyState.map { it.sortedByDescending { entry -> entry.id }.take(500) }

    override fun getUserSettings(): Flow<UserSettingsEntity?> = userSettingsState

    override suspend fun getUserSettingsDirect(): UserSettingsEntity? = userSettingsState.value

    override suspend fun saveUserSettings(settings: UserSettingsEntity) {
        userSettingsState.value = settings
    }
}
