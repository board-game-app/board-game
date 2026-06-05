package ru.internet.boardgames.spygame.data.local.assets

import android.content.Context
import android.content.res.AssetManager
import androidx.room.withTransaction
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.internet.boardgames.spygame.data.local.datastore.SettingsDataStore
import ru.internet.boardgames.spygame.data.local.db.CategoryDao
import ru.internet.boardgames.spygame.data.local.db.CategoryEntity
import ru.internet.boardgames.spygame.data.local.db.SpyGameDatabase
import ru.internet.boardgames.spygame.data.local.db.WordDao
import ru.internet.boardgames.spygame.data.local.db.WordEntity


/**
 * Unit-тесты для [ContentLoader].
 *
 * Стратегия: мокаем [Context.assets] через [AssetManager], возвращая JSON-строки
 * как [ByteArrayInputStream]. [AppDatabase.withTransaction] мокается через
 * [mockkStatic], чтобы блок транзакции выполнялся синхронно.
 *
 * Проверяем:
 * 1. Версия актуальна → никаких запросов к БД
 * 2. Версия устарела → очистка + вставка категорий и слов
 * 3. Корректное число вставленных категорий и слов
 * 4. Правильный порядок операций (delete before insert)
 * 5. Сохранение новой версии в DataStore
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ContentLoaderTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    // ─── Моки ─────────────────────────────────────────────────────────────────
    private val mockContext       : Context          = mockk()
    private val mockAssets        : AssetManager     = mockk()
    private val mockDatabase      : SpyGameDatabase = mockk()
    private val mockCategoryDao   : CategoryDao = mockk()
    private val mockWordDao       : WordDao = mockk()
    private val mockSettingsStore : SettingsDataStore = mockk()

    // ─── Тестовые JSON-данные ─────────────────────────────────────────────────

    /** Манифест: contentVersion = 3, только русский язык. */
    private val manifestJson = """
        {
            "contentVersion": 3,
            "lastUpdated": "2025-01-01",
            "supportedLanguages": ["ru"]
        }
    """.trimIndent()

    /** Две категории по 10 слов каждая. */
    private val categoriesRuJson = """
        {
            "language": "ru",
            "version": 3,
            "categories": [
                {
                    "id": "airport",
                    "name": "Аэропорт",
                    "words": ["Пилот","Стюардесса","Пассажир","Таможенник","Охранник",
                              "Механик","Диспетчер","Носильщик","Турист","Бизнесмен"]
                },
                {
                    "id": "hospital",
                    "name": "Больница",
                    "words": ["Врач","Медсестра","Пациент","Хирург","Санитар",
                              "Анестезиолог","Педиатр","Главврач","Фельдшер","Регистратор"]
                }
            ]
        }
    """.trimIndent()

    /** Манифест с двумя языками для теста мультиязычной вставки. */
    private val manifestTwoLangsJson = """
        {
            "contentVersion": 5,
            "lastUpdated": "2025-06-01",
            "supportedLanguages": ["ru", "en"]
        }
    """.trimIndent()

    private val categoriesEnJson = """
        {
            "language": "en",
            "version": 5,
            "categories": [
                {
                    "id": "airport",
                    "name": "Airport",
                    "words": ["Pilot","Flight attendant","Passenger","Customs officer","Security guard",
                              "Mechanic","Air traffic controller","Porter","Tourist","Businessman"]
                }
            ]
        }
    """.trimIndent()

    // ─── Setup ────────────────────────────────────────────────────────────────

    @Before
    fun setUp() {
        every { mockContext.assets } returns mockAssets
        every { mockDatabase.categoryDao() } returns mockCategoryDao
        every { mockDatabase.wordDao() }     returns mockWordDao

        // withTransaction — выполняем блок немедленно (без реального Room)
        mockkStatic("androidx.room.RoomDatabaseKt")

        val transactionLambda = slot<suspend () -> Any>()
        coEvery { mockDatabase.withTransaction(capture(transactionLambda)) } coAnswers {
            transactionLambda.captured.invoke()
        }

        // DAO-методы по умолчанию ничего не делают
        coEvery { mockCategoryDao.insertCategories(any()) } just Runs
        coEvery { mockWordDao.insertWords(any()) }          just Runs
        coEvery { mockCategoryDao.deleteAll() }             just Runs
        coEvery { mockWordDao.deleteAll() }                 just Runs
    }

    private fun createLoader() = ContentLoader(mockContext, mockDatabase, mockSettingsStore)

    // ─── Тест 1: версия актуальна — ничего не делаем ─────────────────────────

    @Test
    fun `seedIfNeeded does nothing when stored version matches manifest`() =
        runTest(testDispatcher) {
            // Сохранённая версия == версии манифеста
            coEvery { mockSettingsStore.getContentVersion() } returns 3
            every { mockAssets.open("content/manifest.json") } returns
                    manifestJson.byteInputStream()

            createLoader().seedIfNeeded()

            coVerify(exactly = 0) { mockCategoryDao.insertCategories(any()) }
            coVerify(exactly = 0) { mockWordDao.insertWords(any()) }
            coVerify(exactly = 0) { mockCategoryDao.deleteAll() }
            coVerify(exactly = 0) { mockWordDao.deleteAll() }
        }

    @Test
    fun `seedIfNeeded does nothing when stored version is newer than manifest`() =
        runTest(testDispatcher) {
            coEvery { mockSettingsStore.getContentVersion() } returns 99  // Новее манифеста
            every { mockAssets.open("content/manifest.json") } returns
                    manifestJson.byteInputStream()

            createLoader().seedIfNeeded()

            coVerify(exactly = 0) { mockCategoryDao.insertCategories(any()) }
        }

    // ─── Тест 2: версия устарела — обновляем БД ───────────────────────────────

    @Test
    fun `seedIfNeeded updates DB when stored version is outdated`() =
        runTest(testDispatcher) {
            coEvery { mockSettingsStore.getContentVersion() } returns 1   // Устарела
            coEvery { mockSettingsStore.setContentVersion(any()) } just Runs
            every { mockAssets.open("content/manifest.json") }           returns manifestJson.byteInputStream()
            every { mockAssets.open("content/categories_ru.json") }      returns categoriesRuJson.byteInputStream()

            createLoader().seedIfNeeded()

            coVerify { mockCategoryDao.insertCategories(any()) }
            coVerify { mockWordDao.insertWords(any()) }
        }

    @Test
    fun `seedIfNeeded updates DB when stored version is 0 (first launch)`() =
        runTest(testDispatcher) {
            coEvery { mockSettingsStore.getContentVersion() } returns 0
            coEvery { mockSettingsStore.setContentVersion(any()) } just Runs
            every { mockAssets.open("content/manifest.json") }      returns manifestJson.byteInputStream()
            every { mockAssets.open("content/categories_ru.json") } returns categoriesRuJson.byteInputStream()

            createLoader().seedIfNeeded()

            coVerify { mockCategoryDao.insertCategories(any()) }
            coVerify { mockWordDao.insertWords(any()) }
        }

    // ─── Тест 3: корректное число вставленных категорий ───────────────────────

    @Test
    fun `seedIfNeeded inserts correct number of categories`() =
        runTest(testDispatcher) {
            coEvery { mockSettingsStore.getContentVersion() } returns 0
            coEvery { mockSettingsStore.setContentVersion(any()) } just Runs
            every { mockAssets.open("content/manifest.json") }      returns manifestJson.byteInputStream()
            every { mockAssets.open("content/categories_ru.json") } returns categoriesRuJson.byteInputStream()

            val capturedCategories = slot<List<CategoryEntity>>()
            coEvery { mockCategoryDao.insertCategories(capture(capturedCategories)) } just Runs

            createLoader().seedIfNeeded()

            val inserted = capturedCategories.captured
            assertEquals("Должно быть вставлено 2 категории", 2, inserted.size)
            assertTrue(inserted.any { it.id == "airport" && it.language == "ru" })
            assertTrue(inserted.any { it.id == "hospital" && it.language == "ru" })
        }

    // ─── Тест 4: корректное число вставленных слов ────────────────────────────

    @Test
    fun `seedIfNeeded inserts correct number of words`() =
        runTest(testDispatcher) {
            coEvery { mockSettingsStore.getContentVersion() } returns 0
            coEvery { mockSettingsStore.setContentVersion(any()) } just Runs
            every { mockAssets.open("content/manifest.json") }      returns manifestJson.byteInputStream()
            every { mockAssets.open("content/categories_ru.json") } returns categoriesRuJson.byteInputStream()

            val capturedWords = slot<List<WordEntity>>()
            coEvery { mockWordDao.insertWords(capture(capturedWords)) } just Runs

            createLoader().seedIfNeeded()

            val inserted = capturedWords.captured
            // 2 категории × 10 слов = 20 слов
            assertEquals("Должно быть вставлено 20 слов", 20, inserted.size)
        }

    @Test
    fun `all inserted words have correct language tag`() =
        runTest(testDispatcher) {
            coEvery { mockSettingsStore.getContentVersion() } returns 0
            coEvery { mockSettingsStore.setContentVersion(any()) } just Runs
            every { mockAssets.open("content/manifest.json") }      returns manifestJson.byteInputStream()
            every { mockAssets.open("content/categories_ru.json") } returns categoriesRuJson.byteInputStream()

            val capturedWords = slot<List<WordEntity>>()
            coEvery { mockWordDao.insertWords(capture(capturedWords)) } just Runs

            createLoader().seedIfNeeded()

            assertTrue(
                "Все слова должны иметь language = 'ru'",
                capturedWords.captured.all { it.language == "ru" }
            )
        }

    @Test
    fun `words are linked to correct category ids`() =
        runTest(testDispatcher) {
            coEvery { mockSettingsStore.getContentVersion() } returns 0
            coEvery { mockSettingsStore.setContentVersion(any()) } just Runs
            every { mockAssets.open("content/manifest.json") }      returns manifestJson.byteInputStream()
            every { mockAssets.open("content/categories_ru.json") } returns categoriesRuJson.byteInputStream()

            val capturedWords = slot<List<WordEntity>>()
            coEvery { mockWordDao.insertWords(capture(capturedWords)) } just Runs

            createLoader().seedIfNeeded()

            val words = capturedWords.captured
            val airportWords  = words.filter { it.categoryId == "airport" }
            val hospitalWords = words.filter { it.categoryId == "hospital" }

            assertEquals("Аэропорт должен иметь 10 слов", 10, airportWords.size)
            assertEquals("Больница должна иметь 10 слов", 10, hospitalWords.size)
        }

    // ─── Тест 5: правильный порядок операций ──────────────────────────────────

    @Test
    fun `seedIfNeeded clears old data before inserting new`() =
        runTest(testDispatcher) {
            coEvery { mockSettingsStore.getContentVersion() } returns 0
            coEvery { mockSettingsStore.setContentVersion(any()) } just Runs
            every { mockAssets.open("content/manifest.json") }      returns manifestJson.byteInputStream()
            every { mockAssets.open("content/categories_ru.json") } returns categoriesRuJson.byteInputStream()

            createLoader().seedIfNeeded()

            // Порядок: сначала удаляем слова (FK → каскад), потом категории, потом вставляем
            coVerifyOrder {
                mockWordDao.deleteAll()
                mockCategoryDao.deleteAll()
                mockCategoryDao.insertCategories(any())
                mockWordDao.insertWords(any())
            }
        }

    // ─── Тест 6: сохранение новой версии ──────────────────────────────────────

    @Test
    fun `seedIfNeeded saves new content version after successful update`() =
        runTest(testDispatcher) {
            coEvery { mockSettingsStore.getContentVersion() } returns 1
            coEvery { mockSettingsStore.setContentVersion(any()) } just Runs
            every { mockAssets.open("content/manifest.json") }      returns manifestJson.byteInputStream()
            every { mockAssets.open("content/categories_ru.json") } returns categoriesRuJson.byteInputStream()

            createLoader().seedIfNeeded()

            // Должна быть сохранена версия из манифеста (3)
            coVerify { mockSettingsStore.setContentVersion(3) }
        }

    @Test
    fun `seedIfNeeded does not save version when no update needed`() =
        runTest(testDispatcher) {
            coEvery { mockSettingsStore.getContentVersion() } returns 3  // Актуальная
            every { mockAssets.open("content/manifest.json") } returns manifestJson.byteInputStream()

            createLoader().seedIfNeeded()

            coVerify(exactly = 0) { mockSettingsStore.setContentVersion(any()) }
        }

    // ─── Тест 7: мультиязычная вставка ───────────────────────────────────────

    @Test
    fun `seedIfNeeded processes all languages from manifest`() =
        runTest(testDispatcher) {
            coEvery { mockSettingsStore.getContentVersion() } returns 0
            coEvery { mockSettingsStore.setContentVersion(any()) } just Runs
            every { mockAssets.open("content/manifest.json") }      returns manifestTwoLangsJson.byteInputStream()
            every { mockAssets.open("content/categories_ru.json") } returns categoriesRuJson.byteInputStream()
            every { mockAssets.open("content/categories_en.json") } returns categoriesEnJson.byteInputStream()

            createLoader().seedIfNeeded()

            // Вставка вызвана дважды — для ru и en
            coVerify(exactly = 2) { mockCategoryDao.insertCategories(any()) }
            coVerify(exactly = 2) { mockWordDao.insertWords(any()) }
        }
}
