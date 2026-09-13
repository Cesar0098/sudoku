package com.example

import com.example.data.db.ActiveGameEntity
import com.example.data.repository.SudokuRepository
import com.example.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuRepositoryTest {

    private val repository = SudokuRepository(FakeSudokuDao())

    /**
     * Fila de "partida guardada" con la celda (0,0) dada y la celda (0,1) con
     * un valor que no coincide con la solucion — exactamente lo que
     * restoreBoard() tiene que decidir si marca como error o no.
     */
    private fun entityWithOneWrongCell(errorsCount: Int = 1): ActiveGameEntity {
        val solution = StringBuilder()
        val puzzle = StringBuilder()
        val currentValues = StringBuilder()
        val givenMask = StringBuilder()

        for (i in 0 until 81) {
            val sol = (i % 9) + 1
            solution.append(sol)
            when (i) {
                0 -> {
                    puzzle.append(sol)
                    currentValues.append(sol)
                    givenMask.append('1')
                }
                1 -> {
                    puzzle.append('0')
                    currentValues.append(if (sol == 9) 1 else sol + 1) // deliberadamente mal
                    givenMask.append('0')
                }
                else -> {
                    puzzle.append('0')
                    currentValues.append('0')
                    givenMask.append('0')
                }
            }
        }

        val notesData = List(81) { "" }.joinToString(";")

        return ActiveGameEntity(
            difficulty = Difficulty.MEDIUM.id,
            puzzle = puzzle.toString(),
            solution = solution.toString(),
            currentValues = currentValues.toString(),
            givenMask = givenMask.toString(),
            notesData = notesData,
            elapsedSeconds = 42,
            errorsCount = errorsCount,
            hintsUsed = 0,
            isPaused = false,
            isCompleted = false
        )
    }

    @Test
    fun `restoreBoard oculta el error cuando showErrors es false`() {
        val (board, difficulty) = repository.restoreBoard(entityWithOneWrongCell(), showErrors = false)

        assertEquals(Difficulty.MEDIUM, difficulty)
        assertFalse(board.getCell(0, 1).isError)
    }

    @Test
    fun `restoreBoard marca el error cuando showErrors es true`() {
        val (board, _) = repository.restoreBoard(entityWithOneWrongCell(), showErrors = true)

        assertTrue(board.getCell(0, 1).isError)
    }

    @Test
    fun `restoreBoard nunca marca error en celdas dadas`() {
        val (board, _) = repository.restoreBoard(entityWithOneWrongCell(), showErrors = true)

        val given = board.getCell(0, 0)
        assertTrue(given.isGiven)
        assertFalse(given.isError)
    }
}
