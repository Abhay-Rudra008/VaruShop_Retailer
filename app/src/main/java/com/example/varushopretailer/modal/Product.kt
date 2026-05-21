package com.example.varushopretailer.modal

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class Product(
    @SerializedName("id") val id: Int,
    @SerializedName("retailer_id") val retailerId: Int,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("price") val price: String,
    @SerializedName("discount_percent") val discount: String? = "0",
    @SerializedName("stock") val stock: Int,
    @SerializedName("status") val status: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("primary_image") val primaryImage: String?,
    @SerializedName("images") val images: List<String>? = emptyList(),
) : Parcelable {

    val firstImageUrl: String?
        get() = if (!images.isNullOrEmpty()) images[0] else primaryImage

    private val priceValue: Double get() = price.toDoubleOrNull() ?: 0.0
    private val discountValue: Double get() = discount?.toDoubleOrNull() ?: 0.0

    val finalPrice: Double
        get() = if (discountValue > 0) priceValue * (1 - (discountValue / 100)) else priceValue

    val isLowStock: Boolean get() = stock in 1..9
    val isOutOfStock: Boolean get() = stock <= 0
}