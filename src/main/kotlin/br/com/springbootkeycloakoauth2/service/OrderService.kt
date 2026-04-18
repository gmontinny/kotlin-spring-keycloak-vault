package br.com.springbootkeycloakoauth2.service

import br.com.springbootkeycloakoauth2.dto.request.OrderRequest
import br.com.springbootkeycloakoauth2.dto.response.OrderResponse
import br.com.springbootkeycloakoauth2.entity.Order
import br.com.springbootkeycloakoauth2.exception.ResourceNotFoundException
import br.com.springbootkeycloakoauth2.mapper.OrderMapper
import br.com.springbootkeycloakoauth2.repository.CustomerRepository
import br.com.springbootkeycloakoauth2.repository.OrderRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class OrderService(
    private val orderRepository: OrderRepository,
    private val customerRepository: CustomerRepository,
    private val orderMapper: OrderMapper
) {

    fun findAll(pageable: Pageable): Page<OrderResponse> =
        orderRepository.findAll(pageable).map { orderMapper.toResponse(it) }

    fun findById(id: String): OrderResponse =
        orderRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Order not found with id: $id") }
            .let { orderMapper.toResponse(it) }

    fun findByCustomerId(customerId: String, pageable: Pageable): Page<OrderResponse> =
        orderRepository.findByCustomerCustomerId(customerId, pageable).map { orderMapper.toResponse(it) }

    fun findByStatus(status: String, pageable: Pageable): Page<OrderResponse> =
        orderRepository.findByOrderStatus(status, pageable).map { orderMapper.toResponse(it) }

    @Transactional
    fun create(request: OrderRequest): OrderResponse {
        val customer = customerRepository.findById(request.customerId)
            .orElseThrow { ResourceNotFoundException("Customer not found with id: ${request.customerId}") }
        val order = orderMapper.toEntity(request, customer)
        return orderMapper.toResponse(orderRepository.save(order))
    }

    @Transactional
    fun updateStatus(id: String, status: String): OrderResponse {
        val order = orderRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Order not found with id: $id") }

        val updatedOrder = Order(
            orderId = order.orderId,
            customer = order.customer,
            orderStatus = status,
            orderPurchaseTimestamp = order.orderPurchaseTimestamp,
            orderApprovedAt = order.orderApprovedAt,
            orderDeliveredCarrierDate = order.orderDeliveredCarrierDate,
            orderDeliveredCustomerDate = order.orderDeliveredCustomerDate,
            orderEstimatedDeliveryDate = order.orderEstimatedDeliveryDate
        )
        return orderMapper.toResponse(orderRepository.save(updatedOrder))
    }
}
