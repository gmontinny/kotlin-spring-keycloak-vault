# Arquitetura e Implementação de uma API E-commerce com Kotlin, Spring Boot, Keycloak, Vault e PostgreSQL

Este projeto (`spring-boot-keycloak-oauth2`) é uma implementação robusta de API REST Level 3 (HATEOAS) para domínio de e-commerce (base Olist), combinando segurança moderna (OAuth2/OIDC + JWT), boas práticas de arquitetura em camadas e operação containerizada.

Ele se destaca por integrar, de forma prática:

- **Spring Boot 4.0.5** com **Kotlin 2.3.0** e **Java 25**
- **Keycloak 24** como provedor de identidade (com Social Login Google)
- **HashiCorp Vault 1.17** para gerenciamento de segredos
- **PostgreSQL 16** com migrações versionadas via **Flyway**
- **Swagger/OpenAPI 3.0.2** com fluxo OAuth2 integrado
- **HATEOAS** para enriquecer respostas REST com links navegáveis
- **34 testes automatizados** (unitários + integração) com H2 em memória

---

## 1) Visão Geral da Solução

A arquitetura segue o modelo em camadas com separação clara de responsabilidades:

```
┌────────────┐   ┌────────────┐   ┌──────────┐   ┌─────────────┐
│ Controller │──▶│  Service   │──▶│  Mapper  │──▶│ Repository  │
│ (HATEOAS)  │   │  (Logic)   │   │(Entity↔  │   │  (JPA)      │
│ + Swagger  │   │            │   │   DTO)   │   │             │
└────────────┘   └────────────┘   └──────────┘   └──────┬──────┘
                                                        │
                                                        ▼
                                                 ┌──────────────┐
                                                 │  PostgreSQL  │
                                                 └──────────────┘
```

| Camada | Responsabilidade |
|--------|-----------------|
| **Controller** | Endpoints REST com HATEOAS links, documentação Swagger (`@Tag`, `@Operation`, `@ApiResponse`), validação de entrada (`@Valid`) |
| **Service** | Lógica de negócio, transações (`@Transactional`), regras de domínio |
| **Mapper** | Conversão entre Entity ↔ DTO, desacoplamento de camadas, geração de IDs (UUID) |
| **Repository** | Acesso a dados via Spring Data JPA com queries derivadas |

A aplicação opera com dependências externas via `docker-compose`:

- **vault** — gerenciamento de secrets
- **postgres-keycloak** — banco dedicado ao Keycloak
- **postgres-app** — banco da aplicação com dados Olist
- **keycloak** — Identity Provider com realm importado automaticamente
- **app** — API Spring Boot

Essa composição simplifica o ambiente local e aproxima o desenvolvimento de um cenário real de produção.

---

## 2) Kotlin 2.3.0 + Java 25: Decisões Técnicas

A escolha de **Kotlin 2.3.0** com **Java 25** traz benefícios concretos:

- **Kotlin 2.3.0** é a primeira versão a suportar JVM target 25 nativamente
- **Data classes** para DTOs eliminam boilerplate (equals, hashCode, toString, copy)
- **Extension functions** para conversões limpas nos Mappers
- **Null safety** nativo reduz NullPointerExceptions em runtime
- **`allOpen` plugin** para compatibilidade com JPA/Hibernate (entities precisam ser open)

### Exemplo: DTO com validação

```kotlin
data class CustomerRequest(
    @field:NotBlank(message = "Zip code prefix is required")
    @field:Size(max = 10)
    val customerZipCodePrefix: String,

    @field:NotBlank(message = "City is required")
    @field:Size(max = 100)
    val customerCity: String,

    @field:NotBlank(message = "State is required")
    @field:Size(min = 2, max = 2, message = "State must be exactly 2 characters")
    val customerState: String
)
```

### Exemplo: Mapper com UUID auto-gerado

```kotlin
@Component
class CustomerMapper {
    fun toEntity(request: CustomerRequest): Customer = Customer(
        customerId = UUID.randomUUID().toString(),
        customerUniqueId = UUID.randomUUID().toString(),
        customerZipCodePrefix = request.customerZipCodePrefix,
        customerCity = request.customerCity,
        customerState = request.customerState
    )
}
```

### Configuração do build

```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm' version '2.3.0'
    id 'org.springframework.boot' version '4.0.5'
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
```

---

## 3) Segurança OAuth2 com Keycloak

O projeto implementa dois papéis de segurança simultaneamente:

1. **OAuth2 Client** — fluxo de login interativo (Swagger UI → Keycloak → redirect com token)
2. **Resource Server** — validação de JWT em endpoints protegidos (API calls com Bearer token)

### Fluxo de Autenticação

