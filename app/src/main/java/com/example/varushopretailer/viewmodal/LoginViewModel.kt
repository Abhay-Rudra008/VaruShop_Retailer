package com.example.varushopretailer.viewmodal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.varushopretailer.modal.LoginResponse
import com.example.varushopretailer.stats.Resource
import com.example.varushopretailer.stats.RetailerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: RetailerRepository
) : BaseViewModel() {

    private val _loginResponse = MutableLiveData<LoginResponse>()
    val loginResponse: LiveData<LoginResponse> = _loginResponse

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _error.value = true
            _message.value = "Email and Password cannot be empty"
            return
        }

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            val result = repository.loginRetailer(email, pass)

            when (result) {
                is Resource.Success -> {
                    result.data?.let { response ->
                        if (response.success) {
                            _loginResponse.value = response
                            _error.value = false
                        } else {
                            _message.value = response.message ?: "Login failed"
                            _error.value = true
                        }
                    } ?: run {
                        _message.value = "Unexpected empty response from server"
                        _error.value = true
                    }
                }

                is Resource.Error -> {
                    _message.value = result.message ?: "An unknown error occurred"
                    _error.value = true
                }

                else -> {}
            }
            _isLoading.value = false
        }
    }
}