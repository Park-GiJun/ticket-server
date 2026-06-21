package com.gijun.ticketserver.infrastructure.config.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    /** HS256/512 서명 키. 최소 32바이트(256bit) 이상이어야 한다. 운영 환경에서는 JWT_SECRET 환경변수로 반드시 교체한다. */
    val secret: String = "local-dev-only-secret-please-override-via-env-0123456789abcdef",
    val accessTokenExpirationMillis: Long = 3_600_000,
    val issuer: String = "ticket-server",
)
