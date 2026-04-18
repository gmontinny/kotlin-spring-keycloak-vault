package br.com.springbootkeycloakoauth2.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Endpoints de autenticação e refresh token")
class AuthController(
    @Value("\${spring.security.oauth2.client.provider.keycloak.issuer-uri:http://localhost:8080/realms/olist-realm}")
    private val issuerUri: String,

    @Value("\${spring.security.oauth2.client.registration.keycloak.client-id:olist-client}")
    private val clientId: String,

    @Value("\${spring.security.oauth2.client.registration.keycloak.client-secret:olist-client-secret}")
    private val clientSecret: String
) {

    private val restTemplate = RestTemplate()

    @PostMapping("/token")
    @Operation(summary = "Obter token com usuário e senha", description = "Autentica no Keycloak e retorna access_token + refresh_token")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Token obtido com sucesso"),
        ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    )
    fun getToken(
        @Parameter(description = "Nome do usuário") @RequestParam username: String,
        @Parameter(description = "Senha do usuário") @RequestParam password: String
    ): ResponseEntity<Map<*, *>> {
        val params = buildTokenParams("password", mapOf("username" to username, "password" to password))
        return callKeycloak(params)
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar token com refresh_token", description = "Usa o refresh_token para obter um novo access_token sem re-autenticar")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Token renovado com sucesso"),
        ApiResponse(responseCode = "400", description = "Refresh token inválido ou expirado")
    )
    fun refreshToken(
        @Parameter(description = "Refresh token obtido no login") @RequestParam("refresh_token") refreshToken: String
    ): ResponseEntity<Map<*, *>> {
        val params = buildTokenParams("refresh_token", mapOf("refresh_token" to refreshToken))
        return callKeycloak(params)
    }

    private fun buildTokenParams(grantType: String, extra: Map<String, String>): LinkedMultiValueMap<String, String> {
        val params = LinkedMultiValueMap<String, String>()
        params.add("grant_type", grantType)
        params.add("client_id", clientId)
        params.add("client_secret", clientSecret)
        extra.forEach { (k, v) -> params.add(k, v) }
        return params
    }

    private fun callKeycloak(params: LinkedMultiValueMap<String, String>): ResponseEntity<Map<*, *>> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val tokenUrl = "$issuerUri/protocol/openid-connect/token"
        val response = restTemplate.postForEntity(tokenUrl, HttpEntity(params, headers), Map::class.java)
        return ResponseEntity.status(response.statusCode).body(response.body)
    }
}
