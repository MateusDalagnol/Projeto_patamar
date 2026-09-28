package com.patamar.app.core.validation

import com.patamar.app.core.utils.Constants
import java.util.Locale

// Regras de validação e normalização de credenciais. Kotlin puro (sem Android)
// para ser a fonte única da verdade da camada de dados e poder ser testado na JVM.
object AuthValidator {

    const val MAX_EMAIL_LENGTH = 254
    const val MAX_NAME_LENGTH = 60
    const val MAX_PASSWORD_LENGTH = 64

    private val EMAIL_REGEX = Regex(
        "^[A-Za-z0-9](?:[A-Za-z0-9._%+-]{0,62}[A-Za-z0-9])?" +
            "@(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$"
    )
    private val NAME_REGEX = Regex("^\\p{L}[\\p{L}\\p{M}' .-]*$")
    private val WHITESPACE = Regex("\\s+")

    fun normalizeEmail(raw: String): String = raw.trim().lowercase(Locale.ROOT)

    fun normalizeName(raw: String): String = raw.trim().replace(WHITESPACE, " ")

    fun validateEmail(raw: String): String? {
        val email = normalizeEmail(raw)
        return when {
            email.isEmpty() -> "Informe o e-mail"
            email.length > MAX_EMAIL_LENGTH -> "E-mail muito longo"
            email.contains("..") || !EMAIL_REGEX.matches(email) -> "E-mail inválido"
            else -> null
        }
    }

    fun validateName(raw: String): String? {
        val name = normalizeName(raw)
        return when {
            name.isEmpty() -> "Informe seu nome"
            name.length < Constants.MIN_NAME_LENGTH -> "Mínimo ${Constants.MIN_NAME_LENGTH} caracteres"
            name.length > MAX_NAME_LENGTH -> "Máximo $MAX_NAME_LENGTH caracteres"
            !NAME_REGEX.matches(name) -> "Use apenas letras, espaços, hífen ou apóstrofo"
            else -> null
        }
    }

    // Senha é usada exatamente como digitada: sem trim, sem normalização.
    fun validateNewPassword(password: String): String? = when {
        password.isEmpty() -> "Informe a senha"
        password.length < Constants.MIN_PASSWORD_LENGTH -> "Mínimo ${Constants.MIN_PASSWORD_LENGTH} caracteres"
        password.length > MAX_PASSWORD_LENGTH -> "Máximo $MAX_PASSWORD_LENGTH caracteres"
        password.any { it.isWhitespace() } -> "A senha não pode conter espaços"
        password.none { it.isUpperCase() } -> "Inclua ao menos uma letra maiúscula"
        password.none { it.isLowerCase() } -> "Inclua ao menos uma letra minúscula"
        password.none { it.isDigit() } -> "Inclua ao menos um número"
        else -> null
    }

    fun validateConfirmPassword(password: String, confirm: String): String? = when {
        confirm.isEmpty() -> "Confirme a senha"
        password != confirm -> "As senhas não coincidem"
        else -> null
    }
}
