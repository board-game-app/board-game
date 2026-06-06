package ru.internet.boardgames.counter.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

/** Qualifier для DataStore модуля :feature:counter */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CounterPreferencesDataStore

/**
 * Хранилище пользовательских предпочтений модуля счётчика.
 *
 * Ключи:
 *   [lastActiveSessionId] — какую сессию открыть при следующем запуске.
 *   [isCompactMode]       — режим отображения карточек: true = компактный (§5.1),
 *                           false = широкий (§5.2). Сохраняется между сессиями (§11).
 */
@Singleton
class CounterDataStore @Inject constructor(
    @param:CounterPreferencesDataStore private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val KEY_LAST_ACTIVE_SESSION_ID =
            longPreferencesKey("counter_last_active_session_id")

        /** Режим карточек: true = компактный, false = широкий */
        private val KEY_IS_COMPACT_MODE =
            booleanPreferencesKey("counter_is_compact_mode")

        /** По умолчанию открывается компактный режим */
        private const val DEFAULT_IS_COMPACT_MODE = true
    }

    // ── lastActiveSessionId ───────────────────────────────────────────────────

    /**
     * Поток id последней активной сессии.
     * null — пользователь ещё не открывал ни одной сессии.
     */
    val lastActiveSessionId: Flow<Long?> =
        dataStore.data.map { it[KEY_LAST_ACTIVE_SESSION_ID] }

    /** Запомнить, какую сессию пользователь открыл последней */
    suspend fun setLastActiveSessionId(sessionId: Long) {
        dataStore.edit { it[KEY_LAST_ACTIVE_SESSION_ID] = sessionId }
    }

    // ── isCompactMode ─────────────────────────────────────────────────────────

    /**
     * Поток режима отображения карточек.
     * true  → компактный (вертикальные карточки §5.1).
     * false → широкий (горизонтальные таблетки §5.2).
     */
    val isCompactMode: Flow<Boolean> =
        dataStore.data.map { it[KEY_IS_COMPACT_MODE] ?: DEFAULT_IS_COMPACT_MODE }

    /** Сохранить выбранный режим отображения карточек */
    suspend fun setCompactMode(isCompact: Boolean) {
        dataStore.edit { it[KEY_IS_COMPACT_MODE] = isCompact }
    }
}
