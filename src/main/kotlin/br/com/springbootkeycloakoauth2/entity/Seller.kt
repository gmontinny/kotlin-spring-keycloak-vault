package br.com.springbootkeycloakoauth2.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "sellers")
class Seller(
    @Id
    @Column(name = "seller_id", length = 50)
    val sellerId: String,

    @Column(name = "seller_zip_code_prefix", length = 10, nullable = false)
    val sellerZipCodePrefix: String,

    @Column(name = "seller_city", length = 100, nullable = false)
    val sellerCity: String,

    @Column(name = "seller_state", length = 2, nullable = false)
    val sellerState: String
)
