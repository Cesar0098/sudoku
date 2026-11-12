package com.example

import com.example.data.db.UserSettingsEntity
import com.example.data.repository.SudokuRepository
import com.example.engine.SudokuGenerator
import com.example.model.*
import com.example.ui.SudokuViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PersistenceTest {
    @Test fun completionIsIdempotentAndDoesNotClearAnotherSession() = runTest {
        val dao = FakeSudokuDao()
        val repository = SudokuRepository(dao)
        val puzzle = SudokuGenerator.generate(Difficulty.EASY, Random(10))
        val board = SudokuBoard.fromGrid(puzzle.puzzle, puzzle.solution)
        repository.saveActiveGame(board, Difficulty.EASY, 10, 0, 0, false, false, "one")
        repository.recordGameStarted(Difficulty.EASY)
        repository.recordGameCompleted("one", Difficulty.EASY, 10, 0, 0)
        repository.recordGameCompleted("one", Difficulty.EASY, 10, 0, 0)
        assertEquals(1, dao.getStatsDirect("easy")!!.gamesWon)
        assertNull(dao.getActiveGameDirect())
        repository.saveActiveGame(board, Difficulty.EASY, 0, 0, 0, false, false, "two")
        repository.recordGameCompleted("one", Difficulty.EASY, 10, 0, 0)
        assertEquals("two", dao.getActiveGameDirect()!!.sessionId)
    }

    @Test fun undoAndRedoSurviveViewModelRecreation() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val stores = mutableListOf<androidx.lifecycle.ViewModelStore>()
        try {
            val dao = FakeSudokuDao()
            val repository = SudokuRepository(dao)
            val puzzle = SudokuGenerator.generate(Difficulty.EASY, Random(12))
            val board = SudokuBoard.fromGrid(puzzle.puzzle, puzzle.solution)
            repository.saveActiveGame(board, Difficulty.EASY, 0, 0, 0, false, false, "test")
            fun model(): SudokuViewModel {
                val vm = SudokuViewModel(repository)
                stores.add(androidx.lifecycle.ViewModelStore().also { it.put("test", vm) })
                vm.resumeSavedGame()
                return vm
            }
            val first = model(); runCurrent()
            val cell = board.cells.first { !it.isGiven }
            first.selectCell(cell.row, cell.col)
            first.inputNumber(cell.solutionValue)
            first.pauseGame(); runCurrent()
            val second = model(); runCurrent()
            assertTrue(second.uiState.value.canUndo)
            second.undo(); second.pauseGame(); runCurrent()
            val third = model(); runCurrent()
            assertEquals(0, third.uiState.value.board.getCell(cell.row, cell.col).value)
            assertTrue(third.uiState.value.canRedo)
            third.redo(); runCurrent()
            assertEquals(cell.solutionValue, third.uiState.value.board.getCell(cell.row, cell.col).value)
            third.updateSettings(UserSettingsEntity(autoCheckErrors = false, errorLimitEnabled = false))
            runCurrent()
            third.selectCell(cell.row, cell.col)
            third.inputNumber(cell.solutionValue % 9 + 1)
            assertEquals(cell.solutionValue % 9 + 1, third.uiState.value.board.getCell(cell.row, cell.col).value)
        } finally {
            stores.forEach { it.clear() }
            Dispatchers.resetMain()
        }
    }
}
