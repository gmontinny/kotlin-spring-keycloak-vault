package br.com.springbootkeycloakoauth2.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "customers")
class Customer(
    @Id
    @Column(name = "customer_id", length = 50)
    val customerId: String,

    @Column(name = "customer_unique_id", length = 50, nullable = false)
    val customerUniqueId: String,

    @Column(name = "customer_zip_code_prefix", length = 10, nullable = false)
    val customerZipCodePrefix: String,

    @Column(name = "customer_city", length = 100, nullable = false)
    val customerCity: String,

    @Column(name = "customer_state", length = 2, nullable = false)
    val customerState: String
)
