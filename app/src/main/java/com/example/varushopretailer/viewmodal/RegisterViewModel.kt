package com.example.varushopretailer.viewmodal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.varushopretailer.stats.Resource
import com.example.varushopretailer.stats.RetailerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: RetailerRepository
) : BaseViewModel() {

    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> = _registerSuccess

    fun register(name: String, email: String, pass: String, imageFile: File?) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            val result = repository.registerRetailer(name, email, pass, imageFile)

            when (result) {
                is Resource.Success -> {
                    result.data?.let { apiResponse ->
                        _message.value = apiResponse.message ?: "Registration successful"

                        if (apiResponse.success) {
                            _registerSuccess.value = true
                            _error.value = false
                        } else {
                            _error.value = true
                        }
                    } ?: run {
                        _message.value = "Unexpected empty response from server."
                        _error.value = true
                    }
                }

                is Resource.Error -> {
                    _message.value = result.message
                    _error.value = true
                }
                is Resource.Loading -> {
                }
            }

            _isLoading.value = false

            withContext(Dispatchers.IO) {
                imageFile?.let { if (it.exists()) it.delete() }
            }
        }
    }
}