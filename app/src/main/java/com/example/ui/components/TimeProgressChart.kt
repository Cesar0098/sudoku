package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.GameHistoryEntity
import java.util.Locale

/**
 * Gráfico de línea simple (sin dependencias externas) con el tiempo de las
 * últimas partidas ganadas de una dificultad. El eje Y está invertido: un
 * tiempo más bajo (mejor) se dibuja más arriba, así "la línea sube" cuando
 * el jugador mejora, que es lo intuitivo.
 */
@Composable
fun TimeProgressChart(
    history: List<GameHistoryEntity>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (history.size < 2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Jugá algunas partidas más para ver tu progreso acá",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Column
        }

        val times = history.map { it.timeSeconds.toFloat() }
        val minTime = times.min()
        val maxTime = times.max()
        val lineColor = MaterialTheme.colorScheme.primary
        val fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)
        val perfectDotColor = MaterialTheme.colorScheme.tertiary

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(vertical = 4.dp)
        ) {
            val w = size.width
            val h = size.height
            val topPad = h * 0.08f
            val bottomPad = h * 0.08f
            val usableH = h - topPad - bottomPad
            val stepX = if (times.size > 1) w / (times.size - 1) else 0f
            val range = (maxTime - minTime).coerceAtLeast(1f)

            fun yFor(t: Float): Float {
                // Invertido a propósito: menor tiempo = mejor = más arriba.
                return topPad + (1f - (t - minTime) / range) * usableH
            }

            // Líneas de referencia horizontales (arriba/medio/abajo)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
            listOf(0f, 0.5f, 1f).forEach { fraction ->
                val y = topPad + fraction * usableH
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashEffect
                )
            }

            val path = Path()
            times.forEachIndexed { i, t ->
                val x = i * stepX
                val y = yFor(t)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            val fillPath = Path().apply {
                addPath(path)
                lineTo((times.size - 1) * stepX, h)
                lineTo(0f, h)
                close()
            }
            drawPath(path = fillPath, color = fillColor)
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            history.forEachIndexed { i, entry ->
                val x = i * stepX
                val y = yFor(entry.timeSeconds.toFloat())
                drawCircle(
                    color = if (entry.isPerfect) perfectDotColor else lineColor,
                    radius = if (entry.isPerfect) 5.dp.toPx() else 3.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }

        Text(
            text = "Últimas ${history.size} partidas · mejor ${formatMinSec(minTime.toLong())} · " +
                "peor ${formatMinSec(maxTime.toLong())}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatMinSec(totalSecs: Long): String {
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    return String.format(Locale.ROOT, "%02d:%02d", mins, secs)
}
