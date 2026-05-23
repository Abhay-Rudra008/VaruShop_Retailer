package com.example.varushopretailer.fragments


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
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
import androidx.core.net.toUri

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
            val intent = Intent(requireContext(), UploadProductActivity::class.java)
            editProductLauncher.launch(intent)
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


        viewModel.deleteSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Product deleted", Toast.LENGTH_SHORT).show()
                fetchProducts(isInitial = false)
            }
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
        productAdapter = ProductAdapter(isEditable = true, onItemClick = { product ->
            val intent = Intent(requireContext(), UploadProductActivity::class.java).apply {
                putExtra("PRODUCT_DATA", product)
            }
            editProductLauncher.launch(intent)
        }, onEmailClick = { product ->
            emailAdminForDeletedProduct(product)
        }, onDeleteClick = { product ->
            showDeleteConfirmationDialog(product)
        })

        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
            setHasFixedSize(true)
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun emailAdminForDeletedProduct(product: Product) {
        val adminEmail = "admin@varushop.com" // Replace with actual admin email
        val subject = "Regarding Deleted Product: ${product.name} (ID: ${product.id})"
        val body = """
            Hello Admin,

            I am reaching out regarding my product that was removed from the store.

            Product Details:
            Name: ${product.name}
            ID: ${product.id}

            Could you please clarify why this was deleted?

            Thank you.
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(adminEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(), "No email app found on this device.", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showDeleteConfirmationDialog(product: Product) {
        val input = EditText(requireContext()).apply {
            hint = "Enter ID: ${product.id}"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            val padding = (16 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
        }

        AlertDialog.Builder(requireContext()).setTitle("Permanently Delete Product")
            .setMessage("This action cannot be undone. To confirm, please type the product ID (${product.id}):")
            .setView(input)
            .setPositiveButton("Delete", null) // Set null initially to prevent auto-dismissal
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }.create().apply {
                setOnShowListener { dialog ->
                    val btnPositive = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
                    btnPositive.setTextColor(
                        ContextCompat.getColor(
                            requireContext(), R.color.status_red_text
                        )
                    ) // Make text red

                    btnPositive.setOnClickListener {
                        val typedId = input.text.toString().trim()

                        if (typedId == product.id.toString()) {
                            val token = prefManager.getToken()
                            if (token != null) {
                                viewModel.permanentlyDeleteProduct(token, product.id)
                            }
                            dialog.dismiss()
                        } else {
                            input.error = "ID does not match"
                        }
                    }
                }
                show()
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