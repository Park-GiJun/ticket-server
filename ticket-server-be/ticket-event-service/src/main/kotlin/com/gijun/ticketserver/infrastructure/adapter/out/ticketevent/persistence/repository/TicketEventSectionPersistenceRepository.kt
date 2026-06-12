package com.gijun.ticketserver.infrastructure.adapter.out.ticketevent.persistence.repository

import com.gijun.ticketserver.infrastructure.adapter.out.ticketevent.persistence.entity.TicketEventSectionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TicketEventSectionPersistenceRepository : JpaRepository<TicketEventSectionEntity, Long> {
    fun findByTicketEventId(ticketEventId: Long): List<TicketEventSectionEntity>
}
