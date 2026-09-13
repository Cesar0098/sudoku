package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_game")
data class ActiveGameEntity(
    @PrimaryKey val id: Int = 1,
    val difficulty: String,
    val puzzle: String,
    val solution: String,
    val currentValues: String,
    val givenMask: String,
    val notesData: String,
    val elapsedSeconds: Long,
    val errorsCount: Int,
    val hintsUsed: Int,
    val isPaused: Boolean,
    val isCompleted: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
