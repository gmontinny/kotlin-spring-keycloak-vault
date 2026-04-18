package br.com.springbootkeycloakoauth2.config.ratelimit

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class TokenBucket(private val maxTokens: Int) {

    private val tokens = AtomicInteger(maxTokens)
    private val lastRefill = AtomicLong(System.currentTimeMillis())

    fun tryConsume(): Boolean {
        refill()
        return tokens.getAndUpdate { if (it > 0) it - 1 else 0 } > 0
    }

    fun availableTokens(): Int {
        refill()
        return tokens.get()
    }

    private fun refill() {
        val now = System.currentTimeMillis()
        val last = lastRefill.get()
        if (now - last >= 60_000 && lastRefill.compareAndSet(last, now)) {
            tokens.set(maxTokens)
        }
    }
}
