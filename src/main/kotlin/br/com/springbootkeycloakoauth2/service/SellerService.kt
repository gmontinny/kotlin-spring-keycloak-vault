package br.com.springbootkeycloakoauth2.service

import br.com.springbootkeycloakoauth2.dto.request.SellerRequest
import br.com.springbootkeycloakoauth2.dto.response.SellerResponse
import br.com.springbootkeycloakoauth2.exception.ResourceNotFoundException
import br.com.springbootkeycloakoauth2.mapper.SellerMapper
import br.com.springbootkeycloakoauth2.repository.SellerRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SellerService(
    private val sellerRepository: SellerRepository,
    private val sellerMapper: SellerMapper
) {

    fun findAll(pageable: Pageable): Page<SellerResponse> =
        sellerRepository.findAll(pageable).map { sellerMapper.toResponse(it) }

    fun findById(id: String): SellerResponse =
        sellerRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Seller not found with id: $id") }
            .let { sellerMapper.toResponse(it) }

    fun findByState(state: String, pageable: Pageable): Page<SellerResponse> =
        sellerRepository.findBySellerState(state, pageable).map { sellerMapper.toResponse(it) }

    @Transactional
    fun create(request: SellerRequest): SellerResponse {
        val seller = sellerMapper.toEntity(request)
        return sellerMapper.toResponse(sellerRepository.save(seller))
    }

    @Transactional
    fun update(id: String, request: SellerRequest): SellerResponse {
        if (!sellerRepository.existsById(id)) {
            throw ResourceNotFoundException("Seller not found with id: $id")
        }
        val seller = sellerMapper.toEntity(id, request)
        return sellerMapper.toResponse(sellerRepository.save(seller))
    }

    @Transactional
    fun delete(id: String) {
        if (!sellerRepository.existsById(id)) {
            throw ResourceNotFoundException("Seller not found with id: $id")
        }
        sellerRepository.deleteById(id)
    }
}
