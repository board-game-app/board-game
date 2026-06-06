pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "BoardGames"

// ── Модули проекта ────────────────────────────────────────────────────────────
include(":app")

// Каждая игра — отдельный feature-модуль.
// Добавление новой игры: include(":feature:sound-quiz") и создать модуль.
include(":feature:spy-game")
include(":feature:counter")
