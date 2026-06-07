package com.gijun.ticketserver.application.user.dto

import com.gijun.ticketserver.domain.model.UserModel
import com.gijun.ticketserver.domain.model.UserRole
import com.gijun.ticketserver.domain.model.UserStatus
import java.time.Instant

data class UserResult(
    val id: Long,
    val email: String,
    val name: String,
    val role: UserRole,
    val status: UserStatus,
    val createdAt: Instant?,
) {
    companion object {
        fun from(user: UserModel): UserResult = UserResult(
            id = requireNotNull(user.id) { "저장된 사용자에는 id 가 존재해야 합니다" },
            email = user.email,
            name = user.name,
            role = user.role,
            status = user.status,
            createdAt = user.createdAt,
        )
    }
}

data class TokenResult(
    val accessToken: String,
    val tokenType: String,
    val expiresInSeconds: Long,
)
