package com.patamar.app.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.patamar.app.data.model.FilterPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedPrefsManager @Inject constructor(@ApplicationContext context: Context) : AuthStore {

    private val gson = Gson()

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "patamar_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun setOnboardingComplete(done: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, done).apply()
    }

    fun isOnboardingComplete(): Boolean = prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)

    override fun saveSession(userId: String, isGuest: Boolean) {
        prefs.edit()
            .putString(KEY_SESSION_USER_ID, userId)
            .putBoolean(KEY_SESSION_IS_GUEST, isGuest)
            .apply()
    }

    fun getSessionUserId(): String? = prefs.getString(KEY_SESSION_USER_ID, null)

    fun isGuestSession(): Boolean = prefs.getBoolean(KEY_SESSION_IS_GUEST, false)

    fun hasActiveSession(): Boolean = getSessionUserId() != null

    fun saveFilters(filters: FilterPreferences) {
        prefs.edit().putString(KEY_FILTERS, gson.toJson(filters)).apply()
    }

    fun getFilters(): FilterPreferences {
        val json = prefs.getString(KEY_FILTERS, null) ?: return FilterPreferences()
        return try {
            gson.fromJson(json, FilterPreferences::class.java) ?: FilterPreferences()
        } catch (e: Exception) {
            FilterPreferences()
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun areNotificationsEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)

    // Newsletter/resumo de eventos: 1x por dia, na primeira vez que o app abre
    // naquele dia — não a cada sessão/visita ao Mapa.
    fun shouldShowDailyNewsletter(): Boolean {
        val lastShown = prefs.getString(KEY_NEWSLETTER_LAST_SHOWN_DATE, null)
        return lastShown != LocalDate.now().toString()
    }

    fun markNewsletterShownToday() {
        prefs.edit().putString(KEY_NEWSLETTER_LAST_SHOWN_DATE, LocalDate.now().toString()).apply()
    }

    // Tentativas de login por e-mail (já normalizado). Persistidas para o bloqueio
    // sobreviver a recriação da tela e à morte do processo.
    override fun getLoginFailCount(email: String): Int = prefs.getInt("$KEY_LOGIN_FAILS:$email", 0)

    override fun getLoginLockUntil(email: String): Long = prefs.getLong("$KEY_LOGIN_LOCK:$email", 0L)

    override fun saveLoginFailure(email: String, failCount: Int, lockUntil: Long) {
        prefs.edit()
            .putInt("$KEY_LOGIN_FAILS:$email", failCount)
            .putLong("$KEY_LOGIN_LOCK:$email", lockUntil)
            .apply()
    }

    override fun clearLoginAttempts(email: String) {
        prefs.edit()
            .remove("$KEY_LOGIN_FAILS:$email")
            .remove("$KEY_LOGIN_LOCK:$email")
            .apply()
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_SESSION_USER_ID)
            .remove(KEY_SESSION_IS_GUEST)
            .apply()
        // Não limpar onboarding_complete nem filtros ao sair
    }

    companion object {
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
        private const val KEY_SESSION_USER_ID = "session_user_id"
        private const val KEY_SESSION_IS_GUEST = "session_is_guest"
        private const val KEY_FILTERS = "filters"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_NEWSLETTER_LAST_SHOWN_DATE = "newsletter_last_shown_date"
        private const val KEY_LOGIN_FAILS = "login_fails"
        private const val KEY_LOGIN_LOCK = "login_lock_until"
    }
}
