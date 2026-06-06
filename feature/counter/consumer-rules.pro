# Правила для потребителей библиотеки :feature:counter.
# Room и Hilt публикуют собственные consumer-правила через AAR,
# поэтому здесь достаточно защитить только доменные модели от стрипинга.

# Сохраняем доменные модели (используются с Kotlin data class + Room)
-keep class ru.internet.boardgames.counter.domain.model.** { *; }

# Сохраняем Room-entity классы
-keep class ru.internet.boardgames.counter.data.local.db.**Entity { *; }