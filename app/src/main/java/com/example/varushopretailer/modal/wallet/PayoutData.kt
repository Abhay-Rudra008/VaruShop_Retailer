package com.example.varushopretailer.modal.wallet

data class PayoutData(
    val availableBalance: String,
    val totalEarnings: String,
    val totalWithdrawn: String,
    val history: List<Transaction>
)