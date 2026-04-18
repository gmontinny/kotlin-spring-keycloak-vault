package br.com.springbootkeycloakoauth2.repository

import br.com.springbootkeycloakoauth2.entity.Seller
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface SellerRepository : JpaRepository<Seller, String> {
    fun findBySellerState(state: String, pageable: Pageable): Page<Seller>
}
