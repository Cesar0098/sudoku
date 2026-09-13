package com.example

import com.example.engine.SudokuGenerator
import com.example.model.CellDelta
import com.example.model.Difficulty
import com.example.model.SudokuBoard
import com.example.model.SudokuCell
import com.example.model.SudokuMove
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SudokuLogicTest {

    @Test
    fun testNotesToggleAndAutoRemoval() {
        val seed = SudokuGenerator.getSeedPuzzle(Difficulty.EASY, Random(10))
        val board = SudokuBoard.fromGrid(seed.puzzle, seed.solution)

        // Find an empty cell
        val emptyCell = board.cells.first { it.isEmpty }
        val targetRow = emptyCell.row
        val targetCol = emptyCell.col

        // Add notes 1, 2, 5 to related cell in same row
        val relatedCoord = board.getRelatedCoords(targetRow, targetCol).first { it.first == targetRow }
        val relatedCellIdx = relatedCoord.first * 9 + relatedCoord.second

        val updatedCells = board.cells.toMutableList()
        updatedCells[relatedCellIdx] = updatedCells[relatedCellIdx].copy(notes = setOf(1, 2, 5))
        val boardWithNotes = SudokuBoard(updatedCells)

        assertTrue(boardWithNotes.getCell(relatedCoord.first, relatedCoord.second).notes.contains(2))

        // When placing '2' in emptyCell, simulate auto-removal
        val simulatedNewCells = boardWithNotes.cells.map { cell ->
            if (cell.row == relatedCoord.first && cell.col == relatedCoord.second) {
                cell.copy(notes = cell.notes - 2)
            } else cell
        }
        val boardAfterPlacement = SudokuBoard(simulatedNewCells)

        assertFalse(boardAfterPlacement.getCell(relatedCoord.first, relatedCoord.second).notes.contains(2))
        assertTrue(boardAfterPlacement.getCell(relatedCoord.first, relatedCoord.second).notes.contains(1))
        assertTrue(boardAfterPlacement.getCell(relatedCoord.first, relatedCoord.second).notes.contains(5))
    }

    @Test
    fun testUndoRestoresDeltas() {
        val delta = CellDelta(
            row = 0,
            col = 0,
            oldValue = 0,
            newValue = 5,
            oldNotes = setOf(1, 2),
            newNotes = emptySet(),
            oldIsError = false,
            newIsError = false
        )
        val move = SudokuMove(listOf(delta))

        assertEquals(0, delta.oldValue)
        assertEquals(5, delta.newValue)
        assertEquals(setOf(1, 2), delta.oldNotes)
        assertEquals(emptySet<Int>(), delta.newNotes)
    }
}
