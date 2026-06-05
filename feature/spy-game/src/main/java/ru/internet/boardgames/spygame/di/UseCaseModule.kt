package ru.internet.boardgames.spygame.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import ru.internet.boardgames.spygame.domain.repository.CategoryRepository
import ru.internet.boardgames.spygame.domain.repository.SettingsRepository
import ru.internet.boardgames.spygame.domain.usecase.GenerateGameSessionUseCase
import ru.internet.boardgames.spygame.domain.usecase.GetRandomCategoryUseCase
import ru.internet.boardgames.spygame.domain.usecase.GetSettingsUseCase
import ru.internet.boardgames.spygame.domain.usecase.SaveSettingsUseCase

/**
 * Предоставляет use case-ы в скоупе ViewModel.
 *
 * Почему [ViewModelComponent] вместо [SingletonComponent]:
 * - Use case-ы не имеют собственного состояния — их не нужно держать в памяти
 *   когда нет активных ViewModel.
 * - [ViewModelScoped] гарантирует один экземпляр use case на одну ViewModel,
 *   что безопасно для многопоточности (каждый viewModelScope независим).
 *
 * [GenerateGameSessionUseCase] не имеет зависимостей — Hilt создаёт его
 * автоматически через @Inject constructor, явный @Provides не нужен.
 * Он указан здесь для наглядности архитектуры.
 */
@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    @ViewModelScoped
    fun provideGetRandomCategoryUseCase(
        categoryRepository: CategoryRepository
    ): GetRandomCategoryUseCase = GetRandomCategoryUseCase(categoryRepository)

    @Provides
    @ViewModelScoped
    fun provideGenerateGameSessionUseCase(): GenerateGameSessionUseCase =
        GenerateGameSessionUseCase()

    @Provides
    @ViewModelScoped
    fun provideGetSettingsUseCase(
        settingsRepository: SettingsRepository
    ): GetSettingsUseCase = GetSettingsUseCase(settingsRepository)

    @Provides
    @ViewModelScoped
    fun provideSaveSettingsUseCase(
        settingsRepository: SettingsRepository
    ): SaveSettingsUseCase = SaveSettingsUseCase(settingsRepository)
}
