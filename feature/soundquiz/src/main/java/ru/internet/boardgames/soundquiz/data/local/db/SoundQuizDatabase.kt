package ru.internet.boardgames.soundquiz.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * База данных модуля :feature:sound-quiz.
 * Префикс таблиц "sq_" исключает конфликты с другими feature-модулями в общем APK.
 */
@Database(
    entities = [CategoryEntity::class, WordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SoundQuizDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun wordDao(): WordDao
}
