package br.com.springbootkeycloakoauth2.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

data class ProductRequest(
    @field:Size(max = 100, message = "Category name must be at most 100 characters")
    val productCategoryName: String? = null,

    @field:Min(value = 0, message = "Name length must be positive")
    val productNameLength: Int? = null,

    @field:Min(value = 0, message = "Description length must be positive")
    val productDescriptionLength: Int? = null,

    @field:Min(value = 0, message = "Photos quantity must be positive")
    val productPhotosQty: Int? = null,

    @field:Min(value = 0, message = "Weight must be positive")
    val productWeightG: Int? = null,

    @field:Min(value = 0, message = "Length must be positive")
    val productLengthCm: Int? = null,

    @field:Min(value = 0, message = "Height must be positive")
    val productHeightCm: Int? = null,

    @field:Min(value = 0, message = "Width must be positive")
    val productWidthCm: Int? = null
)
