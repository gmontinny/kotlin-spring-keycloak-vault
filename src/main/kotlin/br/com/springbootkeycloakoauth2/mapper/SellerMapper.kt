package br.com.springbootkeycloakoauth2.mapper

import br.com.springbootkeycloakoauth2.dto.request.SellerRequest
import br.com.springbootkeycloakoauth2.dto.response.SellerResponse
import br.com.springbootkeycloakoauth2.entity.Seller
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SellerMapper {

    fun toEntity(request: SellerRequest): Seller = Seller(
        sellerId = UUID.randomUUID().toString(),
        sellerZipCodePrefix = request.sellerZipCodePrefix,
        sellerCity = request.sellerCity,
        sellerState = request.sellerState
    )

    fun toEntity(id: String, request: SellerRequest): Seller = Seller(
        sellerId = id,
        sellerZipCodePrefix = request.sellerZipCodePrefix,
        sellerCity = request.sellerCity,
        sellerState = request.sellerState
    )

    fun toResponse(entity: Seller): SellerResponse = SellerResponse(
        sellerId = entity.sellerId,
        sellerZipCodePrefix = entity.sellerZipCodePrefix,
        sellerCity = entity.sellerCity,
        sellerState = entity.sellerState
    )
}
