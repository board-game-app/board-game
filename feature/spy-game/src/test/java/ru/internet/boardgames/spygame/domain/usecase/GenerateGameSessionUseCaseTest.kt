package ru.internet.boardgames.spygame.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.internet.boardgames.spygame.domain.model.Category
import ru.internet.boardgames.spygame.domain.model.GameCard

/**
 * Unit-тесты для [GenerateGameSessionUseCase].
 *
 * Никаких зависимостей от Android, Room или Hilt — чистая Kotlin-логика.
 * Тесты запускаются на JVM без эмулятора.
 *
 * Проверяемые инварианты (из спецификации):
 * - Ровно 1 шпион на сессию
 * - N-1 игроков получают одинаковое слово из категории
 * - Позиция шпиона случайна
 * - Размер колоды = playerCount
 * - Нумерация карточек начинается с 1
 */
class GenerateGameSessionUseCaseTest {

    private lateinit var useCase: GenerateGameSessionUseCase

    /** Стандартная категория с достаточным запасом слов для всех тестов. */
    private val testCategory = Category(
        id    = "airport",
        name  = "Аэропорт",
        words = listOf(
            "Пилот", "Стюардесса", "Пассажир", "Таможенник",
            "Охранник", "Механик", "Диспетчер", "Носильщик",
            "Турист", "Бизнесмен", "Пограничник", "Регистратор"
        )
    )

    @Before
    fun setUp() {
        useCase = GenerateGameSessionUseCase()
    }

    // ─── Инвариант: ровно один шпион ─────────────────────────────────────────

    @Test
    fun `session contains exactly one spy`() {
        val session = useCase(testCategory, playerCount = 6)
        val spyCount = session.cards.count { it.isSpy }
        assertEquals("В сессии должен быть ровно 1 шпион", 1, spyCount)
    }

    @Test
    fun `exactly one spy for minimum player count`() {
        val session = useCase(testCategory, playerCount = 2)
        assertEquals(1, session.cards.count { it.isSpy })
    }

    @Test
    fun `exactly one spy for maximum player count`() {
        val session = useCase(testCategory, playerCount = 10)
        assertEquals(1, session.cards.count { it.isSpy })
    }

    // ─── Инвариант: N-1 игроков получают одно слово ───────────────────────────

    @Test
    fun `all non-spy cards share the same word`() {
        val session = useCase(testCategory, playerCount = 6)
        val nonSpyWords = session.cards
            .filter { !it.isSpy }
            .map { it.word }
            .toSet()

        assertEquals(
            "Все обычные игроки должны получить одинаковое слово",
            1,
            nonSpyWords.size
        )
    }

    @Test
    fun `non-spy word belongs to category words`() {
        repeat(30) {
            val session = useCase(testCategory, playerCount = 5)
            val sharedWord = session.cards.first { !it.isSpy }.word
            assertTrue(
                "Слово '$sharedWord' должно быть из категории",
                sharedWord in testCategory.words
            )
        }
    }

    // ─── Инвариант: слово шпиона — плейсхолдер ───────────────────────────────

    @Test
    fun `spy card word is SPY_WORD_PLACEHOLDER`() {
        val session = useCase(testCategory, playerCount = 6)
        val spyCard = session.cards.first { it.isSpy }
        assertEquals(
            "Слово шпиона должно быть ${GameCard.SPY_WORD_PLACEHOLDER}",
            GameCard.SPY_WORD_PLACEHOLDER,
            spyCard.word
        )
    }

    @Test
    fun `spy word is different from non-spy word`() {
        val session = useCase(testCategory, playerCount = 4)
        val spyWord   = session.cards.first { it.isSpy }.word
        val playerWord = session.cards.first { !it.isSpy }.word
        assertNotEquals("Слово шпиона не должно совпадать со словом игрока", spyWord, playerWord)
    }

    // ─── Инвариант: размер колоды ─────────────────────────────────────────────

    @Test
    fun `card count equals player count`() {
        for (count in 2..10) {
            val session = useCase(testCategory, playerCount = count)
            assertEquals(
                "Число карточек должно быть равно числу игроков ($count)",
                count,
                session.cards.size
            )
        }
    }

    @Test
    fun `totalPlayers field matches player count`() {
        val session = useCase(testCategory, playerCount = 7)
        assertEquals(7, session.totalPlayers)
        assertEquals(7, session.cards.size)
    }

