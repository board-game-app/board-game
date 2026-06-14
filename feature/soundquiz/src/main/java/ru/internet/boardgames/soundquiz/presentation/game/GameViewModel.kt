package ru.internet.boardgames.soundquiz.presentation.game

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.internet.boardgames.soundquiz.data.local.assets.SoundQuizContentLoader
import ru.internet.boardgames.soundquiz.domain.model.ActiveCard
import ru.internet.boardgames.soundquiz.domain.model.CardState
import ru.internet.boardgames.soundquiz.domain.usecase.GenerateGameSessionUseCase
import ru.internet.boardgames.soundquiz.domain.usecase.GetSoundQuizSettingsUseCase
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class GameViewModel @Inject constructor(
    private val generateGameSession: GenerateGameSessionUseCase,
    private val contentLoader: SoundQuizContentLoader,
    private val getSettings: GetSoundQuizSettingsUseCase,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    companion object {
        private const val TICK_MS         = 50L
        private const val FLIP_DURATION_MS = 300L
    }

    init {
        loadGame()
        observeSettingsForRegeneration()
        observeTimerSetting()
    }

    // ── Инициализация ────────────────────────────────────────────────────────

    private fun loadGame() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                contentLoader.initialize()
                val session = generateGameSession()
                _uiState.update { it.copy(session = session, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    /**
     * Пересоздаём сессию при изменении настроек numberOfCategories, wordsPerCategory
     * или pinnedCategoryIds. Изменение только timerSeconds — НЕ пересоздаёт.
     * drop(1) — пропускаем первую эмиссию (уже обработана в loadGame).
     */
    private fun observeSettingsForRegeneration() {
        viewModelScope.launch {
            getSettings()
                .map { Triple(it.numberOfCategories, it.wordsPerCategory, it.pinnedCategoryIds) }
                .distinctUntilChanged()
                .drop(1)
                .collect {
                    if (!_uiState.value.isLoading) {
                        onNewGame()
                    }
                }
        }
    }

    /**
     * Применяет новое значение таймера к текущей сессии без перезапуска игры.
     * Работающий таймер не прерывается; новое значение будет использовано
     * для следующей карточки.
     */
    private fun observeTimerSetting() {
        viewModelScope.launch {
            getSettings()
                .map { it.timerSeconds }
                .distinctUntilChanged()
                .drop(1)
                .collect { newTimerSeconds ->
                    _uiState.update { state ->
                        val session = state.session ?: return@update state
                        state.copy(session = session.copy(timerSeconds = newTimerSeconds))
                    }
                }
        }
    }

    fun onNewGame() {
        cancelTimer()
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading        = true,
                    isGameOver       = false,
                    passPhoneMessage = false,
                    error            = null,
                    timerRemainingMs = 0L,
                    timerProgress    = 1f,
                    isTimerRunning   = false
                )
            }
            try {
                val session = generateGameSession()
                _uiState.update { it.copy(session = session, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    // ── Игровые действия ─────────────────────────────────────────────────────

    fun onStackTap(stackIndex: Int) {
        val state   = _uiState.value
        val session = state.session ?: return
        if (session.activeCard != null) return

        val stack = session.stacks.getOrNull(stackIndex) ?: return
        if (stack.words.isEmpty()) return

        _uiState.update {
            it.copy(
                session = session.copy(
                    activeCard = ActiveCard(
                        stackIndex = stackIndex,
                        word       = stack.words.first(),
                        state      = CardState.FACE_DOWN
                    )
                )
            )
        }
    }

    fun onCardTap() {
        val state      = _uiState.value
        val session    = state.session ?: return
        val activeCard = session.activeCard ?: return

        when (activeCard.state) {
            CardState.FACE_DOWN -> {
                _uiState.update {
                    it.copy(session = session.copy(activeCard = activeCard.copy(state = CardState.REVEALED)))
                }
                val totalMs = (_uiState.value.session?.timerSeconds ?: 60) * 1000L
                _uiState.update { it.copy(timerRemainingMs = totalMs) }
                // Таймер НЕ стартует при флипе —
                // он запустится при первом прикосновении к слову (onFirstWordPress)
            }
            CardState.REVEALED, CardState.ANIMATING_BACK -> Unit
        }
    }

    fun onWordExplained() {
        cancelTimer()
        val state      = _uiState.value
        val session    = state.session ?: return
        val activeCard = session.activeCard ?: return

        val updatedStacks = session.stacks.mapIndexed { index, stack ->
            if (index == activeCard.stackIndex) {
                stack.copy(
                    words     = stack.words.filter { it.id != activeCard.word.id },
                    explained = stack.explained + activeCard.word
                )
            } else stack
        }

        val newSession = session.copy(stacks = updatedStacks, activeCard = null)
        _uiState.update {
            it.copy(session = newSession, timerRemainingMs = 0L, timerProgress = 1f,
                isGameOver = newSession.isGameOver)
        }
    }

    fun onReadyForNextPlayer() {
        _uiState.update { it.copy(passPhoneMessage = false) }
    }

    /**
     * Первое прикосновение к слову на лицевой стороне карточки.
     * Именно здесь запускается таймер — пользователь сам решает, когда начать отсчёт.
     * Повторные вызовы игнорируются (таймер уже идёт).
     */
    fun onFirstWordPress() {
        if (!_uiState.value.isTimerRunning) {
            startTimer()
        }
    }

    // ── Таймер ────────────────────────────────────────────────────────────────

    private fun startTimer() {
        timerJob?.cancel()
        val totalMs = (_uiState.value.session?.timerSeconds ?: 60) * 1000L
        _uiState.update { it.copy(timerRemainingMs = totalMs, timerProgress = 1f, isTimerRunning = true) }

        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(TICK_MS.milliseconds)
                val remaining = (_uiState.value.timerRemainingMs - TICK_MS).coerceAtLeast(0L)
                _uiState.update {
                    it.copy(timerRemainingMs = remaining, timerProgress = remaining.toFloat() / totalMs)
                }
                if (remaining <= 0L) {
                    onTimerExpired()
                    break
                }
            }
        }
    }

    private fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update { it.copy(isTimerRunning = false) }
    }

    private suspend fun onTimerExpired() {
        val state      = _uiState.value
        val session    = state.session ?: return
        val activeCard = session.activeCard ?: return

        // Звуковой + вибро-сигнал при истечении таймера
        playTimeUpSignal()

        _uiState.update {
            it.copy(
                isTimerRunning = false,
                session        = session.copy(activeCard = activeCard.copy(state = CardState.ANIMATING_BACK))
            )
        }
        delay(FLIP_DURATION_MS.milliseconds)

        _uiState.update { current ->
            val cur     = current.session ?: return@update current
            val curCard = cur.activeCard ?: return@update current
            current.copy(
                session          = cur.copy(activeCard = curCard.copy(state = CardState.FACE_DOWN)),
                passPhoneMessage = true,
                timerRemainingMs = 0L,
                timerProgress    = 0f
            )
        }
    }

    // ── Звук и вибрация ──────────────────────────────────────────────────────

    private fun playTimeUpSignal() {
        vibrate()
        playBeep()
    }

    @Suppress("DEPRECATION")
    private fun vibrate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator.vibrate(
                    VibrationEffect.createOneShot(400L, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(VibrationEffect.createOneShot(400L, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (_: Exception) {
            // Вибрация не критична — игра продолжается
        }
    }

    private fun playBeep() {
        viewModelScope.launch {
            var toneGen: ToneGenerator? = null
            try {
                toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, ToneGenerator.MAX_VOLUME)
                toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 600)
                delay(700L.milliseconds) // ждём окончания тона перед release
            } catch (_: Exception) {
                // Звук не критичен
            } finally {
                toneGen?.release()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
