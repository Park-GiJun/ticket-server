package com.gijun.ticketserver.infrastructure.config

import com.gijun.ticketserver.domain.exception.TicketEventException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/** ticket-event 도메인 예외를 HTTP 상태로 매핑한다. 공통 예외는 [CommonExceptionHandler] 가 처리한다. */
@RestControllerAdvice
class TicketEventExceptionHandler {

    @ExceptionHandler(TicketEventException::class)
    fun handleTicketEventException(e: TicketEventException): ResponseEntity<ApiErrorResponse> {
        val status = when (e) {
            is TicketEventException.TicketEventNotFound -> HttpStatus.NOT_FOUND
            is TicketEventException.SectionNotFound -> HttpStatus.NOT_FOUND
            is TicketEventException.SeatNotFound -> HttpStatus.NOT_FOUND
            is TicketEventException.InvalidStatusTransition -> HttpStatus.CONFLICT
            is TicketEventException.InvalidSeatStatusTransition -> HttpStatus.CONFLICT
        }
        return ResponseEntity.status(status)
            .body(ApiErrorResponse(status.value(), e.message ?: status.reasonPhrase))
    }
}
