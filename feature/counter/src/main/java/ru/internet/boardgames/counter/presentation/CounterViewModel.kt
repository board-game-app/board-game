package ru.internet.boardgames.counter.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.internet.boardgames.counter.domain.model.CardDisplayMode
import ru.internet.boardgames.counter.domain.model.Counter
import ru.internet.boardgames.counter.domain.model.Session
import ru.internet.boardgames.counter.domain.usecase.ApplyDeltaUseCase
import ru.internet.boardgames.counter.domain.usecase.CreateCounterUseCase
import ru.internet.boardgames.counter.domain.usecase.CreateSessionUseCase
import ru.internet.boardgames.counter.domain.usecase.DeleteCounterUseCase
import ru.internet.boardgames.counter.domain.usecase.DeleteSessionUseCase
import ru.internet.boardgames.counter.domain.usecase.EnsureDefaultSessionUseCase
import ru.internet.boardgames.counter.domain.usecase.GetActiveSessionUseCase
import ru.internet.boardgames.counter.domain.usecase.GetCardDisplayModeUseCase
import ru.internet.boardgames.counter.domain.usecase.GetCountersUseCase
import ru.internet.boardgames.counter.domain.usecase.GetSessionsUseCase
import ru.internet.boardgames.counter.domain.usecase.ParseActionsUseCase
import ru.internet.boardgames.counter.domain.usecase.RenameSessionUseCase
import ru.internet.boardgames.counter.domain.usecase.ResetCounterUseCase
import ru.internet.boardgames.counter.domain.usecase.SetActiveSessionUseCase
import ru.internet.boardgames.counter.domain.usecase.SetCardDisplayModeUseCase
import ru.internet.boardgames.counter.domain.usecase.UpdateCounterUseCase
import javax.inject.Inject

/**
 * ViewModel модуля счётчика.
 *
 * ВАЖНО: получать только через Activity scope:
 *   val activity = LocalContext.current as ComponentActivity
 *   val vm: CounterViewModel = hiltViewModel(activity)
 *
 * Один экземпляр разделяется между CounterScreen и CounterSheetContent.
 */
