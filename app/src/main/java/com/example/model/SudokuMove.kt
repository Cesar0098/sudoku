package com.example.model

data class CellDelta(
    val row: Int,
    val col: Int,
    val oldValue: Int,
    val newValue: Int,
    val oldNotes: Set<Int>,
    val newNotes: Set<Int>,
    val oldIsError: Boolean = false,
    val newIsError: Boolean = false
)

data class SudokuMove(
    val deltas: List<CellDelta>
)
