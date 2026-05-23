package com.example.varushopretailer.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.varushopretailer.R
import com.example.varushopretailer.activity.AnalyticsActivity
import com.example.varushopretailer.adapter.ProductAdapter
import com.example.varushopretailer.databinding.FragmentStatsBinding
import com.example.varushopretailer.helper.LangPrefManager
import com.example.varushopretailer.viewmodal.StatsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatsFragment : Fragment(R.layout.fragment_stats) {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter

    private val viewModel: StatsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStatsBinding.bind(view)


        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        setupSwipeRefresh()

        fetchInitialData()
    }


    private fun fetchInitialData() {
        val token = LangPrefManager(requireContext()).getToken()
        token?.let {
            viewModel.loadAllStats(it)
        }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            isEditable = false,
            showActions = false
        ) { product ->
            Toast.makeText(requireContext(), "${product.name} selected", Toast.LENGTH_SHORT).show()
        }


        binding.rvTopSelling.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupClickListeners() {
        binding.btnRetry.setOnClickListener { fetchInitialData() }

        binding.btnOpenAnalytics.setOnClickListener {
            startActivity(Intent(requireActivity(), AnalyticsActivity::class.java))
        }

    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.swipeRefresh.isRefreshing = loading

            if (loading) {
                if (!binding.swipeRefresh.isRefreshing) {
                    binding.shimmerStats.visibility = View.VISIBLE
                    binding.shimmerStats.startShimmer()
                    binding.statsNestedScroll.visibility = View.GONE
                }
                binding.llErrorState.visibility = View.GONE
            } else {
                binding.shimmerStats.stopShimmer()
                binding.shimmerStats.visibility = View.GONE
                if (viewModel.error.value != true) {
                    binding.statsNestedScroll.visibility = View.VISIBLE
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { isError ->
            if (isError == true) {
                binding.llErrorState.visibility = View.VISIBLE
                binding.statsNestedScroll.visibility = View.GONE
                binding.shimmerStats.visibility = View.GONE
            } else {
                binding.llErrorState.visibility = View.GONE
            }
        }

        viewModel.dashboardData.observe(viewLifecycleOwner) { data ->
            data?.let {
                binding.layoutTotalOrderReceived.tvStatLabel.text = "Total Orders"
                binding.layoutTotalOrderReceived.tvStatValue.text =
                    (it.total_orders ?: 0).toString()

                binding.layoutCancelledOrders.tvStatLabel.text = " Total Cancelled"
                binding.layoutCancelledOrders.tvStatValue.text =
                    (it.cancelled_orders ?: 0).toString()
            }
        }

        viewModel.monthlyRevenue.observe(viewLifecycleOwner) { revenueList ->
            if (!revenueList.isNullOrEmpty()) {
                val currentMonth = revenueList.last()

                binding.tvRevenueTitle.text = "TOTAL REVENUE"
                val revenueValue = currentMonth.revenue?.toDoubleOrNull() ?: 0.0
                binding.tvTotalRevenue.text = "₹${String.format("%,.0f", revenueValue)}"

                val growth = currentMonth.growth_percentage ?: 0.0
                val isPositive = growth >= 0

                binding.tvGrowthPill.apply {
                    text = if (isPositive) "+${
                        String.format(
                            "%.1f", growth
                        )
                    }% ↑" else "${String.format("%.1f", growth)}% ↓"

                    val colorRes = if (isPositive) R.color.green_800 else R.color.red_800
                    val bgRes = if (isPositive) R.color.green_100 else R.color.red_100

                    setTextColor(ContextCompat.getColor(context, colorRes))
                    backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, bgRes))
                }
            }
        }

        viewModel.topProducts.observe(viewLifecycleOwner) { products ->
            binding.let { b ->
                val isEmpty = products.isNullOrEmpty()
                b.rvTopSelling.visibility = if (isEmpty) View.GONE else View.VISIBLE
                b.llEmptyProducts.visibility = if (isEmpty) View.VISIBLE else View.GONE

                if (!isEmpty) {
                    productAdapter.submitList(products)
                }
            }
        }

        viewModel.message.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                viewModel.clearMessage()
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            fetchInitialData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}