package com.example.varushopretailer.helper

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton


@Singleton
class LangPrefManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val pref: SharedPreferences =
        context.getSharedPreferences("VaruShopPref", Context.MODE_PRIVATE)


    fun saveToken(token: String) = pref.edit(commit = true) { putString("token", token) }

    fun getToken(): String? = pref.getString("token", null)

    fun saveRefreshToken(refreshToken: String) =
        pref.edit(commit = true) { putString("refresh_token", refreshToken) }

    fun getRefreshToken(): String? = pref.getString("refresh_token", null)


    fun isLoggedIn(): Boolean = !getToken().isNullOrEmpty()

    fun clearSession() {
        pref.edit(commit = true) {
            remove("token")
            remove("refresh_token")
        }
    }


    fun setLanguageSelected(value: Boolean) = pref.edit { putBoolean("language_selected", value) }
    fun isLanguageSelected(): Boolean = pref.getBoolean("language_selected", false)

    fun saveLanguage(languageCode: String) = pref.edit { putString("language_code", languageCode) }
    fun getLanguage(): String = pref.getString("language_code", "en") ?: "en"


    fun setDarkMode(isEnabled: Boolean) = pref.edit { putBoolean("is_dark_mode", isEnabled) }
    fun isDarkMode(): Boolean = pref.getBoolean("is_dark_mode", false)


    fun savePin(pin: String) = pref.edit { putString("app_pin", pin) }
    fun getPin(): String? = pref.getString("app_pin", null)

    fun setPinEnabled(enabled: Boolean) = pref.edit { putBoolean("pin_enabled", enabled) }
    fun isPinEnabled(): Boolean = pref.getBoolean("pin_enabled", false)

    fun setBiometricEnabled(enabled: Boolean) =
        pref.edit { putBoolean("biometric_enabled", enabled) }

    fun isBiometricEnabled(): Boolean = pref.getBoolean("biometric_enabled", false)
}