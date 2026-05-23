package com.example.varushopretailer.viewmodal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.varushopretailer.modal.ApiResponse
import com.example.varushopretailer.modal.Product
import com.example.varushopretailer.stats.Resource
import com.example.varushopretailer.stats.RetailerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageProductsViewModel @Inject constructor(
    private val repository: RetailerRepository
) : BaseViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    // ADDED: LiveData to track successful deletions
    private val _deleteSuccess = MutableLiveData<Boolean>()
    val deleteSuccess: LiveData<Boolean> = _deleteSuccess

    private var searchJob: Job? = null

    fun loadProducts(
        token: String, search: String? = null, filter: String? = null, isInitial: Boolean = false
    ) {
        searchJob?.cancel()

        if (isInitial) {
            _isLoading.value = true
        }
        _error.value = null

        searchJob = viewModelScope.launch {
            if (!search.isNullOrEmpty() && !isInitial) {
                delay(500L)
            }

            val result = repository.getMyProducts(search, filter)

            when (result) {
                is Resource.Success -> {
                    // Explicitly type casting to help the Kotlin compiler
                    val apiResponse: ApiResponse<List<Product>>? = result.data

                    if (apiResponse != null && apiResponse.success) {
                        _products.value = apiResponse.data ?: emptyList()
                        _error.value = false
                    } else {
                        _message.value = apiResponse?.message ?: "Failed to fetch products"
                        _error.value = true
                    }
                }

                is Resource.Error -> {
                    _error.value = true
                    _products.value = emptyList()
                    _message.value = result.message
                }

                else -> {}
            }

            if (isInitial) {
                _isLoading.value = false
            }
        }
    }

    // ADDED: The permanent delete function
    fun permanentlyDeleteProduct(token: String, productId: Int) {
        _isLoading.value = true
        _error.value = null

        // Resetting to false before making the call
        _deleteSuccess.value = false

        viewModelScope.launch {
            val result = repository.deleteProduct(productId)

            when (result) {
                is Resource.Success -> {
                    val apiResponse = result.data

                    if (apiResponse != null && apiResponse.success) {
                        _message.value = apiResponse.message ?: "Product deleted permanently."
                        _deleteSuccess.value = true
                        _error.value = false
                    } else {
                        _message.value = apiResponse?.message ?: "Failed to delete product."
                        _error.value = true
                    }
                }

                is Resource.Error -> {
                    _message.value = result.message
                    _error.value = true
                }

                is Resource.Loading -> {}
            }
            _isLoading.value = false
        }
    }
}