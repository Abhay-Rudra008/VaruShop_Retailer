package com.example.varushopretailer.modal



data class DashboardData(
    val store_name: String? = null,
    val today_sales: String? = null,
    val pending_orders: Int? = null,
    val total_orders: Int? = null,
    val cancelled_orders: Int? = null,
    val returned_orders: Int? = null,
    val low_stock_count: Int? = null
)