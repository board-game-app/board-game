package ru.internet.boardgames.spygame.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.internet.boardgames.spygame.data.local.db.CategoryDao
import ru.internet.boardgames.spygame.data.local.db.SpyGameDatabase
import ru.internet.boardgames.spygame.data.local.db.WordDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSpyGameDatabase(
        @ApplicationContext context: Context
    ): SpyGameDatabase = Room.databaseBuilder(
        context,
        SpyGameDatabase::class.java,
        "spy_game.db"          // Изолированный файл БД этой фичи
    )
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    @Provides
    @Singleton
    fun provideCategoryDao(db: SpyGameDatabase): CategoryDao = db.categoryDao()

    @Provides
    @Singleton
    fun provideWordDao(db: SpyGameDatabase): WordDao = db.wordDao()
}
