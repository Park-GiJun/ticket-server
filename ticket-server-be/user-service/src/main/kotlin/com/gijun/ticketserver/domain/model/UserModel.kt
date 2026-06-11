package com.gijun.ticketserver.domain.model

import com.gijun.ticketserver.domain.enums.UserRole
import com.gijun.ticketserver.domain.enums.UserStatus
import java.time.Instant

/**
 * 사용자 도메인 모델. 영속/웹 등 인프라 관심사로부터 독립적이다.
 * password 는 항상 인코딩된 값(평문 금지)을 보관한다.
 */
data class UserModel(
    val id: Long? = null,
    val email: String,
    val encodedPassword: String,
    val name: String,
    val role: UserRole = UserRole.USER,
    val status: UserStatus = UserStatus.ACTIVE,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    val isActive: Boolean get() = status == UserStatus.ACTIVE

    fun withPassword(newEncodedPassword: String): UserModel =
        copy(encodedPassword = newEncodedPassword)
}
