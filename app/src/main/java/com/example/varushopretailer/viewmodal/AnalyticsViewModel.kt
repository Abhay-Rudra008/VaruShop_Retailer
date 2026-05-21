package com.example.varushopretailer.viewmodal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.varushopretailer.modal.GraphData
import com.example.varushopretailer.stats.Resource
import com.example.varushopretailer.stats.RetailerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: RetailerRepository
) : BaseViewModel() {

    private val _graphState = MutableLiveData<GraphState>()
    val graphState: LiveData<GraphState> = _graphState

    data class GraphState(
        val totalValue: String = "0",
        val graphDataList: List<GraphData> = emptyList()
    )

    private val _pieState = MutableLiveData<PieState>()
    val pieState: LiveData<PieState> = _pieState

    data class PieState(
        val deliveredOrders: Int = 0,
        val pendingOrders: Int = 0,
        val cancelledOrders: Int = 0,
        val returnedOrders: Int = 0
    )

    fun loadGraphData(timeFilter: String = "MONTH", metricFilter: String = "REVENUE") {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val graphRes = repository.getGraphData(timeFilter, metricFilter)

                if (graphRes is Resource.Success) {
                    val graphList = graphRes.data?.data ?: emptyList()

                    val totalSum = graphList.sumOf { it.value.toDouble() }

                    _graphState.value = GraphState(
                        totalValue = totalSum.toString(),
                        graphDataList = graphList
                    )
                } else {
                    _error.value = true
                    _message.value = "Failed to load graph data."
                }
            } catch (e: Exception) {
                _error.value = true
                _message.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadPieChartData(timeFilter: String = "MONTH") {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val dashRes = repository.getDashboardData(timeFilter)

                if (dashRes is Resource.Success) {
                    val dashObj = dashRes.data?.data

                    val total = dashObj?.total_orders ?: 0
                    val pending = dashObj?.pending_orders ?: 0
                    val cancelled = dashObj?.cancelled_orders ?: 0
                    val returned = dashObj?.returned_orders ?: 0
                    val delivered = total - pending - cancelled - returned

                    _pieState.value = PieState(
                        deliveredOrders = if (delivered > 0) delivered else 0,
                        pendingOrders = pending,
                        cancelledOrders = cancelled,
                        returnedOrders = returned
                    )
                } else {
                    _error.value = true
                    _message.value = "Failed to load pie chart data."
                }
            } catch (e: Exception) {
                _error.value = true
                _message.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }
}