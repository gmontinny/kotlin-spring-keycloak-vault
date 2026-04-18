package br.com.springbootkeycloakoauth2.controller

import br.com.springbootkeycloakoauth2.dto.response.OrderResponse
import br.com.springbootkeycloakoauth2.service.OrderService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.data.domain.PageImpl
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = [
    "spring.autoconfigure.exclude=" +
        "org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration," +
        "org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration," +
        "org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration"
])
class OrderControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var orderService: OrderService

    private val now = LocalDateTime.of(2025, 1, 15, 10, 30, 0)

    private val orderResponse = OrderResponse(
        orderId = "o1",
        customerId = "c1",
        orderStatus = "delivered",
        orderPurchaseTimestamp = now,
        orderApprovedAt = null,
        orderDeliveredCarrierDate = null,
        orderDeliveredCustomerDate = null,
        orderEstimatedDeliveryDate = null
    )

    @Test
    fun `GET order by id returns 200 with ADMIN`() {
        `when`(orderService.findById("o1")).thenReturn(orderResponse)

        mockMvc.perform(
            get("/api/v1/orders/o1")
                .with(jwt().authorities(SimpleGrantedAuthority("ROLE_ADMIN")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.orderId").value("o1"))
            .andExpect(jsonPath("$.orderStatus").value("delivered"))
    }

    @Test
    fun `GET order by id returns 200 with USER`() {
        `when`(orderService.findById("o1")).thenReturn(orderResponse)

        mockMvc.perform(
            get("/api/v1/orders/o1")
                .with(jwt().authorities(SimpleGrantedAuthority("ROLE_USER")))
        )
            .andExpect(status().isOk)
    }

    @Test
    fun `GET orders returns 401 without token`() {
        mockMvc.perform(get("/api/v1/orders"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `GET orders list returns 200`() {
        val page = PageImpl(listOf(orderResponse))
        `when`(orderService.findAll(any())).thenReturn(page)

        mockMvc.perform(
            get("/api/v1/orders?page=0&size=10")
                .with(jwt().authorities(SimpleGrantedAuthority("ROLE_ADMIN")))
        )
            .andExpect(status().isOk)
    }
}
