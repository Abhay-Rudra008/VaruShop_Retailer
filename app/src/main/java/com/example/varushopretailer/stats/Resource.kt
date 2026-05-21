package com.example.varushopretailer.stats


sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null,
    val isNetworkError: Boolean = false
) {
    class Success<T>(data: T) : Resource<T>(data)

    class Error<T>(
        message: String,
        isNetworkError: Boolean = false,
        data: T? = null
    ) : Resource<T>(data, message, isNetworkError)

    class Loading<T>(data: T? = null) : Resource<T>(data)
}