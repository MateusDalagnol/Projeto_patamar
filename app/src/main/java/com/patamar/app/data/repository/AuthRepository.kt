package com.patamar.app.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.patamar.app.core.security.AuthStore
import com.patamar.app.core.security.PasswordHasher
import com.patamar.app.core.utils.Constants
import com.patamar.app.core.validation.AuthValidator
import com.patamar.app.data.local.db.UserDao
import com.patamar.app.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val termsAccepted: Boolean
)

data class RegisterErrors(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null,
    val confirmPassword: String? = null,
    val terms: String? = null
) {
    val hasAny: Boolean get() = listOf(name, email, password, confirmPassword, terms).any { it != null }
}

sealed class RegisterResult {
    data class Success(val user: User) : RegisterResult()
    data class Invalid(val errors: RegisterErrors) : RegisterResult()
}

sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    data class InvalidInput(val emailError: String?, val passwordError: String?) : LoginResult()
    data class WrongCredentials(val attemptsLeft: Int) : LoginResult()
    data class Locked(val secondsLeft: Int) : LoginResult()
}

// "Back" do app: como não existe servidor, esta é a camada que decide se um cadastro ou
// login é válido. A UI só coleta os campos e mostra o que esta classe devolve — nenhuma
// regra fica só no formulário. Unicidade de e-mail é garantida aqui E pelo índice único
// da tabela users.
@Singleton
class AuthRepository internal constructor(
    private val userDao: UserDao,
    private val store: AuthStore,
    private val clock: () -> Long
) {

    @Inject
    constructor(userDao: UserDao, store: AuthStore) : this(userDao, store, System::currentTimeMillis)

    suspend fun register(request: RegisterRequest): RegisterResult {
        val errors = RegisterErrors(
            name = AuthValidator.validateName(request.name),
            email = AuthValidator.validateEmail(request.email),
            password = AuthValidator.validateNewPassword(request.password),
            confirmPassword = AuthValidator.validateConfirmPassword(request.password, request.confirmPassword),
            terms = if (request.termsAccepted) null else "Aceite os termos para continuar"
        )
        if (errors.hasAny) return RegisterResult.Invalid(errors)

        val email = AuthValidator.normalizeEmail(request.email)
        if (userDao.countByEmail(email) > 0) return RegisterResult.Invalid(EMAIL_TAKEN)

        val salt = PasswordHasher.generateSalt()
        val hash = withContext(Dispatchers.Default) { PasswordHasher.hash(request.password, salt) }
        val user = User(
            id = UUID.randomUUID().toString(),
            name = AuthValidator.normalizeName(request.name),
            email = email,
            passwordHash = hash,
            salt = salt
        )
        try {
            userDao.insert(user)
        } catch (e: SQLiteConstraintException) {
            // Outro cadastro com o mesmo e-mail passou entre a checagem e o insert.
            return RegisterResult.Invalid(EMAIL_TAKEN)
        }
        store.saveSession(user.id, isGuest = false)
        return RegisterResult.Success(user)
    }

    suspend fun login(rawEmail: String, password: String): LoginResult {
        val emailError = AuthValidator.validateEmail(rawEmail)
        val passwordError = if (password.isEmpty()) "Informe a senha" else null
        if (emailError != null || passwordError != null) {
            return LoginResult.InvalidInput(emailError, passwordError)
        }

        val email = AuthValidator.normalizeEmail(rawEmail)
        val now = clock()
        val lockUntil = store.getLoginLockUntil(email)
        if (now < lockUntil) return LoginResult.Locked(secondsUntil(lockUntil, now))

        val user = userDao.findByEmail(email)
        val passwordOk = withContext(Dispatchers.Default) {
            if (user != null) {
                PasswordHasher.verify(password, user.passwordHash, user.salt)
            } else {
                PasswordHasher.burnCycles(password)
                false
            }
        }

        if (!passwordOk || user == null) {
            val fails = store.getLoginFailCount(email) + 1
            if (fails >= Constants.MAX_LOGIN_ATTEMPTS) {
                val newLock = clock() + Constants.LOGIN_LOCKOUT_MS
                store.saveLoginFailure(email, failCount = 0, lockUntil = newLock)
                return LoginResult.Locked(secondsUntil(newLock, clock()))
            }
            store.saveLoginFailure(email, failCount = fails, lockUntil = 0L)
            return LoginResult.WrongCredentials(attemptsLeft = Constants.MAX_LOGIN_ATTEMPTS - fails)
        }

        store.clearLoginAttempts(email)
        store.saveSession(user.id, isGuest = false)
        return LoginResult.Success(user)
    }

    private fun secondsUntil(target: Long, now: Long): Int = ((target - now + 999) / 1000).toInt()

    private companion object {
        val EMAIL_TAKEN = RegisterErrors(email = "Este e-mail já está cadastrado")
    }
}
