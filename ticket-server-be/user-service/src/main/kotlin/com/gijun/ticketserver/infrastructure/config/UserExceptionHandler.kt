package com.gijun.ticketserver.infrastructure.config

import com.gijun.ticketserver.domain.exception.UserException
import com.gijun.ticketserver.shared.web.ApiErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/** user 도메인 예외를 HTTP 상태로 매핑한다. 공통 예외는 [CommonExceptionHandler] 가 처리한다. */
@RestControllerAdvice
class UserExceptionHandler {

    @ExceptionHandler(UserException::class)
    fun handleUserException(e: UserException): ResponseEntity<ApiErrorResponse> {
        val status = when (e) {
            is UserException.EmailAlreadyExists -> HttpStatus.CONFLICT
            is UserException.UserNotFound -> HttpStatus.NOT_FOUND
            is UserException.InvalidCredentials -> HttpStatus.UNAUTHORIZED
            is UserException.InactiveUser -> HttpStatus.FORBIDDEN
            is UserException.InvalidEmail,
            is UserException.InvalidPassword,
            is UserException.InvalidResetToken -> HttpStatus.BAD_REQUEST
        }
        return ResponseEntity.status(status)
            .body(ApiErrorResponse(status.value(), e.message ?: status.reasonPhrase))
    }
}
