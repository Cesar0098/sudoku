package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Sudoku Color Tokens
data class SudokuColors(
    val boardBackground: Color,
    val cellBackground: Color,
    val cellSelectedBackground: Color,
    val cellSelectedBorder: Color,
    val cellScopeBackground: Color,
    val cellSameNumberBackground: Color,
    val cellErrorBackground: Color,
    val gridLineThin: Color,
    val gridLineThick: Color,
    val givenText: Color,
    val userText: Color,
    val errorText: Color,
    val noteText: Color,
    val keypadBackground: Color,
    val keypadBorder: Color,
    val keypadText: Color,
    val keypadActiveBackground: Color,
    val keypadActiveText: Color,
    val keypadCompletedText: Color,
    val controlActiveColor: Color,
    val headerLabelColor: Color,
    val completionGlow: Color
)

val LightSudokuColors = SudokuColors(
    boardBackground = Color(0xFFF7F7F7),            // Gris cálido muy claro, fondo general de la app de referencia
    cellBackground = Color(0xFFFFFFFF),             // Blanco puro de cada celda
    cellSelectedBackground = Color(0xFFACC9FD),     // Azul cielo vívido (celda seleccionada / mismo número)
    cellSelectedBorder = Color(0xFF175090),         // Azul tinta (mismo tono que "Score" y los números del usuario)
    cellScopeBackground = Color(0xFFEDF3EF),        // Wash gris-verdoso muy sutil para fila/columna/bloque
    cellSameNumberBackground = Color(0xFFACC9FD),   // La referencia usa el mismo azul para todas las coincidencias
    cellErrorBackground = Color(0xFFFDE8E7),        // Soft error tint (sin referencia; se mantiene)
    gridLineThin = Color(0xFFD2D6D2),               // Línea fina gris clara
    gridLineThick = Color(0xFF5A5A59),              // Charcoal medio para bloques 3x3 y borde exterior
    givenText = Color(0xFF282828),                  // Casi negro para las pistas originales
    userText = Color(0xFF175090),                   // Azul tinta para los números del usuario
    errorText = Color(0xFFE53935),                  // Vibrant solid red for incorrect numbers
    noteText = Color(0xFF3D3D3D),                   // Gris oscuro para las notas/candidatos
    keypadBackground = Color(0xFFFFFFFF),           // Tarjetas blancas del teclado
    keypadBorder = Color(0xFFE5E5E5),               // Borde sutil
    keypadText = Color(0xFF8C8B8D),                 // Gris medio para 1-9
    keypadActiveBackground = Color(0xFF175090),     // Azul tinta para la tecla activa
    keypadActiveText = Color(0xFFFFFFFF),           // Active white text
    keypadCompletedText = Color(0xFFC7C7C7),        // Gris claro atenuado
    controlActiveColor = Color(0xFF175090),
    headerLabelColor = Color(0xFF828282),           // Gris de "Mistakes / Expert / Timer"
    completionGlow = Color(0xFF2FBF83)              // Verde suave para fila/columna/bloque completado
)

val DarkSudokuColors = SudokuColors(
    boardBackground = Color(0xFF000000),            // Negro puro, fondo general de la referencia (Sudoku.com)
    cellBackground = Color(0xFF25242C),             // Slate oscuro de cada celda
    cellSelectedBackground = Color(0xFF143B76),      // Azul marino de la celda seleccionada
    cellSelectedBorder = Color(0xFF5C8FD6),          // Azul más claro para que el borde resalte sobre el slate
    cellScopeBackground = Color(0xFF2D2E39),         // Leve elevación sobre cellBackground para fila/columna/bloque
    cellSameNumberBackground = Color(0xFF143B76),    // La referencia usa el mismo azul para todas las coincidencias
    cellErrorBackground = Color(0xFF4C1D1D),        // Soft dark red tint (sin referencia; se mantiene)
    gridLineThin = Color(0xFF1A1921),               // Línea fina, más oscura que la celda
    gridLineThick = Color(0xFF000000),              // Negro puro para bloques 3x3 y borde exterior
    givenText = Color(0xFF7F7F86),                  // Gris azulado para las pistas originales
    userText = Color(0xFF536886),                   // Azul apagado (mismo tono que los números del teclado)
    errorText = Color(0xFFEF5350),                  // Vibrant coral red for incorrect entries
    noteText = Color(0xFF5C5C63),                   // Gris azulado atenuado para notas/candidatos
    keypadBackground = Color(0xFF1D1C23),           // Panel del teclado, algo más claro que el fondo
    keypadBorder = Color(0xFF2A2930),               // Borde sutil
    keypadText = Color(0xFF536886),                 // Azul apagado de los números 1-9
    keypadActiveBackground = Color(0xFF6081B1),     // Azul del badge "Pista" para la tecla activa
    keypadActiveText = Color(0xFFFFFFFF),           // Active white text
    keypadCompletedText = Color(0xFF3F4B5C),        // Versión atenuada del azul del teclado
    controlActiveColor = Color(0xFF7E99C8),         // Azul perenne de los íconos (paleta/ajustes/atrás)
    headerLabelColor = Color(0xFF6F6F6F),           // Gris de "Difícil / Errores / Timer"
    completionGlow = Color(0xFF4ADE80)              // Verde suave para fila/columna/bloque completado
)

val SepiaSudokuColors = SudokuColors(
    boardBackground = Color(0xFFF5EFEB),
    cellBackground = Color(0xFFFAF7F2),
    cellSelectedBackground = Color(0xFFDFCCB7),     // Warm biscuit selection
    cellSelectedBorder = Color(0xFF7D4E3A),         // Sepia brown border
    cellScopeBackground = Color(0xFFEFE7DE),        // Neutral soft warm-gray wash for 3x3 block, row & col
    cellSameNumberBackground = Color(0xFFE4D5C3),   // Matching numbers highlight
    cellErrorBackground = Color(0xFFFDE8E7),        // Soft red tint
    gridLineThin = Color(0xFFDDD2C6),               // Warm beige
    gridLineThick = Color(0xFF6B5848),              // Rich sepia brown
    givenText = Color(0xFF3E2F23),                  // Espresso ink for original clues
    userText = Color(0xFF28569A),                   // Signature denim blue for user entries (distinct from given)
    errorText = Color(0xFFE53935),                  // Clean vibrant red for incorrect entries
    noteText = Color(0xFF8D7B68),                   // Soft coffee for notes
    keypadBackground = Color(0xFFEDE4D8),           // Warm cream pill
    keypadBorder = Color(0xFFD3C5B4),               // Cream border
    keypadText = Color(0xFF4A3E36),                 // Neutral espresso text
    keypadActiveBackground = Color(0xFF7D4E3A),     // Active key
    keypadActiveText = Color(0xFFFAF7F2),           // Light cream text
    keypadCompletedText = Color(0xFFC5B8A8),        // Dimmed cream
    controlActiveColor = Color(0xFF7D4E3A),
    headerLabelColor = Color(0xFF7D4E3A),
    completionGlow = Color(0xFF6E9C4F)              // Verde suave (tono cálido) para fila/columna/bloque completado
)

val LocalSudokuColors = staticCompositionLocalOf { LightSudokuColors }

val MaterialTheme.sudokuColors: SudokuColors
    @Composable
    @ReadOnlyComposable
    get() = LocalSudokuColors.current
