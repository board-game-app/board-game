package ru.internet.boardgames.soundquiz.domain.model

/** Доменная модель слова карточки */
data class Word(
    val id: Long,
    val categoryId: Long,
    val word: String
)
