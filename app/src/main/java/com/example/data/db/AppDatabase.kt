package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ActiveGameEntity::class,
        GameStatsEntity::class,
        UserSettingsEntity::class,
        GameHistoryEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sudokuDao(): SudokuDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // v1 -> v2: agrega la tabla game_history (una fila por partida ganada,
        // para el gráfico de progreso) y dos columnas de racha de partidas
        // perfectas en game_stats. Con esta Migration explícita los datos
        // existentes (partida activa, estadísticas acumuladas) se conservan
        // en vez de perderse por la destructiva de más abajo.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE game_stats ADD COLUMN currentPerfectStreak INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE game_stats ADD COLUMN bestPerfectStreak INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `game_history` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `difficulty` TEXT NOT NULL,
                        `completedAt` INTEGER NOT NULL,
                        `timeSeconds` INTEGER NOT NULL,
                        `errors` INTEGER NOT NULL,
                        `hints` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        // v2 -> v3: agrega el conteo total de victorias perfectas (a
        // diferencia de currentPerfectStreak/bestPerfectStreak, que son
        // rachas, esta columna es un total acumulado para la tarjeta
        // "Victorias perfectas" de Estadísticas).
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE game_stats ADD COLUMN perfectWins INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sudoku_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    // Red de seguridad: si en el futuro cambia el esquema otra
                    // vez sin una Migration explícita, Room recrea la base en
                    // vez de crashear al abrir la app (se pierden los datos
                    // locales en ese caso puntual, pero no hay crash).
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
