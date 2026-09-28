package com.patamar.app.core.extensions

import android.util.Patterns

fun String.isValidEmail(): Boolean =
    isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.hasDigit(): Boolean = any { it.isDigit() }

fun String.hasUpperCase(): Boolean = any { it.isUpperCase() }
