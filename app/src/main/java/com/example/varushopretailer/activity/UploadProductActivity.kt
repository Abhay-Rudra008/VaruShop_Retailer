package com.example.varushopretailer.activity

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.varushopretailer.adapter.ImagePickerAdapter
import com.example.varushopretailer.databinding.ActivityUploadProductBinding
import com.example.varushopretailer.helper.BaseActivity
import com.example.varushopretailer.helper.LangPrefManager
import com.example.varushopretailer.modal.Category
import com.example.varushopretailer.modal.Product
import com.example.varushopretailer.viewmodal.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UploadProductActivity : BaseActivity() {

    private lateinit var binding: ActivityUploadProductBinding
    private val selectedImages = mutableListOf<Uri>()
    private val deletedExistingUrls = mutableListOf<String>() // Tracker for deleted images

    private lateinit var imageAdapter: ImagePickerAdapter
    private var categoryList: List<Category> = emptyList()
    private var selectedCategoryId: Int = -1
    private var existingProduct: Product? = null

    private val viewModel: ProductViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        existingProduct = intent.getParcelableExtra("PRODUCT_DATA")

        setupToolbar()
        setupImageRecycler()
        setupUploadButton()
        observeViewModel()

        viewModel.fetchCategories()

        existingProduct?.let { product ->
            preFillBasicData(product)
            viewModel.fetchProductImages(product.id) // Fetch full images in background
        }
    }

    @SuppressLint("SetTextI18n")
    private fun preFillBasicData(product: Product) {
        binding.toolbar.title = "Update Product"
        binding.btnUpload.text = "Update Product"

        binding.etProductName.setText(product.name)
        binding.etProductDesc.setText(product.description)
        binding.etPrice.setText(product.price)
        binding.etStock.setText(product.stock.toString())

        // Load the discount percent
        binding.etDiscount.setText(product.discount?.toString() ?: "0")

        selectedCategoryId = product.categoryId
    }

    private fun loadImagesIntoAdapter(imagesList: List<String>) {
        selectedImages.clear()

        fun getCloudinaryUrl(imageString: String): String {
            return if (imageString.startsWith("http")) {
                imageString.replace("http://", "https://")
            } else {
                "https://res.cloudinary.com/YOUR_CLOUD_NAME/image/upload/$imageString" // Change this!
            }
        }

        imagesList.forEach { img ->
            selectedImages.add(getCloudinaryUrl(img).toUri())
        }

        imageAdapter.submitList(ArrayList(selectedImages))
    }

    private fun observeViewModel() {
        viewModel.categories.observe(this) { categories ->
            this.categoryList = categories
            val adapter = ArrayAdapter(
                this, android.R.layout.simple_dropdown_item_1line, categories.map { it.name })
            binding.autoCompleteCategory.setAdapter(adapter)

            existingProduct?.let { prod ->
                val catName = categories.find { it.id == prod.categoryId }?.name
                binding.autoCompleteCategory.setText(catName, false)
            }
        }

        viewModel.productImages.observe(this) { imagesList ->
            if (imagesList.isNotEmpty()) {
                loadImagesIntoAdapter(imagesList)
            }
        }

        binding.autoCompleteCategory.setOnItemClickListener { parent, _, position, _ ->
            val selectedName = parent.getItemAtPosition(position).toString()
            selectedCategoryId = categoryList.find { it.name == selectedName }?.id ?: -1
            binding.tilCategory.error = null
        }

        viewModel.isLoading.observe(this) { loading ->
            binding.loadingLayout.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnUpload.isEnabled = !loading
        }

        viewModel.message.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                viewModel.clearMessage()
            }
        }

        viewModel.uploadSuccess.observe(this) { success ->
            if (success) {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun setupUploadButton() {
        binding.btnUpload.setOnClickListener {
            val name = binding.etProductName.text.toString().trim()
            val desc = binding.etProductDesc.text.toString().trim()
            val price = binding.etPrice.text.toString().trim()
            val stock = binding.etStock.text.toString().trim()
            val discountText = binding.etDiscount.text.toString().trim().ifEmpty { "0" }
            val token = LangPrefManager(this).getToken() ?: return@setOnClickListener

            when {
                selectedImages.isEmpty() -> showToast("Please add at least one image")
                selectedCategoryId == -1 -> binding.tilCategory.error = "Select a category"
                name.isEmpty() -> binding.etProductName.error = "Required"
                price.isEmpty() -> binding.etPrice.error = "Required"
                else -> {
                    if (existingProduct != null) {
                        viewModel.updateProduct(
                            token = token,
                            productId = existingProduct!!.id,
                            name = name,
                            desc = desc,
                            price = price,
                            stock = stock,
                            catId = selectedCategoryId,
                            discount = discountText,
                            uris = selectedImages,
                            deletedUrls = deletedExistingUrls, // Send the tracked deletions!
                            context = this
                        )
                    } else {
                        viewModel.uploadProduct(
                            token = token,
                            name = name,
                            desc = desc,
                            price = price,
                            stock = stock,
                            catId = selectedCategoryId,
                            discount = discountText,
                            uris = selectedImages,
                            context = this
                        )
                    }
                }
            }
        }
    }

    private fun setupImageRecycler() {
        imageAdapter = ImagePickerAdapter(onRemoveClick = { position ->
            val uriToRemove = selectedImages[position]

            // If user removes a web image, track it for deletion on the backend
            if (uriToRemove.scheme?.startsWith("http") == true) {
                deletedExistingUrls.add(uriToRemove.toString())
            }

            selectedImages.removeAt(position)
            imageAdapter.submitList(ArrayList(selectedImages))
        }, onAddClick = { pickImagesLauncher.launch("image/*") })

        binding.rvImages.apply {
            layoutManager = LinearLayoutManager(
                this@UploadProductActivity, LinearLayoutManager.HORIZONTAL, false
            )
            adapter = imageAdapter
        }
    }

    private val pickImagesLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (!uris.isNullOrEmpty()) {
                selectedImages.addAll(uris)
                imageAdapter.submitList(ArrayList(selectedImages))
            }
        }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun showToast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}