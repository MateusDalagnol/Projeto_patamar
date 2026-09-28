package com.patamar.app.core.security

// O que a camada de autenticação precisa persistir (sessão + tentativas de login).
// Interface separada do EncryptedPrefsManager pra AuthRepository ser testável na JVM.
interface AuthStore {
    fun saveSession(userId: String, isGuest: Boolean = false)
    fun getLoginFailCount(email: String): Int
    fun getLoginLockUntil(email: String): Long
    fun saveLoginFailure(email: String, failCount: Int, lockUntil: Long)
    fun clearLoginAttempts(email: String)
}
