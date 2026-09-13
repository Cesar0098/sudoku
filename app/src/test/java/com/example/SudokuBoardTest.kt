package com.example

import com.example.model.SudokuBoard
import com.example.model.SudokuCell
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SudokuBoardTest {

    private fun emptyCells(): MutableList<SudokuCell> =
        MutableList(81) { i -> SudokuCell(row = i / 9, col = i % 9) }

    @Test
    fun `countCorrectInstances ignora colocaciones incorrectas que countInstances si cuenta`() {
        // Bug que se corrigio: el teclado usaba countInstances() para calcular
        // "cuantos quedan" de un numero, pero esa funcion cuenta cualquier celda
        // con ese valor, este bien o mal puesto. Si el jugador puso el 5 tres
        // veces y una esta mal, countInstances(5) da 3 pero solo 2 son validas.
        val cells = emptyCells()
        cells[0] = cells[0].copy(value = 5, solutionValue = 5) // correcta
        cells[1] = cells[1].copy(value = 5, solutionValue = 5) // correcta
        cells[2] = cells[2].copy(value = 5, solutionValue = 7) // mal puesta
        val board = SudokuBoard(cells)

        assertEquals(3, board.countInstances(5))
        assertEquals(2, board.countCorrectInstances(5))
    }

    @Test
    fun `isNumberCompleted exige las nueve correctas, no solo las nueve colocadas`() {
        val cells = emptyCells()
        for (i in 0 until 9) {
            val correcta = i != 8
            cells[i] = cells[i].copy(value = 5, solutionValue = if (correcta) 5 else 9)
        }
        val board = SudokuBoard(cells)

        assertEquals(9, board.countInstances(5))
        assertEquals(8, board.countCorrectInstances(5))
        assertFalse(board.isNumberCompleted(5))
    }
}
