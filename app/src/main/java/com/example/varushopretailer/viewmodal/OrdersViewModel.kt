package com.example.varushopretailer.viewmodal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.varushopretailer.modal.Order
import com.example.varushopretailer.stats.RetailerRepository
import kotlinx.coroutines.launch
import com.example.varushopretailer.modal.order.OrderDetailData
import com.example.varushopretailer.stats.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val repository: RetailerRepository
) : BaseViewModel() {
    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders

    private val _isDetailLoading = MutableLiveData<Boolean>()
    val isDetailLoading: LiveData<Boolean> = _isDetailLoading

    private val _orderDetail = MutableLiveData<OrderDetailData>()
    val orderDetail: LiveData<OrderDetailData> = _orderDetail


    fun updateStatus(
        token: String,
        orderId: Int,
        newStatus: String,
        currentFilter: String,
        query: String
    ) {
        _isLoading.value = true

        viewModelScope.launch {
            when (val result = repository.updateOrderStatus( orderId, newStatus)) {
                is Resource.Success -> {
                    _message.value = "Order marked as $newStatus"
                    _isLoading.value = false
                    loadOrders(token, currentFilter, query)
                }

                is Resource.Error -> {
                    _message.value = result.message
                    _isLoading.value = false
                }

                is Resource.Loading -> { }
            }
        }
    }
    fun cancelSingleItem(token: String, orderId: Int, productId: Int) {
        _isDetailLoading.value = true

        viewModelScope.launch {
            when (val result = repository.cancelOrderItem(orderId, productId)) {
                is Resource.Success -> {
                    _message.value = "Item cancelled successfully"
                    loadOrderDetail(token, orderId)
                }
                is Resource.Error -> {
                    _message.value = result.message
                    _isDetailLoading.value = false
                }
                is Resource.Loading -> { }
            }
        }
    }
    fun loadOrderDetail(token: String, orderId: Int) {
        _isDetailLoading.value = true

        viewModelScope.launch {
            val result = repository.getOrderDetails( orderId)
            when (result) {
                is Resource.Success -> {
                    result.data?.let { apiResponse ->
                        if (apiResponse.success) {
                            apiResponse.data?.let { data ->
                                _orderDetail.value = data
                            } ?: run {
                                _message.value = "Order details are empty"
                            }
                        } else {
                            _message.value = apiResponse.message ?: "Failed to load details"
                        }
                    }
                }

                is Resource.Error -> {
                    _message.value = result.message
                }

                is Resource.Loading -> { }
            }
            _isDetailLoading.value = false
        }
    }

    fun loadOrders(token: String, status: String, query: String) {
        if (_isLoading.value == true) return

        _isLoading.value = true
        _error.value = null
        _orders.value = emptyList()

        viewModelScope.launch {
            val result = repository.getOrdersByStatus( status, 1, 100, query)
            _isLoading.value = false

            when (result) {
                is Resource.Success -> {
                    val apiResponse = result.data
                    if (apiResponse != null && apiResponse.success) {
                        _error.value = false
                        _orders.value = apiResponse.data?.toList() ?: emptyList()
                    } else {
                        _message.value = apiResponse?.message ?: "Failed to load orders"
                        _error.value = true
                    }
                }

                is Resource.Error -> {
                    _message.value = result.message
                    _error.value = true
                }

                is Resource.Loading -> { }
            }
        }
    }
}