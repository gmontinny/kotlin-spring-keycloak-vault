package br.com.springbootkeycloakoauth2.mapper

import br.com.springbootkeycloakoauth2.dto.request.CustomerRequest
import br.com.springbootkeycloakoauth2.dto.response.CustomerResponse
import br.com.springbootkeycloakoauth2.entity.Customer
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CustomerMapper {

    fun toEntity(request: CustomerRequest): Customer = Customer(
        customerId = UUID.randomUUID().toString(),
        customerUniqueId = UUID.randomUUID().toString(),
        customerZipCodePrefix = request.customerZipCodePrefix,
        customerCity = request.customerCity,
        customerState = request.customerState
    )

    fun toEntity(id: String, request: CustomerRequest, existingUniqueId: String): Customer = Customer(
        customerId = id,
        customerUniqueId = existingUniqueId,
        customerZipCodePrefix = request.customerZipCodePrefix,
        customerCity = request.customerCity,
        customerState = request.customerState
    )

    fun toResponse(entity: Customer): CustomerResponse = CustomerResponse(
        customerId = entity.customerId,
        customerUniqueId = entity.customerUniqueId,
        customerZipCodePrefix = entity.customerZipCodePrefix,
        customerCity = entity.customerCity,
        customerState = entity.customerState
    )
}
