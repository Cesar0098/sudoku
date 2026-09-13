package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.GameHistoryEntity
import com.example.data.db.GameStatsEntity
import com.example.model.Difficulty
import com.example.ui.components.TimeProgressChart
import com.example.ui.icons.MaterialSymbolsOutlined
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    statsList: List<GameStatsEntity>,
    onBack: () -> Unit,
    historyByDifficulty: Map<String, List<GameHistoryEntity>> = emptyMap(),
    modifier: Modifier = Modifier
) {
    val difficulties = Difficulty.entries
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val currentDifficulty = difficulties[selectedTabIndex]

    val currentStats = statsList.firstOrNull { it.difficulty == currentDifficulty.id }
        ?: GameStatsEntity(difficulty = currentDifficulty.id)
    val currentHistory = historyByDifficulty[currentDifficulty.id].orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Estadísticas",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("stats_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.testTag("stats_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SecondaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                difficulties.forEachIndexed { index, difficulty ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = difficulty.displayName,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Partidas jugadas
                StatCard(
                    title = "Partidas jugadas",
                    value = "${currentStats.gamesPlayed}",
                    icon = Icons.Outlined.BarChart,
                    modifier = Modifier.fillMaxWidth()
                )

                // Win rate
                StatCard(
                    title = "Porcentaje de victorias",
                    value = "${currentStats.winRatePercent}%",
                    icon = Icons.Outlined.EmojiEvents,
                    modifier = Modifier.fillMaxWidth()
                )

                // Times y racha: en una sola columna (en vez de 2 tarjetas
                // por fila) para que el título de cada estadística entre
                // completo y no se corte con "..." en pantallas angostas.
                StatCard(
                    title = "Mejor tiempo",
                    value = formatSeconds(currentStats.bestTimeSeconds),
                    icon = MaterialSymbolsOutlined.Timer,
                    modifier = Modifier.fillMaxWidth()
                )
                StatCard(
                    title = "Tiempo promedio",
                    value = formatSeconds(currentStats.averageTimeSeconds),
                    icon = Icons.Outlined.HourglassBottom,
                    modifier = Modifier.fillMaxWidth()
                )

                // Errores y pistas: mismo criterio que arriba, una sola
                // columna para que no se corte la descripción.
                StatCard(
                    title = "Errores totales",
                    value = "${currentStats.totalErrors}",
                    icon = Icons.Outlined.ReportProblem,
                    modifier = Modifier.fillMaxWidth()
                )
                StatCard(
                    title = "Pistas usadas",
                    value = "${currentStats.totalHintsUsed}",
                    icon = Icons.Outlined.Lightbulb,
                    modifier = Modifier.fillMaxWidth()
                )

                // Victorias perfectas (conteo total) junto con la racha de
                // partidas perfectas (ganadas sin errores ni pistas)
                StatCard(
                    title = "Victorias perfectas",
                    value = "${currentStats.perfectWins}",
                    icon = Icons.Outlined.WorkspacePremium,
                    modifier = Modifier.fillMaxWidth()
                )
                StatCard(
                    title = "Racha actual",
                    value = "${currentStats.currentPerfectStreak}",
                    icon = Icons.Outlined.LocalFireDepartment,
                    modifier = Modifier.fillMaxWidth()
                )
                StatCard(
                    title = "Récord de racha",
                    value = "${currentStats.bestPerfectStreak}",
                    icon = Icons.Outlined.Star,
                    modifier = Modifier.fillMaxWidth()
                )

                // Progreso de tiempos
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Progreso de tiempos",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TimeProgressChart(history = currentHistory)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = title,
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun formatSeconds(totalSecs: Long): String {
    if (totalSecs <= 0L) return "--:--"
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    return String.format(Locale.ROOT, "%02d:%02d", mins, secs)
}
