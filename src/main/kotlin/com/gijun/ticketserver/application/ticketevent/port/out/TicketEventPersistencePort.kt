package com.gijun.ticketserver.application.ticketevent.port.out

import com.gijun.ticketserver.domain.model.TicketEventModel

interface TicketEventPersistencePort {
    fun save(ticketEvent: TicketEventModel): TicketEventModel
    fun findById(id: Long): TicketEventModel?
    fun existsById(id: Long): Boolean
}
