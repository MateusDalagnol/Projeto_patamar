package com.patamar.app.core.di

import android.content.Context
import androidx.room.Room
import com.patamar.app.core.security.AuthStore
import com.patamar.app.core.security.EncryptedPrefsManager
import com.patamar.app.data.local.db.EventDao
import com.patamar.app.data.local.db.Migrations
import com.patamar.app.data.local.db.PatamarDatabase
import com.patamar.app.data.local.db.SavedEventDao
import com.patamar.app.data.local.db.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PatamarDatabase =
        Room.databaseBuilder(context, PatamarDatabase::class.java, "patamar.db")
            .addMigrations(Migrations.MIGRATION_2_3)
            .fallbackToDestructiveMigration() // BETA: versões sem migração escrita recriam o banco
            .build()

    @Provides
    fun provideEventDao(db: PatamarDatabase): EventDao = db.eventDao()

    @Provides
    fun provideUserDao(db: PatamarDatabase): UserDao = db.userDao()

    @Provides
    fun provideSavedEventDao(db: PatamarDatabase): SavedEventDao = db.savedEventDao()

    @Provides
    @Singleton
    fun provideEncryptedPrefsManager(@ApplicationContext context: Context): EncryptedPrefsManager =
        EncryptedPrefsManager(context)

    @Provides
    fun provideAuthStore(prefs: EncryptedPrefsManager): AuthStore = prefs
}
