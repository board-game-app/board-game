package ru.internet.boardgames.spygame.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Единственная Room-база данных приложения.
 *
 * [exportSchema] = false — не экспортируем схему в assets,
 * достаточно для офлайн-приложения без миграций между устройствами.
 *
 * При изменении схемы (добавление полей, таблиц) — увеличить [version]
 * и добавить миграцию или использовать fallbackToDestructiveMigration.
 */
@Database(
    entities = [
        CategoryEntity::class,
        WordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SpyGameDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao

    abstract fun wordDao(): WordDao
}
