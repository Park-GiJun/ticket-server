package com.gijun.ticketserver.infrastructure.adapter.out.ticketevent.persistence.adapter

import com.gijun.ticketserver.application.ticketevent.port.out.TicketEventSectionPersistencePort
import com.gijun.ticketserver.domain.model.TicketEventSectionModel
import com.gijun.ticketserver.infrastructure.adapter.out.ticketevent.persistence.entity.TicketEventSectionEntity
import com.gijun.ticketserver.infrastructure.adapter.out.ticketevent.persistence.repository.TicketEventSectionPersistenceRepository
import org.springframework.stereotype.Component

@Component
class TicketEventSectionPersistenceAdapter(
    private val repository: TicketEventSectionPersistenceRepository,
) : TicketEventSectionPersistencePort {

    override fun saveAll(sections: List<TicketEventSectionModel>): List<TicketEventSectionModel> =
        repository.saveAll(sections.map { TicketEventSectionEntity.fromModel(it) }).map { it.toModel() }

    override fun findById(id: Long): TicketEventSectionModel? =
        repository.findById(id).orElse(null)?.toModel()

    override fun findByTicketEventId(ticketEventId: Long): List<TicketEventSectionModel> =
        repository.findByTicketEventId(ticketEventId).map { it.toModel() }
}
