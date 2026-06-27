package com.gijun.ticketserver.application.ticketevent.handler

import com.gijun.ticketserver.application.ticketevent.dto.SearchTicketEventsQuery
import com.gijun.ticketserver.application.ticketevent.dto.TicketEventResult
import com.gijun.ticketserver.application.ticketevent.port.`in`.GetTicketEventUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.SearchTicketEventsUseCase
import com.gijun.ticketserver.application.ticketevent.port.out.TicketEventPersistencePort
import com.gijun.ticketserver.domain.exception.TicketEventException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TicketEventQueryHandler(
    private val ticketEventPersistencePort: TicketEventPersistencePort,
) : GetTicketEventUseCase,
    SearchTicketEventsUseCase {

    override fun getById(id: Long): TicketEventResult {
        val model = ticketEventPersistencePort.findById(id)
            ?: throw TicketEventException.TicketEventNotFound()
        return TicketEventResult.from(model)
    }

    override fun search(query: SearchTicketEventsQuery): List<TicketEventResult> =
        ticketEventPersistencePort.search(query.category, query.status)
            .map { TicketEventResult.from(it) }
}