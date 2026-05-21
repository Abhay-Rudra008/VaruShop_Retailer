package com.example.varushopretailer.viewmodal

import com.example.varushopretailer.stats.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

abstract class BaseRepository {
    suspend fun <T> safeApiCall(apiCall: suspend () -> T): Resource<T> {
        return withContext(Dispatchers.IO) {
            try {
                Resource.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                when (throwable) {
                    is SocketTimeoutException -> Resource.Error(
                        "Server timeout. Please try again.",
                        true
                    )

                    is IOException -> Resource.Error("No internet connection.", true)

                    is HttpException -> {
                        var errorMessage = "Server error (Code: ${throwable.code()})"
                        try {
                            val errorString = throwable.response()?.errorBody()?.string()
                            if (!errorString.isNullOrEmpty()) {
                                val jsonObject = JSONObject(errorString)
                                if (jsonObject.has("message")) {
                                    errorMessage = jsonObject.getString("message")
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        Resource.Error(errorMessage, false)
                    }

                    else -> Resource.Error(throwable.localizedMessage ?: "Unknown Error", false)
                }
            }
        }
    }
}