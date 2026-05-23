package com.example.varushopretailer.viewmodal


import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.varushopretailer.modal.ApiResponse
import com.example.varushopretailer.modal.Category
import com.example.varushopretailer.stats.Resource
import com.example.varushopretailer.stats.RetailerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: RetailerRepository
) : BaseViewModel() {

    private val _uploadSuccess = MutableLiveData<Boolean>()
    private val _updateSuccess = MutableLiveData<Boolean>()
    val uploadSuccess: LiveData<Boolean> = _uploadSuccess

    private val _productImages = MutableLiveData<List<String>>()
    val productImages: LiveData<List<String>> = _productImages

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    fun fetchCategories() {
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = repository.getCategories()) {
                is Resource.Success -> {
                    // Explicitly cast to help the compiler
                    val apiResponse: ApiResponse<List<Category>>? = result.data

                    if (apiResponse != null && apiResponse.success) {
                        _categories.value = apiResponse.data ?: emptyList()
                        _error.value = false
                    } else {
                        _message.value = apiResponse?.message ?: "Failed to load categories"
                        _error.value = true
                    }
                }

                is Resource.Error -> {
                    _message.value = result.message
                    _error.value = true
                    _categories.value = emptyList()
                }

                is Resource.Loading -> {}
            }
            _isLoading.value = false
        }
    }

    fun fetchProductImages(productId: Int) {
        viewModelScope.launch {
            val result = repository.getProductImages(productId)

            when (result) {
                is Resource.Success -> {
                    // Explicitly cast to help the compiler
                    val apiResponse: ApiResponse<List<String>>? = result.data

                    if (apiResponse != null && apiResponse.success) {
                        _productImages.value = apiResponse.data ?: emptyList()
                    }
                }

                is Resource.Error -> {
                    _message.value = result.message
                    _productImages.value = emptyList()
                }

                is Resource.Loading -> {}
            }
        }
    }

    fun uploadProduct(
        token: String,
        name: String,
        desc: String,
        price: String,
        stock: String,
        catId: Int,
        discount: String,
        uris: List<Uri>,
        context: Context
    ) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            val tempFiles = mutableListOf<File>()

            val imageFiles = withContext(Dispatchers.IO) {
                try {
                    uris.map { uriToFile(it, context).also { file -> tempFiles.add(file) } }
                } catch (e: Exception) {
                    null
                }
            }

            if (imageFiles == null) {
                _message.value = "Failed to process images."
                _isLoading.value = false
                return@launch
            }

            when (val result = repository.uploadProductWithImages(
                name, desc, price, stock, catId, discount, imageFiles
            )) {
                is Resource.Success -> {
                    // Add .body() right here! 👇
                    val apiResponse: ApiResponse<String>? = result.data?.body()

                    if (apiResponse != null) {
                        _message.value = apiResponse.message
                        if (apiResponse.success) {
                            _uploadSuccess.value = true
                            _error.value = false
                        } else {
                            _error.value = true
                        }
                    } else {
                        // Fallback if body is null (e.g., HTTP 500 or 404)
                        _message.value = result.data?.message() ?: "Unknown error occurred"
                        _error.value = true
                    }
                }

                is Resource.Error -> {
                    _message.value = result.message
                    _error.value = true
                }

                is Resource.Loading -> {}
            }

            withContext(Dispatchers.IO) { tempFiles.forEach { if (it.exists()) it.delete() } }
            _isLoading.value = false
        }
    }

    fun updateProduct(
        token: String,
        productId: Int,
        name: String,
        desc: String,
        price: String,
        stock: String,
        catId: Int,
        discount: String,
        uris: List<Uri>,
        deletedUrls: List<String>,
        context: Context
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            val tempFiles = mutableListOf<File>()

            val imageFiles = withContext(Dispatchers.IO) {
                uris.filter { it.scheme == "content" || it.scheme == "file" }.map { uri ->
                    uriToFile(uri, context).also { tempFiles.add(it) }
                }
            }

            when (val result = repository.updateProduct(
                productId, name, desc, price, stock, catId, imageFiles, deletedUrls, discount
            )) {
                is Resource.Success -> {
                    // Add .body() right here! 👇
                    val apiResponse: ApiResponse<String>? = result.data?.body()

                    if (apiResponse != null) {
                        _message.value = apiResponse.message
                        if (apiResponse.success) {
                            _uploadSuccess.value = true
                            _error.value = false
                        } else {
                            _error.value = true
                        }
                    } else {
                        // Fallback if body is null (e.g., HTTP 500 or 404)
                        _message.value = result.data?.message() ?: "Unknown error occurred"
                        _error.value = true
                    }
                }

                is Resource.Error -> _message.value = result.message
                is Resource.Loading -> {}
            }

            withContext(Dispatchers.IO) { tempFiles.forEach { if (it.exists()) it.delete() } }
            _isLoading.value = false
        }
    }

    private fun uriToFile(uri: Uri, context: Context): File {
        val file =
            File(context.cacheDir, "prod_${System.currentTimeMillis()}_${(0..1000).random()}.jpg")
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            file
        } catch (e: Exception) {
            if (file.exists()) file.delete()
            throw e
        }
    }
}