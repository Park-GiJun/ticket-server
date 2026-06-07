package com.gijun.ticketserver.infrastructure.adapter.out.user.token

import com.gijun.ticketserver.application.user.port.out.IssuedToken
import com.gijun.ticketserver.application.user.port.out.TokenPayload
import com.gijun.ticketserver.application.user.port.out.UserTokenPort
import com.gijun.ticketserver.domain.model.UserModel
import com.gijun.ticketserver.domain.enums.UserRole
import com.gijun.ticketserver.infrastructure.security.JwtProperties
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date

@Component
class JwtTokenProvider(
    private val properties: JwtProperties,
) : UserTokenPort {

    private val key = Keys.hmacShaKeyFor(properties.secret.toByteArray(StandardCharsets.UTF_8))

    override fun issueAccessToken(user: UserModel): IssuedToken {
        val userId = requireNotNull(user.id) { "토큰 발급 대상 사용자에는 id 가 존재해야 합니다" }
        val now = System.currentTimeMillis()
        val expiresAt = now + properties.accessTokenExpirationMillis

        val token = Jwts.builder()
            .issuer(properties.issuer)
            .subject(userId.toString())
            .claim(CLAIM_EMAIL, user.email)
            .claim(CLAIM_ROLE, user.role.name)
            .issuedAt(Date(now))
            .expiration(Date(expiresAt))
            .signWith(key)
            .compact()

        return IssuedToken(
            token = token,
            tokenType = TOKEN_TYPE,
            expiresInSeconds = properties.accessTokenExpirationMillis / 1000,
        )
    }

    override fun validateToken(token: String): TokenPayload? = try {
        val claims = Jwts.parser()
            .verifyWith(key)
            .requireIssuer(properties.issuer)
            .build()
            .parseSignedClaims(token)
            .payload

        TokenPayload(
            userId = claims.subject.toLong(),
            email = claims.get(CLAIM_EMAIL, String::class.java),
            role = UserRole.valueOf(claims.get(CLAIM_ROLE, String::class.java)),
        )
    } catch (_: JwtException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }

    companion object {
        private const val TOKEN_TYPE = "Bearer"
        private const val CLAIM_EMAIL = "email"
        private const val CLAIM_ROLE = "role"
    }
}
