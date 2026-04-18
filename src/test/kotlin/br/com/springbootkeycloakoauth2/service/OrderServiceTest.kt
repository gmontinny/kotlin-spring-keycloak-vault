package br.com.springbootkeycloakoauth2.service

import br.com.springbootkeycloakoauth2.dto.request.OrderRequest
import br.com.springbootkeycloakoauth2.dto.response.OrderResponse
import br.com.springbootkeycloakoauth2.entity.Customer
import br.com.springbootkeycloakoauth2.entity.Order
import br.com.springbootkeycloakoauth2.exception.ResourceNotFoundException
import br.com.springbootkeycloakoauth2.mapper.OrderMapper
import br.com.springbootkeycloakoauth2.repository.CustomerRepository
import br.com.springbootkeycloakoauth2.repository.OrderRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class OrderServiceTest {

    @Mock
    lateinit var orderRepository: OrderRepository

    @Mock
    lateinit var customerRepository: CustomerRepository

    @Mock
    lateinit var orderMapper: OrderMapper

    @InjectMocks
    lateinit var orderService: OrderService

    private val customer = Customer(
        customerId = "c1",
        customerUniqueId = "u1",
        customerZipCodePrefix = "01001",
        customerCity = "São Paulo",
        customerState = "SP"
    )

    private val now = LocalDateTime.now()

    private val order = Order(
        orderId = "o1",
        customer = customer,
        orderStatus = "delivered",
        orderPurchaseTimestamp = now
    )

    private val response = OrderResponse(
        orderId = "o1",
        customerId = "c1",
        orderStatus = "delivered",
        orderPurchaseTimestamp = now,
        orderApprovedAt = null,
        orderDeliveredCarrierDate = null,
        orderDeliveredCustomerDate = null,
        orderEstimatedDeliveryDate = null
    )

    private val request = OrderRequest(
        customerId = "c1",
        orderStatus = "created"
    )

    @Test
    fun `findAll returns paged orders`() {
        val page = PageImpl(listOf(order))
        whenever(orderRepository.findAll(any<Pageable>())).thenReturn(page)
        whenever(orderMapper.toResponse(any())).thenReturn(response)

        val result = orderService.findAll(Pageable.unpaged())

        assertEquals(1, result.totalElements)
        assertEquals("delivered", result.content[0].orderStatus)
    }

    @Test
    fun `findById returns order when found`() {
        whenever(orderRepository.findById("o1")).thenReturn(Optional.of(order))
        whenever(orderMapper.toResponse(any())).thenReturn(response)

        val result = orderService.findById("o1")

        assertEquals("o1", result.orderId)
    }

    @Test
    fun `findById throws exception when not found`() {
        whenever(orderRepository.findById("invalid")).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            orderService.findById("invalid")
        }
    }

    @Test
    fun `create throws exception when customer not found`() {
        whenever(customerRepository.findById("invalid")).thenReturn(Optional.empty())

        val invalidRequest = OrderRequest(customerId = "invalid", orderStatus = "created")

        assertThrows<ResourceNotFoundException> {
            orderService.create(invalidRequest)
        }
    }

    @Test
    fun `create saves and returns order`() {
        whenever(customerRepository.findById("c1")).thenReturn(Optional.of(customer))
        whenever(orderMapper.toEntity(any(), any())).thenReturn(order)
        whenever(orderRepository.save(any<Order>())).thenReturn(order)
        whenever(orderMapper.toResponse(any())).thenReturn(response)

        val result = orderService.create(request)

        assertEquals("c1", result.customerId)
        verify(orderRepository).save(any<Order>())
    }

    @Test
    fun `updateStatus throws exception when not found`() {
        whenever(orderRepository.findById("invalid")).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            orderService.updateStatus("invalid", "shipped")
        }
    }

    @Test
    fun `updateStatus updates and returns order`() {
        val updatedResponse = response.copy(orderStatus = "shipped")
        whenever(orderRepository.findById("o1")).thenReturn(Optional.of(order))
        whenever(orderRepository.save(any<Order>())).thenReturn(order)
        whenever(orderMapper.toResponse(any())).thenReturn(updatedResponse)

        val result = orderService.updateStatus("o1", "shipped")

        assertEquals("shipped", result.orderStatus)
    }
}
