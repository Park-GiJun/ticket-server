package com.gijun.ticketserver.shared.web

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/** 모든 서비스가 공유하는 표준 에러 응답 바디. */
data class ApiErrorResponse(
    val status: Int,
    val message: String,
    val fieldErrors: Map<String, String?> = emptyMap(),
)

/**
 * 도메인과 무관한 공통 예외(요청 값 검증 실패, 도메인 불변식 위반에 따른 IllegalArgumentException)를 처리한다.
 * 각 서비스의 도메인 예외 핸들러(@RestControllerAdvice)와 함께 동작한다.
 */
@RestControllerAdvice
class CommonExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ApiErrorResponse> {
        val fieldErrors = e.bindingResult.fieldErrors.associate { it.field to it.defaultMessage }
        return ResponseEntity.badRequest()
            .body(ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), "요청 값이 올바르지 않습니다", fieldErrors))
    }

    /** 도메인 모델의 생성 불변식(require) 위반 등. 잘못된 입력이므로 400 으로 매핑한다. */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.badRequest()
            .body(ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), e.message ?: "잘못된 요청입니다"))
}
