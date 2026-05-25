package com.example.varushopretailer.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.varushopretailer.adapter.OrderProductsAdapter
import com.example.varushopretailer.databinding.FragmentOrderDetailBottomSheetBinding
import com.example.varushopretailer.helper.LangPrefManager
import com.example.varushopretailer.modal.order.OrderProduct
import com.example.varushopretailer.viewmodal.OrdersViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderDetailBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentOrderDetailBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var productsAdapter: OrderProductsAdapter
    private var currentOrderId: Int = -1

    private val viewModel: OrdersViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentOrderId = arguments?.getInt(ARG_ORDER_ID) ?: return

        setupRecyclerView()
        observeViewModel()

        val token = LangPrefManager(requireContext()).getToken() ?: return
        viewModel.loadOrderDetail(token, currentOrderId)
    }

    private fun setupRecyclerView() {
        // 🔥 PASS THE CALLBACK HERE
        productsAdapter = OrderProductsAdapter { product ->
            showCancelItemConfirmation(product)
        }

        binding.rvSheetProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productsAdapter
        }
    }

    // 🔥 Show Confirmation Dialog before cancelling a specific item
    private fun showCancelItemConfirmation(product: OrderProduct) {
        MaterialAlertDialogBuilder(requireContext()).setTitle("Cancel Item?")
            .setMessage("Are you sure you want to cancel '${product.name}'? Stock will be restored and this cannot be undone.")
            .setPositiveButton("Yes, Cancel") { _, _ ->
                val token = LangPrefManager(requireContext()).getToken() ?: return@setPositiveButton
                viewModel.cancelSingleItem(token, currentOrderId, product.product_id)
            }.setNegativeButton("No", null).show()
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun observeViewModel() {

        viewModel.isDetailLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.pbSheetLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.orderDetail.observe(viewLifecycleOwner) { detail ->
            if (detail.order_id != currentOrderId) return@observe

            binding.tvSheetOrderId.text = "Order #ORD-${detail.order_id}"
            binding.tvSheetStatus.text = detail.status.uppercase()
            binding.tvSheetTotalAmount.text = "₹${String.format("%,.2f", detail.total_amount)}"

            binding.tvSheetCustomerName.text =
                detail.delivery_address?.receiver_name ?: detail.customer.name

            val fullAddressText = buildString {
                detail.delivery_address?.let { addr ->
                    if (!addr.label.isNullOrEmpty()) append("[${addr.label}]\n")
                    append(addr.full_address ?: "")

                    if (!addr.city.isNullOrEmpty()) append(", ${addr.city}")
                    if (!addr.state.isNullOrEmpty()) append(", ${addr.state}")
                    if (!addr.pincode.isNullOrEmpty()) append(" - ${addr.pincode}")

                    if (!addr.phone.isNullOrEmpty()) append("\nPhone: ${addr.phone}")
                } ?: append("Delivery address not available")
            }
            binding.tvSheetCustomerAddress.text = fullAddressText

            val currentStatus = detail.status.uppercase()

            // 🔥 RESTRICTION: Only allow item cancellation if the order is still "ORDERED"
            val canCancelItems = currentStatus == "ORDERED"
            productsAdapter.canCancelItems = canCancelItems

            productsAdapter.submitList(detail.products)
            setupActionButtons(currentStatus)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupActionButtons(currentStatus: String) {
        binding.btnUpdateStatus.visibility = View.GONE
        binding.tvStatusMessage.visibility = View.GONE

        when (currentStatus) {
            "ORDERED" -> {
                binding.btnUpdateStatus.text = "Confirm Order"
                binding.btnUpdateStatus.visibility = View.VISIBLE
                binding.btnUpdateStatus.setOnClickListener { updateStatus("CONFIRMED") }


            }

            "CONFIRMED" -> {
                binding.btnUpdateStatus.text = "Start Processing"
                binding.btnUpdateStatus.visibility = View.VISIBLE
                binding.btnUpdateStatus.setOnClickListener { updateStatus("PROCESSING") }
            }

            "PROCESSING" -> {
                binding.btnUpdateStatus.text = "Mark Ready for Pickup"
                binding.btnUpdateStatus.visibility = View.VISIBLE
                binding.btnUpdateStatus.setOnClickListener { updateStatus("READY_FOR_PICKUP") }
            }

            "READY_FOR_PICKUP" -> {
                binding.btnUpdateStatus.text = "Package Picked Up"
                binding.btnUpdateStatus.visibility = View.VISIBLE
                binding.btnUpdateStatus.setOnClickListener { updateStatus("PICKED_UP") }
            }

            "PICKED_UP" -> {
                binding.btnUpdateStatus.text = "Mark Out for Delivery"
                binding.btnUpdateStatus.visibility = View.VISIBLE
                binding.btnUpdateStatus.setOnClickListener { updateStatus("OUT_FOR_DELIVERY") }
            }

            "OUT_FOR_DELIVERY" -> {
                binding.btnUpdateStatus.text = "Mark as Delivered"
                binding.btnUpdateStatus.visibility = View.VISIBLE
                binding.btnUpdateStatus.setOnClickListener { updateStatus("DELIVERED") }
            }

            "DELIVERED" -> {
                binding.tvStatusMessage.text = "Order delivered successfully."
                binding.tvStatusMessage.visibility = View.VISIBLE
            }

            "RETURN_REQUESTED" -> {
                binding.btnUpdateStatus.text = "Item Received (Approve Return)"
                binding.btnUpdateStatus.visibility = View.VISIBLE
                binding.btnUpdateStatus.setOnClickListener { updateStatus("RETURNED") }
            }

            "CANCELLED" -> {
                binding.tvStatusMessage.text = "This order was cancelled."
                binding.tvStatusMessage.visibility = View.VISIBLE
            }

            "RETURNED" -> {
                binding.tvStatusMessage.text = "Return completed."
                binding.tvStatusMessage.visibility = View.VISIBLE
            }

            else -> {
                binding.tvStatusMessage.text = "Current Status: $currentStatus"
                binding.tvStatusMessage.visibility = View.VISIBLE
            }
        }
    }

    private fun updateStatus(newStatus: String) {
        val token = LangPrefManager(requireContext()).getToken() ?: return
        viewModel.updateStatus(token, currentOrderId, newStatus, "", "")
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ORDER_ID = "arg_order_id"

        fun newInstance(orderId: Int): OrderDetailBottomSheetFragment {
            val fragment = OrderDetailBottomSheetFragment()
            val args = Bundle().apply {
                putInt(ARG_ORDER_ID, orderId)
            }
            fragment.arguments = args
            return fragment
        }
    }
}