package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.UserSettingsEntity
import com.example.model.Difficulty
import com.example.ui.GameUiState
import com.example.ui.components.GameControlsBar
import com.example.ui.components.GameInfoBar
import com.example.ui.components.GameOverDialog
import com.example.ui.components.NumberKeypad
import com.example.ui.components.PauseOverlay
import com.example.ui.components.SudokuGrid
import com.example.ui.components.VictoryDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    uiState: GameUiState,
    userSettings: UserSettingsEntity,
    onBackToHome: () -> Unit,
    onNavigateSettings: () -> Unit,
    onCellClick: (row: Int, col: Int) -> Unit,
    onNumberClick: (Int) -> Unit,
    onUndoClick: () -> Unit,
    onEraseClick: () -> Unit,
    onNotesToggleClick: () -> Unit,
    onHintClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onRestartClick: () -> Unit,
    onNewGameClick: (Difficulty) -> Unit,
    modifier: Modifier = Modifier
) {
    // Efecto de "escala + fade suave" en todo el tablero al completar la partida,
    // antes de mostrar el diálogo de victoria (en vez de aparecer de golpe apenas
    // se coloca el último número).
    var showVictoryDialog by remember { mutableStateOf(false) }
    val boardScale = remember { Animatable(1f) }
    val boardAlpha = remember { Animatable(1f) }

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            boardAlpha.animateTo(0.6f, animationSpec = tween(220))
            boardScale.animateTo(1.045f, animationSpec = tween(220))
            boardScale.animateTo(1f, animationSpec = tween(260))
            boardAlpha.animateTo(1f, animationSpec = tween(260))
            showVictoryDialog = true
        } else {
            showVictoryDialog = false
            boardScale.snapTo(1f)
            boardAlpha.snapTo(1f)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Sudoku",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackToHome,
                            modifier = Modifier.testTag("game_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Volver al inicio"
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onPauseClick,
                            modifier = Modifier.testTag("top_pause_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Pause,
                                contentDescription = "Pausar"
                            )
                        }
                        IconButton(
                            onClick = onNavigateSettings,
                            modifier = Modifier.testTag("game_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Configuración"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            if (uiState.isGenerating) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Generando tablero ${uiState.difficulty.displayName}...",
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Info Bar (Difficulty, Errors, Time)
                    GameInfoBar(
                        difficulty = uiState.difficulty,
                        errorsCount = uiState.errorsCount,
                        maxErrors = uiState.maxErrors,
                        isAutoCheck = userSettings.autoCheckErrors,
                        elapsedSeconds = uiState.elapsedSeconds
                    )

                    // 9x9 Sudoku Board
                    SudokuGrid(
                        board = uiState.board,
                        selectedCell = uiState.selectedCell,
                        highlightDuplicates = userSettings.highlightDuplicates,
                        highlightScope = userSettings.highlightScope,
                        onCellClick = onCellClick,
                        completionBurst = uiState.completionBurst,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .graphicsLayer {
                                scaleX = boardScale.value
                                scaleY = boardScale.value
                                alpha = boardAlpha.value
                            }
                    )

                    // Control actions: Undo, Erase, Notes, Hint
                    GameControlsBar(
                        isNotesMode = uiState.isNotesMode,
                        canUndo = uiState.canUndo,
                        hintsUsed = uiState.hintsUsed,
                        onUndoClick = onUndoClick,
                        onEraseClick = onEraseClick,
                        onNotesToggleClick = onNotesToggleClick,
                        onHintClick = onHintClick
                    )

                    // Numeric Keypad 1..9
                    val selectedNumber = uiState.selectedCell?.let {
                        uiState.board.getCell(it.first, it.second).value
                    } ?: 0

                    NumberKeypad(
                        board = uiState.board,
                        selectedNumber = selectedNumber,
                        onNumberClick = onNumberClick,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
        }

        // Pause Overlay
        if (uiState.isPaused) {
            PauseOverlay(
                elapsedSeconds = uiState.elapsedSeconds,
                difficulty = uiState.difficulty,
                onResume = onResumeClick,
                onRestart = onRestartClick,
                onHome = onBackToHome
            )
        }

        // Victory Dialog (se muestra recién cuando termina el efecto del tablero)
        if (showVictoryDialog) {
            VictoryDialog(
                elapsedSeconds = uiState.elapsedSeconds,
                difficulty = uiState.difficulty,
                errors = uiState.errorsCount,
                hintsUsed = uiState.hintsUsed,
                onNewGame = { onNewGameClick(uiState.difficulty) },
                onHome = onBackToHome
            )
        }

        // Game Over Dialog
        if (uiState.isGameOver) {
            GameOverDialog(
                difficulty = uiState.difficulty,
                onRestart = onRestartClick,
                onNewGame = { onNewGameClick(uiState.difficulty) },
                onHome = onBackToHome
            )
        }
    }
}
