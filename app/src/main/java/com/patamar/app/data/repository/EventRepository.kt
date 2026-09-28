package com.patamar.app.data.repository

import com.patamar.app.data.local.db.EventDao
import com.patamar.app.data.local.db.SavedEventDao
import com.patamar.app.data.local.mock.MockDataSource
import com.patamar.app.data.model.Event
import com.patamar.app.data.model.SavedEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val eventDao: EventDao,
    private val savedEventDao: SavedEventDao
) {

    // BETA: grava (ou atualiza, por id) todos os eventos mock a cada abertura do app.
    // Assim instalações antigas recebem os eventos novos e as datas relativas ("hoje",
    // "daqui a 3 dias") não ficam no passado. Salvos referenciam só o id do evento.
    suspend fun seedMockEvents() {
        eventDao.insertAll(MockDataSource.events)
    }

    fun observeAllEvents(): Flow<List<Event>> = eventDao.observeAll()

    fun searchEvents(query: String): Flow<List<Event>> = eventDao.search(query)

    suspend fun getEventById(id: String): Event? = eventDao.getById(id)

    fun observeSavedEvents(userId: String): Flow<List<Event>> = savedEventDao.observeSavedEvents(userId)

    fun observeSavedCount(userId: String): Flow<Int> = savedEventDao.observeSavedCount(userId)

    suspend fun isEventSaved(userId: String, eventId: String): Boolean =
        savedEventDao.isSaved(userId, eventId) > 0

    suspend fun toggleSaved(userId: String, eventId: String) {
        if (isEventSaved(userId, eventId)) {
            savedEventDao.deleteByIds(userId, eventId)
        } else {
            savedEventDao.save(SavedEvent(userId, eventId))
        }
    }

    suspend fun removeSaved(userId: String, eventId: String) {
        savedEventDao.deleteByIds(userId, eventId)
    }
}
