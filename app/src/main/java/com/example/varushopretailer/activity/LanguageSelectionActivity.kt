package com.example.varushopretailer.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.varushopretailer.adapter.LanguageAdapter
import com.example.varushopretailer.databinding.ActivityLanguageSelectionBinding
import com.example.varushopretailer.helper.BaseActivity
import com.example.varushopretailer.helper.LangPrefManager
import com.example.varushopretailer.modal.Language
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class LanguageSelectionActivity : BaseActivity() {

    private lateinit var binding: ActivityLanguageSelectionBinding
    private var selectedLocale: String? = null
    private lateinit var languageAdapter: LanguageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        val languages = listOf(
            Language("English", "English", "🇺🇸", "en"),
            Language("Hindi", "हिन्दी", "🇮🇳", "hi"),
            Language("Spanish", "Español", "🇪🇸", "es"),
            Language("French", "Français", "🇫🇷", "fr"),
            Language("German", "Deutsch", "🇩🇪", "de")
        )

        val currentAppLocale = AppCompatDelegate.getApplicationLocales()[0]?.language
        val systemLocale = Locale.getDefault().language
        val localeToSelect = currentAppLocale ?: systemLocale

        var matchFound = false
        languages.forEach {
            if (it.code == localeToSelect) {
                it.isSelected = true
                selectedLocale = it.code
                matchFound = true
            }
        }

        if (!matchFound) {
            languages.find { it.code == "en" }?.isSelected = true
            selectedLocale = "en"
        }

        languageAdapter = LanguageAdapter(languages) { selectedLanguage ->
            selectedLocale = selectedLanguage.code
            binding.btnContinue.isEnabled = true
        }

        binding.rvLanguages.apply {
            layoutManager = LinearLayoutManager(this@LanguageSelectionActivity)
            adapter = languageAdapter
            val selectedIndex = languages.indexOfFirst { it.isSelected }
            if (selectedIndex != -1) scrollToPosition(selectedIndex)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                languageAdapter.filter(query)

                if (languageAdapter.itemCount == 0) {
                    binding.layoutNoResults.visibility = View.VISIBLE
                    binding.rvLanguages.visibility = View.GONE
                } else {
                    binding.layoutNoResults.visibility = View.GONE
                    binding.rvLanguages.visibility = View.VISIBLE
                }
            }
        })

        binding.btnContinue.setOnClickListener {
            hideKeyboard()

            selectedLocale?.let { code ->

                val prefManager = LangPrefManager(this)
                prefManager.saveLanguage(code)
                prefManager.setLanguageSelected(true)

                val appLocale = LocaleListCompat.forLanguageTags(code)
                AppCompatDelegate.setApplicationLocales(appLocale)

                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        }
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}