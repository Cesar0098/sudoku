package com.example

import com.example.data.db.UserSettingsEntity
import com.example.data.repository.SudokuRepository
import com.example.engine.SudokuGenerator
import com.example.model.Difficulty
import com.example.model.SudokuBoard
import com.example.ui.SudokuViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/**
 * Cubre los bugs de estado que se encontraron y corrigieron en SudokuViewModel /
 * SudokuRepository: una partida perdida por limite de errores quedaba
 * "resumible" en vez de descartarse, y al retomar una partida guardada se
 * ignoraba el ajuste de auto-comprobacion de errores.
 *
 * Usa FakeSudokuDao (en memoria) en vez de Room, asi que no hace falta
 * Robolectric: el ViewModel real corre igual, solo cambia de donde vienen
 * los datos.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SudokuViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dao: FakeSudokuDao
    private lateinit var repository: SudokuRepository
    private lateinit var viewModel: SudokuViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        dao = FakeSudokuDao()
        repository = SudokuRepository(dao)
        viewModel = SudokuViewModel(repository)
    }

    @After
    fun tearDown() {
        viewModel.pauseGame()
        Dispatchers.resetMain()
    }

    // Guarda una partida "activa" directamente via el repositorio, con un
    // tablero real generado de forma sincronica (sin pasar por
    // Dispatchers.Default dentro de una corrutina, para no depender de un
    // dispatcher real dentro del scheduler de test).
    private suspend fun seedActiveGame(
        difficulty: Difficulty = Difficulty.EASY,
        errorsCount: Int = 0,
        seed: Int = 1
    ) {
        val generated = SudokuGenerator.getSeedPuzzle(difficulty, Random(seed))
        val board = SudokuBoard.fromGrid(generated.puzzle, generated.solution)
        repository.saveActiveGame(
            board = board,
            difficulty = difficulty,
            elapsedSeconds = 0,
            errorsCount = errorsCount,
            hintsUsed = 0,
            isPaused = false,
            isCompleted = false
        )
    }

    @Test
    fun `resumeSavedGame no revive una partida que ya llego al limite de errores`() = runTest(testDispatcher) {
        viewModel.updateSettings(UserSettingsEntity(errorLimitEnabled = true))
        runCurrent()

        seedActiveGame(errorsCount = viewModel.uiState.value.maxErrors)
        runCurrent()

        viewModel.resumeSavedGame()
        runCurrent()

        // Se descarta en vez de resumirse.
        assertNull(dao.getActiveGameDirect())
        assertEquals(SudokuBoard.createEmpty(), viewModel.uiState.value.board)
        assertFalse(viewModel.uiState.value.isGameOver)
        viewModel.pauseGame()
    }

    @Test
    fun `inputNumber al llegar al limite de errores no deja la partida resumible`() = runTest(testDispatcher) {
        viewModel.updateSettings(UserSettingsEntity(errorLimitEnabled = true))
        runCurrent()

        seedActiveGame()
        runCurrent()
        viewModel.resumeSavedGame()
        runCurrent()

        val maxErrors = viewModel.uiState.value.maxErrors
        var wrongEntriesMade = 0
        for (cell in viewModel.uiState.value.board.cells) {
            if (wrongEntriesMade >= maxErrors) break
            if (cell.isGiven) continue
            viewModel.selectCell(cell.row, cell.col)
            val wrongValue = if (cell.solutionValue == 9) 1 else cell.solutionValue + 1
            viewModel.inputNumber(wrongValue)
            wrongEntriesMade++
        }
        runCurrent()

        assertEquals(maxErrors, wrongEntriesMade)
        assertTrue(viewModel.uiState.value.isGameOver)
        // Antes: al perder, la partida se guardaba igual y "Continuar partida"
        // la traia de vuelta ya perdida. Ahora se descarta.
        assertNull(dao.getActiveGameDirect())
        viewModel.pauseGame()
    }

    @Test
    fun `inputNumber respeta auto-check y limite de errores apagados`() = runTest(testDispatcher) {
        viewModel.updateSettings(UserSettingsEntity(autoCheckErrors = false, errorLimitEnabled = false))
        runCurrent()

        seedActiveGame()
        runCurrent()
        viewModel.resumeSavedGame()
        runCurrent()

        val target = viewModel.uiState.value.board.cells.first { !it.isGiven }
        viewModel.selectCell(target.row, target.col)
        val wrongValue = if (target.solutionValue == 9) 1 else target.solutionValue + 1
        viewModel.inputNumber(wrongValue)

        val updated = viewModel.uiState.value.board.getCell(target.row, target.col)
        assertEquals(wrongValue, updated.value)
        assertFalse(updated.isError)
        assertFalse(viewModel.uiState.value.isGameOver)
        viewModel.pauseGame()
    }

    @Test
    fun `inputNumber marca el error cuando auto-check esta activado`() = runTest(testDispatcher) {
        viewModel.updateSettings(UserSettingsEntity(autoCheckErrors = true, errorLimitEnabled = false))
        runCurrent()

        seedActiveGame()
        runCurrent()
        viewModel.resumeSavedGame()
        runCurrent()

        val target = viewModel.uiState.value.board.cells.first { !it.isGiven }
        viewModel.selectCell(target.row, target.col)
        val wrongValue = if (target.solutionValue == 9) 1 else target.solutionValue + 1
        viewModel.inputNumber(wrongValue)

        val updated = viewModel.uiState.value.board.getCell(target.row, target.col)
        assertTrue(updated.isError)
        viewModel.pauseGame()
    }

    @Test
    fun `resumeSavedGame respeta el ajuste de auto-check al recalcular errores`() = runTest(testDispatcher) {
        // Simula una partida guardada que ya tenia una celda mal puesta,
        // sin pasar por inputNumber.
        val generated = SudokuGenerator.getSeedPuzzle(Difficulty.EASY, Random(2))
        val board = SudokuBoard.fromGrid(generated.puzzle, generated.solution)
        val emptyCell = board.cells.first { it.isEmpty }
        val wrongValue = if (emptyCell.solutionValue == 9) 1 else emptyCell.solutionValue + 1
        val boardWithMistake = SudokuBoard(
            board.cells.map { cell ->
                if (cell.row == emptyCell.row && cell.col == emptyCell.col) {
                    cell.copy(value = wrongValue)
                } else cell
            }
        )
        repository.saveActiveGame(
            board = boardWithMistake,
            difficulty = Difficulty.EASY,
            elapsedSeconds = 0,
            errorsCount = 1,
            hintsUsed = 0,
            isPaused = false,
            isCompleted = false
        )

        viewModel.updateSettings(UserSettingsEntity(autoCheckErrors = false, errorLimitEnabled = false))
        runCurrent()

        viewModel.resumeSavedGame()
        runCurrent()

        // Antes: restoreBoard recalculaba el error ignorando el ajuste. Ahora
        // lo respeta tambien al retomar, igual que en inputNumber().
        val resumedCell = viewModel.uiState.value.board.getCell(emptyCell.row, emptyCell.col)
        assertFalse(resumedCell.isError)
        viewModel.pauseGame()
    }
}
