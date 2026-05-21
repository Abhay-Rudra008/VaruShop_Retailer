package com.example.varushopretailer.modal

data class Language(
    val name: String,
    val nativeName: String,
    val flagEmoji: String,
    val code: String,
    var isSelected: Boolean = false
)