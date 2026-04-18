package br.com.springbootkeycloakoauth2.dto.response

import org.springframework.hateoas.RepresentationModel

data class ProductResponse(
    val productId: String,
    val productCategoryName: String?,
    val productNameLength: Int?,
    val productDescriptionLength: Int?,
    val productPhotosQty: Int?,
    val productWeightG: Int?,
    val productLengthCm: Int?,
    val productHeightCm: Int?,
    val productWidthCm: Int?
) : RepresentationModel<ProductResponse>()
