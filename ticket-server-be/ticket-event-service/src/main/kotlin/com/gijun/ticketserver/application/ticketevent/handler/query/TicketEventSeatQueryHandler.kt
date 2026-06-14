package com.gijun.ticketserver.application.ticketevent.handler.query

import com.gijun.ticketserver.application.ticketevent.dto.result.SeatAvailabilityResult
import com.gijun.ticketserver.application.ticketevent.dto.result.TicketEventSeatResult
import com.gijun.ticketserver.application.ticketevent.port.`in`.query.GetSeatAvailabilityUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.query.GetSeatUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.query.ListSeatsByEventUseCase
import com.gijun.ticketserver.application.ticketevent.port.out.persistence.TicketEventSeatPersistencePort
import com.gijun.ticketserver.domain.exception.TicketEventException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TicketEventSeatQueryHandler(
    private val seatPersistencePort: TicketEventSeatPersistencePort,
) : GetSeatUseCase,
    ListSeatsByEventUseCase,
    GetSeatAvailabilityUseCase {

    override fun getById(ticketEventId: Long, id: Long): TicketEventSeatResult {
        val model = seatPersistencePort.findById(id)
            ?.takeIf { it.ticketEventId == ticketEventId }
            ?: throw TicketEventException.SeatNotFound()
        return TicketEventSeatResult.from(model)
    }

    override fun listByEvent(ticketEventId: Long): List<TicketEventSeatResult> =
        seatPersistencePort.findByTicketEventId(ticketEventId)
            .map { TicketEventSeatResult.from(it) }

    override fun getAvailability(ticketEventId: Long): SeatAvailabilityResult =
        SeatAvailabilityResult.from(
            ticketEventId,
            seatPersistencePort.countByTicketEventIdGroupedByStatus(ticketEventId),
        )
}
