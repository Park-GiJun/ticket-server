package com.gijun.ticketserver.infrastructure.adapter.out.ticketevent.persistence.adapter

import com.gijun.ticketserver.application.ticketevent.port.out.TicketEventSeatPersistencePort
import com.gijun.ticketserver.domain.enums.SeatStatus
import com.gijun.ticketserver.domain.model.TicketEventSeatModel
import com.gijun.ticketserver.infrastructure.adapter.out.ticketevent.persistence.entity.TicketEventSeatEntity
import com.gijun.ticketserver.infrastructure.adapter.out.ticketevent.persistence.repository.TicketEventSeatPersistenceRepository
import org.springframework.stereotype.Component

@Component
class TicketEventSeatPersistenceAdapter(
    private val repository: TicketEventSeatPersistenceRepository,
) : TicketEventSeatPersistencePort {

    override fun saveAll(seats: List<TicketEventSeatModel>): List<TicketEventSeatModel> =
        repository.saveAll(seats.map { TicketEventSeatEntity.fromModel(it) }).map { it.toModel() }

    override fun findById(id: Long): TicketEventSeatModel? =
        repository.findById(id).orElse(null)?.toModel()

    override fun findByTicketEventId(ticketEventId: Long): List<TicketEventSeatModel> =
        repository.findByTicketEventId(ticketEventId).map { it.toModel() }

    override fun existsByTicketEventIdAndStatus(ticketEventId: Long, status: SeatStatus): Boolean =
        repository.existsByTicketEventIdAndStatus(ticketEventId, status)

    override fun countByTicketEventIdGroupedByStatus(ticketEventId: Long): Map<SeatStatus, Long> =
        repository.countGroupedByStatus(ticketEventId).associate { it.status to it.cnt }
}
