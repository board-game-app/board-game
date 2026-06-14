# Правила для потребителей библиотеки :feature:sound-quiz

# Room — сохраняем сущности и DAO
-keep class ru.internet.boardgames.soundquiz.data.local.db.** { *; }

# Kotlinx Serialization — сохраняем DTO-классы
-keep class ru.internet.boardgames.soundquiz.data.model.** { *; }
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Hilt — не трогать сгенерированные компоненты
-keep class ru.internet.boardgames.soundquiz.di.** { *; }

# DataStore — сохраняем ключи Preferences
-keepclassmembers class * {
    @androidx.datastore.preferences.core.PreferencesKey *;
}
