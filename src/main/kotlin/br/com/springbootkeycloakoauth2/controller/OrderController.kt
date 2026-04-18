package br.com.springbootkeycloakoauth2.controller

import br.com.springbootkeycloakoauth2.dto.request.OrderRequest
import br.com.springbootkeycloakoauth2.dto.response.OrderResponse
import br.com.springbootkeycloakoauth2.service.OrderService
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
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Gerenciamento de pedidos")
class OrderController(
    private val orderService: OrderService,
    private val pagedResourcesAssembler: PagedResourcesAssembler<OrderResponse>
) {

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Listar todos os pedidos", description = "Retorna lista paginada de pedidos com links HATEOAS")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Lista de pedidos retornada com sucesso"),
        ApiResponse(responseCode = "401", description = "Não autenticado")
    )
    fun findAll(pageable: Pageable): ResponseEntity<PagedModel<EntityModel<OrderResponse>>> {
        val page = orderService.findAll(pageable)
        val pagedModel = pagedResourcesAssembler.toModel(page) { order ->
            EntityModel.of(order.addLinks())
        }
        return ResponseEntity.ok(pagedModel)
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Buscar pedido por ID", description = "Retorna um pedido específico com links HATEOAS")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Pedido encontrado"),
        ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    )
    fun findById(@Parameter(description = "ID do pedido") @PathVariable id: String): ResponseEntity<OrderResponse> {
        val order = orderService.findById(id).addLinks()
        return ResponseEntity.ok(order)
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Buscar pedidos por cliente", description = "Retorna lista paginada de pedidos de um cliente específico")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Lista de pedidos retornada com sucesso"),
        ApiResponse(responseCode = "401", description = "Não autenticado")
    )
    fun findByCustomerId(
        @Parameter(description = "ID do cliente") @PathVariable customerId: String,
        pageable: Pageable
    ): ResponseEntity<PagedModel<EntityModel<OrderResponse>>> {
        val page = orderService.findByCustomerId(customerId, pageable)
        val pagedModel = pagedResourcesAssembler.toModel(page) { order ->
            EntityModel.of(order.addLinks())
        }
        return ResponseEntity.ok(pagedModel)
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Buscar pedidos por status", description = "Retorna lista paginada de pedidos filtrados por status")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Lista de pedidos retornada com sucesso")
    )
    fun findByStatus(
        @Parameter(description = "Status do pedido (delivered, shipped, etc)") @PathVariable status: String,
        pageable: Pageable
    ): ResponseEntity<PagedModel<EntityModel<OrderResponse>>> {
        val page = orderService.findByStatus(status, pageable)
        val pagedModel = pagedResourcesAssembler.toModel(page) { order ->
            EntityModel.of(order.addLinks())
        }
        return ResponseEntity.ok(pagedModel)
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar novo pedido", description = "Cria um novo pedido para um cliente existente")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Pedido criado com sucesso"),
        ApiResponse(responseCode = "400", description = "Dados inválidos"),
        ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
        ApiResponse(responseCode = "403", description = "Sem permissão - requer role ADMIN")
    )
    fun create(@Valid @RequestBody request: OrderRequest): ResponseEntity<OrderResponse> {
        val order = orderService.create(request).addLinks()
        return ResponseEntity.status(HttpStatus.CREATED).body(order)
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar status do pedido", description = "Atualiza apenas o status de um pedido existente")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
        ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
        ApiResponse(responseCode = "403", description = "Sem permissão - requer role ADMIN")
    )
    fun updateStatus(
        @Parameter(description = "ID do pedido") @PathVariable id: String,
        @Parameter(description = "Novo status") @RequestParam status: String
    ): ResponseEntity<OrderResponse> {
        val order = orderService.updateStatus(id, status).addLinks()
        return ResponseEntity.ok(order)
    }

    private fun OrderResponse.addLinks(): OrderResponse {
        add(linkTo(methodOn(OrderController::class.java).findById(orderId)).withSelfRel())
        add(linkTo(methodOn(OrderController::class.java).findAll(Pageable.unpaged())).withRel("orders"))
        add(linkTo(methodOn(CustomerController::class.java).findById(customerId)).withRel("customer"))
        return this
    }
}
