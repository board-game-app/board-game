package ru.internet.boardgames.spygame.domain.model

/**
 * Доменная модель одной игровой карточки.
 *
 * @param index        Порядковый номер (1-based), отображается на рубашке: "Аэропорт № 3".
 * @param isSpy        Признак шпиона. Если true — на обороте показывается слово «Шпион»
 *                     (берётся из stringResource в UI, а не хранится здесь).
 * @param word         Слово категории для обычного игрока.
 *                     Для шпиона содержит [SPY_WORD_PLACEHOLDER] — UI заменяет его
 *                     на локализованную строку из resources.
 * @param categoryName Локализованное название категории для отображения на рубашке.
 */
data class GameCard(
    val index: Int,
    val isSpy: Boolean,
    val word: String,
    val categoryName: String
) {
    companion object {
        /**
         * Константа-заглушка для слова шпиона.
         * UI-слой проверяет [isSpy] и заменяет этот placeholder
         * на stringResource(R.string.spy_word).
         */
        const val SPY_WORD_PLACEHOLDER = "SPY_PLACEHOLDER"
    }
}
