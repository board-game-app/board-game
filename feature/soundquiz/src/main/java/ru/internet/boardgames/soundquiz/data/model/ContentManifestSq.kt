package ru.internet.boardgames.soundquiz.data.model

import kotlinx.serialization.Serializable

/**
 * DTO манифеста встроенного контента (manifest_sq.json).
 * Суффикс "Sq" исключает конфликт с ContentManifest SpyGame если оба модуля в одном APK.
 */
@Serializable
data class ContentManifestSq(
    val contentVersion: Int,
    val lastUpdated: String,
    val supportedLanguages: List<String>
)