package com.patamar.app.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.patamar.app.databinding.ActivitySplashBinding
import com.patamar.app.ui.onboarding.OnboardingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({ navigateNext() }, SPLASH_DURATION_MS)
    }

    private fun navigateNext() {
        // A pedido: boas-vindas + login sempre aparecem, mesmo já tendo sido
        // vistos/logados antes (sem atalho por sessão/onboarding salvos).
        startActivity(Intent(this, OnboardingActivity::class.java))
        finish()
    }

    companion object {
        private const val SPLASH_DURATION_MS = 600L
    }
}
