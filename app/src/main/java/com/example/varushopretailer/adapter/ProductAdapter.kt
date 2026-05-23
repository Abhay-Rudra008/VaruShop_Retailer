package com.example.varushopretailer.adapter

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.varushopretailer.R
import com.example.varushopretailer.databinding.ItemProductManageBinding
import com.example.varushopretailer.modal.Product


class ProductAdapter(
    private val isEditable: Boolean = true,
    private val showActions: Boolean = true,
    private val onItemClick: (Product) -> Unit = {},
    private val onEmailClick: (Product) -> Unit = {},
    private val onDeleteClick: (Product) -> Unit = {}
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    inner class ProductViewHolder(private val binding: ItemProductManageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            // 1. Root Click (Disabled if Admin Deleted)
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val product = getItem(position)
                    if (!product.isAdminDeleted) {
                        onItemClick(product)
                    }
                }
            }

            // 2. Edit Click
            binding.btnEditProduct.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }

            // 3. Email Admin Click
            binding.btnEmailAdmin.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEmailClick(getItem(position))
                }
            }

            // 4. Delete Click (Always accessible)
            binding.btnDeleteProduct.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(getItem(position))
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(product: Product) {
            Glide.with(binding.ivProduct.context)
                .load(product.firstImageUrl)
                .placeholder(R.color.edit_text_bg)
                .error(R.drawable.app_logo)
                .transition(DrawableTransitionOptions.withCrossFade())
                .transform(CenterCrop(), RoundedCorners(24))
                .into(binding.ivProduct)

            binding.tvProductName.text = product.name ?: "Unnamed Product"

            if (!showActions) {
                // READ-ONLY MODE (For StatsFragment)
                binding.root.alpha = 1.0f
                binding.btnEditProduct.visibility = View.GONE
                binding.btnEmailAdmin.visibility = View.GONE
                binding.btnDeleteProduct.visibility = View.GONE
            } else {
                // MANAGE MODE (For ManageProductsFragment)
                if (product.isAdminDeleted) {
                    binding.root.alpha = 0.5f
                    binding.btnEditProduct.visibility = View.GONE
                    binding.btnEmailAdmin.visibility = View.VISIBLE
                } else {
                    binding.root.alpha = 1.0f
                    binding.btnEditProduct.visibility = if (isEditable) View.VISIBLE else View.GONE
                    binding.btnEmailAdmin.visibility = View.GONE
                }
                binding.btnDeleteProduct.visibility = View.VISIBLE
            }

            // --- PRICE LOGIC ---
            val originalPrice = product.price.toDoubleOrNull() ?: 0.0
            val discPercent = product.discount?.toDoubleOrNull() ?: 0.0

            if (discPercent > 0) {
                val discountedPrice = originalPrice - (originalPrice * (discPercent / 100))
                binding.tvProductPrice.text = "₹${discountedPrice.toInt()}"

                binding.tvOldPrice.apply {
                    visibility = View.VISIBLE
                    text = "₹${originalPrice.toInt()}"
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
            } else {
                binding.tvProductPrice.text = "₹${originalPrice.toInt()}"
                binding.tvOldPrice.visibility = View.GONE
            }

            // --- STOCK LOGIC ---
            updateStockUI(product)
        }

        private fun updateStockUI(product: Product) {
            val context = binding.tvStockStatus.context
            val (colorRes, bgRes, label) = when {
                product.isOutOfStock -> {
                    Triple(
                        R.color.status_red_text,
                        R.drawable.bg_status_pill_red,
                        context.getString(R.string.out_of_stock)
                    )
                }

                product.isLowStock -> {
                    Triple(
                        R.color.status_orange_text,
                        R.drawable.bg_status_pill_orange,
                        "${context.getString(R.string.low_stock)}: ${product.stock}"
                    )
                }

                else -> {
                    Triple(
                        R.color.status_green_text,
                        R.drawable.bg_status_pill_green,
                        "${context.getString(R.string.in_stock)}: ${product.stock}"
                    )
                }
            }

            binding.tvStockStatus.apply {
                text = label
                setTextColor(ContextCompat.getColor(context, colorRes))
                setBackgroundResource(bgRes)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductManageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}