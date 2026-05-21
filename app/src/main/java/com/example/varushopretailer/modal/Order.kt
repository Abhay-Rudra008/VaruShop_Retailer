package com.example.varushopretailer.modal

data class Order(
    val id: Int,
    val order_id: Int,
    val retailer_id: Int,
    val status: String,
    val total_amount: Double,
    val created_at: String,
    val address: String,
    val payment_status: String
)