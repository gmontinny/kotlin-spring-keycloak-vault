package br.com.springbootkeycloakoauth2.repository

import br.com.springbootkeycloakoauth2.entity.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<Product, String> {
    fun findByProductCategoryName(categoryName: String, pageable: Pageable): Page<Product>
}
