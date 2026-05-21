package com.example.varushopretailer.modal

data class LowStockProduct(
    val id: String,
    val name: String,
    val current_stock: Int,
    val threshold: Int
)