```
┌────────┐         ┌──────────┐         ┌──────────────┐
│ Client │──(1)──▶│ Keycloak │◀──(2)──▶│    Google    │
│        │◀──(3)──│  /token  │         │   (Social)   │
└───┬────┘         └──────────┘         └──────────────┘
    │ (4) JWT Token
    ▼
┌──────────────────┐
│  Spring Boot API │  → Valida JWT → Extrai roles → Autoriza
└──────────────────┘
```

### Extração de Roles do JWT

O Keycloak emite tokens com roles no claim `realm_access.roles`. O `KeycloakRealmRoleConverter` extrai e converte para authorities do Spring Security:

```kotlin
class KeycloakRealmRoleConverter : Converter<Jwt, Collection<GrantedAuthority>> {
    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val realmAccess = jwt.claims["realm_access"] as? Map<*, *> ?: return emptyList()
        val roles = realmAccess["roles"] as? List<*> ?: return emptyList()
        return roles.filterIsInstance<String>().map { SimpleGrantedAuthority("ROLE_$it") }
    }
}
```

### Resolução do Issuer no Docker

Um desafio técnico importante: o Swagger UI roda no browser (acessa `localhost:8080`), mas a API dentro do Docker valida contra `keycloak:8080` (hostname interno). O token JWT tem `iss: http://localhost:8080/realms/olist-realm`, causando `iss claim is not valid`.

A solução: usar `NimbusJwtDecoder.withJwkSetUri()` que busca as chaves JWKS via hostname interno mas não valida o issuer:

```kotlin
@Bean
fun jwtDecoder(): JwtDecoder =
    NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build()
```

### Roles e Permissões

| Role | GET | POST | PUT | PATCH | DELETE |
|------|-----|------|-----|-------|--------|
| **ADMIN** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **USER** | ✅ | ❌ | ❌ | ❌ | ❌ |

---

## 4) Gestão de Segredos com Vault

O projeto usa **Spring Cloud Vault** com `config.import: optional:vault://`, permitindo carregar segredos sem fixá-los no código.

### Secrets gerenciados (path: `secret/keycloak-oauth`)

- `spring.datasource.username` / `spring.datasource.password`
- `spring.security.oauth2.client.registration.keycloak.client-id` / `client-secret`
- `spring.security.oauth2.client.registration.google.client-id` / `client-secret`

### Benefício prático

Sem Vault, credenciais ficam em `.env` ou variáveis locais — expostas no repositório.
Com Vault, o app consome segredos centralizados, com possibilidade de rotação e auditoria.

No `docker-compose.yml`, o serviço `vault` sobe em modo dev e executa `vault/init-secrets.sh`, acelerando onboarding do time.

---

## 5) Persistência e Migração de Dados

### Estratégia de Flyway

As migrations são separadas em dois diretórios para suportar execução local e Docker:

| Diretório | Versão | Descrição | Quando roda |
|-----------|--------|-----------|-------------|
| `structure/` | V1 | Criação de tabelas, constraints e índices | Sempre (local + Docker) |
| `data/` | V2 | Carga dos CSVs via `COPY` do PostgreSQL | Apenas Docker |

```yaml
flyway:
  locations: ${FLYWAY_LOCATIONS:classpath:db/migration/structure}
```

- **Local**: tabelas criadas vazias, dados populados via API
- **Docker**: `FLYWAY_LOCATIONS=classpath:db/migration/structure,classpath:db/migration/data`

### Tratamento de dados duplicados

O CSV `olist_order_reviews_dataset.csv` contém `review_id` duplicados. A migration V2 usa tabela temporária para deduplicar:

```sql
CREATE TEMP TABLE tmp_order_reviews (...);
COPY tmp_order_reviews(...) FROM '/data/olist_order_reviews_dataset.csv' ...;
INSERT INTO order_reviews(...)
SELECT DISTINCT ON (review_id) * FROM tmp_order_reviews
ORDER BY review_id, review_creation_date DESC;
DROP TABLE tmp_order_reviews;
```

### Modelo de dados

O projeto mapeia 9 entidades JPA do dataset Olist: `Customer`, `Order`, `OrderItem`, `OrderPayment`, `OrderReview`, `Product`, `ProductCategoryTranslation`, `Seller` e `Geolocation`.

---

## 6) API REST Level 3 com HATEOAS

Os DTOs de resposta estendem `RepresentationModel`, permitindo incluir links de navegação nas respostas. Cada recurso inclui:

- **self** — link para o próprio recurso
- **collection** — link para a listagem
- **relacionamentos** — links para recursos relacionados

### Exemplo de response

