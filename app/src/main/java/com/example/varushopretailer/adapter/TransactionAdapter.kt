package com.example.varushopretailer.adapter


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.varushopretailer.databinding.ItemTransactionBinding
import com.example.varushopretailer.modal.wallet.Transaction


class TransactionAdapter :
    ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding =
            ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(transaction: Transaction) {
            binding.tvAmount.text = "₹${transaction.amount}"
            binding.tvBankDetails.text = transaction.bank_account_details

            binding.tvDate.text = transaction.created_at.take(10)

            binding.tvStatus.text = transaction.status.uppercase()

            when (transaction.status.uppercase()) {
                "REQUESTED", "PENDING" -> {
                    binding.tvStatus.setTextColor("#FFA000".toColorInt()) // Orange
                }

                "PAID_OUT", "CLEARED", "SETTLED" -> {
                    binding.tvStatus.setTextColor("#4CAF50".toColorInt()) // Green
                }

                "REJECTED", "REFUNDED", "FAILED" -> {
                    binding.tvStatus.setTextColor("#F44336".toColorInt()) // Red
                }

                else -> {
                    binding.tvStatus.setTextColor("#757575".toColorInt()) // Gray
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}