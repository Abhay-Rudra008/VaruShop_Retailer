package com.example.varushopretailer.viewmodal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.varushopretailer.helper.LangPrefManager
import com.example.varushopretailer.modal.DashboardData
import com.example.varushopretailer.modal.MonthlyRevenue
import com.example.varushopretailer.modal.User
import com.example.varushopretailer.stats.Resource
import com.example.varushopretailer.stats.RetailerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: RetailerRepository, private val pref: LangPrefManager
) : BaseViewModel() {

    private val _dashboardData = MutableLiveData<DashboardData>()
    val dashboardData: LiveData<DashboardData> = _dashboardData

    private val _lowStockCount = MutableLiveData<Int>()
    val lowStockCount: LiveData<Int> = _lowStockCount

    private val _latestRevenue = MutableLiveData<MonthlyRevenue>()
    val latestRevenue: LiveData<MonthlyRevenue> = _latestRevenue

    private val _profileData = MutableLiveData<User?>()
    val profileData: LiveData<User?> = _profileData

    fun loadProfileData() {

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val profileDeferred = async { repository.getProfile() }
                val dashDeferred = async { repository.getDashboardData() }
                val lowStockDeferred = async { repository.getLowStock() }
                val revenueDeferred = async { repository.getMonthlyRevenue() }

                val profileRes = profileDeferred.await()
                val dashRes = dashDeferred.await()
                val lowRes = lowStockDeferred.await()
                val revRes = revenueDeferred.await()

                if (profileRes is Resource.Error || dashRes is Resource.Error) {
                    _error.value = true
                } else {
                    _error.value = false

                    if (profileRes is Resource.Success) _profileData.value = profileRes.data?.data
                    if (dashRes is Resource.Success) {
                        dashRes.data?.data?.let { data ->
                            _dashboardData.value = data
                        }
                    }
                    if (lowRes is Resource.Success) _lowStockCount.value =
                        lowRes.data?.data?.size ?: 0

                    if (revRes is Resource.Success) {
                        val data = revRes.data?.data
                        if (!data.isNullOrEmpty()) _latestRevenue.value = data.last()
                    }
                }
            } catch (e: Exception) {
                _error.value = true
                _message.value = "An unexpected error occurred: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun handleSessionExpired() {
        _message.value = "Session expired. Please login again."
        _isLoading.value = false
    }
}