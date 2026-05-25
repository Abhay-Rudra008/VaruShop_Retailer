package com.example.varushopretailer.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Or use coil if you prefer
import com.example.varushopretailer.databinding.ItemOrderProductBinding
import com.example.varushopretailer.modal.order.OrderProduct

class OrderProductsAdapter(
    private val onCancelItemClick: (OrderProduct) -> Unit
) : ListAdapter<OrderProduct, OrderProductsAdapter.ProductViewHolder>(DiffCallback()) {

    var canCancelItems: Boolean = false
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemOrderProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(private val binding: ItemOrderProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "DefaultLocale")
        fun bind(product: OrderProduct) {
            binding.tvProductName.text = product.name
            binding.tvProductQty.text = "Qty: ${product.quantity}"
            binding.tvProductPrice.text = "₹${String.format("%,.2f", product.price)}"

            Glide.with(binding.root.context).load(product.image_url)
                .placeholder(android.R.color.darker_gray).into(binding.ivProductImage)

            val isCancelled = product.status?.equals("CANCELLED", ignoreCase = true) == true

            if (isCancelled) {
                binding.tvItemStatus.visibility = View.VISIBLE
                binding.tvItemStatus.text = "CANCELLED"
                binding.btnCancelItem.visibility = View.GONE
                binding.tvProductName.paintFlags = binding.tvProductName.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.tvItemStatus.visibility = View.GONE
                binding.tvProductName.paintFlags = binding.tvProductName.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()

                // Only show cancel button if the box hasn't shipped yet
                binding.btnCancelItem.visibility = if (canCancelItems) View.VISIBLE else View.GONE
            }

            binding.btnCancelItem.setOnClickListener {
                onCancelItemClick(product)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<OrderProduct>() {
        override fun areItemsTheSame(oldItem: OrderProduct, newItem: OrderProduct): Boolean {
            return oldItem.product_id == newItem.product_id
        }

        override fun areContentsTheSame(oldItem: OrderProduct, newItem: OrderProduct): Boolean {
            return oldItem == newItem
        }
    }
}