@HiltViewModel
class CounterViewModel @Inject constructor(
    private val getSessionsUseCase: GetSessionsUseCase,
    private val createSessionUseCase: CreateSessionUseCase,
    private val getActiveSessionUseCase: GetActiveSessionUseCase,
    private val setActiveSessionUseCase: SetActiveSessionUseCase,
    private val getCountersUseCase: GetCountersUseCase,
    private val createCounterUseCase: CreateCounterUseCase,
    private val applyDeltaUseCase: ApplyDeltaUseCase,
    private val resetCounterUseCase: ResetCounterUseCase,
    private val deleteCounterUseCase: DeleteCounterUseCase,
    private val updateCounterUseCase: UpdateCounterUseCase,
    private val getCardDisplayModeUseCase: GetCardDisplayModeUseCase,
    private val setCardDisplayModeUseCase: SetCardDisplayModeUseCase,
    private val parseActionsUseCase: ParseActionsUseCase,
    // ── Изменение 3: автосоздание сессии при первом запуске ──────────────────
    private val ensureDefaultSessionUseCase: EnsureDefaultSessionUseCase,
    // ── Изменение 4: управление сессиями через drawer ─────────────────────────
    private val deleteSessionUseCase: DeleteSessionUseCase,
    private val renameSessionUseCase: RenameSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CounterUiState())
    val uiState: StateFlow<CounterUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "CounterViewModel"
    }

    init {
        // Изменение 3: перед запуском реактивных наблюдателей убедиться,
        // что существует хотя бы одна сессия (создаёт «Основная», если пусто)
        viewModelScope.launch {
            ensureDefaultSessionUseCase()
        }
        observeSessions()
        observeActiveSessionWithCounters()
        observeCardDisplayMode()
    }

    // ── Наблюдение потоков ────────────────────────────────────────────────────

    private fun observeSessions() {
        viewModelScope.launch {
            getSessionsUseCase()
                .catch { e -> Log.e(TAG, "Ошибка наблюдения сессий", e) }
                .collect { sessions ->
                    _uiState.update { it.copy(activeSessions = sessions) }
                }
        }
    }

    /**
     * Реактивная цепочка: активная сессия → счётчики этой сессии.
     * flatMapLatest отменяет старую Room-подписку при каждой смене сессии.
     */
    private fun observeActiveSessionWithCounters() {
        viewModelScope.launch {
            getActiveSessionUseCase()
                .catch { e -> Log.e(TAG, "Ошибка наблюдения активной сессии", e) }
                .flatMapLatest { session ->
                    _uiState.update { state ->
                        state.copy(
                            activeSession = session,
                            isLoading = false,
                            // Автоимя «Счётчик N» для диалога
                            newCounterDefaultName = "Счётчик ${(state.counters.size + 1)}"
                        )
                    }
                    if (session != null) {
                        getCountersUseCase(session.id)
                            .catch { e ->
                                Log.e(TAG, "Ошибка счётчиков сессии ${session.id}", e)
                                emit(emptyList())
                            }
                    } else {
                        flowOf(emptyList())
                    }
                }
                .collect { counters ->
                    _uiState.update { state ->
                        state.copy(
                            counters = counters,
                            // Обновляем автоимя после получения счётчиков
                            newCounterDefaultName = "Счётчик ${(counters.size + 1)}"
                        )
                    }
                }
        }
    }

    private fun observeCardDisplayMode() {
        viewModelScope.launch {
            getCardDisplayModeUseCase()
                .catch { e -> Log.e(TAG, "Ошибка режима карточек", e) }
                .collect { mode ->
                    _uiState.update { it.copy(cardDisplayMode = mode) }
                }
        }
    }

    // ── Сессии ────────────────────────────────────────────────────────────────

    fun createSession(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                createSessionUseCase(name.trim())
                _uiState.update {
                    it.copy(
                        showCreateSessionDialog = false,
                        isDrawerOpen = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка создания сессии", e)
            }
        }
    }

    fun switchSession(sessionId: Long) {
        viewModelScope.launch {
            setActiveSessionUseCase(sessionId)
            _uiState.update { it.copy(showSessionPickerDialog = false) }
        }
    }

    // ── Режим карточек ────────────────────────────────────────────────────────

    fun toggleCardDisplayMode() {
        val newMode = if (_uiState.value.cardDisplayMode == CardDisplayMode.COMPACT)
            CardDisplayMode.WIDE else CardDisplayMode.COMPACT
        viewModelScope.launch { setCardDisplayModeUseCase(newMode) }
    }

    // ── Диалог «Новый счётчик» (§6) ───────────────────────────────────────────

    /**
     * Открывает диалог «Новый счётчик».
     * Изменение 2: если в сессии есть счётчики, actions копируются у последнего
     * по displayOrder счётчика.
     */
    fun showNewCounterDialog() {
        val prefillActions = _uiState.value.counters
            .maxByOrNull { it.displayOrder }
            ?.actions
            ?: emptyList()

        _uiState.update { state ->
            state.copy(
                showNewCounterDialog = true,
                newCounterDialogName = state.newCounterDefaultName,
                newCounterDialogColorArgb = DEFAULT_COUNTER_COLOR_ARGB,
                newCounterPrefillActions = prefillActions
            )
        }
    }

    fun hideNewCounterDialog() {
        _uiState.update { it.copy(showNewCounterDialog = false) }
    }

    fun onNewCounterDialogNameChange(name: String) {
        _uiState.update { it.copy(newCounterDialogName = name) }
    }

    fun onNewCounterDialogColorChange(colorArgb: Long) {
        _uiState.update { it.copy(newCounterDialogColorArgb = colorArgb) }
    }

    /**
     * Быстрое создание счётчика из диалога (§6, кнопка «ОК»).
     * Изменение 2: передаёт скопированные actions из [CounterUiState.newCounterPrefillActions].
     */
    fun createCounterFromDialog() {
        val state = _uiState.value
        val sessionId = state.activeSession?.id ?: return
        val name = state.newCounterDialogName.ifBlank { state.newCounterDefaultName }
        viewModelScope.launch {
            try {
                createCounterUseCase(
                    sessionId = sessionId,
                    name = name,
                    colorArgb = state.newCounterDialogColorArgb,
                    actions = state.newCounterPrefillActions
                )
                _uiState.update { it.copy(showNewCounterDialog = false) }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка быстрого создания счётчика", e)
            }
        }
    }

    /**
     * Подготовить состояние EditScreen для создания нового счётчика.
     * Вызывается при нажатии «Развернуть» в диалоге (§6).
     * Изменение 2: actionsRaw заполняется из [CounterUiState.newCounterPrefillActions],
     * чтобы пользователь видел скопированные действия и мог их изменить.
     * После вызова — навигация на COUNTER_EDIT_ROUTE.
     */
    fun startNewCounterFromDialog() {
        val state = _uiState.value
        val defaultName = state.newCounterDialogName.ifBlank { state.newCounterDefaultName }
        val prefillActionsRaw = parseActionsUseCase.toDisplayString(state.newCounterPrefillActions)
        _uiState.update {
            it.copy(
                showNewCounterDialog = false,
                editState = EditCounterState(
                    isNewCounter = true,
                    originalCounter = null,
                    name = defaultName,
                    colorArgb = state.newCounterDialogColorArgb,
                    actionsRaw = prefillActionsRaw
                )
            )
        }
    }

    /**
     * Подготовить состояние EditScreen для редактирования существующего счётчика.
     * Изменение 1: вызывается при тапе на карточку (было — при долгом нажатии).
     * После вызова — навигация на COUNTER_EDIT_ROUTE.
     */
    fun startEditExistingCounter(counter: Counter) {
        _uiState.update {
            it.copy(
                editState = EditCounterState(
                    isNewCounter = false,
                    originalCounter = counter,
                    name = counter.name,
                    valueText = counter.value.toString(),
                    resetValueText = counter.resetValue.toString(),
                    colorArgb = counter.colorArgb,
                    incrementStepText = counter.incrementStep.toString(),
                    decrementStepText = counter.decrementStep.toString(),
                    actionsRaw = parseActionsUseCase.toDisplayString(counter.actions)
                )
            )
        }
    }

    // ── Поля EditScreen ───────────────────────────────────────────────────────

    fun onEditNameChange(name: String) =
        _uiState.update { it.copy(editState = it.editState.copy(name = name)) }

    fun onEditValueChange(text: String) =
        _uiState.update { it.copy(editState = it.editState.copy(valueText = text)) }

    fun onEditResetValueChange(text: String) =
        _uiState.update { it.copy(editState = it.editState.copy(resetValueText = text)) }

    fun onEditColorChange(colorArgb: Long) =
        _uiState.update { it.copy(editState = it.editState.copy(colorArgb = colorArgb)) }

    fun onEditIncrementStepChange(text: String) =
        _uiState.update { it.copy(editState = it.editState.copy(incrementStepText = text)) }

    fun onEditDecrementStepChange(text: String) =
        _uiState.update { it.copy(editState = it.editState.copy(decrementStepText = text)) }

    fun onEditActionsRawChange(raw: String) =
        _uiState.update { it.copy(editState = it.editState.copy(actionsRaw = raw)) }

    fun clearEditActions() =
        _uiState.update { it.copy(editState = it.editState.copy(actionsRaw = "")) }

    fun requestDiscardChanges() {
        _uiState.update {
            it.copy(editState = it.editState.copy(showDiscardChangesDialog = true))
        }
    }

    fun dismissDiscardChangesDialog() {
        _uiState.update {
            it.copy(editState = it.editState.copy(showDiscardChangesDialog = false))
        }
    }

    /**
     * Сохранить счётчик из EditScreen (§7, кнопка «✓»).
     * Создаёт или обновляет в зависимости от [EditCounterState.isNewCounter].
     * @return true — успешно сохранено (ViewModel готов, можно навигировать назад)
     */
    fun saveEditCounter(): Boolean {
        val editState = _uiState.value.editState
        if (editState.name.isBlank()) return false

        val value = editState.valueText.toIntOrNull() ?: 0
        val resetValue = editState.resetValueText.toIntOrNull() ?: 0
        val incrementStep = editState.incrementStepText.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val decrementStep = editState.decrementStepText.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val actions = parseActionsUseCase(editState.actionsRaw)

        viewModelScope.launch {
            try {
                if (editState.isNewCounter) {
                    val sessionId = _uiState.value.activeSession?.id ?: return@launch
                    createCounterUseCase(
                        sessionId = sessionId,
                        name = editState.name,
                        colorArgb = editState.colorArgb,
                        incrementStep = incrementStep,
                        decrementStep = decrementStep,
                        resetValue = resetValue,
                        actions = actions,
                        initialValue = value
                    )
                } else {
                    val original = editState.originalCounter ?: return@launch
                    updateCounterUseCase(
                        original.copy(
                            name = editState.name,
                            value = value,
                            resetValue = resetValue,
                            colorArgb = editState.colorArgb,
                            incrementStep = incrementStep,
                            decrementStep = decrementStep,
                            actions = actions
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка сохранения счётчика", e)
            }
        }
        return true
    }

    // ── Счётчики (главный экран) ──────────────────────────────────────────────

    /** delta = +incrementStep, -decrementStep или значение быстрого действия */
    fun applyDelta(counterId: Long, delta: Int) {
        viewModelScope.launch { applyDeltaUseCase(counterId, delta) }
    }

    /**
     * Запросить подтверждение сброса (§5.4).
     * Изменение 1: теперь вызывается при долгом нажатии (было — при тапе).
     */
    fun requestResetCounter(counter: Counter) {
        _uiState.update { it.copy(counterPendingReset = counter) }
    }

    fun dismissResetDialog() {
        _uiState.update { it.copy(counterPendingReset = null) }
    }

    /** Подтверждённый сброс */
    fun confirmResetCounter() {
        val counter = _uiState.value.counterPendingReset ?: return
        viewModelScope.launch {
            resetCounterUseCase(counter.id, counter.resetValue)
            _uiState.update { it.copy(counterPendingReset = null) }
        }
    }

    /** Запросить подтверждение удаления счётчика (§11) */
    fun requestDeleteCounter(counter: Counter) {
        _uiState.update { it.copy(counterPendingDelete = counter) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(counterPendingDelete = null) }
    }

    /** Подтверждённое удаление счётчика */
    fun confirmDeleteCounter() {
        val counter = _uiState.value.counterPendingDelete ?: return
        viewModelScope.launch {
            deleteCounterUseCase(counter.id)
            _uiState.update { it.copy(counterPendingDelete = null) }
        }
    }

    // ── Диалоги главного экрана ───────────────────────────────────────────────

    fun showCreateSessionDialog() =
        _uiState.update { it.copy(showCreateSessionDialog = true) }

    fun hideCreateSessionDialog() =
        _uiState.update { it.copy(showCreateSessionDialog = false) }

    fun showSessionPickerDialog() =
        _uiState.update { it.copy(showSessionPickerDialog = true) }

    fun hideSessionPickerDialog() =
        _uiState.update { it.copy(showSessionPickerDialog = false) }

    // ── Drawer управления сессиями (Изменение 4) ──────────────────────────────

    fun openDrawer() {
        _uiState.update { it.copy(isDrawerOpen = true) }
    }

    fun closeDrawer() {
        _uiState.update {
            it.copy(
                isDrawerOpen = false,
                // Сбрасываем режим переименования при закрытии
                renamingSessionId = null,
                renamingSessionName = ""
            )
        }
    }

    /** Начать inline-переименование сессии в drawer */
    fun startRenameSession(session: Session) {
        _uiState.update {
            it.copy(
                renamingSessionId = session.id,
                renamingSessionName = session.name
            )
        }
    }

    fun onRenameSessionNameChange(name: String) {
        _uiState.update { it.copy(renamingSessionName = name) }
    }

    /** Сохранить новое имя сессии */
    fun confirmRenameSession() {
        val sessionId = _uiState.value.renamingSessionId ?: return
        val newName = _uiState.value.renamingSessionName.trim()
        if (newName.isBlank()) return
        val session = _uiState.value.activeSessions.find { it.id == sessionId } ?: return
        viewModelScope.launch {
            try {
                renameSessionUseCase(session, newName)
                _uiState.update {
                    it.copy(renamingSessionId = null, renamingSessionName = "")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка переименования сессии", e)
            }
        }
    }

    /** Отменить inline-переименование без сохранения */
    fun cancelRenameSession() {
        _uiState.update { it.copy(renamingSessionId = null, renamingSessionName = "") }
    }

    /** Запросить подтверждение удаления сессии */
    fun requestDeleteSession(session: Session) {
        _uiState.update { it.copy(sessionPendingDelete = session) }
    }

    fun dismissDeleteSessionDialog() {
        _uiState.update { it.copy(sessionPendingDelete = null) }
    }

    /**
     * Подтверждённое удаление сессии.
     * Если удалённая сессия была активной — переключается на следующую
     * или вызывает [EnsureDefaultSessionUseCase], чтобы создать «Основную».
     */
    fun confirmDeleteSession() {
        val session = _uiState.value.sessionPendingDelete ?: return
        val wasActive = session.id == _uiState.value.activeSession?.id
        // Вычисляем оставшиеся сессии до удаления, пока состояние ещё актуально
        val remaining = _uiState.value.activeSessions.filter { it.id != session.id }
        viewModelScope.launch {
            try {
                deleteSessionUseCase(session.id)
                if (wasActive) {
                    if (remaining.isNotEmpty()) {
                        // Переключаемся на первую оставшуюся сессию
                        setActiveSessionUseCase(remaining.first().id)
                    } else {
                        // Сессий не осталось — сбросить активную и создать «Основную»
                        setActiveSessionUseCase(-1L)
                        ensureDefaultSessionUseCase()
                    }
                }
                _uiState.update {
                    it.copy(
                        sessionPendingDelete = null,
                        isDrawerOpen = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка удаления сессии", e)
                _uiState.update { it.copy(sessionPendingDelete = null) }
            }
        }
    }
}
