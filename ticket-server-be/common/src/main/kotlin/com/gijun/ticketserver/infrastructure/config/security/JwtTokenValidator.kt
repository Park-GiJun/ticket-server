package com.gijun.ticketserver.infrastructure.config.security

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.nio.charset.StandardCharsets

/** 검증된 토큰에서 추출한 신원 정보. role 은 도메인 enum 의존을 피하려 문자열로 둔다. */
data class JwtClaims(
    val userId: Long,
    val email: String,
    val role: String,
)

/**
 * JWT 서명/발급자/만료를 검증하고 신원 클레임을 추출한다. 토큰 발급 측([user-service] 의 JwtTokenProvider)과
 * 동일한 비밀키·issuer 를 공유하며, gateway 의 인증 필터가 이 검증기를 사용한다.
 */
class JwtTokenValidator(
    private val properties: JwtProperties,
) {

    private val key = Keys.hmacShaKeyFor(properties.secret.toByteArray(StandardCharsets.UTF_8))

    fun validate(token: String): JwtClaims? = try {
        val claims = Jwts.parser()
            .verifyWith(key)
            .requireIssuer(properties.issuer)
            .build()
            .parseSignedClaims(token)
            .payload

        JwtClaims(
            userId = claims.subject.toLong(),
            email = claims.get(CLAIM_EMAIL, String::class.java),
            role = claims.get(CLAIM_ROLE, String::class.java),
        )
    } catch (_: JwtException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }

    companion object {
        const val CLAIM_EMAIL = "email"
        const val CLAIM_ROLE = "role"
    }
}
