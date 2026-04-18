package br.com.springbootkeycloakoauth2.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class Order(
    @Id
    @Column(name = "order_id", length = 50)
    val orderId: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    val customer: Customer,

    @Column(name = "order_status", length = 20, nullable = false)
    val orderStatus: String,

    @Column(name = "order_purchase_timestamp", nullable = false)
    val orderPurchaseTimestamp: LocalDateTime,

    @Column(name = "order_approved_at")
    val orderApprovedAt: LocalDateTime? = null,

    @Column(name = "order_delivered_carrier_date")
    val orderDeliveredCarrierDate: LocalDateTime? = null,

    @Column(name = "order_delivered_customer_date")
    val orderDeliveredCustomerDate: LocalDateTime? = null,

    @Column(name = "order_estimated_delivery_date")
    val orderEstimatedDeliveryDate: LocalDateTime? = null,

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    val items: List<OrderItem> = emptyList(),

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    val payments: List<OrderPayment> = emptyList(),

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    val reviews: List<OrderReview> = emptyList()
)
