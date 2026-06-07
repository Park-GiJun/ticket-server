package com.gijun.ticketserver.application.ticketevent.handler

import com.gijun.ticketserver.application.ticketevent.port.`in`.GetTicketEventUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.SearchTicketEventsUseCase
import com.gijun.ticketserver.application.ticketevent.port.out.TicketEventPersistencePort
import com.gijun.ticketserver.domain.enums.TicketEventCategory
import com.gijun.ticketserver.domain.enums.TicketEventStatus
import com.gijun.ticketserver.domain.exception.TicketEventException
import com.gijun.ticketserver.domain.model.TicketEventModel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TicketEventQueryHandler(
    private val ticketEventPersistencePort: TicketEventPersistencePort,
) : GetTicketEventUseCase,
    SearchTicketEventsUseCase {

    override fun getById(id: Long): TicketEventModel =
        ticketEventPersistencePort.findById(id)
            ?: throw TicketEventException.TicketEventNotFound()

    override fun search(
        category: TicketEventCategory?,
        status: TicketEventStatus?,
    ): List<TicketEventModel> =
        ticketEventPersistencePort.search(category, status)
}
