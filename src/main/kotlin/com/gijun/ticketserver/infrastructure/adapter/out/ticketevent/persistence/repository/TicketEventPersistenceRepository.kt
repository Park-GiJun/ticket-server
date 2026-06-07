package com.gijun.ticketserver.infrastructure.adapter.out.ticketevent.persistence.repository

import com.gijun.ticketserver.infrastructure.adapter.out.ticketevent.persistence.entity.TicketEventEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TicketEventPersistenceRepository : JpaRepository<TicketEventEntity, Long>
