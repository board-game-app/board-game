package ru.internet.boardgames.spygame.data.local.assets

import android.content.Context
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import ru.internet.boardgames.spygame.data.local.datastore.SettingsDataStore
import ru.internet.boardgames.spygame.data.local.db.CategoryEntity
import ru.internet.boardgames.spygame.data.local.db.SpyGameDatabase
import ru.internet.boardgames.spygame.data.local.db.WordEntity
import ru.internet.boardgames.spygame.data.model.CategoriesFileDto
import ru.internet.boardgames.spygame.data.model.ContentManifest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Загрузчик контента SpyGame из JSON-ассетов в Room.
 *
 * Логика идентична SpyGame-оригиналу: сравниваем версию манифеста
 * с сохранённой в DataStore, и пересеиваем БД только при изменении.
 *
 * [BoardGamesApplication] инжектирует этот класс и вызывает
 * [seedIfNeeded] при старте приложения.
 */
@Singleton
class ContentLoader @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val database: SpyGameDatabase,
    private val settingsDataStore: SettingsDataStore
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun seedIfNeeded() {
        val manifest     = loadManifest()
        val storedVersion = settingsDataStore.getContentVersion()

        if (manifest.contentVersion > storedVersion) {
            database.withTransaction {
                database.wordDao().deleteAll()
                database.categoryDao().deleteAll()

                for (language in manifest.supportedLanguages) {
                    val data = loadCategories(language)
                    insertCategoriesWithWords(data, language)
                }
            }
            settingsDataStore.setContentVersion(manifest.contentVersion)
        }
    }

    private fun loadManifest(): ContentManifest =
        context.assets.open("content/manifest.json")
            .bufferedReader().use { it.readText() }
            .let { json.decodeFromString(it) }

    private fun loadCategories(language: String): CategoriesFileDto =
        context.assets.open("content/categories_$language.json")
            .bufferedReader().use { it.readText() }
            .let { json.decodeFromString(it) }

    private suspend fun insertCategoriesWithWords(data: CategoriesFileDto, language: String) {
        database.categoryDao().insertCategories(
            data.categories.map { dto ->
                CategoryEntity(id = dto.id, name = dto.name, language = language)
            }
        )
        database.wordDao().insertWords(
            data.categories.flatMap { dto ->
                dto.words.map { word ->
                    WordEntity(categoryId = dto.id, word = word, language = language)
                }
            }
        )
    }
}
