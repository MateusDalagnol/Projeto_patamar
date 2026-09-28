package com.patamar.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.patamar.app.data.model.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<Event>)

    @Query("SELECT * FROM events ORDER BY date, time")
    fun observeAll(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE id = :eventId LIMIT 1")
    suspend fun getById(eventId: String): Event?

    @Query("SELECT COUNT(*) FROM events")
    suspend fun count(): Int

    @Query(
        "SELECT * FROM events WHERE " +
            "(:query = '' OR name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%') " +
            "ORDER BY date, time"
    )
    fun search(query: String): Flow<List<Event>>
}
