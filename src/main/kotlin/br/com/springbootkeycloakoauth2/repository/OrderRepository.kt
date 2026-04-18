package br.com.springbootkeycloakoauth2.repository

import br.com.springbootkeycloakoauth2.entity.Order
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, String> {
    fun findByCustomerCustomerId(customerId: String, pageable: Pageable): Page<Order>
    fun findByOrderStatus(status: String, pageable: Pageable): Page<Order>
}
