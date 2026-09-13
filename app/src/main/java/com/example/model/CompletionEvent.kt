package com.example.model

/**
 * Tipo de línea que se acaba de completar (los 9 valores correctos).
 */
enum class LineType { ROW, COLUMN, BLOCK }

/**
 * Una fila, columna o bloque 3x3 puntual que se completó con un movimiento.
 */
data class LineCompletionEvent(
    val type: LineType,
    val index: Int
)

/**
 * Agrupa los eventos de finalización que dispara un mismo movimiento (puede
 * completarse una fila, una columna y un bloque a la vez con una sola celda).
 * `id` es único por ráfaga para que la UI pueda usarlo como key de animación:
 * si no cambia, no hay que volver a reproducir el efecto.
 */
data class CompletionBurst(
    val events: List<LineCompletionEvent>,
    val id: Long = System.nanoTime()
)
