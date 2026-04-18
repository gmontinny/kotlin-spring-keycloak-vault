package br.com.springbootkeycloakoauth2.service

import br.com.springbootkeycloakoauth2.dto.request.ProductRequest
import br.com.springbootkeycloakoauth2.dto.response.ProductResponse
import br.com.springbootkeycloakoauth2.exception.ResourceNotFoundException
import br.com.springbootkeycloakoauth2.mapper.ProductMapper
import br.com.springbootkeycloakoauth2.repository.ProductRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ProductService(
    private val productRepository: ProductRepository,
    private val productMapper: ProductMapper
) {

    fun findAll(pageable: Pageable): Page<ProductResponse> =
        productRepository.findAll(pageable).map { productMapper.toResponse(it) }

    fun findById(id: String): ProductResponse =
        productRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Product not found with id: $id") }
            .let { productMapper.toResponse(it) }

    fun findByCategory(categoryName: String, pageable: Pageable): Page<ProductResponse> =
        productRepository.findByProductCategoryName(categoryName, pageable).map { productMapper.toResponse(it) }

    @Transactional
    fun create(request: ProductRequest): ProductResponse {
        val product = productMapper.toEntity(request)
        return productMapper.toResponse(productRepository.save(product))
    }

    @Transactional
    fun update(id: String, request: ProductRequest): ProductResponse {
        if (!productRepository.existsById(id)) {
            throw ResourceNotFoundException("Product not found with id: $id")
        }
        val product = productMapper.toEntity(id, request)
        return productMapper.toResponse(productRepository.save(product))
    }

    @Transactional
    fun delete(id: String) {
        if (!productRepository.existsById(id)) {
            throw ResourceNotFoundException("Product not found with id: $id")
        }
        productRepository.deleteById(id)
    }
}
