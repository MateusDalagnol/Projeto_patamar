package com.patamar.app.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {

    // v2 -> v3: e-mail único. Normaliza os e-mails já gravados (trim + minúsculas),
    // remove duplicatas (mantém a conta mais antiga de cada e-mail) e cria o índice único.
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("UPDATE users SET email = lower(trim(email))")
            db.execSQL("DELETE FROM users WHERE rowid NOT IN (SELECT MIN(rowid) FROM users GROUP BY email)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_users_email ON users(email)")
        }
    }
}
