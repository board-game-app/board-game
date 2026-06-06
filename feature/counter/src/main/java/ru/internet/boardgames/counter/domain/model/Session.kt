package ru.internet.boardgames.counter.domain.model

/**
 * Доменная модель игровой сессии.
 * Не содержит зависимостей на Android/Room — чистый Kotlin.
 */
data class Session(
    val id: Long,
    val name: String,
    /** Время создания в мс (System.currentTimeMillis) */
    val createdAt: Long
)