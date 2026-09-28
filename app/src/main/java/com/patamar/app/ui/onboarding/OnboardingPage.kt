package com.patamar.app.ui.onboarding

import androidx.annotation.DrawableRes

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    @DrawableRes val iconRes: Int
)
