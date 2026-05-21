package com.example.varushopretailer.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.varushopretailer.databinding.ActivityLoginBinding
import com.example.varushopretailer.helper.LangPrefManager
import com.example.varushopretailer.viewmodal.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    @Inject
    lateinit var prefManager: LangPrefManager

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.login(email, password)
        }
        binding.tvSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.loginProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
        }

        viewModel.message.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.loginResponse.observe(this) { response ->
            if (response.success) {

                response.data?.let { loginData ->
                    val token = loginData.token
                    val refreshToken = loginData.refreshToken

                    if (!token.isNullOrEmpty()) {
                        prefManager.saveToken(token)
                    }
                    if (!refreshToken.isNullOrEmpty()) {
                        prefManager.saveRefreshToken(refreshToken)
                    }

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } ?: run {
                    Toast.makeText(this, "Login data missing from server", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

}