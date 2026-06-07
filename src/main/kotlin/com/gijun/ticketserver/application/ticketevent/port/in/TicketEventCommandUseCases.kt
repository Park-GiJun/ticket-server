package com.gijun.ticketserver.application.ticketevent.port.`in`

import com.gijun.ticketserver.domain.model.TicketEventModel

/**
 * 티켓 이벤트 명령(Command) 유스케이스. **1 인터페이스 = 1 함수** 규칙을 따른다.
 *
 * 입력/출력은 우선 도메인 모델([TicketEventModel])을 사용한다.
 * Command/Result DTO 는 핸들러·웹 계층을 구현할 때 도입한다.
 */

interface CreateTicketEventUseCase {
    fun create(ticketEvent: TicketEventModel): TicketEventModel
}

interface UpdateTicketEventUseCase {
    fun update(ticketEvent: TicketEventModel): TicketEventModel
}

interface OpenTicketEventUseCase {
    fun open(id: Long): TicketEventModel
}

interface CloseTicketEventUseCase {
    fun close(id: Long): TicketEventModel
}

interface CancelTicketEventUseCase {
    fun cancel(id: Long): TicketEventModel
}
