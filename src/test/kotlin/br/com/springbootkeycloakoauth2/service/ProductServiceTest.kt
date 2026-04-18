package br.com.springbootkeycloakoauth2.service

import br.com.springbootkeycloakoauth2.dto.request.ProductRequest
import br.com.springbootkeycloakoauth2.dto.response.ProductResponse
import br.com.springbootkeycloakoauth2.entity.Product
import br.com.springbootkeycloakoauth2.exception.ResourceNotFoundException
import br.com.springbootkeycloakoauth2.mapper.ProductMapper
import br.com.springbootkeycloakoauth2.repository.ProductRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.*

@ExtendWith(MockitoExtension::class)
class ProductServiceTest {

    @Mock
    lateinit var productRepository: ProductRepository

    @Mock
    lateinit var productMapper: ProductMapper

    @InjectMocks
    lateinit var productService: ProductService

    private val product = Product(
        productId = "p1",
        productCategoryName = "electronics",
        productWeightG = 500,
        productLengthCm = 20,
        productHeightCm = 10,
        productWidthCm = 15
    )

    private val response = ProductResponse(
        productId = "p1",
        productCategoryName = "electronics",
        productNameLength = null,
        productDescriptionLength = null,
        productPhotosQty = null,
        productWeightG = 500,
        productLengthCm = 20,
        productHeightCm = 10,
        productWidthCm = 15
    )

    private val request = ProductRequest(
        productCategoryName = "electronics",
        productWeightG = 500,
        productLengthCm = 20,
        productHeightCm = 10,
        productWidthCm = 15
    )

    @Test
    fun `findAll returns paged products`() {
        val page = PageImpl(listOf(product))
        `when`(productRepository.findAll(any(Pageable::class.java))).thenReturn(page)
        `when`(productMapper.toResponse(product)).thenReturn(response)

        val result = productService.findAll(Pageable.unpaged())

        assertEquals(1, result.totalElements)
        assertEquals("electronics", result.content[0].productCategoryName)
    }

    @Test
    fun `findById returns product when found`() {
        `when`(productRepository.findById("p1")).thenReturn(Optional.of(product))
        `when`(productMapper.toResponse(product)).thenReturn(response)

        val result = productService.findById("p1")

        assertEquals("p1", result.productId)
        assertEquals(500, result.productWeightG)
    }

    @Test
    fun `findById throws exception when not found`() {
        `when`(productRepository.findById("invalid")).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            productService.findById("invalid")
        }
    }

    @Test
    fun `create saves and returns product`() {
        `when`(productMapper.toEntity(request)).thenReturn(product)
        `when`(productRepository.save(product)).thenReturn(product)
        `when`(productMapper.toResponse(product)).thenReturn(response)

        val result = productService.create(request)

        assertEquals("electronics", result.productCategoryName)
        verify(productRepository).save(product)
    }

    @Test
    fun `delete throws exception when not found`() {
        `when`(productRepository.existsById("invalid")).thenReturn(false)

        assertThrows<ResourceNotFoundException> {
            productService.delete("invalid")
        }
    }
}
