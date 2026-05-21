package com.example.varushopretailer.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.varushopretailer.R
import com.example.varushopretailer.activity.LoginActivity
import com.example.varushopretailer.activity.ProfileActivity
import com.example.varushopretailer.adapter.OrdersAdapter
import com.example.varushopretailer.databinding.FragmentHomeBinding
import com.example.varushopretailer.helper.LangPrefManager
import com.example.varushopretailer.viewmodal.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var recentOrdersAdapter: OrdersAdapter

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        setupClickListeners()
        observeViewModel()

        val token = LangPrefManager(requireContext()).getToken()
        if (!token.isNullOrEmpty()) {
            viewModel.loadAllHomeData()
        } else {
            handleLogout()
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun observeViewModel() {
        viewModel.error.observe(viewLifecycleOwner) { isError ->
            if (isError == true) {
                binding.llErrorState.visibility = View.VISIBLE
                binding.shimmerLayout.visibility = View.GONE
            } else {
                binding.llErrorState.visibility = View.GONE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading) {
                if (viewModel.error.value != true) {
                    binding.shimmerLayout.visibility = View.VISIBLE
                    binding.shimmerLayout.startShimmer()
                    binding.rvRecentOrders.visibility = View.GONE
                }
            } else {
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
        }

        viewModel.profileData.observe(viewLifecycleOwner) { user ->
            if (user != null && !user.profile_image.isNullOrEmpty()) {
                val secureUrl = user.profile_image.replace("http://", "https://")

                binding.tvStoreName.text = user.name

                Glide.with(this).load(secureUrl).placeholder(R.drawable.ic_user_placeholder)
                    .error(R.drawable.app_logo).circleCrop().into(binding.ivProfileTop)
            } else {
                binding.ivProfileTop.setImageResource(R.drawable.app_logo)
            }
        }

        //  Dashboard Data
        viewModel.dashboardData.observe(viewLifecycleOwner) { data ->
            data?.let {
                // Handle Store Name
                binding.tvStoreName.text = it.store_name ?: "My Store"

                // Handle Sales
                val salesValue = it.today_sales?.toDoubleOrNull() ?: 0.0
                binding.tvTodaySales.text = "₹${String.format("%,.0f", salesValue)}"

                // Handle Pending Orders
                binding.tvPendingOrders.text = (it.pending_orders ?: 0).toString()
            }
        }

        // Recent Orders List
        viewModel.recentOrders.observe(viewLifecycleOwner) { orders ->
            if (orders.isNullOrEmpty()) {
                binding.rvRecentOrders.visibility = View.GONE
                binding.layoutEmpty.visibility = View.VISIBLE
            } else {
                binding.rvRecentOrders.visibility = View.VISIBLE
                binding.layoutEmpty.visibility = View.GONE
                recentOrdersAdapter.submitList(orders)
            }
        }

        // Error Messages
        viewModel.message.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty() && context != null) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        recentOrdersAdapter = OrdersAdapter { clickedOrder ->
            val bottomSheet = OrderDetailBottomSheetFragment.newInstance(clickedOrder.order_id)
            bottomSheet.show(childFragmentManager, "OrderDetailSheet")
        }

        binding.rvRecentOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentOrdersAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            val token = LangPrefManager(requireContext()).getToken()
            token?.let { viewModel.loadAllHomeData() }
        }
    }

    private fun setupClickListeners() {
        binding.btnRetry.setOnClickListener {
            val token = LangPrefManager(requireContext()).getToken()
            if (!token.isNullOrEmpty()) {
                viewModel.loadAllHomeData()
            }
        }

        binding.btnViewAllOrders.setOnClickListener {
            (activity as? HomeNavigationListener)?.navigateToOrders()
        }

        binding.ivProfileTop.setOnClickListener {
            startActivity(Intent(requireActivity(), ProfileActivity::class.java))
        }
    }

    private fun handleLogout() {
        Toast.makeText(context, "Session Expired", Toast.LENGTH_SHORT).show()
        startActivity(Intent(requireContext(), LoginActivity::class.java))
    }

    interface HomeNavigationListener {
        fun navigateToOrders()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}