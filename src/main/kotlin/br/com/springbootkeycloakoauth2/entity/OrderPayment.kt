package br.com.springbootkeycloakoauth2.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "order_payments")
class OrderPayment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,

    @Column(name = "payment_sequential", nullable = false)
    val paymentSequential: Int,

    @Column(name = "payment_type", length = 30, nullable = false)
    val paymentType: String,

    @Column(name = "payment_installments", nullable = false)
    val paymentInstallments: Int,

    @Column(name = "payment_value", nullable = false, precision = 10, scale = 2)
    val paymentValue: BigDecimal
)
