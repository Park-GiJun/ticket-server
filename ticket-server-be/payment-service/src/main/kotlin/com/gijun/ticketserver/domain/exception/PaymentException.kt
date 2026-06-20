package com.gijun.ticketserver.domain.exception

/**
 * 결제 도메인 비즈니스 예외. sealed 로 두어 예외 처리기에서 when 으로 망라한다.
 * TODO: 하위 예외 정의 (예: PaymentNotFound, InvalidStatusTransition, PaymentGatewayError ...).
 */
sealed class PaymentException(message: String) : RuntimeException(message)
