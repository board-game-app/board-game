package ru.internet.boardgames.spygame.domain.usecase

import ru.internet.boardgames.spygame.domain.model.Category
import ru.internet.boardgames.spygame.domain.model.GameCard
import ru.internet.boardgames.spygame.domain.model.GameCard.Companion.SPY_WORD_PLACEHOLDER
import ru.internet.boardgames.spygame.domain.model.GameSession
import javax.inject.Inject

/**
 * Use case генерации игровой сессии.
 *
 * Правила генерации (из спецификации):
 * - 1 шпион на сессию
 * - N-1 игроков получают одно и то же случайное слово из категории
 * - Позиция шпиона в колоде — случайная
 * - Слово шпиона на карточке = [SPY_WORD_PLACEHOLDER]; UI заменяет его на
 *   локализованную строку из ресурсов
 *
 * Не имеет внешних зависимостей — чистая бизнес-логика, легко тестируется.
 */
class GenerateGameSessionUseCase @Inject constructor() {

    /**
     * @param category    Категория, выбранная для этой сессии.
     * @param playerCount Число игроков (≥ 2). Определяет размер колоды.
     * @return [GameSession] с перемешанными карточками и ровно одним шпионом.
     */
    operator fun invoke(category: Category, playerCount: Int): GameSession {
        require(playerCount >= 2) {
            "Число игроков должно быть не меньше 2, получено: $playerCount"
        }
        require(category.words.isNotEmpty()) {
            "Категория '${category.name}' не содержит слов"
        }

        // Случайная позиция шпиона среди всех игроков (0-based)
        val spyIndex = (0 until playerCount).random()

        // Одно слово для всех обычных игроков — выбирается случайно
        val sharedWord = category.words.random()

        val cards = (0 until playerCount).map { i ->
            GameCard(
                index = i + 1,          // Номер на рубашке: 1, 2, 3, ...
                isSpy = (i == spyIndex),
                word = if (i == spyIndex) SPY_WORD_PLACEHOLDER else sharedWord,
                categoryName = category.name
            )
        }

        return GameSession(
            category = category,
            cards = cards,
            totalPlayers = playerCount
        )
    }
}
