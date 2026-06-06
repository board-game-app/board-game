package ru.internet.boardgames.counter.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room-база данных модуля :feature:counter.
 *
 * Версия 2 — добавлены поля счётчика:
 *   resetValue, colorArgb, incrementStep, decrementStep, actions.
 * Схема сессий не изменилась.
 */
@Database(
    entities = [SessionEntity::class, CounterEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(ActionsTypeConverter::class)
abstract class CounterDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao
    abstract fun counterDao(): CounterDao

    companion object {

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE counters ADD COLUMN resetValue INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE counters ADD COLUMN colorArgb INTEGER NOT NULL DEFAULT 4294288931"
                )
                database.execSQL(
                    "ALTER TABLE counters ADD COLUMN incrementStep INTEGER NOT NULL DEFAULT 1"
                )
                database.execSQL(
                    "ALTER TABLE counters ADD COLUMN decrementStep INTEGER NOT NULL DEFAULT 1"
                )
                database.execSQL(
                    "ALTER TABLE counters ADD COLUMN actions TEXT NOT NULL DEFAULT ''"
                )
            }
        }
    }
}
