package com.example.varushopretailer.stats

import com.example.varushopretailer.modal.ApiResponse
import com.example.varushopretailer.modal.Category
import com.example.varushopretailer.modal.DashboardData
import com.example.varushopretailer.modal.GraphData
import com.example.varushopretailer.modal.LoginData
import com.example.varushopretailer.modal.LoginResponse
import com.example.varushopretailer.modal.LowStockProduct
import com.example.varushopretailer.modal.MonthlyRevenue
import com.example.varushopretailer.modal.Order
import com.example.varushopretailer.modal.Product
import com.example.varushopretailer.modal.User
import com.example.varushopretailer.modal.order.OrderDetailData
import com.example.varushopretailer.modal.wallet.PayoutData
import com.example.varushopretailer.modal.wallet.WithdrawRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface RetailerApiService {


    @POST("auth/login")
    suspend fun login(
        @Body body: Map<String, String>
    ): LoginResponse

    @Multipart
    @POST("auth/register")
    suspend fun register(
        @Part("name") name: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part("role") role: RequestBody,
        @Part profile_image: MultipartBody.Part?
    ): ApiResponse<Unit>

    @GET("auth/profile")
    suspend fun getProfile(): ApiResponse<User>

    @POST("auth/refresh")
    fun refreshTokenSync(
        @Header("Authorization") refreshToken: String
    ): retrofit2.Call<ApiResponse<LoginData>>


    @GET("retailer/dashboard")
    suspend fun getDashboard(
        @Query("timeFilter") timeFilter: String? = null
    ): ApiResponse<DashboardData>

    @GET("retailer/analytics/graph-data")
    suspend fun getGraphData(
        @Query("timeFilter") timeFilter: String, @Query("metric") metric: String
    ): ApiResponse<List<GraphData>>

    @GET("retailer/analytics/top-products")
    suspend fun getTopSellingProducts(
        @Query("page") page: Int, @Query("limit") limit: Int = 10
    ): ApiResponse<List<Product>>

    @GET("retailer/analytics/monthly-revenue")
    suspend fun getMonthlyRevenue(
        @Query("timeFilter") timeFilter: String? = null
    ): ApiResponse<List<MonthlyRevenue>>

    @GET("retailer/analytics/low-stock")
    suspend fun getLowStock(): ApiResponse<List<LowStockProduct>>


    @GET("retailer/orders")
    suspend fun getOrdersByStatus(
        @Query("status") status: String?,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("search") search: String?
    ): ApiResponse<List<Order>>

    @GET("retailer/orders/{orderId}")
    suspend fun getOrderDetails(
        @Path("orderId") orderId: Int
    ): ApiResponse<OrderDetailData>

    @GET("retailer/orders/recent")
    suspend fun getRecentOrders(): ApiResponse<List<Order>>


    @GET("retailer/products")
    suspend fun getMyProducts(
        @Query("search") search: String?, @Query("filter") filter: String?
    ): ApiResponse<List<Product>>

    @GET("retailer/categories")
    suspend fun getCategories(): ApiResponse<List<Category>>

    @DELETE("retailer/products/delete/{id}")
    suspend fun deleteProduct(
        @Path("id") productId: Int
    ): ApiResponse<Unit>

    @Multipart
    @PUT("retailer/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("stock") stock: RequestBody,
        @Part("category_id") categoryId: RequestBody,
        @Part("discount") discount: RequestBody,
        @Part("deletedImageIds") deletedImageIds: RequestBody,
        @Part images: List<MultipartBody.Part>?
    ): ApiResponse<Unit>

    @Multipart
    @POST("retailer/products")
    suspend fun uploadProduct(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("stock") stock: RequestBody,
        @Part("category_id") categoryId: RequestBody,
        @Part("discount") discount: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): ApiResponse<Unit>

    @PUT("retailer/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") orderId: Int, @Body statusMap: Map<String, String>
    ): ApiResponse<Unit>

    @GET("retailer/payouts")
    suspend fun getPayoutDashboard(): ApiResponse<PayoutData>

    @POST("retailer/payouts/withdraw")
    suspend fun requestWithdrawal(
        @Body request: WithdrawRequest
    ): ApiResponse<Unit>


}