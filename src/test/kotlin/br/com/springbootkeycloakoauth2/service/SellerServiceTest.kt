package br.com.springbootkeycloakoauth2.service

import br.com.springbootkeycloakoauth2.dto.request.SellerRequest
import br.com.springbootkeycloakoauth2.dto.response.SellerResponse
import br.com.springbootkeycloakoauth2.entity.Seller
import br.com.springbootkeycloakoauth2.exception.ResourceNotFoundException
import br.com.springbootkeycloakoauth2.mapper.SellerMapper
import br.com.springbootkeycloakoauth2.repository.SellerRepository
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
class SellerServiceTest {

    @Mock
    lateinit var sellerRepository: SellerRepository

    @Mock
    lateinit var sellerMapper: SellerMapper

    @InjectMocks
    lateinit var sellerService: SellerService

    private val seller = Seller(
        sellerId = "s1",
        sellerZipCodePrefix = "01001",
        sellerCity = "São Paulo",
        sellerState = "SP"
    )

    private val response = SellerResponse(
        sellerId = "s1",
        sellerZipCodePrefix = "01001",
        sellerCity = "São Paulo",
        sellerState = "SP"
    )

    private val request = SellerRequest(
        sellerZipCodePrefix = "01001",
        sellerCity = "São Paulo",
        sellerState = "SP"
    )

    @Test
    fun `findAll returns paged sellers`() {
        val page = PageImpl(listOf(seller))
        `when`(sellerRepository.findAll(any(Pageable::class.java))).thenReturn(page)
        `when`(sellerMapper.toResponse(seller)).thenReturn(response)

        val result = sellerService.findAll(Pageable.unpaged())

        assertEquals(1, result.totalElements)
        assertEquals("SP", result.content[0].sellerState)
    }

    @Test
    fun `findById returns seller when found`() {
        `when`(sellerRepository.findById("s1")).thenReturn(Optional.of(seller))
        `when`(sellerMapper.toResponse(seller)).thenReturn(response)

        val result = sellerService.findById("s1")

        assertEquals("s1", result.sellerId)
    }

    @Test
    fun `findById throws exception when not found`() {
        `when`(sellerRepository.findById("invalid")).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            sellerService.findById("invalid")
        }
    }

    @Test
    fun `create saves and returns seller`() {
        `when`(sellerMapper.toEntity(request)).thenReturn(seller)
        `when`(sellerRepository.save(seller)).thenReturn(seller)
        `when`(sellerMapper.toResponse(seller)).thenReturn(response)

        val result = sellerService.create(request)

        assertEquals("São Paulo", result.sellerCity)
        verify(sellerRepository).save(seller)
    }

    @Test
    fun `update throws exception when not found`() {
        `when`(sellerRepository.existsById("invalid")).thenReturn(false)

        assertThrows<ResourceNotFoundException> {
            sellerService.update("invalid", request)
        }
    }

    @Test
    fun `delete removes seller when found`() {
        `when`(sellerRepository.existsById("s1")).thenReturn(true)

        sellerService.delete("s1")

        verify(sellerRepository).deleteById("s1")
    }
}
