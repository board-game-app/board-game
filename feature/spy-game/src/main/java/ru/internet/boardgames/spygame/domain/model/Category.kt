package ru.internet.boardgames.spygame.domain.model

/**
 * Доменная модель категории игры.
 *
 * @param id      Уникальный строковый идентификатор, напр. "airport".
 *                Не зависит от языка — используется как ключ при exclude.
 * @param name    Локализованное название, напр. "Аэропорт" или "Airport".
 * @param words   Список слов категории на нужном языке.
 *                Гарантированно непустой — ContentLoader проверяет это при seeding-е.
 */
data class Category(
    val id: String,
    val name: String,
    val words: List<String>
)
