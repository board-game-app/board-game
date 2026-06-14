package ru.internet.boardgames.soundquiz.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.internet.boardgames.soundquiz.data.local.db.CategoryDao
import ru.internet.boardgames.soundquiz.data.local.db.SoundQuizDatabase
import ru.internet.boardgames.soundquiz.data.local.db.WordDao
import javax.inject.Singleton

/**
 * Hilt-модуль базы данных.
 * Имя БД "sound_quiz_db" не конфликтует с другими feature-модулями.
 * SoundQuizDatabase — синглтон на весь процесс приложения.
 * DAOs не скопированы как Singleton: они лёгкие обёртки над БД,
 * а единственность гарантирована синглтоном самой базы.
 */
@Module
@InstallIn(SingletonComponent::class)
object SoundQuizDatabaseModule {

    @Provides
    @Singleton
    fun provideSoundQuizDatabase(
        @ApplicationContext context: Context
    ): SoundQuizDatabase = Room.databaseBuilder(
        context,
        SoundQuizDatabase::class.java,
        "sound_quiz_db"
    ).build()

    @Provides
    fun provideCategoryDao(database: SoundQuizDatabase): CategoryDao =
        database.categoryDao()

    @Provides
    fun provideWordDao(database: SoundQuizDatabase): WordDao =
        database.wordDao()
}
