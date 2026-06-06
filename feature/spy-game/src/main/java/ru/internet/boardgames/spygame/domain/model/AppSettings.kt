package ru.internet.boardgames.spygame.domain.model

/**
 * Снапшот настроек приложения.
 * Эмитится [GetSettingsUseCase] при каждом изменении настройки.
 *
 * Язык приложения больше не хранится в настройках — он определяется
 * автоматически из системной локали ([java.util.Locale.getDefault]).
 *
 * @param playerCount Число игроков (2..10).
 */
data class AppSettings(
    val playerCount: Int
)
