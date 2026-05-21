package com.example.varushopretailer.utils

import com.example.varushopretailer.helper.LangPrefManager
import com.example.varushopretailer.helper.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject


class AuthInterceptor @Inject constructor(
    private val prefManager: LangPrefManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        val token = prefManager.getToken()

        if (!token.isNullOrEmpty()) {
            request = request.newBuilder().header("Authorization", "Bearer $token").build()
        }

        val response = chain.proceed(request)

        if (response.code == 401) {
            prefManager.clearSession()
            SessionManager.triggerLogout()
        }

        return response
    }
}