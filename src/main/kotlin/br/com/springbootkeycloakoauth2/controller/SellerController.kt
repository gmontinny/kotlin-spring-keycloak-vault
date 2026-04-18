package br.com.springbootkeycloakoauth2.controller

import br.com.springbootkeycloakoauth2.dto.request.SellerRequest
import br.com.springbootkeycloakoauth2.dto.response.SellerResponse
import br.com.springbootkeycloakoauth2.service.SellerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedResourcesAssembler
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.PagedModel
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/sellers")
@Tag(name = "Sellers", description = "Gerenciamento de vendedores")
class SellerController(
    private val sellerService: SellerService,
    private val pagedResourcesAssembler: PagedResourcesAssembler<SellerResponse>
) {

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Listar todos os vendedores", description = "Retorna lista paginada de vendedores com links HATEOAS")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Lista de vendedores retornada com sucesso"),
        ApiResponse(responseCode = "401", description = "Não autenticado")
    )
    fun findAll(pageable: Pageable): ResponseEntity<PagedModel<EntityModel<SellerResponse>>> {
        val page = sellerService.findAll(pageable)
        val pagedModel = pagedResourcesAssembler.toModel(page) { seller ->
            EntityModel.of(seller.addLinks())
        }
        return ResponseEntity.ok(pagedModel)
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Buscar vendedor por ID", description = "Retorna um vendedor específico com links HATEOAS")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Vendedor encontrado"),
        ApiResponse(responseCode = "404", description = "Vendedor não encontrado")
    )
    fun findById(@Parameter(description = "ID do vendedor") @PathVariable id: String): ResponseEntity<SellerResponse> {
        val seller = sellerService.findById(id).addLinks()
        return ResponseEntity.ok(seller)
    }

    @GetMapping("/state/{state}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Buscar vendedores por estado", description = "Retorna lista paginada de vendedores filtrados por estado")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Lista de vendedores retornada com sucesso")
    )
    fun findByState(
        @Parameter(description = "Sigla do estado (ex: SP, RJ)") @PathVariable state: String,
        pageable: Pageable
    ): ResponseEntity<PagedModel<EntityModel<SellerResponse>>> {
        val page = sellerService.findByState(state, pageable)
        val pagedModel = pagedResourcesAssembler.toModel(page) { seller ->
            EntityModel.of(seller.addLinks())
        }
        return ResponseEntity.ok(pagedModel)
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar novo vendedor", description = "Cadastra um novo vendedor no sistema")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Vendedor criado com sucesso"),
        ApiResponse(responseCode = "400", description = "Dados inválidos"),
        ApiResponse(responseCode = "403", description = "Sem permissão - requer role ADMIN")
    )
    fun create(@Valid @RequestBody request: SellerRequest): ResponseEntity<SellerResponse> {
        val seller = sellerService.create(request).addLinks()
        return ResponseEntity.status(HttpStatus.CREATED).body(seller)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar vendedor", description = "Atualiza os dados de um vendedor existente")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Vendedor atualizado com sucesso"),
        ApiResponse(responseCode = "400", description = "Dados inválidos"),
        ApiResponse(responseCode = "404", description = "Vendedor não encontrado"),
        ApiResponse(responseCode = "403", description = "Sem permissão - requer role ADMIN")
    )
    fun update(
        @Parameter(description = "ID do vendedor") @PathVariable id: String,
        @Valid @RequestBody request: SellerRequest
    ): ResponseEntity<SellerResponse> {
        val seller = sellerService.update(id, request).addLinks()
        return ResponseEntity.ok(seller)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar vendedor", description = "Remove um vendedor do sistema")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Vendedor removido com sucesso"),
        ApiResponse(responseCode = "404", description = "Vendedor não encontrado"),
        ApiResponse(responseCode = "403", description = "Sem permissão - requer role ADMIN")
    )
    fun delete(@Parameter(description = "ID do vendedor") @PathVariable id: String): ResponseEntity<Void> {
        sellerService.delete(id)
        return ResponseEntity.noContent().build()
    }

    private fun SellerResponse.addLinks(): SellerResponse {
        add(linkTo(methodOn(SellerController::class.java).findById(sellerId)).withSelfRel())
        add(linkTo(methodOn(SellerController::class.java).findAll(Pageable.unpaged())).withRel("sellers"))
        return this
    }
}
