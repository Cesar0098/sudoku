package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Visual Sudoku Logo matching the 3x3 grid design:
 * S | U | [ ]
 * D | O | K
 * [ ] | [ ] | U
 * with deep navy background and crisp white rounded cells.
 */
@Composable
fun SudokuLogo(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF0C192C),
    cellColor: Color = Color(0xFFFFFFFF),
    textColor: Color = Color(0xFF0C192C),
    cornerRadius: Dp = 18.dp
) {
    val grid = listOf(
        listOf("S", "U", ""),
        listOf("D", "O", "K"),
        listOf("", "", "U")
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .padding(cornerRadius * 0.45f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (r in 0 until 3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    for (c in 0 until 3) {
                        val letter = grid[r][c]
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .padding(cornerRadius * 0.18f)
                                .clip(RoundedCornerShape(cornerRadius * 0.25f))
                                .background(cellColor),
                            contentAlignment = Alignment.Center
                        ) {
                            if (letter.isNotEmpty()) {
                                Text(
                                    text = letter,
                                    color = textColor,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.SansSerif,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
