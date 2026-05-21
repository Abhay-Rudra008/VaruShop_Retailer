package com.example.varushopretailer.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.varushopretailer.adapter.OrdersAdapter
import com.example.varushopretailer.databinding.FragmentOrdersBinding
import com.example.varushopretailer.helper.LangPrefManager
import com.example.varushopretailer.viewmodal.OrdersViewModel
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding

    private lateinit var ordersAdapter: OrdersAdapter

    private val viewModel: OrdersViewModel by viewModels()

    private var searchQuery = ""
    private var currentStatus = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupTabs()
        setupSearch()
        setupSwipeRefresh()
        observeViewModel()

        fetchOrders()

        binding?.btnRetry?.setOnClickListener {
            fetchOrders()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding?.let { b ->
                if (loading) {
                    if (!b.swipeRefresh.isRefreshing) {
                        b.rvOrders.visibility = View.GONE
                        b.llEmptyState.visibility = View.GONE
                        b.llErrorState.visibility = View.GONE
                        b.shimmerView.visibility = View.VISIBLE
                        b.shimmerView.startShimmer()
                    }
                } else {
                    b.shimmerView.stopShimmer()
                    b.shimmerView.visibility = View.GONE
                    b.swipeRefresh.isRefreshing = false
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { isError ->
            binding?.let { b ->
                if (isError == true) {
                    b.llErrorState.visibility = View.VISIBLE
                    b.rvOrders.visibility = View.GONE
                    b.llEmptyState.visibility = View.GONE
                    b.shimmerView.visibility = View.GONE
                } else {
                    b.llErrorState.visibility = View.GONE
                }
            }
        }

        viewModel.orders.observe(viewLifecycleOwner) { list ->
            binding?.let { b ->
                ordersAdapter.submitList(list?.toList())

                val isEmpty = list.isNullOrEmpty()
                val isNotLoading = viewModel.isLoading.value == false

                if (!isEmpty) {
                    b.rvOrders.visibility = View.VISIBLE
                    b.llEmptyState.visibility = View.GONE
                    b.llErrorState.visibility = View.GONE
                } else if (isNotLoading) {
                    b.llEmptyState.visibility = View.VISIBLE
                    b.rvOrders.visibility = View.GONE
                    b.llErrorState.visibility = View.GONE
                }
            }
        }

        viewModel.message.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty() && isAdded) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                viewModel.clearMessage()
            }
        }
    }

    private fun setupRecyclerView() {
        ordersAdapter = OrdersAdapter { clickedOrder ->
            val bottomSheet = OrderDetailBottomSheetFragment.newInstance(clickedOrder.order_id)
            bottomSheet.show(childFragmentManager, "OrderDetailSheet")
        }

        binding?.rvOrders?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ordersAdapter
        }
    }

    private fun setupTabs() {
        val statuses = listOf("All", "Ordered", "Confirmed", "Shipped", "Delivered")

        binding?.tabLayoutOrders?.apply {
            if (tabCount == 0) {
                statuses.forEach { addTab(newTab().setText(it)) }
            }

            clearOnTabSelectedListeners()

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    currentStatus = when (tab?.position) {
                        0 -> ""
                        1 -> "ORDERED"
                        2 -> "CONFIRMED"
                        3 -> "SHIPPED"
                        4 -> "DELIVERED"
                        else -> ""
                    }
                    binding?.llEmptyState?.visibility = View.GONE
                    binding?.llErrorState?.visibility = View.GONE
                    fetchOrders()
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
    }

    private fun setupSearch() {
        var searchJob: Job? = null
        binding?.etSearchOrders?.addTextChangedListener { text ->
            searchJob?.cancel()
            searchJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(600)
                val newQuery = text.toString().trim()
                if (newQuery != searchQuery) {
                    searchQuery = newQuery
                    fetchOrders()
                }
            }
        }
    }

    private fun fetchOrders() {
        if (!isAdded) return
        val token = LangPrefManager(requireContext()).getToken() ?: return
        viewModel.loadOrders(token, currentStatus, searchQuery)
    }

    private fun setupSwipeRefresh() {
        binding?.swipeRefresh?.setOnRefreshListener {
            fetchOrders()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}