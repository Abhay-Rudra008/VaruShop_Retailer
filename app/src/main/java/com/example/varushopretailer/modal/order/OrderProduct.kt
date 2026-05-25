package com.example.varushopretailer.modal.order

data class OrderProduct(
    val product_id: Int,
    val name: String,
    val quantity: Int,
    val price: Double,
    val image_url: String?,
    val status: String?
)