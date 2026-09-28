package com.patamar.app.core.di

import com.patamar.app.core.security.EncryptedPrefsManager
import com.patamar.app.data.local.db.EventDao
import com.patamar.app.data.local.db.SavedEventDao
import com.patamar.app.data.local.db.UserDao
import com.patamar.app.data.repository.EventRepository
import com.patamar.app.data.repository.FilterRepository
import com.patamar.app.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideEventRepository(eventDao: EventDao, savedEventDao: SavedEventDao): EventRepository =
        EventRepository(eventDao, savedEventDao)

    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao): UserRepository = UserRepository(userDao)

    @Provides
    @Singleton
    fun provideFilterRepository(prefsManager: EncryptedPrefsManager): FilterRepository =
        FilterRepository(prefsManager)
}
