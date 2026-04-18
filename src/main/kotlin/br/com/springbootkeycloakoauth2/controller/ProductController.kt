package br.com.springbootkeycloakoauth2.controller

import br.com.springbootkeycloakoauth2.dto.request.ProductRequest
import br.com.springbootkeycloakoauth2.dto.response.ProductResponse
import br.com.springbootkeycloakoauth2.service.ProductService
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
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Gerenciamento de produtos")
class ProductController(
    private val productService: ProductService,
    private val pagedResourcesAssembler: PagedResourcesAssembler<ProductResponse>
) {

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Listar todos os produtos", description = "Retorna lista paginada de produtos com links HATEOAS")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso"),
        ApiResponse(responseCode = "401", description = "Não autenticado")
    )
    fun findAll(pageable: Pageable): ResponseEntity<PagedModel<EntityModel<ProductResponse>>> {
        val page = productService.findAll(pageable)
        val pagedModel = pagedResourcesAssembler.toModel(page) { product ->
            EntityModel.of(product.addLinks())
        }
        return ResponseEntity.ok(pagedModel)
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Buscar produto por ID", description = "Retorna um produto específico com links HATEOAS")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Produto encontrado"),
        ApiResponse(responseCode = "404", description = "Produto não encontrado")
    )
    fun findById(@Parameter(description = "ID do produto") @PathVariable id: String): ResponseEntity<ProductResponse> {
        val product = productService.findById(id).addLinks()
        return ResponseEntity.ok(product)
    }

    @GetMapping("/category/{categoryName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Buscar produtos por categoria", description = "Retorna lista paginada de produtos filtrados por categoria")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso")
    )
    fun findByCategory(
        @Parameter(description = "Nome da categoria") @PathVariable categoryName: String,
        pageable: Pageable
    ): ResponseEntity<PagedModel<EntityModel<ProductResponse>>> {
        val page = productService.findByCategory(categoryName, pageable)
        val pagedModel = pagedResourcesAssembler.toModel(page) { product ->
            EntityModel.of(product.addLinks())
        }
        return ResponseEntity.ok(pagedModel)
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar novo produto", description = "Cria um novo produto no catálogo")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Produto criado com sucesso"),
        ApiResponse(responseCode = "400", description = "Dados inválidos"),
        ApiResponse(responseCode = "403", description = "Sem permissão - requer role ADMIN")
    )
    fun create(@Valid @RequestBody request: ProductRequest): ResponseEntity<ProductResponse> {
        val product = productService.create(request).addLinks()
        return ResponseEntity.status(HttpStatus.CREATED).body(product)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar produto", description = "Atualiza os dados de um produto existente")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
        ApiResponse(responseCode = "400", description = "Dados inválidos"),
        ApiResponse(responseCode = "404", description = "Produto não encontrado"),
        ApiResponse(responseCode = "403", description = "Sem permissão - requer role ADMIN")
    )
    fun update(
        @Parameter(description = "ID do produto") @PathVariable id: String,
        @Valid @RequestBody request: ProductRequest
    ): ResponseEntity<ProductResponse> {
        val product = productService.update(id, request).addLinks()
        return ResponseEntity.ok(product)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar produto", description = "Remove um produto do catálogo")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Produto removido com sucesso"),
        ApiResponse(responseCode = "404", description = "Produto não encontrado"),
        ApiResponse(responseCode = "403", description = "Sem permissão - requer role ADMIN")
    )
    fun delete(@Parameter(description = "ID do produto") @PathVariable id: String): ResponseEntity<Void> {
        productService.delete(id)
        return ResponseEntity.noContent().build()
    }

    private fun ProductResponse.addLinks(): ProductResponse {
        add(linkTo(methodOn(ProductController::class.java).findById(productId)).withSelfRel())
        add(linkTo(methodOn(ProductController::class.java).findAll(Pageable.unpaged())).withRel("products"))
        productCategoryName?.let {
            add(linkTo(methodOn(ProductController::class.java).findByCategory(it, Pageable.unpaged())).withRel("category"))
        }
        return this
    }
}
