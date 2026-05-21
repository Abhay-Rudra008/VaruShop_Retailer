package com.example.varushopretailer.viewmodal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class BaseViewModel : ViewModel() {

    protected val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    protected val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    protected val _error = MutableLiveData<Boolean?>()
    val error: LiveData<Boolean?> = _error

    fun clearMessage() {
        _message.value = ""
    }

    fun clearError() {
        _error.value = null
    }
}