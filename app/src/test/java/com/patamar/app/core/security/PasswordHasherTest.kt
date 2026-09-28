package com.patamar.app.core.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

class PasswordHasherTest {

    @Test fun `hash novo verifica a senha correta e rejeita a errada`() {
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash("Teste@123", salt)
        assertTrue(hash.startsWith("pbkdf2$"))
        assertTrue(PasswordHasher.verify("Teste@123", hash, salt))
        assertFalse(PasswordHasher.verify("teste@123", hash, salt))
        assertFalse(PasswordHasher.verify("Teste@123 ", hash, salt))
    }

    @Test fun `salts diferentes geram hashes diferentes`() {
        val a = PasswordHasher.generateSalt()
        val b = PasswordHasher.generateSalt()
        assertNotEquals(a, b)
        assertEquals(32, a.length)
        assertNotEquals(PasswordHasher.hash("Teste@123", a), PasswordHasher.hash("Teste@123", b))
    }

    @Test fun `hash legado sha256 continua valido`() {
        val salt = "patamar_beta_salt_2024"
        val legacy = MessageDigest.getInstance("SHA-256")
            .digest("$salt:Teste@123".toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        assertTrue(PasswordHasher.verify("Teste@123", legacy, salt))
        assertFalse(PasswordHasher.verify("Teste@124", legacy, salt))
    }

    @Test fun `hash malformado nao passa`() {
        assertFalse(PasswordHasher.verify("x", "pbkdf2\$abc\$00", "salt"))
        assertFalse(PasswordHasher.verify("x", "", "salt"))
    }
}
