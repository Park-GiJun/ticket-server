package com.gijun.ticketserver.infrastructure.config

import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 결제 도메인 예외를 HTTP 상태로 매핑한다. 공통 예외는 common 의 CommonExceptionHandler 가 처리한다.
 * TODO: @ExceptionHandler(PaymentException) 추가 후 when 으로 상태 매핑.
 */
@RestControllerAdvice
class PaymentExceptionHandler
