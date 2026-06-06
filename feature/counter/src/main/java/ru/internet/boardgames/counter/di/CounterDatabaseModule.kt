package ru.internet.boardgames.counter.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.internet.boardgames.counter.data.local.datastore.CounterPreferencesDataStore
import ru.internet.boardgames.counter.data.local.db.CounterDao
import ru.internet.boardgames.counter.data.local.db.CounterDatabase
import ru.internet.boardgames.counter.data.local.db.SessionDao
import javax.inject.Singleton

/**
 * Hilt-модуль инфраструктурных зависимостей data-слоя:
 * Room-база (v2 + MIGRATION_1_2), DAO и DataStore.
 *
 * Все зависимости живут в SingletonComponent —
 * один экземпляр на всё время жизни приложения.
 */
@Module
@InstallIn(SingletonComponent::class)
object CounterDatabaseModule {

    /**
     * Room-база данных v2.
     *
     * MIGRATION_1_2 обязательна — пользователи v1 (без новых полей Counter)
     * получат плавный апгрейд без потери данных.
     *
     * fallbackToDestructiveMigration НЕ вызывается намеренно:
     * любое изменение схемы в будущем должно иметь явную Migration.
     */
    @Provides
    @Singleton
    fun provideCounterDatabase(
        @ApplicationContext context: Context
    ): CounterDatabase = Room.databaseBuilder(
        context,
        CounterDatabase::class.java,
        "counter_database"
    )
        .addMigrations(CounterDatabase.MIGRATION_1_2)
        .build()

    /**
     * DAO сессий — лёгкий интерфейс поверх синглтон-базы,
     * отдельной @Singleton-аннотации не требует.
     */
    @Provides
    fun provideSessionDao(database: CounterDatabase): SessionDao =
        database.sessionDao()

    /**
     * DAO счётчиков.
     */
    @Provides
    fun provideCounterDao(database: CounterDatabase): CounterDao =
        database.counterDao()

    /**
     * DataStore<Preferences> для модуля счётчика.
     *
     * Аннотирован @CounterPreferencesDataStore — Hilt отличает его
     * от DataStore других модулей приложения.
     *
     * Хранит два ключа:
     *   counter_last_active_session_id — последняя открытая сессия.
     *   counter_is_compact_mode        — режим карточек (compact / wide).
     *
     * PreferenceDataStoreFactory.create — единственный корректный способ
     * создания DataStore в библиотечном модуле (property-делегат недоступен).
     */
    @Provides
    @Singleton
    @CounterPreferencesDataStore
    fun provideCounterPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("counter_preferences") }
    )
}
