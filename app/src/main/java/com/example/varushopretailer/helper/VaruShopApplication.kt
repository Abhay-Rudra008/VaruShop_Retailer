package com.example.varushopretailer.helper

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VaruShopApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val prefManager = LangPrefManager(this)

        val isDarkMode = prefManager.isDarkMode()
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}