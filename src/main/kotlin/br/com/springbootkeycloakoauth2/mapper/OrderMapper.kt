package br.com.springbootkeycloakoauth2.mapper

import br.com.springbootkeycloakoauth2.dto.request.OrderRequest
import br.com.springbootkeycloakoauth2.dto.response.OrderResponse
import br.com.springbootkeycloakoauth2.entity.Customer
import br.com.springbootkeycloakoauth2.entity.Order
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Component
class OrderMapper {

    fun toEntity(request: OrderRequest, customer: Customer): Order = Order(
        orderId = UUID.randomUUID().toString(),
        customer = customer,
        orderStatus = request.orderStatus,
        orderPurchaseTimestamp = LocalDateTime.now()
    )

    fun toResponse(entity: Order): OrderResponse = OrderResponse(
        orderId = entity.orderId,
        customerId = entity.customer.customerId,
        orderStatus = entity.orderStatus,
        orderPurchaseTimestamp = entity.orderPurchaseTimestamp,
        orderApprovedAt = entity.orderApprovedAt,
        orderDeliveredCarrierDate = entity.orderDeliveredCarrierDate,
        orderDeliveredCustomerDate = entity.orderDeliveredCustomerDate,
        orderEstimatedDeliveryDate = entity.orderEstimatedDeliveryDate
    )
}
