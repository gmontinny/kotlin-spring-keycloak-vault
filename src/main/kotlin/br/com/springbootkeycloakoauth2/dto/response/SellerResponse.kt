package br.com.springbootkeycloakoauth2.dto.response

import org.springframework.hateoas.RepresentationModel

data class SellerResponse(
    val sellerId: String,
    val sellerZipCodePrefix: String,
    val sellerCity: String,
    val sellerState: String
) : RepresentationModel<SellerResponse>()
