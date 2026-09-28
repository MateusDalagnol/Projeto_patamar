package com.patamar.app.ui.onboarding

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.patamar.app.R
import com.patamar.app.core.security.EncryptedPrefsManager
import com.patamar.app.databinding.ActivityOnboardingBinding
import com.patamar.app.ui.auth.AuthActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {

    @Inject lateinit var prefsManager: EncryptedPrefsManager

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var adapter: OnboardingAdapter
    private val dotViews = mutableListOf<View>()

    private val pages = listOf(
        OnboardingPage(
            "O que tá rolando perto de você, agora.",
            "Shows, feiras, arte e balada — tudo num mapa, sem precisar procurar.",
            R.drawable.ic_map_pin
        ),
        OnboardingPage(
            "Filtre pelo que você curte.",
            "Escolha as categorias e veja só o que faz sentido pra você.",
            R.drawable.ic_sliders
        ),
        OnboardingPage(
            "Salve e não perca nada.",
            "Favoritos e alertas do que tá chegando perto.",
            R.drawable.ic_star
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = OnboardingAdapter(pages)
        binding.viewPager.adapter = adapter
        setupDots()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
                updateButtonLabel(position)
            }
        })

        binding.btnContinue.setOnClickListener {
            val next = binding.viewPager.currentItem + 1
            if (next < pages.size) {
                binding.viewPager.currentItem = next
            } else {
                finishOnboarding()
            }
        }

        updateButtonLabel(0)
    }

    private fun setupDots() {
        binding.dotsContainer.removeAllViews()
        dotViews.clear()
        pages.indices.forEach { index ->
            val dot = View(this).apply {
                layoutParams = ViewGroup.MarginLayoutParams(dpToPx(16), dpToPx(4)).apply {
                    marginEnd = dpToPx(6)
                }
                background = GradientDrawable().apply {
                    cornerRadius = dpToPx(2).toFloat()
                    setColor(
                        if (index == 0) getColorCompat(R.color.text_primary)
                        else getColorCompat(R.color.dot_inactive)
                    )
                }
            }
            dotViews.add(dot)
            binding.dotsContainer.addView(dot)
        }
    }

    private fun updateDots(selected: Int) {
        dotViews.forEachIndexed { index, dot ->
            val drawable = dot.background as GradientDrawable
            drawable.setColor(
                if (index == selected) getColorCompat(R.color.text_primary)
                else getColorCompat(R.color.dot_inactive)
            )
        }
    }

    private fun updateButtonLabel(position: Int) {
        binding.btnContinue.text = if (position == pages.size - 1) {
            getString(R.string.onboarding_start)
        } else {
            getString(R.string.onboarding_continue)
        }
    }

    private fun finishOnboarding() {
        prefsManager.setOnboardingComplete(true)
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun getColorCompat(resId: Int) = androidx.core.content.ContextCompat.getColor(this, resId)
}
