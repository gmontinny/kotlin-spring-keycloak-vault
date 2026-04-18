package br.com.springbootkeycloakoauth2.repository

import br.com.springbootkeycloakoauth2.entity.Customer
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface CustomerRepository : JpaRepository<Customer, String> {
    fun findByCustomerState(state: String, pageable: Pageable): Page<Customer>
    fun findByCustomerCity(city: String, pageable: Pageable): Page<Customer>
}
