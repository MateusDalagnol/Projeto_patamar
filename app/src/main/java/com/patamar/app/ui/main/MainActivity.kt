package com.patamar.app.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.patamar.app.R
import com.patamar.app.core.security.EncryptedPrefsManager
import com.patamar.app.data.repository.EventRepository
import com.patamar.app.databinding.ActivityMainBinding
import com.patamar.app.ui.auth.AuthActivity
import com.patamar.app.ui.explore.ExploreFragment
import com.patamar.app.ui.map.MapFragment
import com.patamar.app.ui.profile.ProfileFragment
import com.patamar.app.ui.saved.SavedFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var prefsManager: EncryptedPrefsManager
    @Inject lateinit var eventRepository: EventRepository

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private val isGuest get() = prefsManager.isGuestSession()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            showFragment(MapFragment())
        }

        // Sem a "pílula" atrás do ícone ativo (indicador do Material 3).
        binding.bottomNav.isItemActiveIndicatorEnabled = false

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> { showFragment(MapFragment()); true }
                R.id.nav_explore -> { showFragment(ExploreFragment()); true }
                R.id.nav_saved -> {
                    if (isGuest) {
                        Snackbar.make(binding.root, "Crie uma conta para salvar eventos", Snackbar.LENGTH_LONG)
                            .setAction("Entrar") { navigateToAuth() }
                            .show()
                        false
                    } else {
                        showFragment(SavedFragment()); true
                    }
                }
                R.id.nav_profile -> { showFragment(ProfileFragment()); true }
                else -> false
            }
        }

        observeSavedBadge()
        observeFocusRequest()
    }

    private fun observeFocusRequest() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.pendingFocusEventId.collect { id ->
                    if (id != null && binding.bottomNav.selectedItemId != R.id.nav_map) {
                        binding.bottomNav.selectedItemId = R.id.nav_map
                    }
                }
            }
        }
    }

    private fun observeSavedBadge() {
        val userId = prefsManager.getSessionUserId() ?: return
        if (isGuest) return
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                eventRepository.observeSavedCount(userId).collect { count ->
                    val badge = binding.bottomNav.getOrCreateBadge(R.id.nav_saved)
                    badge.isVisible = count > 0
                    badge.number = count
                }
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun navigateToAuth() {
        startActivity(AuthActivity.intent(this))
    }
}
