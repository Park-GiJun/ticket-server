package com.gijun.ticketserver.infrastructure.config.security

import com.gijun.ticketserver.domain.enums.UserRole

/**
 * 인증된 요청의 SecurityContext 에 보관되는 주체(principal).
 * 컨트롤러에서 @AuthenticationPrincipal 로 주입받는다.
 */
data class AuthenticatedUser(
    val userId: Long,
    val email: String,
    val role: UserRole,
)
