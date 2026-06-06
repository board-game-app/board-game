package ru.internet.boardgames.counter.domain.usecase

import javax.inject.Inject

/**
 * Парсинг строки поля «Действия» из Экрана редактирования (§7.5, §8 ТЗ).
 *
 * Правила (§8):
 *   1. Разбить строку по пробелам.
 *   2. Каждый токен парсить как Int.
 *   3. Невалидные токены (не числа, пустые строки) — игнорировать.
 *   4. Результат отсортировать по возрастанию.
 *   5. Пустой список → кнопки действий не отображаются.
 *
 * Вынесен в use case, а не в ViewModel — логика парсинга тестируема изолированно.
 */
class ParseActionsUseCase @Inject constructor() {

    operator fun invoke(raw: String): List<Int> =
        raw
            .split(" ")
            .mapNotNull { token -> token.trim().toIntOrNull() }
            .sorted()

    /**
     * Обратное преобразование: List<Int> → строка для поля ввода.
     * Числа разделяются пробелом — формат, ожидаемый пользователем (§7.5).
     */
    fun toDisplayString(actions: List<Int>): String =
        actions.joinToString(separator = " ")
}
