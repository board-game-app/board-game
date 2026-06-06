package ru.internet.boardgames

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.internet.boardgames.spygame.data.local.assets.ContentLoader
import javax.inject.Inject

/**
 * Application-класс всего проекта.
 *
 * Единственное место с @HiltAndroidApp — Hilt автоматически
 * подхватывает @Module из всех feature-модулей, подключённых
 * как зависимости :app.
 *
 * Добавление новой игры:
 * 1. Добавить implementation(project(":feature:sound-quiz")) в app/build.gradle.kts
 * 2. Добавить @Inject lateinit var soundQuizContentLoader: SoundQuizContentLoader
 * 3. Вызвать seedIfNeeded() в applicationScope.launch ниже
 */
@HiltAndroidApp
class BoardGamesApplication : Application() {

    @Inject
    lateinit var spyGameContentLoader: ContentLoader

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            // Каждая игра сидирует свою БД независимо
            runCatching { spyGameContentLoader.seedIfNeeded() }
                .onFailure { it.printStackTrace() }

        }
    }
}
