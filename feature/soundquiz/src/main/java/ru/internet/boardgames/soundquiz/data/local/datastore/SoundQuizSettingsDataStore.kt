package ru.internet.boardgames.soundquiz.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ru.internet.boardgames.soundquiz.domain.model.GameSettings
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.soundQuizDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "sound_quiz_settings"
)

@Singleton
class SoundQuizSettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.soundQuizDataStore

    companion object {
        val KEY_NUMBER_OF_CATEGORIES = intPreferencesKey("sq_number_of_categories")
        val KEY_WORDS_PER_CATEGORY   = intPreferencesKey("sq_words_per_category")
        val KEY_TIMER_SECONDS        = intPreferencesKey("sq_timer_seconds")
        val KEY_PINNED_CATEGORY_IDS  = stringSetPreferencesKey("sq_pinned_category_ids")
        // sq_language намеренно удалён: язык всегда определяется из локали устройства
        val KEY_CONTENT_VERSION      = intPreferencesKey("sq_content_version")
    }

    val settingsFlow: Flow<GameSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            GameSettings(
                numberOfCategories = prefs[KEY_NUMBER_OF_CATEGORIES] ?: 3,
                wordsPerCategory   = prefs[KEY_WORDS_PER_CATEGORY]   ?: 5,
                timerSeconds       = prefs[KEY_TIMER_SECONDS]        ?: 60,
                pinnedCategoryIds  = prefs[KEY_PINNED_CATEGORY_IDS]
                    ?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
            )
        }

    suspend fun saveSettings(settings: GameSettings) {
        dataStore.edit { prefs ->
            prefs[KEY_NUMBER_OF_CATEGORIES] = settings.numberOfCategories
            prefs[KEY_WORDS_PER_CATEGORY]   = settings.wordsPerCategory
            prefs[KEY_TIMER_SECONDS]        = settings.timerSeconds
            prefs[KEY_PINNED_CATEGORY_IDS]  =
                settings.pinnedCategoryIds.map { it.toString() }.toSet()
        }
    }

    suspend fun getContentVersion(): Int =
        dataStore.data
            .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
            .map { it[KEY_CONTENT_VERSION] ?: 0 }
            .first()

    suspend fun saveContentVersion(version: Int) {
        dataStore.edit { prefs -> prefs[KEY_CONTENT_VERSION] = version }
    }
}
