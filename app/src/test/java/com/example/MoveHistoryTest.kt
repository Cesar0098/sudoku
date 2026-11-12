package com.example

import com.example.model.*
import com.example.engine.SudokuSolver
import org.junit.Assert.*
import org.junit.Test

class MoveHistoryTest {
    @Test fun historyRoundTripPreservesNotesAndErrors() {
        val moves = listOf(SudokuMove(listOf(
            CellDelta(0, 8, 0, 4, setOf(2, 4), emptySet(), false, true),
            CellDelta(0, 1, 0, 0, setOf(4, 7), setOf(7)))))
        assertEquals(moves, MoveHistoryCodec.decode(MoveHistoryCodec.encode(moves)))
        assertTrue(MoveHistoryCodec.decode("broken").isEmpty())
    }
    @Test fun invalidFullGridHasNoSolution() {
        assertEquals(0, SudokuSolver.countSolutions(IntArray(81) { 1 }))
        assertFalse(SudokuSolver.hasUniqueSolution(IntArray(81) { 1 }))
    }
}
