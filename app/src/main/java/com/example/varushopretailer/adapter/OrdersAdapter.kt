package com.example.varushopretailer.adapter


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.varushopretailer.databinding.ItemOrderPreviewBinding
import com.example.varushopretailer.modal.Order


class OrdersAdapter(private val onItemClick: (Order) -> Unit) :
    ListAdapter<Order, OrdersAdapter.OrderViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(
            ItemOrderPreviewBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(private val binding: ItemOrderPreviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        @SuppressLint("DefaultLocale", "SetTextI18n")
        fun bind(order: Order) {
            binding.tvOrderId.text = "#ORD-${order.order_id}"

            val amount = order.total_amount ?: 0.0
            binding.tvOrderAmount.text = "₹${String.format("%,.2f", amount)}"

            val rawDate = order.created_at ?: ""
            binding.tvOrderDate.text = if (rawDate.contains("T")) {
                rawDate.split("T")[0]
            } else {
                rawDate
            }

            if (order.address.isNullOrEmpty()) {
                binding.tvCustomerName.visibility = View.GONE
            } else {
                binding.tvCustomerName.visibility = View.VISIBLE
                binding.tvCustomerName.text = order.address
                binding.tvCustomerName.isSelected = true
            }

            binding.tvItemCount.visibility = View.GONE

            val status = order.status?.uppercase() ?: "PENDING"
            binding.tvStatusText.text = status
            setupStatusBadge(status)
        }

        private fun setupStatusBadge(status: String) {
            val (bgColor, textColor) = when (status) {
                "ORDERED", "PENDING" -> "#26FFC107" to "#FFC107"
                "CONFIRMED", "PROCESSING" -> "#262196F3" to "#2196F3"
                "SHIPPED", "OUT_FOR_DELIVERY" -> "#269C27B0" to "#9C27B0"
                "DELIVERED" -> "#2628A745" to "#28A745"
                "CANCELLED", "RETURNED" -> "#26F44336" to "#F44336"
                else -> "#269E9E9E" to "#9E9E9E"
            }

            binding.cvStatusBadge.setCardBackgroundColor(bgColor.toColorInt())
            binding.tvStatusText.setTextColor(textColor.toColorInt())
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.order_id == newItem.order_id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}