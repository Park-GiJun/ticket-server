package com.gijun.ticketserver.application.user.port.out

import com.gijun.ticketserver.domain.model.UserModel
import com.gijun.ticketserver.domain.model.UserRole

/**
 * 인증 토큰 발급/검증 포트. application 레이어가 특정 JWT 라이브러리에 의존하지 않도록 분리한다.
 */
interface UserTokenPort {
    fun issueAccessToken(user: UserModel): IssuedToken
    fun validateToken(token: String): TokenPayload?
}

data class IssuedToken(
    val token: String,
    val tokenType: String,
    val expiresInSeconds: Long,
)

data class TokenPayload(
    val userId: Long,
    val email: String,
    val role: UserRole,
)
