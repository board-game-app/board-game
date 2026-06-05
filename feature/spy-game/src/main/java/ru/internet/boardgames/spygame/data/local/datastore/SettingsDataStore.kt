package ru.internet.boardgames.spygame.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// Делегат DataStore объявлен на уровне файла — требование API preferencesDataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "spy_game_settings")

/**
 * Ключи настроек DataStore.
 * Вынесены в object для удобного доступа из тестов.
 */
object SettingsKeys {
    val PLAYER_COUNT    = intPreferencesKey("player_count")
    val LANGUAGE        = stringPreferencesKey("language")
    val CONTENT_VERSION = intPreferencesKey("content_version")
}

const val DEFAULT_PLAYER_COUNT    = 6
const val DEFAULT_LANGUAGE        = "system"
const val DEFAULT_CONTENT_VERSION = 0

/**
 * Обёртка над DataStore<Preferences>.
 * Предоставляет типизированный доступ к настройкам приложения.
 *
 * Используется как Singleton — создаётся один раз через Hilt.
 */
@Singleton
class SettingsDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // ─────────────────────────────────────────
    // Flows (для подписки из ViewModel / Repository)
    // ─────────────────────────────────────────

    /** Количество игроков. Эмитит дефолт при ошибке чтения. */
    val playerCount: Flow<Int> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs -> prefs[SettingsKeys.PLAYER_COUNT] ?: DEFAULT_PLAYER_COUNT }

    /** Код языка: "ru", "en" или "system". */
    val language: Flow<String> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs -> prefs[SettingsKeys.LANGUAGE] ?: DEFAULT_LANGUAGE }

    // ─────────────────────────────────────────
    // Suspend-функции (запись)
    // ─────────────────────────────────────────

    suspend fun setPlayerCount(count: Int) {
        dataStore.edit { prefs -> prefs[SettingsKeys.PLAYER_COUNT] = count }
    }

    suspend fun setLanguage(language: String) {
        dataStore.edit { prefs -> prefs[SettingsKeys.LANGUAGE] = language }
    }

    // ─────────────────────────────────────────
    // Версионирование контента (seeding)
    // ─────────────────────────────────────────

    /**
     * Разовое чтение версии контента.
     * [ContentLoader] вызывает это при запуске приложения.
     */
    suspend fun getContentVersion(): Int =
        dataStore.data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
            .map { prefs -> prefs[SettingsKeys.CONTENT_VERSION] ?: DEFAULT_CONTENT_VERSION }
            .first()

    /** Сохраняем новую версию после успешного seeding-а. */
    suspend fun setContentVersion(version: Int) {
        dataStore.edit { prefs -> prefs[SettingsKeys.CONTENT_VERSION] = version }
    }
}
