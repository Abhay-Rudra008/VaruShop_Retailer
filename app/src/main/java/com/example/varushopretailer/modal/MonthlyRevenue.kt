package com.example.varushopretailer.modal

data class MonthlyRevenue(
    val month: String?,
    val revenue: String?,
    val order_count: Int?,
    val growth_percentage: Double
)