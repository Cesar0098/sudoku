package com.example.model

data class SudokuCell(
    val row: Int,
    val col: Int,
    val value: Int = 0,
    val solutionValue: Int = 0,
    val isGiven: Boolean = false,
    val notes: Set<Int> = emptySet(),
    val isError: Boolean = false
) {
    val isEmpty: Boolean get() = value == 0
    val blockIndex: Int get() = (row / 3) * 3 + (col / 3)
}
