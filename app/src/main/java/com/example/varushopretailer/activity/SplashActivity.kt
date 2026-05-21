package com.example.varushopretailer.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.os.Handler
import android.os.Looper
import com.example.varushopretailer.databinding.ActivitySplashBinding
import com.example.varushopretailer.helper.BaseActivity
import com.example.varushopretailer.helper.LangPrefManager
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var pref: LangPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pref = LangPrefManager(this)

        binding.splashLayout.alpha = 0f
        binding.splashLayout.animate().alpha(1f).setDuration(800).start()

        Handler(Looper.getMainLooper()).postDelayed({
            navigateNext()
        }, 2000)
    }

    private fun navigateNext() {
        val intent = when {
            // First Priority: Language Selection
            !pref.isLanguageSelected() -> {
                Intent(this, LanguageSelectionActivity::class.java)
            }

            // Second Priority: Check if Logged In
            pref.getToken() == null -> {
                Intent(this, LoginActivity::class.java)
            }

            // Third Priority: Security Check
            pref.isPinEnabled() || pref.isBiometricEnabled() -> {
                Intent(this, PinLockActivity::class.java)
            }

            // DEFAULT: Security is OFF and User is Logged In
            else -> {
                Intent(this, MainActivity::class.java)
            }
        }

        startActivity(intent)
        finish()
    }
}