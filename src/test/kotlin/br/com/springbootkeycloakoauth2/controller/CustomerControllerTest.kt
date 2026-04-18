package br.com.springbootkeycloakoauth2.controller

import br.com.springbootkeycloakoauth2.dto.response.CustomerResponse
import br.com.springbootkeycloakoauth2.service.CustomerService
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = [
    "spring.autoconfigure.exclude=" +
        "org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration," +
        "org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration," +
        "org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration"
])
class CustomerControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var customerService: CustomerService

    private val customerResponse = CustomerResponse(
        customerId = "c1",
        customerUniqueId = "u1",
        customerZipCodePrefix = "01001",
        customerCity = "São Paulo",
        customerState = "SP"
    )

    @Test
    fun `GET customer by id returns 200 with ADMIN`() {
        `when`(customerService.findById("c1")).thenReturn(customerResponse)

        mockMvc.perform(
            get("/api/v1/customers/c1")
                .with(jwt().authorities(SimpleGrantedAuthority("ROLE_ADMIN")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.customerId").value("c1"))
            .andExpect(jsonPath("$.customerCity").value("São Paulo"))
    }

    @Test
    fun `GET customer by id returns 200 with USER`() {
        `when`(customerService.findById("c1")).thenReturn(customerResponse)

        mockMvc.perform(
            get("/api/v1/customers/c1")
                .with(jwt().authorities(SimpleGrantedAuthority("ROLE_USER")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.customerState").value("SP"))
    }

    @Test
    fun `GET customer returns 401 without token`() {
        mockMvc.perform(get("/api/v1/customers/c1"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `GET customers list returns 200`() {
        val page = PageImpl(listOf(customerResponse))
        `when`(customerService.findAll(any())).thenReturn(page)

        mockMvc.perform(
            get("/api/v1/customers?page=0&size=10")
                .with(jwt().authorities(SimpleGrantedAuthority("ROLE_ADMIN")))
        )
            .andExpect(status().isOk)
    }
}
