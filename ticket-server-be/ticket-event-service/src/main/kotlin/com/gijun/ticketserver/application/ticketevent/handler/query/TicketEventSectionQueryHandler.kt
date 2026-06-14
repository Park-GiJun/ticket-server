package com.gijun.ticketserver.application.ticketevent.handler.query

import com.gijun.ticketserver.application.ticketevent.dto.result.TicketEventSectionResult
import com.gijun.ticketserver.application.ticketevent.port.`in`.query.GetSectionUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.query.ListSectionsByEventUseCase
import com.gijun.ticketserver.application.ticketevent.port.out.persistence.TicketEventSectionPersistencePort
import com.gijun.ticketserver.domain.exception.TicketEventException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TicketEventSectionQueryHandler(
    private val sectionPersistencePort: TicketEventSectionPersistencePort,
) : GetSectionUseCase,
    ListSectionsByEventUseCase {

    override fun getById(ticketEventId: Long, id: Long): TicketEventSectionResult {
        val model = sectionPersistencePort.findById(id)
            ?.takeIf { it.ticketEventId == ticketEventId }
            ?: throw TicketEventException.SectionNotFound()
        return TicketEventSectionResult.from(model)
    }

    override fun listByEvent(ticketEventId: Long): List<TicketEventSectionResult> =
        sectionPersistencePort.findByTicketEventId(ticketEventId)
            .map { TicketEventSectionResult.from(it) }
}
