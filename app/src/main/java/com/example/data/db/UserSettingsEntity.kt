package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    SEPIA
}

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val themeMode: String = AppThemeMode.SYSTEM.name,
    val autoCheckErrors: Boolean = true,
    val errorLimitEnabled: Boolean = true,
    val autoRemoveNotes: Boolean = true,
    val highlightDuplicates: Boolean = true,
    val highlightScope: Boolean = true
)
