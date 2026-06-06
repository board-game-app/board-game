package ru.internet.boardgames.counter.presentation

import ru.internet.boardgames.counter.R
import ru.internet.boardgames.counter.domain.model.CardDisplayMode
import ru.internet.boardgames.counter.domain.model.Counter
import ru.internet.boardgames.counter.domain.model.Session

/** Дефолтный цвет нового счётчика: оранжевый #F5A623 (первый в палитре §7.3) */
internal const val DEFAULT_COUNTER_COLOR_ARGB = 0xFFF5A623L

/**
 * Единое UI-состояние модуля счётчика.
 * Используется Activity-scope ViewModel — одно состояние для всех точек входа.
 */
data class CounterUiState(

    // ── Данные ────────────────────────────────────────────────────────────────
    val activeSessions: List<Session> = emptyList(),
    val activeSession: Session? = null,
    val counters: List<Counter> = emptyList(),
    val isLoading: Boolean = true,

    /** Текущий режим отображения карточек (§5) */
    val cardDisplayMode: CardDisplayMode = CardDisplayMode.COMPACT,

    // ── Диалоги главного экрана ───────────────────────────────────────────────
    val showCreateSessionDialog: Boolean = false,
    val showSessionPickerDialog: Boolean = false,

    // ── Диалог «Новый счётчик» (§6) ───────────────────────────────────────────
    val showNewCounterDialog: Boolean = false,
    /** Автоимя «Счётчик N» — вычисляется в ViewModel по размеру counters */
    val newCounterDefaultName: String = R.string.new_counter_default_name.toString(),
    val newCounterDialogName: String = "",
    val newCounterDialogColorArgb: Long = DEFAULT_COUNTER_COLOR_ARGB,

    /**
     * Список actions, скопированный у последнего по displayOrder счётчика сессии.
     * Автоматически заполняется при открытии диалога «Новый счётчик», если
     * в сессии уже есть хотя бы один счётчик (Изменение 2).
     */
    val newCounterPrefillActions: List<Int> = emptyList(),

    // ── Диалог подтверждения удаления счётчика (§11) ──────────────────────────
    /** Счётчик, ожидающий подтверждения удаления; null = диалог скрыт */
    val counterPendingDelete: Counter? = null,

    // ── Диалог подтверждения сброса (§5.4) ───────────────────────────────────
    /** Счётчик, ожидающий подтверждения сброса; null = диалог скрыт */
    val counterPendingReset: Counter? = null,

    // ── Состояние экрана редактирования (§7) ─────────────────────────────────
    val editState: EditCounterState = EditCounterState(),

    // ── Боковой drawer управления сессиями (Изменение 4) ─────────────────────
    /** true — drawer открыт */
    val isDrawerOpen: Boolean = false,
    /** id сессии в режиме inline-переименования; null = нет активного переименования */
    val renamingSessionId: Long? = null,
    /** Текст в поле переименования */
    val renamingSessionName: String = "",
    /** Сессия, ожидающая подтверждения удаления; null = диалог скрыт */
    val sessionPendingDelete: Session? = null
)

/**
 * Состояние формы экрана редактирования/создания счётчика (§7 ТЗ).
 * Встроено в [CounterUiState], так как ViewModel — Activity-scope.
 */
data class EditCounterState(
    /** true → создание нового счётчика; false → редактирование существующего */
    val isNewCounter: Boolean = true,
    /** Оригинальный счётчик до редактирования; null при создании */
    val originalCounter: Counter? = null,

    // ── Поля формы (§7.1–§7.5) ────────────────────────────────────────────────
    val name: String = "",
    /** Строковое значение — пользователь может вводить отрицательные числа */
    val valueText: String = "0",
    val resetValueText: String = "0",
    val colorArgb: Long = DEFAULT_COUNTER_COLOR_ARGB,
    val incrementStepText: String = "1",
    val decrementStepText: String = "1",
    /** Строка вида «10 -5 20» — парсится через ParseActionsUseCase (§8) */
    val actionsRaw: String = "",

    /** true → показать диалог «Отменить изменения?» при нажатии ✕ (§11) */
    val showDiscardChangesDialog: Boolean = false
) {
    /**
     * Есть ли несохранённые изменения.
     * При создании — «изменения» есть, если пользователь что-то ввёл.
     */
    val hasUnsavedChanges: Boolean
        get() {
            if (isNewCounter) return name.isNotBlank() || actionsRaw.isNotBlank()
            val original = originalCounter ?: return false
            return name.trim() != original.name
                    || valueText.toIntOrNull() != original.value
                    || resetValueText.toIntOrNull() != original.resetValue
                    || colorArgb != original.colorArgb
                    || incrementStepText.toIntOrNull() != original.incrementStep
                    || decrementStepText.toIntOrNull() != original.decrementStep
                    || actionsRaw.trim() != original.actions.joinToString(" ")
        }
}
