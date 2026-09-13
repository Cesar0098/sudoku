package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CompletionBurst
import com.example.model.LineType
import com.example.model.SudokuBoard
import com.example.model.SudokuCell
import com.example.ui.theme.sudokuColors
import kotlinx.coroutines.delay

// Cuánto tarda en "viajar" la onda de una celda a la siguiente dentro de la
// fila/columna/bloque completado.
private const val SWEEP_STEP_MS = 35L

@Composable
fun SudokuGrid(
    board: SudokuBoard,
    selectedCell: Pair<Int, Int>?,
    highlightDuplicates: Boolean,
    highlightScope: Boolean,
    onCellClick: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier,
    completionBurst: CompletionBurst? = null
) {
    val colors = MaterialTheme.sudokuColors
    val selectedValue = selectedCell?.let { board.getCell(it.first, it.second).value } ?: 0

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(colors.boardBackground)
            .border(2.dp, colors.gridLineThick, RoundedCornerShape(6.dp))
            .padding(1.dp)
            .testTag("sudoku_grid")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (row in 0 until 9) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    for (col in 0 until 9) {
                        val cell = board.getCell(row, col)
                        val isSelected = selectedCell?.first == row && selectedCell.second == col

                        val isSameNumber = highlightDuplicates &&
                                selectedValue != 0 &&
                                cell.value == selectedValue

                        // Shade the 3x3 block, the row, and the column whenever an individual cell is
                        // selected — pero solo si el usuario tiene activado "Resaltar fila, columna y
                        // bloque" en Ajustes; si no, esta variable queda en false y el highlight no se
                        // dibuja (ver también el textColor de más abajo).
                        val isInScope = highlightScope && selectedCell != null && (
                                selectedCell.first == row ||
                                        selectedCell.second == col ||
                                        ((selectedCell.first / 3 == row / 3) && (selectedCell.second / 3 == col / 3))
                                )

                        // OJO: no comparar contra cell.solutionValue acá. cell.isError ya viene
                        // calculado desde el ViewModel respetando "Auto-comprobación de errores"
                        // (ver shouldShowErrors en SudokuViewModel) — si se le suma un chequeo directo
                        // contra la solución, un número mal puesto se pinta de rojo igual aunque el
                        // usuario haya apagado esa opción, y el ajuste deja de tener efecto visible.
                        val isIncorrect = !cell.isGiven && cell.value != 0 && cell.isError

                        // Posición de esta celda dentro de la onda de brillo (0..8) si
                        // pertenece a la fila/columna/bloque que se acaba de completar;
                        // null si esta celda no participa de la ráfaga actual.
                        val sweepPosition = completionBurst?.sweepPositionFor(row, col)

                        val targetBgColor = when {
                            isSelected -> colors.cellSelectedBackground
                            isIncorrect -> colors.cellErrorBackground
                            isSameNumber -> colors.cellSameNumberBackground
                            isInScope -> colors.cellScopeBackground
                            else -> colors.cellBackground
                        }

                        val animatedBgColor by animateColorAsState(
                            targetValue = targetBgColor,
                            animationSpec = tween(durationMillis = 150),
                            label = "cellBg"
                        )

                        SudokuCellView(
                            cell = cell,
                            isSelected = isSelected,
                            backgroundColor = animatedBgColor,
                            burstId = completionBurst?.id,
                            sweepPosition = sweepPosition,
                            highlightedNumber = if (highlightDuplicates) selectedValue else 0,
                            onClick = { onCellClick(row, col) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .testTag("cell_${row}_${col}")
                        )

                        // Vertical divider
                        if (col < 8) {
                            val isBlockBorder = (col + 1) % 3 == 0
                            val dividerColor = if (isBlockBorder) colors.gridLineThick else colors.gridLineThin
                            val dividerWidth = if (isBlockBorder) 2.dp else 0.75.dp
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(dividerWidth.value / 40f)
                                    .background(dividerColor)
                            )
                        }
                    }
                }

                // Horizontal divider
                if (row < 8) {
                    val isBlockBorder = (row + 1) % 3 == 0
                    val dividerColor = if (isBlockBorder) colors.gridLineThick else colors.gridLineThin
                    val dividerHeight = if (isBlockBorder) 2.dp else 0.75.dp
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(dividerHeight.value / 40f)
                            .background(dividerColor)
                    )
                }
            }
        }
    }
}

