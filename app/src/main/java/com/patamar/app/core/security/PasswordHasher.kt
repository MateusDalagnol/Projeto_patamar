package com.patamar.app.core.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

// Hash novo: "pbkdf2$<iterações>$<hex>" (PBKDF2-HmacSHA256).
// Hash legado (usuários criados antes desta versão): SHA-256("$salt:$senha") em hex,
// sem prefixo — continua sendo aceito na verificação.
// TODO: produção — trocar por Argon2id ou bcrypt.
object PasswordHasher {

    private const val SEPARATOR = '$'
    private const val PREFIX = "pbkdf2"
    private const val ITERATIONS = 40_000
    private const val KEY_BITS = 256

    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.toHex()
    }

    fun hash(password: String, salt: String): String =
        listOf(PREFIX, ITERATIONS.toString(), pbkdf2(password, salt, ITERATIONS)).joinToString(SEPARATOR.toString())

    fun verify(input: String, stored: String, salt: String): Boolean {
        val parts = stored.split(SEPARATOR)
        val (computed, expected) = if (parts.size == 3 && parts[0] == PREFIX) {
            val iterations = parts[1].toIntOrNull() ?: return false
            pbkdf2(input, salt, iterations) to parts[2]
        } else {
            legacySha256(input, salt) to stored
        }
        return MessageDigest.isEqual(computed.toByteArray(), expected.toByteArray())
    }

    // Gasta o mesmo tempo de um hash real quando o e-mail não existe, para não
    // revelar por tempo de resposta se a conta existe.
    fun burnCycles(password: String) {
        pbkdf2(password, "patamar_dummy_salt", ITERATIONS)
    }

    private fun pbkdf2(password: String, salt: String, iterations: Int): String {
        val spec = PBEKeySpec(password.toCharArray(), salt.toByteArray(Charsets.UTF_8), iterations, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded.toHex()
        } finally {
            spec.clearPassword()
        }
    }

    private fun legacySha256(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest("$salt:$password".toByteArray(Charsets.UTF_8)).toHex()
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
