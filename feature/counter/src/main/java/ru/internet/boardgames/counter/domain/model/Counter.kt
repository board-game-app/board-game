package ru.internet.boardgames.counter.domain.model

/**
 * Доменная модель счётчика.
 * Версия 2: добавлены цвет, шаги, сброс и быстрые действия (§5, §7 ТЗ).
 * sessionId и displayOrder сохранены — принадлежность к сессии не изменилась.
 */
data class Counter(
    val id: Long,
    val sessionId: Long,
    val name: String,
    val value: Int,

    /** Значение, к которому возвращается счётчик при сбросе (§7.2) */
    val resetValue: Int,

    /**
     * Цвет счётчика — ARGB Long (например 0xFFF5A623L = оранжевый).
     * В Compose: Color(colorArgb).
     */
    val colorArgb: Long,

    /** Шаг кнопки «+» (§7.4); всегда ≥ 1 */
    val incrementStep: Int,

    /** Шаг кнопки «−» (§7.4); всегда ≥ 1 */
    val decrementStep: Int,

    /**
     * Список значений быстрых действий, отсортированных по возрастанию (§8 п.4).
     * Пустой список → кнопки быстрых действий не отображаются.
     */
    val actions: List<Int>,

    val displayOrder: Int
)
