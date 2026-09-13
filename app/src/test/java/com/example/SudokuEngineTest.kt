package com.example

import com.example.engine.SudokuGenerator
import com.example.engine.SudokuSolver
import com.example.model.Difficulty
import com.example.model.SudokuBoard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SudokuEngineTest {

    @Test
    fun testGenerateCompleteGridIsValid() {
        val grid = SudokuGenerator.generateCompleteGrid(Random(42))
        assertEquals(81, grid.size)
        // Check all cells filled 1..9
        assertTrue(grid.all { it in 1..9 })

        // Check rows
        for (r in 0 until 9) {
            val rowDigits = (0 until 9).map { c -> grid[r * 9 + c] }.toSet()
            assertEquals(9, rowDigits.size)
        }

        // Check columns
        for (c in 0 until 9) {
            val colDigits = (0 until 9).map { r -> grid[r * 9 + c] }.toSet()
            assertEquals(9, colDigits.size)
        }

        // Check 3x3 blocks
        for (b in 0 until 9) {
            val startRow = (b / 3) * 3
            val startCol = (b % 3) * 3
            val blockDigits = mutableSetOf<Int>()
            for (r in 0 until 3) {
                for (c in 0 until 3) {
                    blockDigits.add(grid[(startRow + r) * 9 + (startCol + c)])
                }
            }
            assertEquals(9, blockDigits.size)
        }
    }

    @Test
    fun testSolverCanSolveValidPuzzle() {
        val seed = SudokuGenerator.getSeedPuzzle(Difficulty.EASY, Random(100))
        val copy = seed.puzzle.clone()
        val solved = SudokuSolver.solve(copy)
        assertTrue(solved)
        assertTrue(copy.contentEquals(seed.solution))
    }

    @Test
    fun testUniquenessChecker() {
        val seed = SudokuGenerator.getSeedPuzzle(Difficulty.EASY, Random(100))
        val solutions = SudokuSolver.countSolutions(seed.puzzle, limit = 2)
        assertEquals(1, solutions)
    }

    @Test
    fun testBoardRelatedCoordinates() {
        val board = SudokuBoard.createEmpty()
        val related = board.getRelatedCoords(0, 0)
        // 8 in row, 8 in col, 4 remaining in 3x3 block = 20 distinct related cells
        assertEquals(20, related.size)
        assertFalse(related.contains(0 to 0))
        assertTrue(related.contains(0 to 5))
        assertTrue(related.contains(5 to 0))
        assertTrue(related.contains(1 to 1))
    }

    @Test
    fun testBoardCompletion() {
        val seed = SudokuGenerator.getSeedPuzzle(Difficulty.EASY, Random(1))
        val board = SudokuBoard.fromGrid(seed.solution, seed.solution)
        assertTrue(board.isComplete)
        assertTrue(board.isSolvedCorrectly)
        assertEquals(9, board.countInstances(1))
        assertTrue(board.isNumberCompleted(1))
    }
}
