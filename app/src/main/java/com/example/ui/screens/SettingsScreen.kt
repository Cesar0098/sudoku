package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AppThemeMode
import com.example.data.db.UserSettingsEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userSettings: UserSettingsEntity,
    onUpdateSettings: (UserSettingsEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configuración",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_back_button")
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
        modifier = modifier.testTag("settings_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Theme Section
            Text(
                text = "TEMA VISUAL",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    ThemeOptionRow(
                        title = "Tema Claro",
                        description = "Papel nítido y moderno",
                        previewColor = Color(0xFFF7F7F7),
                        borderColor = Color(0xFFD2D6D2),
                        isSelected = userSettings.themeMode == AppThemeMode.LIGHT.name,
                        onSelect = {
                            onUpdateSettings(userSettings.copy(themeMode = AppThemeMode.LIGHT.name))
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ThemeOptionRow(
                        title = "Tema Oscuro",
                        description = "Gris pizarra y descanso visual",
                        previewColor = Color(0xFF25242C),
                        borderColor = Color(0xFF000000),
                        isSelected = userSettings.themeMode == AppThemeMode.DARK.name,
                        onSelect = {
                            onUpdateSettings(userSettings.copy(themeMode = AppThemeMode.DARK.name))
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ThemeOptionRow(
                        title = "Tema Sepia",
                        description = "Tono pergamino cálido y relajante",
                        previewColor = Color(0xFFFBF6EE),
                        borderColor = Color(0xFFD3C5B4),
                        isSelected = userSettings.themeMode == AppThemeMode.SEPIA.name,
                        onSelect = {
                            onUpdateSettings(userSettings.copy(themeMode = AppThemeMode.SEPIA.name))
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ThemeOptionRow(
                        title = "Tema del Sistema",
                        description = "Sigue la preferencia del dispositivo",
                        previewColor = Color(0xFF94A3B8),
                        borderColor = Color(0xFFCBD5E1),
                        isSelected = userSettings.themeMode == AppThemeMode.SYSTEM.name,
                        onSelect = {
                            onUpdateSettings(userSettings.copy(themeMode = AppThemeMode.SYSTEM.name))
                        }
                    )
                }
            }

            // Gameplay Section
            Text(
                text = "REGLAS Y JUGABILIDAD",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingSwitchRow(
                        title = "Auto-comprobación de errores",
                        description = "Indica inmediatamente si un número introducido es incorrecto",
                        isChecked = userSettings.autoCheckErrors,
                        onCheckedChange = {
                            onUpdateSettings(userSettings.copy(autoCheckErrors = it))
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingSwitchRow(
                        title = "Límite de 3 errores",
                        description = "Finaliza la partida al cometer 3 errores",
                        isChecked = userSettings.errorLimitEnabled,
                        onCheckedChange = {
                            onUpdateSettings(userSettings.copy(errorLimitEnabled = it))
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingSwitchRow(
                        title = "Eliminar notas automáticamente",
                        description = "Borra el número colocado de las notas de la fila, columna y bloque",
                        isChecked = userSettings.autoRemoveNotes,
                        onCheckedChange = {
                            onUpdateSettings(userSettings.copy(autoRemoveNotes = it))
                        }
                    )
                }
            }

            // Visual Helpers Section
            Text(
                text = "AYUDAS VISUALES",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingSwitchRow(
                        title = "Resaltar números iguales",
                        description = "Resalta todos los números idénticos al seleccionado",
                        isChecked = userSettings.highlightDuplicates,
                        onCheckedChange = {
                            onUpdateSettings(userSettings.copy(highlightDuplicates = it))
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingSwitchRow(
                        title = "Resaltar fila, columna y bloque",
                        description = "Muestra el área de influencia de la casilla seleccionada",
                        isChecked = userSettings.highlightScope,
                        onCheckedChange = {
                            onUpdateSettings(userSettings.copy(highlightScope = it))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    description: String,
    previewColor: Color,
    borderColor: Color,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(previewColor)
                .background(borderColor.copy(alpha = 0.4f), shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        RadioButton(
            selected = isSelected,
            onClick = onSelect
        )
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}
