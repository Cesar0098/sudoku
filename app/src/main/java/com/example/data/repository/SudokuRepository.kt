package com.example.data.repository

import com.example.data.db.ActiveGameEntity
import com.example.data.db.GameHistoryEntity
import com.example.data.db.GameStatsEntity
import com.example.data.db.SudokuDao
import com.example.data.db.UserSettingsEntity
import com.example.model.Difficulty
import com.example.model.SudokuBoard
import com.example.model.SudokuCell
import kotlinx.coroutines.flow.Flow

class SudokuRepository(private val dao: SudokuDao) {

    val activeGameFlow: Flow<ActiveGameEntity?> = dao.getActiveGame()
    val allStatsFlow: Flow<List<GameStatsEntity>> = dao.getAllStats()
    val userSettingsFlow: Flow<UserSettingsEntity?> = dao.getUserSettings()
    val recentHistoryFlow: Flow<List<GameHistoryEntity>> = dao.getRecentHistory()

    suspend fun getActiveGame(): ActiveGameEntity? = dao.getActiveGameDirect()

    suspend fun getUserSettings(): UserSettingsEntity {
        return dao.getUserSettingsDirect() ?: UserSettingsEntity().also {
            dao.saveUserSettings(it)
        }
    }

    suspend fun saveUserSettings(settings: UserSettingsEntity) {
        dao.saveUserSettings(settings)
    }

    suspend fun saveActiveGame(
        board: SudokuBoard,
        difficulty: Difficulty,
        elapsedSeconds: Long,
        errorsCount: Int,
        hintsUsed: Int,
        isPaused: Boolean,
        isCompleted: Boolean
    ) {
        val puzzleChars = CharArray(81)
        val solutionChars = CharArray(81)
        val currentValuesChars = CharArray(81)
        val givenChars = CharArray(81)
        val notesSb = StringBuilder()

        for (i in 0 until 81) {
            val cell = board.cells[i]
            puzzleChars[i] = if (cell.isGiven) ('0' + cell.value) else '0'
            solutionChars[i] = ('0' + cell.solutionValue)
            currentValuesChars[i] = ('0' + cell.value)
            givenChars[i] = if (cell.isGiven) '1' else '0'

            val cellNotes = cell.notes.sorted().joinToString("")
            notesSb.append(cellNotes)
            if (i < 80) notesSb.append(";")
        }

        val entity = ActiveGameEntity(
            difficulty = difficulty.id,
            puzzle = String(puzzleChars),
            solution = String(solutionChars),
            currentValues = String(currentValuesChars),
            givenMask = String(givenChars),
            notesData = notesSb.toString(),
            elapsedSeconds = elapsedSeconds,
            errorsCount = errorsCount,
            hintsUsed = hintsUsed,
            isPaused = isPaused,
            isCompleted = isCompleted
        )

        dao.saveActiveGame(entity)
    }

    suspend fun clearActiveGame() {
        dao.clearActiveGame()
    }

    fun restoreBoard(entity: ActiveGameEntity, showErrors: Boolean): Pair<SudokuBoard, Difficulty> {
        val difficulty = Difficulty.fromId(entity.difficulty)
        val notesTokens = entity.notesData.split(";")

        val cells = mutableListOf<SudokuCell>()
        for (i in 0 until 81) {
            val r = i / 9
            val c = i % 9
            val value = entity.currentValues.getOrNull(i)?.digitToIntOrNull() ?: 0
            val sol = entity.solution.getOrNull(i)?.digitToIntOrNull() ?: 0
            val isGiven = entity.givenMask.getOrNull(i) == '1'

            val notes = if (i < notesTokens.size) {
                notesTokens[i].mapNotNull { it.digitToIntOrNull() }.toSet()
            } else {
                emptySet()
            }

            // Antes: se recalculaba siempre desde la solución, ignorando el ajuste
            // "autoCheckErrors". Ahora respeta el mismo criterio que inputNumber().
            val isError = showErrors && !isGiven && value != 0 && value != sol

            cells.add(
                SudokuCell(
                    row = r,
                    col = c,
                    value = value,
                    solutionValue = sol,
                    isGiven = isGiven,
                    notes = notes,
                    isError = isError
                )
            )
        }

        return Pair(SudokuBoard(cells), difficulty)
    }

    suspend fun recordGameStarted(difficulty: Difficulty) {
        val existing = dao.getStatsDirect(difficulty.id) ?: GameStatsEntity(difficulty = difficulty.id)
        val updated = existing.copy(gamesPlayed = existing.gamesPlayed + 1)
        dao.insertOrUpdateStats(updated)
    }

    suspend fun recordGameCompleted(
        difficulty: Difficulty,
        elapsedSeconds: Long,
        errors: Int,
        hints: Int
    ) {
        val existing = dao.getStatsDirect(difficulty.id) ?: GameStatsEntity(difficulty = difficulty.id)
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
        dao.insertOrUpdateStats(updated)

        dao.insertGameHistory(
            GameHistoryEntity(
                difficulty = difficulty.id,
                completedAt = System.currentTimeMillis(),
                timeSeconds = elapsedSeconds,
                errors = errors,
                hints = hints
            )
        )
    }
}
