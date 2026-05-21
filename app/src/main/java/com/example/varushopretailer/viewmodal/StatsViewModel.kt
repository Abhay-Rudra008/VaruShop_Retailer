package com.example.varushopretailer.viewmodal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.varushopretailer.modal.DashboardData
import com.example.varushopretailer.modal.MonthlyRevenue
import com.example.varushopretailer.modal.Product
import com.example.varushopretailer.stats.Resource
import com.example.varushopretailer.stats.RetailerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: RetailerRepository
) : BaseViewModel() {

    private val _dashboardData = MutableLiveData<DashboardData>()
    val dashboardData: LiveData<DashboardData> = _dashboardData

    private val _monthlyRevenue = MutableLiveData<List<MonthlyRevenue>>()
    val monthlyRevenue: LiveData<List<MonthlyRevenue>> = _monthlyRevenue

    private val _topProducts = MutableLiveData<List<Product>>()
    val topProducts: LiveData<List<Product>> = _topProducts


    private var currentPage = 1
    private var isLastPage = false

    fun loadAllStats(token: String) {
        currentPage = 1
        isLastPage = false
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val dashboardDef = async { repository.getDashboardData() }
                val revenueDef = async { repository.getMonthlyRevenue() }
                val productsDef =
                    async { repository.getTopSellingProducts(page = currentPage, limit = 10) }

                val dashRes = dashboardDef.await()
                val revRes = revenueDef.await()
                val prodRes = productsDef.await()

                _error.value = false

                if (dashRes is Resource.Success) {
                    dashRes.data?.data?.let { data ->
                        _dashboardData.value = data
                    }
                }
                if (revRes is Resource.Success) {
                    _monthlyRevenue.value = revRes.data?.data ?: emptyList()
                }
                if (prodRes is Resource.Success) {
                    val list = prodRes.data?.data ?: emptyList()
                    _topProducts.value = list

                    if (list.size < 10) isLastPage = true else currentPage++
                }

            } catch (e: Exception) {
                _error.value = true
                _message.value = "Sync failed: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

}