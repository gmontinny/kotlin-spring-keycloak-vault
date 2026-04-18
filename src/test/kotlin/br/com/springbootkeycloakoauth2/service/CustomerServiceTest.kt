package br.com.springbootkeycloakoauth2.service

import br.com.springbootkeycloakoauth2.dto.request.CustomerRequest
import br.com.springbootkeycloakoauth2.entity.Customer
import br.com.springbootkeycloakoauth2.exception.ResourceNotFoundException
import br.com.springbootkeycloakoauth2.mapper.CustomerMapper
import br.com.springbootkeycloakoauth2.repository.CustomerRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.*

@ExtendWith(MockitoExtension::class)
class CustomerServiceTest {

    @Mock
    lateinit var customerRepository: CustomerRepository

    @Mock
    lateinit var customerMapper: CustomerMapper

    @InjectMocks
    lateinit var customerService: CustomerService

    private val customer = Customer(
        customerId = "c1",
        customerUniqueId = "u1",
        customerZipCodePrefix = "01001",
        customerCity = "São Paulo",
        customerState = "SP"
    )

    private val request = CustomerRequest(
        customerZipCodePrefix = "01001",
        customerCity = "São Paulo",
        customerState = "SP"
    )

    @Test
    fun `findAll returns paged customers`() {
        val page = PageImpl(listOf(customer))
        `when`(customerRepository.findAll(any(Pageable::class.java))).thenReturn(page)
        `when`(customerMapper.toResponse(customer)).thenReturn(
            br.com.springbootkeycloakoauth2.dto.response.CustomerResponse("c1", "u1", "01001", "São Paulo", "SP")
        )

        val result = customerService.findAll(Pageable.unpaged())

        assertEquals(1, result.totalElements)
        assertEquals("c1", result.content[0].customerId)
    }

    @Test
    fun `findById returns customer when found`() {
        `when`(customerRepository.findById("c1")).thenReturn(Optional.of(customer))
        `when`(customerMapper.toResponse(customer)).thenReturn(
            br.com.springbootkeycloakoauth2.dto.response.CustomerResponse("c1", "u1", "01001", "São Paulo", "SP")
        )

        val result = customerService.findById("c1")

        assertEquals("c1", result.customerId)
        assertEquals("SP", result.customerState)
    }

    @Test
    fun `findById throws exception when not found`() {
        `when`(customerRepository.findById("invalid")).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            customerService.findById("invalid")
        }
    }

    @Test
    fun `create saves and returns customer`() {
        `when`(customerMapper.toEntity(request)).thenReturn(customer)
        `when`(customerRepository.save(customer)).thenReturn(customer)
        `when`(customerMapper.toResponse(customer)).thenReturn(
            br.com.springbootkeycloakoauth2.dto.response.CustomerResponse("c1", "u1", "01001", "São Paulo", "SP")
        )

        val result = customerService.create(request)

        assertEquals("São Paulo", result.customerCity)
        verify(customerRepository).save(customer)
    }

    @Test
    fun `update throws exception when not found`() {
        `when`(customerRepository.findById("invalid")).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            customerService.update("invalid", request)
        }
    }

    @Test
    fun `delete throws exception when not found`() {
        `when`(customerRepository.existsById("invalid")).thenReturn(false)

        assertThrows<ResourceNotFoundException> {
            customerService.delete("invalid")
        }
    }

    @Test
    fun `delete removes customer when found`() {
        `when`(customerRepository.existsById("c1")).thenReturn(true)

        customerService.delete("c1")

        verify(customerRepository).deleteById("c1")
    }
}
