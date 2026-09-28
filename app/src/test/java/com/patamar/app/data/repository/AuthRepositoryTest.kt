package com.patamar.app.data.repository

import com.patamar.app.core.security.AuthStore
import com.patamar.app.core.security.PasswordHasher
import com.patamar.app.core.utils.Constants
import com.patamar.app.data.local.db.UserDao
import com.patamar.app.data.model.User
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.security.MessageDigest

private class FakeUserDao : UserDao {
    val users = mutableListOf<User>()
    override suspend fun insert(user: User) { users += user }
    override suspend fun findByEmail(email: String) = users.firstOrNull { it.email == email }
    override suspend fun findById(userId: String) = users.firstOrNull { it.id == userId }
    override suspend fun countByEmail(email: String) = users.count { it.email == email }
}

private class FakeAuthStore : AuthStore {
    var sessionUserId: String? = null
    var sessionIsGuest: Boolean? = null
    private val fails = mutableMapOf<String, Int>()
    private val locks = mutableMapOf<String, Long>()
    override fun saveSession(userId: String, isGuest: Boolean) { sessionUserId = userId; sessionIsGuest = isGuest }
    override fun getLoginFailCount(email: String) = fails[email] ?: 0
    override fun getLoginLockUntil(email: String) = locks[email] ?: 0L
    override fun saveLoginFailure(email: String, failCount: Int, lockUntil: Long) {
        fails[email] = failCount; locks[email] = lockUntil
    }
    override fun clearLoginAttempts(email: String) { fails.remove(email); locks.remove(email) }
}

class AuthRepositoryTest {

    private lateinit var dao: FakeUserDao
    private lateinit var store: FakeAuthStore
    private var now = 1_000_000L
    private lateinit var repo: AuthRepository

    @Before fun setUp() {
        dao = FakeUserDao()
        store = FakeAuthStore()
        now = 1_000_000L
        repo = AuthRepository(dao, store) { now }
    }

    private fun request(
        name: String = "João da Silva",
        email: String = "Joao@Teste.com",
        password: String = "Abcdef12",
        confirm: String = password,
        terms: Boolean = true
    ) = RegisterRequest(name, email, password, confirm, terms)

    private fun register(req: RegisterRequest = request()) = runBlocking { repo.register(req) }
    private fun login(email: String, password: String) = runBlocking { repo.login(email, password) }
    private fun invalid(result: RegisterResult) = (result as RegisterResult.Invalid).errors

    // ---------- cadastro ----------

    @Test fun `cadastro valido cria conta normalizada, hasheia a senha e abre sessao`() {
        val result = register()
        val user = (result as RegisterResult.Success).user
        assertEquals("joao@teste.com", user.email)
        assertEquals("João da Silva", user.name)
        assertNotEquals("Abcdef12", user.passwordHash)
        assertTrue(PasswordHasher.verify("Abcdef12", user.passwordHash, user.salt))
        assertEquals(user.id, store.sessionUserId)
        assertEquals(false, store.sessionIsGuest)
        assertEquals(listOf(user), dao.users)
    }

    @Test fun `cadastro normaliza espacos e caixa`() {
        val user = (register(request(name = "  Ana   Maria ", email = "  ANA@Mail.COM  ")) as RegisterResult.Success).user
        assertEquals("Ana Maria", user.name)
        assertEquals("ana@mail.com", user.email)
    }

    @Test fun `cadastro com e-mail ja usado e recusado mesmo com caixa diferente`() {
        register()
        val errors = invalid(register(request(name = "Outra Pessoa", email = "JOAO@teste.COM")))
        assertEquals("Este e-mail já está cadastrado", errors.email)
        assertEquals(1, dao.users.size)
    }

    @Test fun `cadastro recusado nao abre sessao nem grava`() {
        invalid(register(request(email = "invalido")))
        assertNull(store.sessionUserId)
        assertTrue(dao.users.isEmpty())
    }

    @Test fun `cadastro devolve todos os erros de uma vez`() {
        val e = invalid(register(RegisterRequest("J1", "x@", "abc", "abd", false)))
        assertNotNull(e.name)
        assertNotNull(e.email)
        assertNotNull(e.password)
        assertEquals("As senhas não coincidem", e.confirmPassword)
        assertEquals("Aceite os termos para continuar", e.terms)
    }

    @Test fun `cadastro valida cada campo`() {
        assertNotNull(invalid(register(request(name = "Jo"))).name)
        assertNotNull(invalid(register(request(name = "Joao9"))).name)
        assertNotNull(invalid(register(request(email = "a@b"))).email)
        assertEquals("Mínimo 8 caracteres", invalid(register(request(password = "Ab1"))).password)
        assertEquals("Inclua ao menos uma letra maiúscula", invalid(register(request(password = "abcdef12"))).password)
        assertEquals("Inclua ao menos um número", invalid(register(request(password = "Abcdefgh"))).password)
        assertEquals("As senhas não coincidem", invalid(register(request(confirm = "Abcdef13"))).confirmPassword)
        assertEquals("Aceite os termos para continuar", invalid(register(request(terms = false))).terms)
        assertTrue(dao.users.isEmpty())
    }

