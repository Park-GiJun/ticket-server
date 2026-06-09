package com.gijun.ticketserver.application.ticketevent.port.`in`

import com.gijun.ticketserver.application.ticketevent.dto.CreateTicketEventCommand
import com.gijun.ticketserver.application.ticketevent.dto.TicketEventResult
import com.gijun.ticketserver.application.ticketevent.dto.UpdateTicketEventCommand

/**
 * 티켓 이벤트 명령(Command) 유스케이스. **1 인터페이스 = 1 함수** 규칙을 따른다.
 */

interface CreateTicketEventUseCase {
    fun create(command: CreateTicketEventCommand): TicketEventResult
}

interface UpdateTicketEventUseCase {
    fun update(command: UpdateTicketEventCommand): TicketEventResult
}

interface OpenTicketEventUseCase {
    fun open(id: Long): TicketEventResult
}

interface CloseTicketEventUseCase {
    fun close(id: Long): TicketEventResult
}

interface CancelTicketEventUseCase {
    fun cancel(id: Long): TicketEventResult
}
