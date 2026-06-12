package com.gijun.ticketserver.application.ticketevent.port.out.persistence

import com.gijun.ticketserver.domain.model.TicketEventSectionModel

interface TicketEventSectionPersistencePort {
    fun saveAll(sections: List<TicketEventSectionModel>): List<TicketEventSectionModel>
    fun findById(id: Long): TicketEventSectionModel?
    fun findByTicketEventId(ticketEventId: Long): List<TicketEventSectionModel>
}
