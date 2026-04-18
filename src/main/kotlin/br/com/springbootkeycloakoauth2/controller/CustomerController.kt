package br.com.springbootkeycloakoauth2.controller

import br.com.springbootkeycloakoauth2.dto.request.CustomerRequest
import br.com.springbootkeycloakoauth2.dto.response.CustomerResponse
import br.com.springbootkeycloakoauth2.service.CustomerService
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
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers", description = "Gerenciamento de clientes")
class CustomerController(
    private val customerService: CustomerService,
    private val pagedResourcesAssembler: PagedResourcesAssembler<CustomerResponse>
) {

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Listar todos os clientes", description = "Retorna lista paginada de clientes com links HATEOAS")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Lista de clientes retornada com sucesso"),
        ApiResponse(responseCode = "401", description = "Não autenticado"),
        ApiResponse(responseCode = "403", description = "Sem permissão")
    )
    fun findAll(pageable: Pageable): ResponseEntity<PagedModel<EntityModel<CustomerResponse>>> {
        val page = customerService.findAll(pageable)
        val pagedModel = pagedResourcesAssembler.toModel(page) { customer ->
            EntityModel.of(customer.addLinks())
        }
        return ResponseEntity.ok(pagedModel)
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Buscar cliente por ID", description = "Retorna um cliente específico com links HATEOAS")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
        ApiResponse(responseCode = "401", description = "Não autenticado")
    )
    fun findById(@Parameter(description = "ID do cliente") @PathVariable id: String): ResponseEntity<CustomerResponse> {
        val customer = customerService.findById(id).addLinks()
        return ResponseEntity.ok(customer)
    }

    @GetMapping("/state/{state}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Buscar clientes por estado", description = "Retorna lista paginada de clientes filtrados por estado")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Lista de clientes retornada com sucesso"),
        ApiResponse(responseCode = "401", description = "Não autenticado")
    )
    fun findByState(
        @Parameter(description = "Sigla do estado (ex: SP, RJ)") @PathVariable state: String,
        pageable: Pageable
    ): ResponseEntity<PagedModel<EntityModel<CustomerResponse>>> {
        val page = customerService.findByState(state, pageable)
        val pagedModel = pagedResourcesAssembler.toModel(page) { customer ->
            EntityModel.of(customer.addLinks())
        }
        return ResponseEntity.ok(pagedModel)
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar novo cliente", description = "Cria um novo cliente no sistema")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Cliente criado com sucesso"),
        ApiResponse(responseCode = "400", description = "Dados inválidos"),
        ApiResponse(responseCode = "401", description = "Não autenticado"),
        ApiResponse(responseCode = "403", description = "Sem permissão - requer role ADMIN")
    )
    fun create(@Valid @RequestBody request: CustomerRequest): ResponseEntity<CustomerResponse> {
        val customer = customerService.create(request).addLinks()
        return ResponseEntity.status(HttpStatus.CREATED).body(customer)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar cliente", description = "Atualiza os dados de um cliente existente")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso"),
        ApiResponse(responseCode = "400", description = "Dados inválidos"),
        ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
        ApiResponse(responseCode = "403", description = "Sem permissão - requer role ADMIN")
    )
    fun update(
        @Parameter(description = "ID do cliente") @PathVariable id: String,
        @Valid @RequestBody request: CustomerRequest
    ): ResponseEntity<CustomerResponse> {
        val customer = customerService.update(id, request).addLinks()
        return ResponseEntity.ok(customer)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar cliente", description = "Remove um cliente do sistema")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Cliente removido com sucesso"),
        ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
        ApiResponse(responseCode = "403", description = "Sem permissão - requer role ADMIN")
    )
    fun delete(@Parameter(description = "ID do cliente") @PathVariable id: String): ResponseEntity<Void> {
        customerService.delete(id)
        return ResponseEntity.noContent().build()
    }

    private fun CustomerResponse.addLinks(): CustomerResponse {
        add(linkTo(methodOn(CustomerController::class.java).findById(customerId)).withSelfRel())
        add(linkTo(methodOn(CustomerController::class.java).findAll(Pageable.unpaged())).withRel("customers"))
        add(linkTo(methodOn(OrderController::class.java).findByCustomerId(customerId, Pageable.unpaged())).withRel("orders"))
        return this
    }
}
