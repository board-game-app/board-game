package ru.internet.boardgames.spygame.data.model

import kotlinx.serialization.Serializable

/**
 * Манифест контента из assets/content/manifest.json.
 *
 * [contentVersion] — монотонно возрастающий целочисленный счётчик.
 *   При каждом обновлении категорий он увеличивается,
 *   что триггерит ContentLoader на пересеивание базы.
 *
 * [supportedLanguages] — список языков для которых есть JSON-файлы.
 */
@Serializable
data class ContentManifest(
    val contentVersion: Int,
    val lastUpdated: String,
    val supportedLanguages: List<String>
)