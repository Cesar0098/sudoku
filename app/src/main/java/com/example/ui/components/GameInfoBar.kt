package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Difficulty
import com.example.ui.theme.sudokuColors
import java.util.Locale

@Composable
fun GameInfoBar(
    difficulty: Difficulty,
    errorsCount: Int,
    maxErrors: Int,
    isAutoCheck: Boolean,
    elapsedSeconds: Long,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.sudokuColors
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val formattedTime = String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Difficulty column (Sleek header: Nivel / DIFICIL)
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "NIVEL",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = colors.headerLabelColor
            )
            Text(
                text = difficulty.displayName.uppercase(Locale.ROOT),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Errors column (Sleek header: Errores / 0/3)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ERRORES",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = colors.headerLabelColor
            )
            Text(
                text = if (isAutoCheck) {
                    "$errorsCount"
                } else {
                    "$errorsCount/$maxErrors"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (errorsCount > 0) colors.errorText else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.testTag("errors_indicator")
            )
        }

        // Time column (Sleek header: Tiempo / 04:37). El botón de pausa vive
        // solo en la barra superior para no duplicar la misma acción dos veces.
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "TIEMPO",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = colors.headerLabelColor
            )
            Text(
                text = formattedTime,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.testTag("timer_text")
            )
        }
    }
}
