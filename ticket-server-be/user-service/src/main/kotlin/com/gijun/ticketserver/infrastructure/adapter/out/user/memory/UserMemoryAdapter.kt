package com.gijun.ticketserver.infrastructure.adapter.out.user.memory

import com.gijun.ticketserver.application.user.port.out.UserMemoryPort
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class UserMemoryAdapter(
    private val redisTemplate: StringRedisTemplate,
) : UserMemoryPort {

    override fun savePasswordResetToken(token: String, email: String, ttl: Duration) {
        redisTemplate.opsForValue().set(key(token), email, ttl)
    }

    override fun findEmailByPasswordResetToken(token: String): String? =
        redisTemplate.opsForValue().get(key(token))

    override fun deletePasswordResetToken(token: String) {
        redisTemplate.delete(key(token))
    }

    private fun key(token: String): String = "$PASSWORD_RESET_KEY_PREFIX$token"

    companion object {
        private const val PASSWORD_RESET_KEY_PREFIX = "user:password-reset:"
    }
}
