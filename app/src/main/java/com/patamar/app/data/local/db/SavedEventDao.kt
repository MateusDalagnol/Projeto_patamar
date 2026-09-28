package com.patamar.app.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.patamar.app.data.model.Event
import com.patamar.app.data.model.SavedEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(savedEvent: SavedEvent)

    @Delete
    suspend fun delete(savedEvent: SavedEvent)

    @Query("DELETE FROM saved_events WHERE userId = :userId AND eventId = :eventId")
    suspend fun deleteByIds(userId: String, eventId: String)

    @Query(
        "SELECT events.* FROM events " +
            "INNER JOIN saved_events ON events.id = saved_events.eventId " +
            "WHERE saved_events.userId = :userId " +
            "ORDER BY saved_events.savedAt DESC"
    )
    fun observeSavedEvents(userId: String): Flow<List<Event>>

    @Query(
        "SELECT COUNT(*) FROM saved_events WHERE userId = :userId AND eventId = :eventId"
    )
    suspend fun isSaved(userId: String, eventId: String): Int

    @Query("SELECT COUNT(*) FROM saved_events WHERE userId = :userId")
    fun observeSavedCount(userId: String): Flow<Int>
}
