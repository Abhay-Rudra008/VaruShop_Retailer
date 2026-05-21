package com.example.varushopretailer.modal

import com.google.gson.annotations.SerializedName



data class LoginData(
    val token: String? = null,

    @SerializedName("refresh_token")
    val refreshToken: String? = null,
    val user: User
)