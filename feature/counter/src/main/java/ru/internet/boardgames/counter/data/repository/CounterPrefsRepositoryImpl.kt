package ru.internet.boardgames.counter.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.internet.boardgames.counter.data.local.datastore.CounterDataStore
import ru.internet.boardgames.counter.domain.model.CardDisplayMode
import ru.internet.boardgames.counter.domain.repository.CounterPrefsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CounterPrefsRepositoryImpl @Inject constructor(
    private val dataStore: CounterDataStore
) : CounterPrefsRepository {

    override fun observeCardDisplayMode(): Flow<CardDisplayMode> =
        dataStore.isCompactMode.map { isCompact ->
            if (isCompact) CardDisplayMode.COMPACT else CardDisplayMode.WIDE
        }

    override suspend fun setCardDisplayMode(mode: CardDisplayMode) {
        dataStore.setCompactMode(mode == CardDisplayMode.COMPACT)
    }

    override fun observeLastActiveSessionId(): Flow<Long?> =
        dataStore.lastActiveSessionId

    override suspend fun setLastActiveSessionId(sessionId: Long) {
        dataStore.setLastActiveSessionId(sessionId)
    }
}
