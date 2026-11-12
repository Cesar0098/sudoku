package com.example

import com.example.engine.SudokuGenerator
import com.example.model.Difficulty
import com.example.model.SudokuBoard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SudokuFullFlowTest {

    @Test
    fun generatedDifficultyMatchesAcrossSeeds() {
        for (difficulty in Difficulty.entries) {
            for (seed in 0 until 16) {
                val generated = SudokuGenerator.generate(difficulty, Random(seed))
                assertEquals(difficulty, com.example.engine.SudokuSolver.evaluateDifficulty(generated.puzzle))
                assertEquals(1, com.example.engine.SudokuSolver.countSolutions(generated.puzzle))
            }
        }
    }

    @Test
    fun sameDailySeedProducesSamePuzzle() {
        assertEquals(SudokuGenerator.generate(Difficulty.MEDIUM, Random(2026316)),
            SudokuGenerator.generate(Difficulty.MEDIUM, Random(2026316)))
    }

    @Test
    fun testGeneratedPuzzleHasUniqueSolution() {
        for (diff in Difficulty.entries) {
            val seed = SudokuGenerator.getSeedPuzzle(diff, Random(123))
            assertEquals(1, com.example.engine.SudokuSolver.countSolutions(seed.puzzle))
            assertEquals(diff, com.example.engine.SudokuSolver.evaluateDifficulty(seed.puzzle))
            assertNotNull(seed.puzzle)
            assertNotNull(seed.solution)
            assertEquals(81, seed.puzzle.size)
            assertEquals(81, seed.solution.size)

            val board = SudokuBoard.fromGrid(seed.puzzle, seed.solution)
            assertFalse(board.isComplete)
            assertTrue(board.cells.any { it.isGiven })
            assertTrue(board.cells.any { it.isEmpty })
        }
    }

    @Test
    fun testHintSystemFindsCorrectCellAndReveals() {
        val seed = SudokuGenerator.getSeedPuzzle(Difficulty.EASY, Random(456))
        val board = SudokuBoard.fromGrid(seed.puzzle, seed.solution)

        val emptyCell = board.cells.first { it.isEmpty }
        val correctValue = emptyCell.solutionValue
        assertTrue(correctValue in 1..9)

        // Revealing hint
        val updatedCells = board.cells.map { cell ->
            if (cell.row == emptyCell.row && cell.col == emptyCell.col) {
                cell.copy(value = correctValue)
            } else cell
        }
        val newBoard = SudokuBoard(updatedCells)
        assertEquals(correctValue, newBoard.getCell(emptyCell.row, emptyCell.col).value)
    }

    @Test
    fun testNumberCompletedStatus() {
        val seed = SudokuGenerator.getSeedPuzzle(Difficulty.EASY, Random(789))
        val fullBoard = SudokuBoard.fromGrid(seed.solution, seed.solution)

        for (num in 1..9) {
            assertTrue(fullBoard.isNumberCompleted(num))
        }
    }
}
