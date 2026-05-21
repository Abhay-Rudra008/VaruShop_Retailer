package com.example.varushopretailer.viewmodal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.varushopretailer.helper.LangPrefManager
import com.example.varushopretailer.helper.SessionManager
import com.example.varushopretailer.modal.ApiResponse
import com.example.varushopretailer.modal.DashboardData
import com.example.varushopretailer.modal.Order
import com.example.varushopretailer.modal.User
import com.example.varushopretailer.stats.Resource
import com.example.varushopretailer.stats.RetailerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RetailerRepository,
    private val prefManager: LangPrefManager
) : BaseViewModel() {

    private val _dashboardData = MutableLiveData<DashboardData>()
    val dashboardData: LiveData<DashboardData> = _dashboardData

    private val _recentOrders = MutableLiveData<List<Order>>()
    val recentOrders: LiveData<List<Order>> = _recentOrders

    private val _profileData = MutableLiveData<User?>()
    val profileData: LiveData<User?> = _profileData

    fun loadAllHomeData() {
        val rawToken = prefManager.getToken()

        if (rawToken.isNullOrEmpty()) {
            _error.value = true
            _message.value = "Session expired. Please login again."
            SessionManager.triggerLogout()
            return
        }



        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            val profileDeferred = async { repository.getProfile() }
            val dashboardDeferred = async { repository.getDashboardData() }
            val ordersDeferred = async { repository.getRecentOrders() }
            val profileRes = profileDeferred.await()
            val dashboardRes = dashboardDeferred.await()
            val ordersRes = ordersDeferred.await()

            val results = listOf(profileRes, dashboardRes, ordersRes)
            val firstError = results.firstOrNull { it is Resource.Error } as? Resource.Error

            if (firstError != null) {
                _error.value = true
                _message.value = firstError.message ?: "Failed to load dashboard data."
            } else {
                _error.value = false

                (profileRes as? Resource.Success<ApiResponse<User>>)?.data?.let { apiRes ->
                    if (apiRes.success) _profileData.value = apiRes.data
                }

                (dashboardRes as? Resource.Success<ApiResponse<DashboardData>>)?.data?.let { apiRes ->
                    if (apiRes.success) {
                        _dashboardData.value =
                            apiRes.data!!
                    }
                }


                (ordersRes as? Resource.Success<ApiResponse<List<Order>>>)?.data?.let { apiRes ->
                    if (apiRes.success) {
                        _recentOrders.value = apiRes.data ?: emptyList()
                    }
                }
            }

            _isLoading.value = false
        }
    }
}