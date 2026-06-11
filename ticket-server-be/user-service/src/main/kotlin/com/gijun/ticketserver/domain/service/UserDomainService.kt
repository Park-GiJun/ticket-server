package com.gijun.ticketserver.domain.service

import com.gijun.ticketserver.domain.exception.UserException
import com.gijun.ticketserver.domain.model.UserModel
import com.gijun.ticketserver.domain.enums.UserRole

/**
 * 사용자 도메인 규칙(이메일/비밀번호 정책, 생성 불변식)을 담당한다.
 * 상태가 없는 순수 클래스이므로 Spring 빈으로 등록하지 않고 직접 생성해서 사용한다.
 */
class UserDomainService {

    fun createUser(
        email: String,
        encodedPassword: String,
        name: String,
        role: UserRole = UserRole.USER,
    ): UserModel {
        val normalizedEmail = normalizeEmail(email)
        validateEmailFormat(normalizedEmail)
        return UserModel(
            email = normalizedEmail,
            encodedPassword = encodedPassword,
            name = name.trim(),
            role = role,
        )
    }

    fun normalizeEmail(email: String): String = email.trim().lowercase()

    fun validateEmailFormat(email: String) {
        if (!EMAIL_REGEX.matches(email)) throw UserException.InvalidEmail()
    }

    /** 평문 비밀번호 정책 검증. 인코딩 이전에 호출한다. */
    fun validatePasswordPolicy(rawPassword: String) {
        if (rawPassword.length < MIN_PASSWORD_LENGTH) {
            throw UserException.InvalidPassword("비밀번호는 ${MIN_PASSWORD_LENGTH}자 이상이어야 합니다")
        }
        if (rawPassword.none { it.isDigit() }) {
            throw UserException.InvalidPassword("비밀번호에 숫자를 포함해야 합니다")
        }
        if (rawPassword.none { it.isLetter() }) {
            throw UserException.InvalidPassword("비밀번호에 영문자를 포함해야 합니다")
        }
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
