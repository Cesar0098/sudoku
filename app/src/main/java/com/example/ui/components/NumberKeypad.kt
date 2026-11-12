package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SudokuBoard
import com.example.ui.theme.sudokuColors

@Composable
fun NumberKeypad(
    board: SudokuBoard,
    onNumberClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    assisted: Boolean = true,
    selectedNumber: Int = 0
) {
    val colors = MaterialTheme.sudokuColors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (num in 1..9) {
            val isCompleted = assisted && board.isNumberCompleted(num)
            val currentCount = if (assisted) board.countCorrectInstances(num) else board.countInstances(num)
            val remaining = (9 - currentCount).coerceAtLeast(0)
            val isSelectedNumber = num == selectedNumber && selectedNumber != 0

            KeypadButton(
                number = num,
                isCompleted = isCompleted,
                isSelected = isSelectedNumber,
                remaining = remaining,
                onClick = { onNumberClick(num) },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("keypad_button_$num")
            )
        }
    }
}

@Composable
private fun KeypadButton(
    number: Int,
    isCompleted: Boolean,
    isSelected: Boolean,
    remaining: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.sudokuColors
    val interactionSource = remember { MutableInteractionSource() }

    val bgColor = when {
        isSelected -> colors.keypadActiveBackground
        isCompleted -> colors.keypadBackground.copy(alpha = 0.4f)
        else -> colors.keypadBackground
    }

    val borderColor = when {
        isSelected -> colors.keypadActiveBackground
        isCompleted -> colors.keypadBorder.copy(alpha = 0.3f)
        else -> colors.keypadBorder
    }

    val textColor = when {
        isSelected -> colors.keypadActiveText
        isCompleted -> colors.keypadCompletedText
        else -> colors.keypadText
    }

    val subTextColor = when {
        isSelected -> colors.keypadActiveText.copy(alpha = 0.8f)
        isCompleted -> colors.keypadCompletedText
        else -> colors.noteText
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(
                enabled = !isCompleted,
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = colors.controlActiveColor.copy(alpha = 0.2f)),
                onClick = onClick
            )
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = number.toString(),
                fontSize = 22.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (isCompleted) "✓" else if (remaining in 1..8) "$remaining" else "",
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = subTextColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