// Dada una celda (row, col), devuelve en qué posición (0..8) le toca dentro de
// la onda si pertenece a alguno de los eventos de esta ráfaga, o null si la
// celda no forma parte de la fila/columna/bloque que se completó.
private fun CompletionBurst.sweepPositionFor(row: Int, col: Int): Int? {
    for (event in events) {
        val position = when (event.type) {
            LineType.ROW -> if (event.index == row) col else null
            LineType.COLUMN -> if (event.index == col) row else null
            LineType.BLOCK -> {
                val blockIndex = (row / 3) * 3 + (col / 3)
                if (event.index == blockIndex) (row % 3) * 3 + (col % 3) else null
            }
        }
        if (position != null) return position
    }
    return null
}

@Composable
private fun SudokuCellView(
    cell: SudokuCell,
    isSelected: Boolean,
    backgroundColor: Color,
    burstId: Long?,
    sweepPosition: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlightedNumber: Int = 0
) {
    val colors = MaterialTheme.sudokuColors
    val interactionSource = remember { MutableInteractionSource() }

    // Brillo de "fila/columna/bloque completado": arranca en 0, sube rápido y
    // se desvanece. El delay según sweepPosition es lo que arma la sensación de
    // onda viajando por la fila/columna/bloque en vez de un flash simultáneo.
    val glowAlpha = remember { Animatable(0f) }
    LaunchedEffect(burstId, sweepPosition) {
        if (burstId != null && sweepPosition != null) {
            glowAlpha.snapTo(0f)
            delay(sweepPosition * SWEEP_STEP_MS)
            glowAlpha.animateTo(1f, animationSpec = tween(90))
            glowAlpha.animateTo(0f, animationSpec = tween(280))
        }
    }

    val cellModifier = if (isSelected) {
        modifier
            .background(backgroundColor)
            .border(2.dp, colors.cellSelectedBorder)
    } else {
        modifier.background(backgroundColor)
    }

    Box(
        modifier = cellModifier
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = colors.controlActiveColor.copy(alpha = 0.2f)),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (glowAlpha.value > 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(colors.completionGlow.copy(alpha = glowAlpha.value * 0.45f))
            )
        }
        if (cell.value != 0) {
            // Igual que en el fondo de la celda: solo cell.isError, que ya respeta el ajuste de
            // "Auto-comprobación de errores".
            val isIncorrect = !cell.isGiven && cell.isError
            val textColor = when {
                isIncorrect -> colors.errorText
                cell.isGiven -> colors.givenText
                else -> colors.userText
            }
            Text(
                text = cell.value.toString(),
                color = textColor,
                fontSize = 24.sp,
                fontWeight = if (cell.isGiven) FontWeight.Bold else FontWeight.Medium,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center
            )
        } else if (cell.notes.isNotEmpty()) {
            NotesGrid(
                notes = cell.notes,
                textColor = colors.noteText,
                highlightedNumber = highlightedNumber,
                modifier = Modifier.fillMaxSize().padding(2.dp)
            )
        }
    }
}

@Composable
private fun NotesGrid(
    notes: Set<Int>,
    textColor: Color,
    modifier: Modifier = Modifier,
    highlightedNumber: Int = 0
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        for (r in 0 until 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (c in 0 until 3) {
                    val num = r * 3 + c + 1
                    val hasNote = notes.contains(num)
                    // El dígito que coincide con el número seleccionado en el
                    // tablero/teclado se resalta solo con negrita, manteniendo
                    // el mismo color de texto de las notas.
                    val isHighlighted = hasNote && num == highlightedNumber
                    Text(
                        text = if (hasNote) num.toString() else "",
                        color = textColor,
                        fontSize = 9.sp,
                        fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                        lineHeight = 9.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
