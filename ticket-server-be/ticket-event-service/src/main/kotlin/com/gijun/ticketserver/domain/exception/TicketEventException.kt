package com.gijun.ticketserver.domain.exception

/**
 * 티켓 이벤트 도메인에서 발생하는 비즈니스 예외. sealed 로 두어 예외 처리기에서 when 으로 망라한다.
 */
sealed class TicketEventException(message: String) : RuntimeException(message) {
    class TicketEventNotFound : TicketEventException("티켓 이벤트를 찾을 수 없습니다")
    class InvalidStatusTransition(reason: String) : TicketEventException(reason)
}
