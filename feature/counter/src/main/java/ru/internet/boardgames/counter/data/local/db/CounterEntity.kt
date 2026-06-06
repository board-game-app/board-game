package ru.internet.boardgames.counter.data.local.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "counters",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class CounterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** FK → sessions.id */
    val sessionId: Long,

    /** Отображаемое имя счётчика */
    val name: String,

    /** Текущее значение */
    val value: Int = 0,

    /** Значение, к которому возвращается счётчик при сбросе (§7.2) */
    val resetValue: Int = 0,

    /**
     * Цвет счётчика в формате ARGB, сохранённый как Long.
     * Пример: 0xFFF5A623L = оранжевый (первый цвет палитры §7.3).
     * Используется как Color(colorArgb) в Compose.
     */
    val colorArgb: Long = DEFAULT_COLOR_ARGB,

    /** Шаг кнопки «+» (§7.4); всегда ≥ 1 */
    val incrementStep: Int = 1,

    /** Шаг кнопки «−» (§7.4); всегда ≥ 1 */
    val decrementStep: Int = 1,

    /**
     * Значения быстрых действий (§7.5), хранятся через запятую: "-10,-5,5,10".
     * Пустая строка → кнопки быстрых действий не отображаются.
     * Конвертация в List<Int> и обратно — через [ActionsTypeConverter].
     */
    val actions: List<Int> = emptyList(),

    /** Порядок отображения в списке (0, 1, 2…) */
    val displayOrder: Int = 0
) {
    companion object {
        /** Оранжевый #F5A623 — первый цвет палитры (§7.3), цвет по умолчанию */
        const val DEFAULT_COLOR_ARGB: Long = 0xFFF5A623L
    }
}
