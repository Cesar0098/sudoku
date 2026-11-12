package com.example.ui.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.Difficulty
import com.example.ui.GameUiState

// Fuente "New Amsterdam" (Google Fonts) empaquetada localmente en
// res/font/new_amsterdam.ttf para que el título siga funcionando 100% offline.
private val NewAmsterdamFont = FontFamily(Font(R.font.new_amsterdam))

@Composable
fun HomeScreen(
    uiState: GameUiState,
    onResumeGame: () -> Unit,
    onStartNewGame: (Difficulty) -> Unit,
    onNavigateStats: () -> Unit,
    onNavigateSettings: () -> Unit,
    onDailyGame: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDifficultyPicker by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .testTag("home_screen"),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            // Escala responsiva según el ancho disponible en esta pantalla, con
            // 320dp (el contenido de un teléfono típico de 360dp, ya sin el
            // padding horizontal) como referencia = escala 1x. Así el título y
            // los botones se ven bien tanto en teléfonos chicos como grandes,
            // sin quedar gigantes en tablets.
            val widthScale = (maxWidth.value / 320f).coerceIn(0.85f, 1.6f)
            val titleFontSize = (64f * widthScale).sp
            val ctaFontSize = (18f * widthScale).coerceIn(16f, 22f).sp
            val secondaryButtonFontSize = (14f * widthScale).coerceIn(13f, 17f).sp

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // "SUDOKU" como protagonista de la pantalla, con la fuente
                // "New Amsterdam" (Google Fonts) pedida para el título, en un
                // tamaño que escala con el ancho de pantalla.
                Text(
                    text = "SUDOKU",
                    fontFamily = NewAmsterdamFont,
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Clásico & Minimalista",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(48.dp))

                if (uiState.isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Generando puzzle...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    OutlinedButton(onClick = onDailyGame, modifier = Modifier.fillMaxWidth()) {
                        Text("Desafío diario · Medio")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // Resume Active Game (if exists)
                    if (uiState.hasActiveSavedGame) {
                        Button(
                            onClick = onResumeGame,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("continue_game_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Continuar partida",
                                fontSize = ctaFontSize,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Play / New Game Button
                    Button(
                        onClick = { showDifficultyPicker = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("play_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = if (uiState.hasActiveSavedGame) {
                            ButtonDefaults.filledTonalButtonColors()
                        } else {
                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        }
                    ) {
                        Text(
                            text = if (uiState.hasActiveSavedGame) "Nueva partida" else "Jugar",
                            fontSize = ctaFontSize,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Respetar el tamaño de fuente elegido por el usuario.
                    val currentDensity = LocalDensity.current
                    val cappedDensity = remember(currentDensity) {
                        Density(
                            density = currentDensity.density,
                            fontScale = currentDensity.fontScale
                        )
                    }

                    CompositionLocalProvider(LocalDensity provides cappedDensity) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onNavigateStats,
                                modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("stats_button"),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.BarChart,
                                    contentDescription = null,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Estadísticas",
                                    fontSize = secondaryButtonFontSize,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            OutlinedButton(
                                onClick = onNavigateSettings,
                                modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("settings_button"),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ajustes",
                                    fontSize = secondaryButtonFontSize,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDifficultyPicker) {
        DifficultyPickerDialog(
            onDismiss = { showDifficultyPicker = false },
            onSelect = { diff ->
                showDifficultyPicker = false
                onStartNewGame(diff)
            }
        )
    }
}

@Composable
fun DifficultyPickerDialog(
    onDismiss: () -> Unit,
    onSelect: (Difficulty) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("difficulty_picker_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Seleccionar dificultad",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                Difficulty.entries.forEach { diff ->
                    Button(
                        onClick = { onSelect(diff) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(vertical = 4.dp)
                            .testTag("difficulty_${diff.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = diff.displayName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}
