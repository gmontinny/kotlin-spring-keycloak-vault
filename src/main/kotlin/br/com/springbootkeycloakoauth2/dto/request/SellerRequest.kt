package br.com.springbootkeycloakoauth2.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SellerRequest(
    @field:NotBlank(message = "Zip code prefix is required")
    @field:Size(max = 10, message = "Zip code prefix must be at most 10 characters")
    val sellerZipCodePrefix: String,

    @field:NotBlank(message = "City is required")
    @field:Size(max = 100, message = "City must be at most 100 characters")
    val sellerCity: String,

    @field:NotBlank(message = "State is required")
    @field:Size(min = 2, max = 2, message = "State must be exactly 2 characters")
    val sellerState: String
)
