package com.example.varushopretailer.helper

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.varushopretailer.R
import com.example.varushopretailer.activity.LanguageSelectionActivity
import com.example.varushopretailer.activity.LoginActivity
import com.example.varushopretailer.activity.RegisterActivity
import com.example.varushopretailer.activity.SplashActivity
import kotlinx.coroutines.launch
import java.util.Locale

open class BaseActivity : AppCompatActivity() {

    lateinit var prefManager: LangPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefManager = LangPrefManager(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                SessionManager.logoutEvent.collect {
                    performLogout()
                }
            }
        }

        if (!isPublicPage() && !prefManager.isLoggedIn()) {
            performLogout()
        }
    }

    private fun performLogout() {
        prefManager.clearSession()

        // Show a message to the user
        Toast.makeText(this, getString(R.string.session_expired), Toast.LENGTH_LONG).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun isPublicPage(): Boolean {
        return this is LoginActivity || this is RegisterActivity || this is SplashActivity || this is LanguageSelectionActivity
    }

    override fun attachBaseContext(newBase: Context) {
        val lang = LangPrefManager(newBase).getLanguage()
        val locale = Locale(lang)
        Locale.setDefault(locale)

        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)

        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }
}