```json
{
  "customerId": "abc123",
  "customerUniqueId": "unique456",
  "customerZipCodePrefix": "01001",
  "customerCity": "São Paulo",
  "customerState": "SP",
  "_links": {
    "self": { "href": "http://localhost:9090/api/v1/customers/abc123" },
    "customers": { "href": "http://localhost:9090/api/v1/customers" },
    "orders": { "href": "http://localhost:9090/api/v1/orders/customer/abc123" }
  }
}
```

### Implementação no Controller

```kotlin
private fun CustomerResponse.addLinks(): CustomerResponse {
    add(linkTo(methodOn(CustomerController::class.java).findById(customerId)).withSelfRel())
    add(linkTo(methodOn(CustomerController::class.java).findAll(Pageable.unpaged())).withRel("customers"))
    add(linkTo(methodOn(OrderController::class.java).findByCustomerId(customerId, Pageable.unpaged())).withRel("orders"))
    return this
}
```

### Paginação

Endpoints de listagem retornam `PagedModel` com links de navegação (`next`, `prev`, `first`, `last`) e metadados de paginação.

---

## 7) Swagger UI com Autenticação Integrada

O Swagger UI permite autenticação direta via Keycloak (Authorization Code + PKCE):

1. Clique em **Authorize** 🔒
2. Redirecionado ao Keycloak
3. Login com **admin/admin** ou **Login com Google**
4. Token JWT injetado automaticamente em todas as requisições

### Configuração

As URLs do Swagger apontam para `localhost` (acessível pelo browser), configuráveis via variáveis de ambiente:

```yaml
app:
  swagger:
    oauth2:
      auth-url: ${SWAGGER_OAUTH2_AUTH_URL:http://localhost:8080/realms/olist-realm/protocol/openid-connect/auth}
      token-url: ${SWAGGER_OAUTH2_TOKEN_URL:http://localhost:8080/realms/olist-realm/protocol/openid-connect/token}
```

O `PagedResourcesAssembler` é ignorado na documentação para evitar parâmetros desnecessários:

```kotlin
init {
    SpringDocUtils.getConfig().addRequestWrapperToIgnore(PagedResourcesAssembler::class.java)
}
```

---

## 8) Testabilidade e Qualidade

### Estratégia de testes

| Tipo | Tecnologia | Quantidade |
|------|-----------|------------|
| **Unitários (Service)** | JUnit 5 + Mockito Kotlin | 25 |
| **Integração (Controller)** | SpringBootTest + MockMvc | 8 |
| **Context Load** | SpringBootTest | 1 |
| **Total** | | **34** |

### Configuração de testes

- **Banco**: H2 em memória (`application-test.yml`)
- **Profile**: `test` — ativa `TestSecurityConfig` com `JwtDecoder` mockado
- **Flyway**: desabilitado (JPA `create-drop`)
- **Vault**: desabilitado
- **OAuth2 Auto-Config**: excluída via `spring.autoconfigure.exclude`

### Desafio: Spring Boot 4.x e testes

