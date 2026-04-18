package br.com.springbootkeycloakoauth2.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.Scopes
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.utils.SpringDocUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.web.PagedResourcesAssembler

@Configuration
class OpenApiConfig(
    @Value("\${app.swagger.oauth2.auth-url}")
    private val authUrl: String,

    @Value("\${app.swagger.oauth2.token-url}")
    private val tokenUrl: String
) {

    init {
        SpringDocUtils.getConfig().addRequestWrapperToIgnore(PagedResourcesAssembler::class.java)
    }

    @Bean
    fun openAPI(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Olist E-Commerce API")
                .description(
                    "API REST Level 3 (HATEOAS) para gerenciamento de e-commerce Olist com autenticação OAuth2 via Keycloak e Google.\n\n" +
                    "**Para autenticar:**\n" +
                    "1. Clique em **Authorize** abaixo\n" +
                    "2. Será redirecionado ao Keycloak\n" +
                    "3. Faça login com **admin/admin** ou clique em **Login com Google**\n" +
                    "4. Após o login, o token JWT será usado automaticamente nas chamadas"
                )
                .version("1.0.0")
                .contact(Contact().name("Olist Team").email("dev@olist.com"))
                .license(License().name("MIT").url("https://opensource.org/licenses/MIT"))
        )
        .addSecurityItem(SecurityRequirement().addList("oauth2"))
        .components(
            Components()
                .addSecuritySchemes("oauth2",
                    SecurityScheme()
                        .type(SecurityScheme.Type.OAUTH2)
                        .description("Login via Keycloak (usuário/senha ou Google)")
                        .flows(
                            OAuthFlows()
                                .authorizationCode(
                                    OAuthFlow()
                                        .authorizationUrl(authUrl)
                                        .tokenUrl(tokenUrl)
                                        .scopes(
                                            Scopes()
                                                .addString("openid", "OpenID Connect")
                                                .addString("profile", "Perfil do usuário")
                                                .addString("email", "Email do usuário")
                                        )
                                )
                        )
                )
        )
}
