package br.com.springbootkeycloakoauth2.config.ratelimit

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.concurrent.ConcurrentHashMap

@Component
class RateLimitFilter(
    @Value("\${rate-limit.requests-per-minute:60}")
    private val requestsPerMinute: Int
) : OncePerRequestFilter() {

    private val buckets = ConcurrentHashMap<String, TokenBucket>()

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        if (!request.requestURI.startsWith("/api/")) {
            filterChain.doFilter(request, response)
            return
        }

        val clientIp = getClientIp(request)
        val bucket = buckets.computeIfAbsent(clientIp) { TokenBucket(requestsPerMinute) }

        if (bucket.tryConsume()) {
            response.setHeader("X-RateLimit-Limit", requestsPerMinute.toString())
            response.setHeader("X-RateLimit-Remaining", bucket.availableTokens().toString())
            filterChain.doFilter(request, response)
        } else {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = "application/json"
            response.setHeader("X-RateLimit-Limit", requestsPerMinute.toString())
            response.setHeader("X-RateLimit-Remaining", "0")
            response.setHeader("Retry-After", "60")
            response.writer.write("""{"status":429,"error":"Too Many Requests","message":"Rate limit exceeded. Try again in 60 seconds."}""")
        }
    }

    private fun getClientIp(request: HttpServletRequest): String {
        val xff = request.getHeader("X-Forwarded-For")
        return if (!xff.isNullOrBlank()) xff.split(",")[0].trim() else request.remoteAddr
    }
}
