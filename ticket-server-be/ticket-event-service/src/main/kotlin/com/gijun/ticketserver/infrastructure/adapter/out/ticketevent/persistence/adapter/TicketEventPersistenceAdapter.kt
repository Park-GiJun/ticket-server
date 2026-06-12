package com.gijun.ticketserver.infrastructure.adapter.out.ticketevent.persistence.adapter

import com.gijun.ticketserver.application.ticketevent.port.out.persistence.TicketEventPersistencePort
import com.gijun.ticketserver.domain.enums.TicketEventCategory
import com.gijun.ticketserver.domain.enums.TicketEventStatus
import com.gijun.ticketserver.domain.model.TicketEventModel
import com.gijun.ticketserver.infrastructure.adapter.out.ticketevent.persistence.entity.TicketEventEntity
import com.gijun.ticketserver.infrastructure.adapter.out.ticketevent.persistence.repository.TicketEventPersistenceRepository
import org.springframework.stereotype.Component

@Component
class TicketEventPersistenceAdapter(
    private val repository: TicketEventPersistenceRepository,
) : TicketEventPersistencePort {

    override fun save(ticketEvent: TicketEventModel): TicketEventModel {
        val entity = ticketEvent.id
            ?.let { id -> repository.findById(id).orElse(null)?.applyFrom(ticketEvent) }
            ?: TicketEventEntity.fromModel(ticketEvent)
        return repository.save(entity).toModel()
    }

    override fun findById(id: Long): TicketEventModel? =
        repository.findById(id).orElse(null)?.toModel()

    override fun existsById(id: Long): Boolean =
        repository.existsById(id)

    override fun search(
        category: TicketEventCategory?,
        status: TicketEventStatus?,
    ): List<TicketEventModel> =
        repository.search(category, status).map { it.toModel() }

    /** 기존 영속 엔티티에 도메인 변경분을 반영한다(상태/일정 등 갱신용). */
    private fun TicketEventEntity.applyFrom(model: TicketEventModel): TicketEventEntity = apply {
        ticketEventName = model.ticketEventName
        ticketOpenAt = model.ticketOpenAt
        ticketClosedAt = model.ticketClosedAt
        ticketEventAt = model.ticketEventAt
        ticketEventStatus = model.ticketEventStatus
        ticketCreationStatus = model.ticketCreationStatus
        ticketEventCategory = model.ticketEventCategory
    }
}