    // ─── Инвариант: нумерация 1-based ────────────────────────────────────────

    @Test
    fun `card indices are 1-based and sequential`() {
        val session = useCase(testCategory, playerCount = 5)
        val indices = session.cards.map { it.index }
        assertEquals(listOf(1, 2, 3, 4, 5), indices)
    }

    @Test
    fun `first card index is 1`() {
        val session = useCase(testCategory, playerCount = 3)
        assertEquals(1, session.cards.first().index)
    }

    // ─── Инвариант: категория сохраняется в каждой карточке ──────────────────

    @Test
    fun `category name propagated to all cards`() {
        val session = useCase(testCategory, playerCount = 4)
        assertTrue(
            "categoryName должно быть задано у всех карточек",
            session.cards.all { it.categoryName == testCategory.name }
        )
    }

    @Test
    fun `session category matches input category`() {
        val session = useCase(testCategory, playerCount = 4)
        assertEquals(testCategory, session.category)
    }

    // ─── Инвариант: случайность позиции шпиона ───────────────────────────────

    @Test
    fun `spy position varies across multiple sessions`() {
        // Запускаем 100 сессий и проверяем, что шпион появлялся минимум в 2 разных позициях
        // (вероятность того, что за 100 попыток шпион всегда оказывается на одной позиции
        //  при 6 игроках ≈ (1/6)^99 ≈ 10^-77 — практически невозможно)
        val spyPositions = (1..100)
            .map { useCase(testCategory, playerCount = 6).cards.indexOfFirst { it.isSpy } }
            .toSet()

        assertTrue(
            "Позиция шпиона должна быть случайной, найдено уникальных позиций: ${spyPositions.size}",
            spyPositions.size > 1
        )
    }

    @Test
    fun `spy can be at first position`() {
        // Проверяем, что шпион хотя бы иногда оказывается первым
        val positions = (1..200).map { useCase(testCategory, 3).cards.indexOfFirst { it.isSpy } }
        assertTrue("Шпион должен иногда быть первым (index 0)", 0 in positions)
    }

    @Test
    fun `spy can be at last position`() {
        val positions = (1..200).map { useCase(testCategory, 3).cards.indexOfFirst { it.isSpy } }
        assertTrue("Шпион должен иногда быть последним (index 2)", 2 in positions)
    }

    // ─── Граничные условия ────────────────────────────────────────────────────

    @Test
    fun `works with minimum 2 players`() {
        val session = useCase(testCategory, playerCount = 2)
        assertEquals(2, session.cards.size)
        assertEquals(1, session.cards.count { it.isSpy })
        // Один шпион, один обычный — слово обычного из категории
        val playerWord = session.cards.first { !it.isSpy }.word
        assertTrue(playerWord in testCategory.words)
    }

    @Test
    fun `works with single-word category`() {
        val singleWordCategory = Category("test", "Тест", listOf("слово"))
        val session = useCase(singleWordCategory, playerCount = 3)
        // Все обычные игроки получают это единственное слово
        val nonSpyWords = session.cards.filter { !it.isSpy }.map { it.word }.toSet()
        assertEquals(setOf("слово"), nonSpyWords)
    }

    // ─── Валидация входных данных ─────────────────────────────────────────────

    @Test(expected = IllegalArgumentException::class)
    fun `throws IllegalArgumentException when playerCount is less than 2`() {
        useCase(testCategory, playerCount = 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws IllegalArgumentException when playerCount is 0`() {
        useCase(testCategory, playerCount = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws IllegalArgumentException when category has no words`() {
        val emptyCategory = Category("test", "Тест", emptyList())
        useCase(emptyCategory, playerCount = 3)
    }

    // ─── Валидация GameSession.init ───────────────────────────────────────────

    @Test
    fun `generated session passes GameSession init invariants`() {
        // GameSession.init бросает IllegalStateException если инварианты нарушены.
        // Этот тест проверяет, что GenerateGameSessionUseCase всегда выдаёт валидную сессию.
        repeat(50) { iteration ->
            val count = (2..10).random()
            val session = useCase(testCategory, playerCount = count)
            assertEquals(
                "Итерация $iteration: размер колоды не совпадает с totalPlayers",
                session.totalPlayers,
                session.cards.size
            )
            assertEquals(
                "Итерация $iteration: должен быть ровно 1 шпион",
                1,
                session.cards.count { it.isSpy }
            )
        }
    }
}
