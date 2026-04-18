package br.com.springbootkeycloakoauth2.dto.response

import org.springframework.hateoas.RepresentationModel
import java.time.LocalDateTime

data class OrderResponse(
    val orderId: String,
    val customerId: String,
    val orderStatus: String,
    val orderPurchaseTimestamp: LocalDateTime,
    val orderApprovedAt: LocalDateTime?,
    val orderDeliveredCarrierDate: LocalDateTime?,
    val orderDeliveredCustomerDate: LocalDateTime?,
    val orderEstimatedDeliveryDate: LocalDateTime?
) : RepresentationModel<OrderResponse>()
