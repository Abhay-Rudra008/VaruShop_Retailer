package com.example.varushopretailer.viewmodal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.varushopretailer.modal.wallet.Transaction
import com.example.varushopretailer.modal.wallet.WithdrawRequest
import com.example.varushopretailer.stats.Resource
import com.example.varushopretailer.stats.RetailerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class WalletViewModel @Inject constructor(
    private val repository: RetailerRepository
) : BaseViewModel() {

    private val _balanceState = MutableLiveData<BalanceState>()
    val balanceState: LiveData<BalanceState> = _balanceState

    data class BalanceState(
        val availableBalance: String = "0.00",
        val totalEarnings: String = "0.00",
        val totalWithdrawn: String = "0.00"
    )

    private val _historyState = MutableLiveData<HistoryState>()
    val historyState: LiveData<HistoryState> = _historyState

    data class HistoryState(
        val transactions: List<Transaction> = emptyList(), val isEmpty: Boolean = true
    )

    private val _withdrawalSuccess = MutableLiveData<Boolean>()
    val withdrawalSuccess: LiveData<Boolean> = _withdrawalSuccess


    fun loadWalletData() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.getPayoutDashboard()

                if (response is Resource.Success) {
                    val dataObj = response.data?.data

                    _balanceState.value = BalanceState(
                        availableBalance = dataObj?.availableBalance ?: "0.00",
                        totalEarnings = dataObj?.totalEarnings ?: "0.00",
                        totalWithdrawn = dataObj?.totalWithdrawn ?: "0.00"
                    )

                    val historyList = dataObj?.history ?: emptyList()
                    _historyState.value = HistoryState(
                        transactions = historyList, isEmpty = historyList.isEmpty()
                    )

                } else {
                    _error.value = true
                    _message.value = response.message ?: "Failed to load wallet data."
                }
            } catch (e: Exception) {
                _error.value = true
                _message.value = e.localizedMessage ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun requestWithdrawal(amount: Double, bankDetails: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val request = WithdrawRequest(amount, bankDetails)
                val response = repository.requestWithdrawal(request)

                if (response is Resource.Success) {
                    _withdrawalSuccess.value = true
                    _message.value = "Funds disbursed successfully!"

                    loadWalletData()
                } else {
                    _withdrawalSuccess.value = false
                    _error.value = true
                    _message.value = response.message ?: "Withdrawal request failed."
                }
            } catch (e: Exception) {
                _withdrawalSuccess.value = false
                _error.value = true
                _message.value = e.localizedMessage ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetWithdrawalState() {
        _withdrawalSuccess.value = false
    }
}