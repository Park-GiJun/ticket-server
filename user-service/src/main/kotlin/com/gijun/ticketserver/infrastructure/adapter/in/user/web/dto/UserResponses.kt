package com.gijun.ticketserver.infrastructure.adapter.`in`.user.web.dto

import com.gijun.ticketserver.application.user.dto.TokenResult
import com.gijun.ticketserver.application.user.dto.UserResult
import com.gijun.ticketserver.domain.enums.UserRole
import com.gijun.ticketserver.domain.enums.UserStatus
import java.time.Instant

data class UserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val role: UserRole,
    val status: UserStatus,
    val createdAt: Instant?,
) {
    companion object {
        fun from(result: UserResult): UserResponse = UserResponse(
            id = result.id,
            email = result.email,
            name = result.name,
            role = result.role,
            status = result.status,
            createdAt = result.createdAt,
        )
    }
}

data class TokenResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long,
) {
    companion object {
        fun from(result: TokenResult): TokenResponse = TokenResponse(
            accessToken = result.accessToken,
            tokenType = result.tokenType,
            expiresIn = result.expiresInSeconds,
        )
    }
}
