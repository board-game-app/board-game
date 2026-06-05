package ru.internet.boardgames.spygame.domain.model

/**
 * Снапшот настроек приложения.
 * Эмитится [GetSettingsUseCase] при каждом изменении любой настройки.
 *
 * @param playerCount Число игроков (2..10).
 * @param language    Код языка: "ru", "en" или "system".
 *                    Интерпретация "system" — в presentation-слое:
 *                    Locale.getDefault().language.take(2), fallback → "en".
 */
data class AppSettings(
    val playerCount: Int,
    val language: String
)
