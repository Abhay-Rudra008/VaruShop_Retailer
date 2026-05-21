package com.example.varushopretailer.stats


import com.example.varushopretailer.modal.wallet.WithdrawRequest
import com.example.varushopretailer.viewmodal.BaseRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject


class RetailerRepository @Inject constructor(
    private val apiService: RetailerApiService
) : BaseRepository() {

    suspend fun loginRetailer(email: String, pass: String) = safeApiCall {
        apiService.login(mapOf("email" to email, "password" to pass))
    }

    suspend fun getProfile() = safeApiCall {
        apiService.getProfile()
    }

    suspend fun registerRetailer(
        name: String,
        email: String,
        pass: String,
        imageFile: File?
    ) = safeApiCall {

        val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val emailBody = email.toRequestBody("text/plain".toMediaTypeOrNull())
        val passBody = pass.toRequestBody("text/plain".toMediaTypeOrNull())

        val roleBody = "RETAILER".toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = imageFile?.let {
            val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("profile_image", it.name, requestFile)
        }

        apiService.register(nameBody, emailBody, passBody, roleBody, imagePart)
    }


    suspend fun getDashboardData(timeFilter: String? = null) = safeApiCall {
        apiService.getDashboard(timeFilter)
    }

    suspend fun getGraphData(timeFilter: String, metric: String) = safeApiCall {
        apiService.getGraphData(timeFilter, metric)
    }

    suspend fun getMonthlyRevenue(timeFilter: String? = null) = safeApiCall {
        apiService.getMonthlyRevenue(timeFilter)
    }

    suspend fun getTopSellingProducts(page: Int, limit: Int) = safeApiCall {
        apiService.getTopSellingProducts(page = page, limit = limit)
    }

    suspend fun getLowStock() = safeApiCall {
        apiService.getLowStock()
    }


    suspend fun getMyProducts(search: String?, filter: String?) = safeApiCall {
        apiService.getMyProducts(search, filter)
    }

    suspend fun getCategories() = safeApiCall {
        apiService.getCategories()
    }


    suspend fun getPayoutDashboard() = safeApiCall {
        apiService.getPayoutDashboard()
    }

    suspend fun requestWithdrawal(request: WithdrawRequest) = safeApiCall {
        apiService.requestWithdrawal(request)
    }

    suspend fun updateProduct(
        productId: Int,
        name: String,
        desc: String,
        price: String,
        stock: String,
        catId: Int,
        files: List<File>,
        discount: String = "0",
        deletedImageIds: List<Int> = emptyList()
    ) = safeApiCall {
        val imageParts = files.map { file ->
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("images", file.name, requestFile)
        }

        val deletedIdsJson = "[${deletedImageIds.joinToString(",")}]"

        apiService.updateProduct(
            id = productId,
            name = name.toRequestBody("text/plain".toMediaTypeOrNull()),
            description = desc.toRequestBody("text/plain".toMediaTypeOrNull()),
            price = price.toRequestBody("text/plain".toMediaTypeOrNull()),
            stock = stock.toRequestBody("text/plain".toMediaTypeOrNull()),
            categoryId = catId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
            discount = discount.toRequestBody("text/plain".toMediaTypeOrNull()),
            deletedImageIds = deletedIdsJson.toRequestBody("text/plain".toMediaTypeOrNull()),
            images = imageParts.ifEmpty { null }
        )
    }

    suspend fun uploadProductWithImages(
        name: String, desc: String, price: String,
        stock: String, catId: Int, discount: String, files: List<File>
    ) = safeApiCall {
        val imageParts = files.map { file ->
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("images", file.name, requestFile)
        }

        apiService.uploadProduct(
            name = name.toRequestBody("text/plain".toMediaTypeOrNull()),
            description = desc.toRequestBody("text/plain".toMediaTypeOrNull()),
            price = price.toRequestBody("text/plain".toMediaTypeOrNull()),
            stock = stock.toRequestBody("text/plain".toMediaTypeOrNull()),
            categoryId = catId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
            discount = discount.toRequestBody("text/plain".toMediaTypeOrNull()),
            images = imageParts
        )
    }


    suspend fun getRecentOrders() = safeApiCall {
        apiService.getRecentOrders()
    }

    suspend fun getOrderDetails(orderId: Int) = safeApiCall {
        apiService.getOrderDetails(orderId)
    }

    suspend fun getOrdersByStatus(status: String?, page: Int, limit: Int, search: String?) =
        safeApiCall {
            val statusQuery = if (status.isNullOrEmpty()) null else status
            apiService.getOrdersByStatus(statusQuery, page, limit, search)
        }

    suspend fun updateOrderStatus(orderId: Int, status: String) = safeApiCall {
        val body = mapOf("status" to status)


        apiService.updateOrderStatus(orderId, body)
    }
}