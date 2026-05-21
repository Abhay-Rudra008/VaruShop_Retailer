package com.example.varushopretailer.modal.order

data class DeliveryAddress(
    val receiver_name: String?,
    val label: String?,
    val phone: String?,
    val full_address: String?,
    val city: String?,
    val state: String?,
    val pincode: String?
)