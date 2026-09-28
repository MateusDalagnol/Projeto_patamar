package com.patamar.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.patamar.app.data.model.Event
import com.patamar.app.data.model.EventTypeConverters
import com.patamar.app.data.model.SavedEvent
import com.patamar.app.data.model.User

@Database(
    entities = [Event::class, User::class, SavedEvent::class],
    // v2: Event ganhou imageUrl (fotos no Explorar) — banco recriado via fallbackToDestructiveMigration
    // v3: users.email com índice único (ver Migrations.MIGRATION_2_3)
    version = 3,
    exportSchema = false
)
@TypeConverters(EventTypeConverters::class)
abstract class PatamarDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun userDao(): UserDao
    abstract fun savedEventDao(): SavedEventDao
}
