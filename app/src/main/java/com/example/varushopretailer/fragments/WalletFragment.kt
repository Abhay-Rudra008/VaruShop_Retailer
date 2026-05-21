package com.example.varushopretailer.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.varushopretailer.adapter.TransactionAdapter
import com.example.varushopretailer.databinding.DialogWithdrawBinding
import com.example.varushopretailer.databinding.FragmentWalletBinding
import com.example.varushopretailer.viewmodal.WalletViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WalletFragment : Fragment() {

    private var _binding: FragmentWalletBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WalletViewModel by viewModels()

    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
        viewModel.loadWalletData()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter()
        binding.rvTransactions.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadWalletData()
        }

        binding.btnRetry.setOnClickListener {
            viewModel.loadWalletData()
        }

        binding.btnRequestWithdrawal.setOnClickListener {
            showWithdrawalDialog()
        }
    }

    private fun showWithdrawalDialog() {
        val dialogBinding = DialogWithdrawBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext()).setTitle("Request Payout")
            .setView(dialogBinding.root).setPositiveButton("Withdraw", null)
            .setNegativeButton("Cancel", null).create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            positiveButton.setOnClickListener {
                val amountStr = dialogBinding.etAmount.text.toString()
                val bankDetails = dialogBinding.etBankDetails.text.toString()

                if (amountStr.isNotEmpty() && bankDetails.isNotEmpty()) {
                    val amount = amountStr.toDoubleOrNull()

                    if (amount != null && amount >= 50.0) {
                        viewModel.requestWithdrawal(amount, bankDetails)
                        dialog.dismiss()
                    } else {
                        dialogBinding.etAmount.error = "Minimum withdrawal is ₹50"
                        dialogBinding.etAmount.requestFocus()
                    }
                } else {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        viewModel.balanceState.observe(viewLifecycleOwner) { state ->
            binding.tvAvailableBalance.text = "₹${state.availableBalance}"
            binding.tvTotalEarnings.text = "₹${state.totalEarnings}"
            binding.tvPendingWithdrawals.text = "₹${state.totalWithdrawn}"
        }

        viewModel.historyState.observe(viewLifecycleOwner) { state ->
            if (state.isEmpty) {
                binding.rvTransactions.visibility = View.GONE
                binding.layoutEmpty.visibility = View.VISIBLE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvTransactions.visibility = View.VISIBLE

                transactionAdapter.submitList(state.transactions.toList()) {
                    binding.rvTransactions.scrollToPosition(0)
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                val isSwiping = binding.swipeRefresh.isRefreshing
                val hasData = transactionAdapter.currentList.isNotEmpty()

                if (!isSwiping && !hasData) {
                    binding.shimmerLayout.visibility = View.VISIBLE
                    binding.shimmerLayout.startShimmer()
                    binding.rvTransactions.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.GONE
                    binding.llErrorState.visibility = View.GONE
                }
            } else {
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { isError ->
            if (isError == true) {
                val hasData = transactionAdapter.currentList.isNotEmpty()

                if (!hasData) {
                    binding.llErrorState.visibility = View.VISIBLE
                    binding.rvTransactions.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.GONE
                }

                Toast.makeText(requireContext(), viewModel.message.value, Toast.LENGTH_LONG).show()
            } else {
                binding.llErrorState.visibility = View.GONE
            }
        }

        viewModel.withdrawalSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(
                    requireContext(), "Withdrawal Requested Successfully!", Toast.LENGTH_LONG
                ).show()
                viewModel.resetWithdrawalState()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}