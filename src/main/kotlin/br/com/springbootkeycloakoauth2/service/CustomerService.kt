package br.com.springbootkeycloakoauth2.service

import br.com.springbootkeycloakoauth2.dto.request.CustomerRequest
import br.com.springbootkeycloakoauth2.dto.response.CustomerResponse
import br.com.springbootkeycloakoauth2.exception.ResourceNotFoundException
import br.com.springbootkeycloakoauth2.mapper.CustomerMapper
import br.com.springbootkeycloakoauth2.repository.CustomerRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CustomerService(
    private val customerRepository: CustomerRepository,
    private val customerMapper: CustomerMapper
) {

    fun findAll(pageable: Pageable): Page<CustomerResponse> =
        customerRepository.findAll(pageable).map { customerMapper.toResponse(it) }

    fun findById(id: String): CustomerResponse =
        customerRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Customer not found with id: $id") }
            .let { customerMapper.toResponse(it) }

    fun findByState(state: String, pageable: Pageable): Page<CustomerResponse> =
        customerRepository.findByCustomerState(state, pageable).map { customerMapper.toResponse(it) }

    @Transactional
    fun create(request: CustomerRequest): CustomerResponse {
        val customer = customerMapper.toEntity(request)
        return customerMapper.toResponse(customerRepository.save(customer))
    }

    @Transactional
    fun update(id: String, request: CustomerRequest): CustomerResponse {
        val existing = customerRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Customer not found with id: $id") }
        val customer = customerMapper.toEntity(id, request, existing.customerUniqueId)
        return customerMapper.toResponse(customerRepository.save(customer))
    }

    @Transactional
    fun delete(id: String) {
        if (!customerRepository.existsById(id)) {
            throw ResourceNotFoundException("Customer not found with id: $id")
        }
        customerRepository.deleteById(id)
    }
}
