package com.patamar.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// E-mail é sempre gravado normalizado (trim + minúsculas) — ver AuthValidator.normalizeEmail.
// O índice único garante no banco que não existem duas contas com o mesmo e-mail.
@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val passwordHash: String,
    val salt: String
)
