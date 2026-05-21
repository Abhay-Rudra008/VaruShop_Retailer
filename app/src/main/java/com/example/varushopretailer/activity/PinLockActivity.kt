package com.example.varushopretailer.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.varushopretailer.R
import com.example.varushopretailer.databinding.ActivityPinLockBinding
import com.example.varushopretailer.helper.BaseActivity
import com.example.varushopretailer.helper.LangPrefManager
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PinLockActivity : BaseActivity() {

    private lateinit var binding: ActivityPinLockBinding
    private lateinit var pref: LangPrefManager
    private var inputPin = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinLockBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pref = LangPrefManager(this)
        if (pref.isBiometricEnabled()) {
            binding.keypadGrid.visibility = View.INVISIBLE
            binding.layoutDots.visibility = View.INVISIBLE
            showBiometricPrompt()
        }

        setupKeypadListeners()

        binding.btnDelete.setOnClickListener {
            if (inputPin.isNotEmpty()) {
                inputPin = inputPin.dropLast(1)
                updatePinDots()
            }
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt =
            BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    startActivity(Intent(this@PinLockActivity, MainActivity::class.java))
                    finish()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    binding.keypadGrid.visibility = View.VISIBLE
                    binding.layoutDots.visibility = View.VISIBLE
                    Toast.makeText(
                        this@PinLockActivity, "Please enter your PIN", Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder().setTitle("Unlock Varu Shop")
            .setSubtitle("Log in using your biometric credential").setNegativeButtonText("Use PIN")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun setupKeypadListeners() {
        // Loop through the GridLayout children
        for (i in 0 until binding.keypadGrid.childCount) {
            val view = binding.keypadGrid.getChildAt(i)
            if (view is Button && view.text.toString().isNotEmpty()) {
                view.setOnClickListener {
                    onNumberPressed(view.text.toString())
                }
            }
        }
    }

    private fun onNumberPressed(number: String) {
        if (inputPin.length < 4) {
            inputPin += number
            updatePinDots()

            if (inputPin.length == 4) {
                binding.root.postDelayed({ verifyPin() }, 200)
            }
        }
    }

    private fun updatePinDots() {
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3, binding.dot4)
        dots.forEachIndexed { index, view ->
            val bg =
                if (index < inputPin.length) R.drawable.bg_pin_dot_active else R.drawable.bg_pin_dot_inactive
            view.setBackgroundResource(bg)
        }
    }

    private fun verifyPin() {
        val savedPin = pref.getPin()
        if (inputPin == savedPin) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            vibrateError()
            // Trigger Shake Animation
            val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
            binding.layoutDots.startAnimation(shake)

            inputPin = ""
            updatePinDots()
            Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show()
        }
    }

    private fun vibrateError() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION") vibrator.vibrate(300)
        }
    }
}