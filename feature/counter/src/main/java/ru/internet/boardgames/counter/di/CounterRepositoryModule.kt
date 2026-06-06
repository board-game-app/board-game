package ru.internet.boardgames.counter.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.internet.boardgames.counter.data.repository.CounterPrefsRepositoryImpl
import ru.internet.boardgames.counter.data.repository.CounterRepositoryImpl
import ru.internet.boardgames.counter.data.repository.SessionRepositoryImpl
import ru.internet.boardgames.counter.domain.repository.CounterPrefsRepository
import ru.internet.boardgames.counter.domain.repository.CounterRepository
import ru.internet.boardgames.counter.domain.repository.SessionRepository
import javax.inject.Singleton

/**
 * Hilt-модуль привязки доменных интерфейсов к реализациям data-слоя.
 *
 * Три репозитория:
 *   [SessionRepository]      → Room (сессии).
 *   [CounterRepository]      → Room (счётчики).
 *   [CounterPrefsRepository] → DataStore (настройки: режим карточек + активная сессия).
 *
 * @Binds + abstract class: компилятор генерирует только делегирование,
 * без лишних методов-обёрток как при @Provides.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CounterRepositoryModule {

    /** Сессии: Room → доменный интерфейс */
    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        impl: SessionRepositoryImpl
    ): SessionRepository

    /** Счётчики: Room → доменный интерфейс */
    @Binds
    @Singleton
    abstract fun bindCounterRepository(
        impl: CounterRepositoryImpl
    ): CounterRepository

    /**
     * Настройки счётчика: DataStore → доменный интерфейс.
     * Включает режим карточек (§5, §11) и id последней активной сессии.
     */
    @Binds
    @Singleton
    abstract fun bindCounterPrefsRepository(
        impl: CounterPrefsRepositoryImpl
    ): CounterPrefsRepository
}