O Spring Boot 4.x mudou pacotes de auto-configuração (ex: `@WebMvcTest` movido para `org.springframework.boot.webmvc.test.autoconfigure`). Os testes de controller usam `@SpringBootTest` + `@AutoConfigureMockMvc` com exclusão explícita das auto-configs OAuth2:

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = [
    "spring.autoconfigure.exclude=" +
        "org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration," +
        "org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration"
])
class CustomerControllerTest { ... }
```

---

## 9) Operação e Deploy

O repositório traz três frentes de entrega:

### Docker Compose (desenvolvimento)

5 serviços orquestrados com healthchecks e dependências:

```
postgres-app → keycloak → app
postgres-keycloak ↗        ↑
vault ─────────────────────┘
```

### Kubernetes (produção)

13 manifests no diretório `k8s/`:

- **Namespace** `olist` para isolamento
- **HPA** com auto-scaling de 2 a 5 pods (CPU 70%, Memória 80%)
- **Ingress** nginx com `api.olist.local` e `auth.olist.local`
- **NetworkPolicies** restringindo acesso aos bancos e Vault
- Scripts `deploy.sh` e `destroy.sh` para deploy automatizado

### Terraform (multi-cloud)

Infra como código para **AWS**, **Azure** e **GCP**:

| Recurso | AWS | Azure | GCP |
|---------|-----|-------|-----|
| **Rede** | VPC + Subnets + NAT | VNet + Subnets + DNS | VPC + Subnet + NAT |
| **Kubernetes** | EKS | AKS | GKE |
| **Banco de Dados** | RDS PostgreSQL 16 | Flexible Server 16 | Cloud SQL PostgreSQL 16 |
| **Container Registry** | ECR | ACR | Artifact Registry |
| **Auto-scaling** | Node Group (1–4) | Node Pool (1–4) | Node Pool (1–4) |

---

## 10) Refresh Token

A API expõe endpoints públicos para obtenção e renovação de tokens, abstraindo a comunicação direta com o Keycloak:

- `POST /api/auth/token` — autentica com usuário/senha e retorna `access_token` + `refresh_token`
- `POST /api/auth/refresh` — renova o `access_token` usando o `refresh_token` sem re-autenticar

### Fluxo

```
Client ──(username/password)──▶ /api/auth/token ──▶ Keycloak ──▶ access_token + refresh_token
Client ──(refresh_token)─────▶ /api/auth/refresh ─▶ Keycloak ──▶ novo access_token
```

Isso simplifica a integração para clientes frontend/mobile que não precisam conhecer as URLs do Keycloak diretamente. O `AuthController` usa `RestTemplate` para fazer a chamada ao endpoint de token do Keycloak internamente:

```kotlin
@PostMapping("/refresh")
fun refreshToken(@RequestParam("refresh_token") refreshToken: String): ResponseEntity<Map<*, *>> {
    val params = buildTokenParams("refresh_token", mapOf("refresh_token" to refreshToken))
    return callKeycloak(params)
}
```

---

## 11) Rate Limiting

A API implementa rate limiting por IP usando o algoritmo **Token Bucket**, sem dependências externas:

- **60 requisições por minuto** por IP (configurável)
- Aplica apenas em endpoints `/api/**`
- Headers informativos: `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `Retry-After`
- Retorna HTTP 429 quando excede o limite

### Implementação

O `TokenBucket` usa `AtomicInteger` e `AtomicLong` para thread-safety sem locks:

```kotlin
class TokenBucket(private val maxTokens: Int) {
    private val tokens = AtomicInteger(maxTokens)
    private val lastRefill = AtomicLong(System.currentTimeMillis())

    fun tryConsume(): Boolean {
        refill()
        return tokens.getAndUpdate { if (it > 0) it - 1 else 0 } > 0
    }

    private fun refill() {
        val now = System.currentTimeMillis()
        val last = lastRefill.get()
        if (now - last >= 60_000 && lastRefill.compareAndSet(last, now)) {
            tokens.set(maxTokens)
        }
    }
}
```

O `RateLimitFilter` (`OncePerRequestFilter`) identifica o cliente por IP (`X-Forwarded-For` ou `remoteAddr`) e mantém um `ConcurrentHashMap<String, TokenBucket>` por IP.

---

## 12) Validação de Entrada

Todos os endpoints de criação/atualização validam payloads com Bean Validation. Erros retornam HTTP 400 com detalhes estruturados:

```json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Request validation failed",
  "details": {
    "customerState": "State must be exactly 2 characters",
    "customerCity": "City is required"
  }
}
```

O `GlobalExceptionHandler` (`@RestControllerAdvice`) trata:

- `ResourceNotFoundException` → 404
- `MethodArgumentNotValidException` → 400 com detalhes por campo
- `Exception` genérica → 500

---

## Referências

### Documentações oficiais

- [Spring Boot 4.0.5](https://spring.io/projects/spring-boot)
- [Kotlin 2.3.0 — What's New](https://kotlinlang.org/docs/whatsnew23.html)
- [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/)
- [Spring HATEOAS](https://docs.spring.io/spring-hateoas/docs/current/reference/html/)
- [Keycloak 24](https://www.keycloak.org/documentation)
- [HashiCorp Vault](https://developer.hashicorp.com/vault/docs)
- [Flyway](https://documentation.red-gate.com/fd)
- [SpringDoc OpenAPI](https://springdoc.org/)
- [Terraform](https://developer.hashicorp.com/terraform/docs)

---

## Conclusão

O projeto demonstra uma arquitetura moderna e pragmática para APIs corporativas: segura, modular, testável e preparada para evolução operacional.

**Pontos fortes:**

- Segurança consistente com OAuth2/OIDC + JWT + Keycloak + Social Login Google
- Refresh token via API para renovação transparente de sessão
- Rate limiting por IP com Token Bucket (sem dependências externas)
- Kotlin 2.3.0 com Java 25 — stack de ponta com null safety e concisão
- Separação clara de camadas com Mappers dedicados
- Governança de segredos com Vault
- Persistência confiável com Flyway + PostgreSQL
- Documentação viva com Swagger e autenticação OAuth2 integrada
- REST Level 3 com HATEOAS e paginação
- 34 testes automatizados com H2 em memória
- Infra multi-cloud com Terraform (AWS, Azure, GCP) e Kubernetes

**Próximos passos de maturidade:**

- Observabilidade (metrics com Micrometer, tracing com OpenTelemetry, logs estruturados)
- Políticas de autorização mais granulares (RBAC/ABAC por recurso)
- Pipelines de CI/CD com validações automatizadas de qualidade e segurança
- Cache distribuído (Redis) para otimização de consultas frequentes