    @Test fun `senha e usada exatamente como digitada`() {
        val user = (register(request(password = "Abcdef12")) as RegisterResult.Success).user
        assertFalse(PasswordHasher.verify("Abcdef12 ", user.passwordHash, user.salt))
    }

    // ---------- login ----------

    @Test fun `login funciona com o e-mail em qualquer caixa e com espacos`() {
        register(request(email = "Joao@Teste.com"))
        store.sessionUserId = null

        val result = login("  JOAO@teste.com ", "Abcdef12")
        assertTrue(result is LoginResult.Success)
        assertEquals("joao@teste.com", (result as LoginResult.Success).user.email)
        assertEquals(result.user.id, store.sessionUserId)
    }

    @Test fun `login com senha errada nao abre sessao`() {
        register()
        store.sessionUserId = null
        assertEquals(LoginResult.WrongCredentials(2), login("joao@teste.com", "Abcdef13"))
        assertNull(store.sessionUserId)
    }

    @Test fun `login com e-mail inexistente da a mesma resposta que senha errada`() {
        assertEquals(LoginResult.WrongCredentials(2), login("naoexiste@teste.com", "Abcdef12"))
    }

    @Test fun `login valida os campos antes de consultar`() {
        assertEquals(LoginResult.InvalidInput("E-mail inválido", null), login("abc", "x"))
        assertEquals(LoginResult.InvalidInput("Informe o e-mail", "Informe a senha"), login("", ""))
        assertEquals(LoginResult.InvalidInput(null, "Informe a senha"), login("a@b.co", ""))
    }

    @Test fun `login nao trata senha com espaco como se fosse sem espaco`() {
        register()
        assertTrue(login("joao@teste.com", "Abcdef12 ") is LoginResult.WrongCredentials)
    }

    @Test fun `apos 3 erros a conta e bloqueada e ate a senha certa e recusada`() {
        register()
        store.sessionUserId = null
        assertEquals(LoginResult.WrongCredentials(2), login("joao@teste.com", "Errada111"))
        assertEquals(LoginResult.WrongCredentials(1), login("joao@teste.com", "Errada222"))
        assertEquals(LoginResult.Locked(30), login("joao@teste.com", "Errada333"))

        now += 10_000
        assertEquals(LoginResult.Locked(20), login("joao@teste.com", "Abcdef12"))
        assertNull(store.sessionUserId)
    }

    @Test fun `bloqueio acaba depois de 30s e o contador recomeca`() {
        register()
        repeat(Constants.MAX_LOGIN_ATTEMPTS) { login("joao@teste.com", "Errada111") }
        now += Constants.LOGIN_LOCKOUT_MS
        assertEquals(LoginResult.WrongCredentials(2), login("joao@teste.com", "Errada111"))
        assertTrue(login("joao@teste.com", "Abcdef12") is LoginResult.Success)
    }

    @Test fun `bloqueio e por e-mail e nao afeta outras contas`() {
        register(request(email = "a@teste.com"))
        register(request(name = "Bia Souza", email = "b@teste.com"))
        repeat(Constants.MAX_LOGIN_ATTEMPTS) { login("a@teste.com", "Errada111") }
        assertTrue(login("a@teste.com", "Abcdef12") is LoginResult.Locked)
        assertTrue(login("b@teste.com", "Abcdef12") is LoginResult.Success)
    }

    @Test fun `bloqueio sobrevive a novo repositorio (recriacao de tela ou processo)`() {
        register()
        repeat(Constants.MAX_LOGIN_ATTEMPTS) { login("joao@teste.com", "Errada111") }
        val novo = AuthRepository(dao, store) { now }
        assertTrue(runBlocking { novo.login("joao@teste.com", "Abcdef12") } is LoginResult.Locked)
    }

    @Test fun `login certo zera o contador de erros`() {
        register()
        login("joao@teste.com", "Errada111")
        login("joao@teste.com", "Errada222")
        assertTrue(login("joao@teste.com", "Abcdef12") is LoginResult.Success)
        assertEquals(LoginResult.WrongCredentials(2), login("joao@teste.com", "Errada111"))
    }

    @Test fun `usuario legado com hash sha256 continua entrando`() {
        val salt = "patamar_beta_salt_2024"
        val legacy = MessageDigest.getInstance("SHA-256")
            .digest("$salt:Teste@123".toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
        dao.users += User("usr_test_001", "Usuário Teste", "teste@patamar.app", legacy, salt)

        val result = login("teste@patamar.app", "Teste@123")
        assertTrue(result is LoginResult.Success)
        assertEquals("usr_test_001", store.sessionUserId)
    }

    @Test fun `conta recem cadastrada consegue entrar depois`() {
        register(request(name = "Maria Clara", email = "Maria.Clara@Gmail.com", password = "Senha@2024"))
        store.sessionUserId = null
        val result = login("maria.clara@gmail.com", "Senha@2024")
        assertTrue(result is LoginResult.Success)
        assertEquals("Maria Clara", (result as LoginResult.Success).user.name)
    }
}
