package com.example.model

data class SudokuBoard(
    val cells: List<SudokuCell>
) {
    init {
        require(cells.size == 81) { "A Sudoku board must contain exactly 81 cells" }
    }

    fun getCell(row: Int, col: Int): SudokuCell {
        return cells[row * 9 + col]
    }

    fun getRowCells(row: Int): List<SudokuCell> {
        return (0 until 9).map { col -> getCell(row, col) }
    }

    fun getColCells(col: Int): List<SudokuCell> {
        return (0 until 9).map { row -> getCell(row, col) }
    }

    fun getBlockCells(blockIndex: Int): List<SudokuCell> {
        val startRow = (blockIndex / 3) * 3
        val startCol = (blockIndex % 3) * 3
        val result = mutableListOf<SudokuCell>()
        for (r in 0 until 3) {
            for (c in 0 until 3) {
                result.add(getCell(startRow + r, startCol + c))
            }
        }
        return result
    }

    fun getRelatedCoords(row: Int, col: Int): Set<Pair<Int, Int>> {
        val related = mutableSetOf<Pair<Int, Int>>()
        for (i in 0 until 9) {
            if (i != col) related.add(row to i)
            if (i != row) related.add(i to col)
        }
        val startRow = (row / 3) * 3
        val startCol = (col / 3) * 3
        for (r in startRow until startRow + 3) {
            for (c in startCol until startCol + 3) {
                if (r != row || c != col) {
                    related.add(r to c)
                }
            }
        }
        return related
    }

    fun countInstances(number: Int): Int {
        if (number !in 1..9) return 0
        return cells.count { it.value == number }
    }

    // A diferencia de countInstances(), solo cuenta las celdas donde el
    // número está colocado Y es correcto. countInstances() por sí solo
    // puede hacer que el teclado muestre "0 restantes" para un número que
    // en realidad se colocó mal varias veces.
    fun countCorrectInstances(number: Int): Int {
        if (number !in 1..9) return 0
        return cells.count { it.value == number && it.value == it.solutionValue }
    }

    fun isNumberCompleted(number: Int): Boolean {
        if (number !in 1..9) return false
        val matching = cells.filter { it.value == number }
        return matching.size == 9 && matching.all { it.value == it.solutionValue }
    }

    // Usados para disparar el efecto de "fila/columna/bloque completado": una
    // fila/columna/bloque cuenta como completa cuando sus 9 celdas tienen el
    // valor correcto (las celdas dadas siempre lo cumplen, así que esto solo
    // se vuelve true cuando falta el último acierto del usuario).
    fun isRowComplete(row: Int): Boolean =
        getRowCells(row).all { it.value == it.solutionValue }

    fun isColComplete(col: Int): Boolean =
        getColCells(col).all { it.value == it.solutionValue }

    fun isBlockComplete(blockIndex: Int): Boolean =
        getBlockCells(blockIndex).all { it.value == it.solutionValue }

    val isComplete: Boolean
        get() = cells.none { it.isEmpty }

    val isSolvedCorrectly: Boolean
        get() = isComplete && cells.all { it.value == it.solutionValue }

    companion object {
        fun createEmpty(): SudokuBoard {
            val cellList = mutableListOf<SudokuCell>()
            for (r in 0 until 9) {
                for (c in 0 until 9) {
                    cellList.add(SudokuCell(row = r, col = c))
                }
            }
            return SudokuBoard(cellList)
        }

        fun fromGrid(
            puzzle: IntArray,
            solution: IntArray
        ): SudokuBoard {
            require(puzzle.size == 81 && solution.size == 81)
            val list = mutableListOf<SudokuCell>()
            for (i in 0 until 81) {
                val r = i / 9
                val c = i % 9
                val v = puzzle[i]
                list.add(
                    SudokuCell(
                        row = r,
                        col = c,
                        value = v,
                        solutionValue = solution[i],
                        isGiven = v != 0
                    )
                )
            }
            return SudokuBoard(list)
        }
    }
}
