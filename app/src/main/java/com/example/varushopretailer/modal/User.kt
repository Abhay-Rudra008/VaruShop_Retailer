package com.example.varushopretailer.modal

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val profile_image : String,
    val is_blocked: Int,
    val is_deleted: Int,
    val is_approved: Int
)