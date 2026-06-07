package com.gijun.ticketserver.infrastructure.config

import com.gijun.ticketserver.domain.exception.UserException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    data class ErrorResponse(
        val status: Int,
        val message: String,
        val fieldErrors: Map<String, String?> = emptyMap(),
    )

    @ExceptionHandler(UserException::class)
    fun handleUserException(e: UserException): ResponseEntity<ErrorResponse> {
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
            .body(ErrorResponse(status.value(), e.message ?: status.reasonPhrase))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val fieldErrors = e.bindingResult.fieldErrors.associate { it.field to it.defaultMessage }
        return ResponseEntity.badRequest()
            .body(ErrorResponse(HttpStatus.BAD_REQUEST.value(), "요청 값이 올바르지 않습니다", fieldErrors))
    }
}
