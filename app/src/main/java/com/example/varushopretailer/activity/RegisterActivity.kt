package com.example.varushopretailer.activity

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.varushopretailer.databinding.ActivityRegisterBinding
import com.example.varushopretailer.helper.BaseActivity
import com.example.varushopretailer.viewmodal.RegisterViewModel
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class RegisterActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var selectedImageFile: File? = null

    private val viewModel: RegisterViewModel by viewModels()

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                binding.ivProfilePreview.setImageURI(it)
                selectedImageFile = uriToFile(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClicks()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            binding.registerProgress.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnRegister.isEnabled = !loading
        }

        viewModel.message.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) showToast(msg)
        }

        viewModel.registerSuccess.observe(this) { success ->
            if (success) {
                showToast("Registration Successful")
                finish()
            }
        }
    }

    private fun setupClicks() {
        binding.btnPickImage.setOnClickListener { checkPermissionAndPickImage() }
        binding.tvLogin.setOnClickListener { finish() }
        binding.btnRegister.setOnClickListener { validateAndRegister() }
    }

    private fun validateAndRegister() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etCnfPassword.text.toString().trim()
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        when {
            name.isEmpty() || email.isEmpty() || password.isEmpty() -> {
                showToast("Fill all fields")
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.tilEmail.error = "Invalid Email"
            }

            password.length < 6 -> {
                binding.tilPassword.error = "Minimum 6 characters required"
            }

            password != confirmPassword -> {
                showToast("Passwords do not match")
            }

            else -> {
                viewModel.register(name, email, password, selectedImageFile)
            }
        }
    }

    private fun checkPermissionAndPickImage() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        PermissionX.init(this).permissions(permission).onExplainRequestReason { scope, deniedList ->
            scope.showRequestReasonDialog(
                deniedList, "Core fundamental are based on this", "OK", "Cancel"
            )
        }.request { allGranted, _, _ ->
            if (allGranted) {
                imagePicker.launch("image/*")
            } else {
                showToast("Permission denied. Cannot pick image.")
            }
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(cacheDir, "temp_profile_${System.currentTimeMillis()}.jpg")
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}