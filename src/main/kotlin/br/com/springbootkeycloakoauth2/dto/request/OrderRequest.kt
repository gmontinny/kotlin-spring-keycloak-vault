package br.com.springbootkeycloakoauth2.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class OrderRequest(
    @field:NotBlank(message = "Customer ID is required")
    @field:Size(max = 50, message = "Customer ID must be at most 50 characters")
    val customerId: String,

    @field:NotBlank(message = "Order status is required")
    @field:Size(max = 20, message = "Order status must be at most 20 characters")
    val orderStatus: String
)
