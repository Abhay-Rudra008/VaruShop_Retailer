package com.example.varushopretailer.modal.order

data class OrderDetailData(
    val order_id: Int,
    val status: String,
    val payment_status: String?,
    val total_amount: Double,
    val created_at: String,
    val customer: CustomerDetails,
    val delivery_address: DeliveryAddress?,
    val products: List<OrderProduct>
)