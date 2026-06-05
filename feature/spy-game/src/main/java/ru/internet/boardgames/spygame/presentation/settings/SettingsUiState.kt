package ru.internet.boardgames.spygame.presentation.settings

import java.util.Locale

// ─────────────────────────────────────────
// Enum языка
// ─────────────────────────────────────────

/**
 * Представление языкового выбора пользователя в UI.
 *
 * Не равно языковому коду для БД — тот определяется через [resolveLanguageCode].
 * "system" означает «как на устройстве», а не конкретный язык.
 */
enum class AppLanguage(val code: String) {
    SYSTEM("system"),
    RUSSIAN("ru"),
    ENGLISH("en");

    companion object {
        /**
         * Восстанавливает [AppLanguage] из строкового кода DataStore.
         * Неизвестный код → [SYSTEM] как безопасный fallback.
         */
        fun fromCode(code: String): AppLanguage =
            entries.find { it.code == code } ?: SYSTEM
    }
}

/**
 * Разрешает реальный код языка для запросов к БД.
 *
 * [AppLanguage.SYSTEM] → язык устройства → "ru" или "en",
 * при неподдерживаемом — fallback на "en".
 *
 * Вызывается перед каждым обращением к [CategoryRepository].
 */
fun AppLanguage.resolveLanguageCode(): String {
    val supported = setOf("ru", "en")
    return when (this) {
        AppLanguage.SYSTEM -> {
            val deviceLang = Locale.getDefault().language.take(2)
            if (deviceLang in supported) deviceLang else "en"
        }
        AppLanguage.RUSSIAN -> "ru"
        AppLanguage.ENGLISH -> "en"
    }
}

// ─────────────────────────────────────────
// UI State
// ─────────────────────────────────────────

/**
 * Состояние экрана настроек.
 *
 * @param playerCount Выбранное число игроков (2..10).
 * @param language    Текущий выбор языка.
 * @param isLoading   true пока DataStore не вернул первое значение.
 */
data class SettingsUiState(
    val playerCount: Int = 6,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val isLoading: Boolean = true
)
