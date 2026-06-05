# Эти правила автоматически применяются к :app при сборке релиза.
# Дублировать их в proguard-rules.pro :app не нужно.

# kotlinx.serialization (используется в ContentLoader для парсинга JSON)
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class ru.internet.boardgames.spygame.data.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class ru.internet.boardgames.spygame.data.model.**$$serializer { *; }

# Room
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt
-keep class dagger.hilt.** { *; }
