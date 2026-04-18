package br.com.springbootkeycloakoauth2.repository

import br.com.springbootkeycloakoauth2.entity.OrderReview
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface OrderReviewRepository : JpaRepository<OrderReview, String> {
    fun findByOrderOrderId(orderId: String, pageable: Pageable): Page<OrderReview>
}
