package com.example.varushopretailer.modal.wallet

data class WithdrawRequest(
    val amount: Double,
    val bank_account_details: String
)