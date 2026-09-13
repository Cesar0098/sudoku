package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.db.AppDatabase
import com.example.data.db.AppThemeMode
import com.example.data.repository.SudokuRepository
import com.example.ui.SudokuViewModel
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.SudokuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = SudokuRepository(database.sudokuDao())
        val viewModelFactory = SudokuViewModel.provideFactory(repository)

        setContent {
            val sudokuViewModel: SudokuViewModel = viewModel(factory = viewModelFactory)
            val userSettings by sudokuViewModel.userSettings.collectAsStateWithLifecycle()
            val themeMode = runCatching { AppThemeMode.valueOf(userSettings.themeMode) }.getOrDefault(AppThemeMode.SYSTEM)

            SudokuTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SudokuApp(sudokuViewModel)
                }
            }
        }
    }
}

@Composable
fun SudokuApp(viewModel: SudokuViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val allStats by viewModel.allStats.collectAsStateWithLifecycle()
    val recentHistoryByDifficulty by viewModel.recentHistoryByDifficulty.collectAsStateWithLifecycle()

    // Lifecycle observer to pause game when app moves to background
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                viewModel.pauseGame()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                uiState = uiState,
                onResumeGame = {
                    viewModel.resumeSavedGame()
                    navController.navigate("game")
                },
                onStartNewGame = { difficulty ->
                    viewModel.startNewGame(difficulty)
                    navController.navigate("game")
                },
                onNavigateStats = {
                    navController.navigate("stats")
                },
                onNavigateSettings = {
                    navController.navigate("settings")
                }
            )
        }

        composable("game") {
            GameScreen(
                uiState = uiState,
                userSettings = userSettings,
                onBackToHome = {
                    viewModel.pauseGame()
                    navController.popBackStack("home", inclusive = false)
                },
                onNavigateSettings = {
                    viewModel.pauseGame()
                    navController.navigate("settings")
                },
                onCellClick = { row, col ->
                    viewModel.selectCell(row, col)
                },
                onNumberClick = { number ->
                    viewModel.inputNumber(number)
                },
                onUndoClick = {
                    viewModel.undo()
                },
                onEraseClick = {
                    viewModel.erase()
                },
                onNotesToggleClick = {
                    viewModel.toggleNotesMode()
                },
                onHintClick = {
                    viewModel.useHint()
                },
                onPauseClick = {
                    viewModel.pauseGame()
                },
                onResumeClick = {
                    viewModel.resumeGame()
                },
                onRestartClick = {
                    viewModel.restartCurrentGame()
                },
                onNewGameClick = { difficulty ->
                    viewModel.startNewGame(difficulty)
                }
            )
        }

        composable("stats") {
            StatsScreen(
                statsList = allStats,
                historyByDifficulty = recentHistoryByDifficulty,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                userSettings = userSettings,
                onUpdateSettings = { updated ->
                    viewModel.updateSettings(updated)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
