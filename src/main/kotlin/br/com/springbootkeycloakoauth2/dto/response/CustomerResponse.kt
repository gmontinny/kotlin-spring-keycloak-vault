package br.com.springbootkeycloakoauth2.dto.response

import org.springframework.hateoas.RepresentationModel

data class CustomerResponse(
    val customerId: String,
    val customerUniqueId: String,
    val customerZipCodePrefix: String,
    val customerCity: String,
    val customerState: String
) : RepresentationModel<CustomerResponse>()
