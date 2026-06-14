package ru.internet.boardgames

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.internet.boardgames.spygame.data.local.assets.ContentLoader as SpyGameContentLoader
import ru.internet.boardgames.soundquiz.data.local.assets.SoundQuizContentLoader
import javax.inject.Inject

/**
 * Application-класс всего проекта.
 *
 * Единственное место с @HiltAndroidApp — Hilt автоматически
 * подхватывает @Module из всех feature-модулей.
 *
 * Каждый feature-модуль сидирует свою БД независимо.
 * SupervisorJob: падение одного seeding-а не отменяет остальные.
 *
 * Добавление новой игры:
 * 1. @Inject lateinit var newGameContentLoader: NewGameContentLoader
 * 2. runCatching { newGameContentLoader.seedIfNeeded() } в launch ниже
 */
@HiltAndroidApp
class BoardGamesApplication : Application() {

    @Inject
    lateinit var spyGameContentLoader: SpyGameContentLoader

    @Inject
    lateinit var soundQuizContentLoader: SoundQuizContentLoader

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            // SpyGame — seeding категорий и слов
            runCatching { spyGameContentLoader.seedIfNeeded() }
                .onFailure { it.printStackTrace() }

            // SoundQuiz — seeding встроенных категорий (независимая БД)
            runCatching { soundQuizContentLoader.initialize() }  // ← ДОБАВЛЕНО
                .onFailure { it.printStackTrace() }
        }
    }
}
