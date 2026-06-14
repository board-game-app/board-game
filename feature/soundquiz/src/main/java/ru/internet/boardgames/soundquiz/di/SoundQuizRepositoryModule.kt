package ru.internet.boardgames.soundquiz.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.internet.boardgames.soundquiz.data.repository.CategoryRepositoryImpl
import ru.internet.boardgames.soundquiz.data.repository.SoundQuizSettingsRepositoryImpl
import ru.internet.boardgames.soundquiz.data.repository.WordRepositoryImpl
import ru.internet.boardgames.soundquiz.domain.repository.CategoryRepository
import ru.internet.boardgames.soundquiz.domain.repository.SoundQuizSettingsRepository
import ru.internet.boardgames.soundquiz.domain.repository.WordRepository
import javax.inject.Singleton

/**
 * Hilt-модуль привязки репозиториев.
 * @Binds + abstract class — более эффективно чем @Provides:
 * Hilt генерирует прямое делегирование без промежуточного метода-фабрики.
 * @Singleton гарантирует единственный экземпляр каждого репозитория.
 *
 * SoundQuizSettingsDataStore и SoundQuizContentLoader объявлены с
 * @Singleton + @Inject constructor — Hilt управляет ими автоматически,
 * дополнительных биндингов не требуется.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SoundQuizRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindWordRepository(
        impl: WordRepositoryImpl
    ): WordRepository

    @Binds
    @Singleton
    abstract fun bindSoundQuizSettingsRepository(
        impl: SoundQuizSettingsRepositoryImpl
    ): SoundQuizSettingsRepository
}
