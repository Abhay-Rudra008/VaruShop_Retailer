package com.example.varushopretailer.helper

import android.content.Context
import android.content.Intent
import com.example.varushopretailer.activity.LoginActivity
import com.example.varushopretailer.stats.RetailerApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefManager: LangPrefManager,
    private val apiServiceProvider: Provider<RetailerApiService>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {

        if (response.priorResponse != null) {
            triggerGlobalLogout()
            return null
        }

        val refreshToken = prefManager.getRefreshToken()

        if (refreshToken.isNullOrEmpty()) {
            triggerGlobalLogout()
            return null
        }

        return try {
            val refreshService = apiServiceProvider.get()

            val refreshResponse = refreshService.refreshTokenSync("Bearer $refreshToken").execute()

            if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                val newTokens = refreshResponse.body()!!.data

                val newAccessToken = newTokens?.token ?: ""
                val newRefreshToken = newTokens?.refreshToken ?: ""

                prefManager.saveToken(newAccessToken)
                prefManager.saveRefreshToken(newRefreshToken)

                response.request.newBuilder().header("Authorization", "Bearer $newAccessToken")
                    .build()
            } else {
                triggerGlobalLogout()
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun triggerGlobalLogout() {
        prefManager.clearSession()

        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }
}