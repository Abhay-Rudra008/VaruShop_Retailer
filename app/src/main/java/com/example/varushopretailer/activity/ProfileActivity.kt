package com.example.varushopretailer.activity


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.varushopretailer.R
import com.example.varushopretailer.databinding.ActivityProfileBinding
import com.example.varushopretailer.helper.BaseActivity
import com.example.varushopretailer.helper.LangPrefManager
import com.example.varushopretailer.viewmodal.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale


@AndroidEntryPoint
class ProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var pref: LangPrefManager

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        pref = LangPrefManager(this)

        AppCompatDelegate.setDefaultNightMode(
            if (pref.isDarkMode()) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        applySavedConfig()

        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()

        if (pref.isLoggedIn()) {
            viewModel.loadProfileData()
        } else {
            handleSessionExpired()
        }
    }

    private fun setupUI() {
        setupEdgeToEdge()
        setupToolbar()
        initStatLabels()
        initMenuRows()
        updateAppVersion()
        setupClickListeners()
    }

    private fun setupEdgeToEdge() {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.appBarLayout.setPadding(0, systemBars.top, 0, 0)
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateAppVersion() {
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName
            binding.txtAppVersion.text = "Version $version"
        } catch (e: Exception) {
            binding.txtAppVersion.text = "Version 1.0.0"
        }
    }

    private fun setupClickListeners() {
        // Logout
        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(this).setTitle("Logout")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Logout") { _, _ ->
                    pref.clearSession()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }.setNegativeButton("Cancel", null).show()
        }

    }

    private fun initStatLabels() {
        binding.statRevenue.txtStatLabel.text = getString(R.string.label_today_sales)
        binding.statPendingOrders.txtStatLabel.text = getString(R.string.label_pending)
        binding.statLowStock.txtStatLabel.text = getString(R.string.label_low_stock)
    }

    private fun initMenuRows() {
        setupLanguageRow(binding.menuLanguage.root)

        // Security Rows
        setupSecurityRow(
            binding.menuBiometric.root,
            R.drawable.ic_fingerprint,
            getString(R.string.menu_biometric),
            "BIO"
        )
        setupSecurityRow(
            binding.menuPin.root, R.drawable.ic_lock, getString(R.string.menu_pin), "PIN"
        )

        // Support Rows
        setupStandardRow(
            binding.menuPrivacy.root, R.drawable.ic_privacy_tip, getString(R.string.menu_privacy)
        ) {
            openWebLink("https://varushop.com/privacy")
        }
        setupStandardRow(binding.menuHelp.root, R.drawable.ic_help, getString(R.string.menu_help)) {
            openWebLink("https://varushop.com/support")
        }
        setupStandardRow(
            binding.menuSecurity.root, R.drawable.ic_security, getString(R.string.menu_security)
        ) {
            openWebLink("https://varushop.com/support")
        }
    }

    private fun setupLanguageRow(view: View) {
        val icon = view.findViewById<ImageView>(R.id.imgMenuIcon)
        val title = view.findViewById<TextView>(R.id.txtMenuTitle)
        val txtValue = view.findViewById<TextView>(R.id.txtSelectedValue)

        icon.setImageResource(R.drawable.ic_language)
        title.text = getString(R.string.menu_language)
        txtValue.visibility = View.VISIBLE
        txtValue.text = when (pref.getLanguage()) {
            "hi" -> "हिंदी"
            "es" -> "Español"
            else -> "English"
        }

        view.setOnClickListener {
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupStandardRow(view: View, iconRes: Int, titleText: String, action: () -> Unit) {
        view.findViewById<ImageView>(R.id.imgMenuIcon).setImageResource(iconRes)
        view.findViewById<TextView>(R.id.txtMenuTitle).text = titleText
        view.findViewById<TextView>(R.id.txtSelectedValue)?.visibility = View.GONE

        view.setOnClickListener {
            action()
        }
    }

    private fun openWebLink(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No browser found to open this link", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun observeViewModel() {
        viewModel.profileData.observe(this) { user ->
            user?.let {
                binding.txtStoreName.text = it.name
                binding.txtEmail.text = it.email

            }
        }

        viewModel.dashboardData.observe(this) { data ->
            val salesValue = data.today_sales?.toDoubleOrNull() ?: 0.0
            binding.statRevenue.txtStatValue.text = if (salesValue > 0) {
                "₹${String.format("%,.0f", salesValue)}"
            } else {
                "₹0"
            }
            binding.statPendingOrders.txtStatValue.text = data.pending_orders.toString()
        }

        viewModel.lowStockCount.observe(this) { count ->
            binding.statLowStock.txtStatValue.text = count.toString()
            binding.statLowStock.txtStatValue.setTextColor(
                if (count > 0) "#F44336".toColorInt() else Color.GRAY
            )
        }


        //  Revenue Summary
        viewModel.latestRevenue.observe(this) { rev ->
            rev?.let {
                val amount = it.revenue?.toDoubleOrNull() ?: 0.0
                val formattedAmount = String.format("%,.0f", amount)
                binding.txtRevenueSummary.text = "Last Month (${it.month}): ₹$formattedAmount"
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupSecurityRow(view: View, icon: Int, title: String, type: String) {
        val switch = view.findViewById<SwitchMaterial>(R.id.switchWidget)
        view.findViewById<ImageView>(R.id.imgMenuIcon).setImageResource(icon)
        view.findViewById<TextView>(R.id.txtMenuTitle).text = title
        view.findViewById<ImageView>(R.id.imgArrow).visibility = View.GONE
        switch.visibility = View.VISIBLE

        switch.isChecked = if (type == "BIO") pref.isBiometricEnabled() else pref.isPinEnabled()

        view.setOnClickListener {
            val currentlyEnabled =
                if (type == "BIO") pref.isBiometricEnabled() else pref.isPinEnabled()
            if (currentlyEnabled) {
                if (type == "BIO") pref.setBiometricEnabled(false) else pref.setPinEnabled(false)
                switch.isChecked = false
            } else {
                handleSecurityActivation(type)
            }
        }
    }

    private fun handleSecurityActivation(type: String) {
        if (type == "BIO") {
            verifyAndEnableBiometric()
        } else {
            showSetPinDialog()
        }
    }

    private fun verifyAndEnableBiometric() {
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = androidx.biometric.BiometricPrompt(
            this, executor, object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                    pref.setBiometricEnabled(true)
                    pref.setPinEnabled(false)
                    refreshSecurityUI()
                }
            })
        val info = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.verify_identity))
            .setNegativeButtonText(getString(R.string.cancel)).build()
        prompt.authenticate(info)
    }

    private fun showSetPinDialog() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            filters = arrayOf(android.text.InputFilter.LengthFilter(4))
        }
        MaterialAlertDialogBuilder(this).setTitle("Set 4-Digit PIN").setView(input)
            .setPositiveButton("Save") { _, _ ->
                if (input.text.length == 4) {
                    pref.savePin(input.text.toString())
                    pref.setPinEnabled(true)
                    pref.setBiometricEnabled(false)
                    refreshSecurityUI()
                }
            }.show()
    }

    private fun refreshSecurityUI() {
        binding.menuBiometric.root.findViewById<SwitchMaterial>(R.id.switchWidget).isChecked =
            pref.isBiometricEnabled()
        binding.menuPin.root.findViewById<SwitchMaterial>(R.id.switchWidget).isChecked =
            pref.isPinEnabled()
    }


    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun applySavedConfig() {
        val locale = Locale(pref.getLanguage())
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun handleSessionExpired() {
        pref.clearSession()
        startActivity(Intent(this, LoginActivity::class.java))
        finishAffinity()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_profile, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_theme -> {
                val isDark = pref.isDarkMode()
                pref.setDarkMode(!isDark)

                AppCompatDelegate.setDefaultNightMode(
                    if (!isDark) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )

                recreate()
                true
            }

            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}