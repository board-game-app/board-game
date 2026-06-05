package ru.internet.boardgames.spygame.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.internet.boardgames.spygame.domain.model.AppSettings
import ru.internet.boardgames.spygame.domain.usecase.GenerateGameSessionUseCase
import ru.internet.boardgames.spygame.domain.usecase.GetRandomCategoryUseCase
import ru.internet.boardgames.spygame.domain.usecase.GetSettingsUseCase
import ru.internet.boardgames.spygame.presentation.settings.AppLanguage
import ru.internet.boardgames.spygame.presentation.settings.resolveLanguageCode
import javax.inject.Inject

/**
 * ViewModel игрового экрана.
 *
 * Ответственности:
 * 1. Подписаться на настройки и загружать сессию при их изменении.
 * 2. Управлять жизненным циклом карточек: STACKED → REVEALED → DISMISSED.
 * 3. Запускать и отменять таймер, обновлять [GameUiState.timerProgress].
 * 4. Обрабатывать запрос «Обновить» (новая категория).
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val getRandomCategoryUseCase: GetRandomCategoryUseCase,
    private val generateGameSessionUseCase: GenerateGameSessionUseCase,
    private val getSettingsUseCase: GetSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    /** Последние известные настройки — нужны для refreshGame() и для сравнения. */
    private var lastSettings: AppSettings? = null

    /** Job таймера текущей открытой карточки. Отменяется при dismiss. */
    private var timerJob: Job? = null

    /**
     * Job загрузки сессии. Отменяет предыдущий запрос при повторном вызове,
     * исключая гонку между параллельными loadSession — побеждает последний вызов.
     */
    private var loadSessionJob: Job? = null

    init {
        observeSettings()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Подписка на настройки
    // ─────────────────────────────────────────────────────────────────────────

    private fun observeSettings() {
        viewModelScope.launch {
            getSettingsUseCase().collect { settings ->
                val prev = lastSettings
                lastSettings = settings

                val shouldReload = when {
                    prev == null -> true                              // Первый запуск
                    prev.language != settings.language -> true        // Сменился язык
                    prev.playerCount != settings.playerCount -> true  // Сменилось число игроков
                    else -> false
                }

                if (shouldReload) {
                    loadSession(settings, excludeCategoryId = null)
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Загрузка сессии
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Загружает новую игровую сессию.
     *
     * @param settings          Актуальные настройки (язык + число игроков).
     * @param excludeCategoryId ID категории, которую нужно исключить (для Refresh).
     */
    private fun loadSession(settings: AppSettings, excludeCategoryId: String?) {
        loadSessionJob?.cancel()
        cancelTimer()

        loadSessionJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            runCatching {
                val languageCode = AppLanguage
                    .fromCode(settings.language)
                    .resolveLanguageCode()

                val category = getRandomCategoryUseCase(
                    language = languageCode,
                    excludeId = excludeCategoryId
                )
                val session = generateGameSessionUseCase(category, settings.playerCount)

                _uiState.update { current ->
                    current.copy(
                        session = session,
                        cardStates = List(session.totalPlayers) { CardUiState.STACKED },
                        timerProgress = 1f,
                        isLoading = false,
                        error = null
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Не удалось загрузить игру"
                    )
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Действия пользователя
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Единая точка обработки тапа по карточке.
     *
     * - STACKED + верхняя карточка → переход в REVEALED, старт таймера.
     * - REVEALED → повторный тап → dismiss.
     * - Всё остальное → игнорируется.
     *
     * @param position Индекс карточки в [GameUiState.cardStates].
     */
    fun onCardTap(position: Int) {
        val state = _uiState.value
        val cardState = state.cardStates.getOrNull(position) ?: return
        print(cardState.name)

        when (cardState) {
            CardUiState.STACKED -> {
                // Только верхняя карточка реагирует на тап
                if (position == state.topStackedPosition) {
                    revealCard(position)
                }
            }
            CardUiState.REVEALED -> dismissCard(position)
            CardUiState.DISMISSED -> Unit // Уже отброшена — ничего не делаем
        }
    }

    /**
     * Отбросить карточку по свайпу.
     * Вызывается напрямую из composable (жест) минуя [onCardTap].
     *
     * Защищённый от двойного вызова: проверяет текущее состояние.
     */
    fun onCardSwiped(position: Int) {
        if (_uiState.value.cardStates.getOrNull(position) == CardUiState.REVEALED) {
            dismissCard(position)
        }
    }

    /**
     * Начать новую игру с другой категорией.
     * Исключает текущую категорию, чтобы гарантировать смену локации.
     */
    fun refreshGame() {
        if (_uiState.value.isLoading) return
        val settings = lastSettings ?: return
        val currentCategoryId = _uiState.value.session?.category?.id
        loadSession(settings, excludeCategoryId = currentCategoryId)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Внутренняя логика карточек
    // ─────────────────────────────────────────────────────────────────────────

    private fun revealCard(position: Int) {
        // карточку в REVEALED и отменит таймер первой — она зависнет без авто-закрытия.
        if (_uiState.value.revealedPosition != null) return
        setCardState(position, CardUiState.REVEALED)
        startTimer(position)
    }

    /**
     * Переводит карточку в DISMISSED и сбрасывает таймер.
     * Вызывается из: повторный тап, свайп, истечение таймера.
     */
    internal fun dismissCard(position: Int) {
        // Проверка идемпотентности — исключаем двойной вызов при гонке
        if (_uiState.value.cardStates.getOrNull(position) != CardUiState.REVEALED) return
        cancelTimer()
        setCardState(position, CardUiState.DISMISSED)
    }

    private fun setCardState(position: Int, newState: CardUiState) {
        _uiState.update { current ->
            val updated = current.cardStates.toMutableList()
            if (position in updated.indices) {
                updated[position] = newState
            }
            current.copy(cardStates = updated)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Таймер
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Запускает таймер для открытой карточки.
     *
     * Реализован как цикл с [TIMER_TICK_MS] задержкой — обновляет
     * [GameUiState.timerProgress] ~20 раз в секунду для плавного прогресс-бара.
     * По истечении [TIMER_DURATION_MS] вызывает [dismissCard] автоматически.
     */
    private fun startTimer(position: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var elapsed = 0L

            while (isActive) {
                delay(TIMER_TICK_MS)
                elapsed += TIMER_TICK_MS

                val progress = 1f - (elapsed.toFloat() / TIMER_DURATION_MS).coerceAtMost(1f)
                _uiState.update { it.copy(timerProgress = progress) }

                if (elapsed >= TIMER_DURATION_MS) {
                    dismissCard(position)
                    return@launch
                }
            }
        }
    }

    /**
     * Отменяет текущий Job таймера и сбрасывает прогресс в 1.0.
     * Вызывается перед каждым dismiss и перед загрузкой новой сессии.
     */
    private fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update { it.copy(timerProgress = 1f) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Константы
    // ─────────────────────────────────────────────────────────────────────────

    companion object {
        /** Продолжительность таймера (мс). 5 секунд по спецификации. */
        const val TIMER_DURATION_MS = 5_000L

        /**
         * Интервал обновления прогресса (мс).
         * 50мс = 20fps — достаточно плавно, не перегружает StateFlow.
         */
        const val TIMER_TICK_MS = 50L
    }
}
