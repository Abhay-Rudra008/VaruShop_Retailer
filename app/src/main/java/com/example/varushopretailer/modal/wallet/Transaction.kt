package com.example.varushopretailer.modal.wallet

data class Transaction(
    val id: Int,
    val amount: String,
    val bank_account_details: String,
    val status: String,
    val admin_notes: String?,
    val created_at: String
)