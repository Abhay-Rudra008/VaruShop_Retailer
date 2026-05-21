package com.example.varushopretailer.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.varushopretailer.R
import com.example.varushopretailer.activity.UploadProductActivity
import com.example.varushopretailer.adapter.ProductAdapter
import com.example.varushopretailer.addSquishAnimation
import com.example.varushopretailer.databinding.FragmentManageProductsBinding
import com.example.varushopretailer.helper.LangPrefManager
import com.example.varushopretailer.modal.Product
import com.example.varushopretailer.viewmodal.ManageProductsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ManageProductsFragment : Fragment() {

    private var _binding: FragmentManageProductsBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private var searchJob: Job? = null
    private lateinit var prefManager: LangPrefManager

    private val editProductLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            fetchProducts(isInitial = false)
        }
    }

    private val viewModel: ManageProductsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageProductsBinding.inflate(inflater, container, false)
        prefManager = LangPrefManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupFilters()
        setupSwipeRefresh()
        observeViewModel()
        fetchProducts(isInitial = true)
        binding.fabUpload.setOnClickListener {
            startActivity(Intent(requireContext(), UploadProductActivity::class.java))
            binding.fabUpload.addSquishAnimation()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.apply {
                if (loading) {
                    shimmerView.startShimmer()
                    shimmerView.visibility = View.VISIBLE
                    rvProducts.visibility = View.GONE
                    llEmptyState.visibility = View.GONE
                    llErrorState.visibility = View.GONE
                } else {
                    shimmerView.stopShimmer()
                    shimmerView.visibility = View.GONE
                    swipeRefresh.isRefreshing = false
                    updateUiVisibility(viewModel.products.value)
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { isError ->
            if (isError == true) {
                binding.apply {
                    llErrorState.visibility = View.VISIBLE
                    rvProducts.visibility = View.GONE
                    llEmptyState.visibility = View.GONE
                }
            } else {
                updateUiVisibility(viewModel.products.value)
            }
        }

        viewModel.products.observe(viewLifecycleOwner) { data ->
            productAdapter.submitList(data)
            updateUiVisibility(data)
        }

        binding.btnRetry.setOnClickListener { fetchProducts(isInitial = true) }
    }

    private fun updateUiVisibility(data: List<Product>?) {
        if (viewModel.isLoading.value == true) return

        binding.apply {
            val isEmpty = data.isNullOrEmpty()
            llErrorState.visibility = if (viewModel.error.value == true) View.VISIBLE else View.GONE

            if (viewModel.error.value != true) {
                llEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
                rvProducts.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
        }
    }

    private fun setupSearch() {
        var lastQuery = ""
        binding.etSearchProducts.addTextChangedListener(afterTextChanged = { text ->
            val query = text.toString().trim()
            if (query == lastQuery) return@addTextChangedListener
            lastQuery = query

            searchJob?.cancel()
            searchJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(600)
                if (isAdded) fetchProducts(isInitial = false)
            }
        })
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(isEditable = true) { product ->
            val intent = Intent(requireContext(), UploadProductActivity::class.java).apply {
                putExtra("PRODUCT_DATA", product)
            }
            editProductLauncher.launch(intent)
        }

        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupFilters() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val imm =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.etSearchProducts.windowToken, 0)
                binding.etSearchProducts.clearFocus()

                fetchProducts(isInitial = true)
            }
        }
    }

    private fun fetchProducts(isInitial: Boolean = false) {
        if (!isAdded) return
        val token = prefManager.getToken() ?: return

        val search = binding.etSearchProducts.text.toString().trim().let {
            if (it.isEmpty()) null else it
        }

        val filter = when (binding.chipGroupFilters.checkedChipId) {
            R.id.chipLowStock -> "LOW_STOCK"
            R.id.chipOutOfStock -> "OUT_OF_STOCK"
            else -> null
        }

        viewModel.loadProducts(token, search, filter, isInitial)
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            fetchProducts(isInitial = false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}