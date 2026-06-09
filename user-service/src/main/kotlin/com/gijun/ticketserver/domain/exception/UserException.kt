package com.gijun.ticketserver.domain.exception

/**
 * 사용자 도메인에서 발생하는 비즈니스 예외. sealed 로 두어 예외 처리기에서 when 으로 망라한다.
 */
sealed class UserException(message: String) : RuntimeException(message) {
    class EmailAlreadyExists(email: String) : UserException("이미 사용 중인 이메일입니다: $email")
    class UserNotFound : UserException("사용자를 찾을 수 없습니다")
    class InvalidCredentials : UserException("이메일 또는 비밀번호가 올바르지 않습니다")
    class InactiveUser : UserException("사용할 수 없는 계정입니다")
    class InvalidEmail : UserException("올바른 이메일 형식이 아닙니다")
    class InvalidPassword(reason: String) : UserException(reason)
    class InvalidResetToken : UserException("유효하지 않거나 만료된 재설정 토큰입니다")
}
