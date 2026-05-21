package com.example.varushopretailer.modal

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData?
)