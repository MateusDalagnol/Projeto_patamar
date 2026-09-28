package com.patamar.app.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.patamar.app.data.local.db.UserDao
import com.patamar.app.data.local.mock.MockDataSource
import com.patamar.app.data.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {

    suspend fun seedTestUserIfEmpty() {
        val testUser = MockDataSource.testUser()
        if (userDao.findByEmail(testUser.email) == null) {
            try {
                userDao.insert(testUser)
            } catch (e: SQLiteConstraintException) {
                // já existe (outro seed passou na frente) — nada a fazer
            }
        }
    }

    suspend fun findByEmail(email: String): User? = userDao.findByEmail(email)

    suspend fun findById(userId: String): User? = userDao.findById(userId)

    suspend fun emailExists(email: String): Boolean = userDao.countByEmail(email) > 0

    suspend fun insert(user: User) = userDao.insert(user)
}
