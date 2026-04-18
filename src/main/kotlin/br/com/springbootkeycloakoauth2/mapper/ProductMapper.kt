package br.com.springbootkeycloakoauth2.mapper

import br.com.springbootkeycloakoauth2.dto.request.ProductRequest
import br.com.springbootkeycloakoauth2.dto.response.ProductResponse
import br.com.springbootkeycloakoauth2.entity.Product
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ProductMapper {

    fun toEntity(request: ProductRequest): Product = Product(
        productId = UUID.randomUUID().toString(),
        productCategoryName = request.productCategoryName,
        productNameLength = request.productNameLength,
        productDescriptionLength = request.productDescriptionLength,
        productPhotosQty = request.productPhotosQty,
        productWeightG = request.productWeightG,
        productLengthCm = request.productLengthCm,
        productHeightCm = request.productHeightCm,
        productWidthCm = request.productWidthCm
    )

    fun toEntity(id: String, request: ProductRequest): Product = Product(
        productId = id,
        productCategoryName = request.productCategoryName,
        productNameLength = request.productNameLength,
        productDescriptionLength = request.productDescriptionLength,
        productPhotosQty = request.productPhotosQty,
        productWeightG = request.productWeightG,
        productLengthCm = request.productLengthCm,
        productHeightCm = request.productHeightCm,
        productWidthCm = request.productWidthCm
    )

    fun toResponse(entity: Product): ProductResponse = ProductResponse(
        productId = entity.productId,
        productCategoryName = entity.productCategoryName,
        productNameLength = entity.productNameLength,
        productDescriptionLength = entity.productDescriptionLength,
        productPhotosQty = entity.productPhotosQty,
        productWeightG = entity.productWeightG,
        productLengthCm = entity.productLengthCm,
        productHeightCm = entity.productHeightCm,
        productWidthCm = entity.productWidthCm
    )
}
