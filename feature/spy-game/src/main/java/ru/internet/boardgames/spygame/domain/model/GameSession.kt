package ru.internet.boardgames.spygame.domain.model

/**
 * Доменная модель игровой сессии.
 *
 * Создаётся [GenerateGameSessionUseCase] и передаётся в [GameViewModel].
 * Неизменяема — при обновлении категории создаётся новая сессия.
 *
 * @param category     Категория этой сессии.
 * @param cards        Перетасованная колода карточек. Размер = [totalPlayers].
 *                     Ровно одна карточка имеет [GameCard.isSpy] == true.
 * @param totalPlayers Общее число игроков (и карточек) в сессии.
 */
data class GameSession(
    val category: Category,
    val cards: List<GameCard>,
    val totalPlayers: Int
) {
    init {
        require(cards.size == totalPlayers) {
            "Число карточек (${cards.size}) должно совпадать с числом игроков ($totalPlayers)"
        }
        require(cards.count { it.isSpy } == 1) {
            "В сессии должен быть ровно один шпион, найдено: ${cards.count { it.isSpy }}"
        }
    }
}
