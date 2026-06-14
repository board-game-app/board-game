package ru.internet.boardgames.soundquiz.data.local.assets

import android.content.Context
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import ru.internet.boardgames.soundquiz.data.local.datastore.SoundQuizSettingsDataStore
import ru.internet.boardgames.soundquiz.data.local.db.CategoryEntity
import ru.internet.boardgames.soundquiz.data.local.db.SoundQuizDatabase
import ru.internet.boardgames.soundquiz.data.local.db.WordEntity
import ru.internet.boardgames.soundquiz.data.model.CategoriesFileSq
import ru.internet.boardgames.soundquiz.data.model.ContentManifestSq
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Загружает встроенные категории и слова из assets в базу данных.
 * Работает независимо от ContentLoader SpyGame — файлы с суффиксом "_sq".
 * Вызывать один раз при старте (например из ViewModel через use case или из DI).
 */
@Singleton
class SoundQuizContentLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: SoundQuizDatabase,
    private val settingsDataStore: SoundQuizSettingsDataStore
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Инициализация встроенного контента.
     * Пропускает загрузку если версия в DataStore >= версии в манифесте.
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        val manifest = loadManifest()
        val storedVersion = settingsDataStore.getContentVersion()

        // Загрузка не нужна — контент актуален
        if (storedVersion >= manifest.contentVersion) return@withContext

        // Загружаем все поддерживаемые локали
        for (language in manifest.supportedLanguages) {
            val fileName = "content/categories_sq_$language.json"
            val categoriesFile = loadCategoriesFile(fileName)
            replaceBuiltInContent(categoriesFile, language)
        }

        settingsDataStore.saveContentVersion(manifest.contentVersion)
    }

    /** Читает и десериализует manifest_sq.json */
    private fun loadManifest(): ContentManifestSq {
        val raw = context.assets
            .open("content/manifest_sq.json")
            .bufferedReader()
            .readText()
        return json.decodeFromString(raw)
    }

    /** Читает и десериализует файл категорий для указанного языка */
    private fun loadCategoriesFile(fileName: String): CategoriesFileSq {
        val raw = context.assets
            .open(fileName)
            .bufferedReader()
            .readText()
        return json.decodeFromString(raw)
    }

    /**
     * Удаляет старые встроенные категории этого языка и вставляет новые.
     * CASCADE DELETE автоматически удаляет связанные слова при удалении категорий.
     * Всё выполняется в одной транзакции Room (withTransaction из room-runtime).
     */
    private suspend fun replaceBuiltInContent(categoriesFile: CategoriesFileSq, language: String) {
        database.withTransaction {
            // Удаляем старый встроенный контент — CASCADE удалит слова
            database.categoryDao().deleteBuiltInByLanguage(language)

            categoriesFile.categories.forEach { dto ->
                val categoryEntity = CategoryEntity(
                    id = 0L,
                    name = dto.name,
                    colorArgb = dto.colorArgb,
                    language = language,
                    isBuiltIn = true,
                    displayOrder = dto.displayOrder
                )
                val categoryId = database.categoryDao().insert(categoryEntity)

                // Если вставка не удалась (теоретически не должно быть после DELETE) — пропускаем
                if (categoryId == -1L) return@forEach

                val wordEntities = dto.words.map { word ->
                    WordEntity(
                        id = 0L,
                        categoryId = categoryId,
                        word = word,
                        language = language
                    )
                }
                database.wordDao().insertAll(wordEntities)
            }
        }
    }
}