package ru.internet.boardgames.counter.data.local.db

import androidx.room.TypeConverter

/**
 * Room TypeConverter: List<Int> ↔ String (через запятую).
 *
 * Логика парсинга соответствует §8 ТЗ:
 *   • Строка разбивается по запятым.
 *   • Невалидные токены (не числа, пустые) — игнорируются.
 *   • Список хранится отсортированным по возрастанию (§8 п.4).
 *
 * Примеры:
 *   ""              → []
 *   "-10,-5,5,10"   → [-10, -5, 5, 10]
 *   "10,-50,100"    → [-50, 10, 100]
 */
class ActionsTypeConverter {

    /** List<Int> → String для сохранения в Room */
    @TypeConverter
    fun fromActions(actions: List<Int>): String =
        actions.joinToString(separator = ",")

    /** String → List<Int> при чтении из Room */
    @TypeConverter
    fun toActions(raw: String): List<Int> {
        if (raw.isBlank()) return emptyList()
        return raw
            .split(",")
            .mapNotNull { token -> token.trim().toIntOrNull() }
            .sorted()
    }
}
