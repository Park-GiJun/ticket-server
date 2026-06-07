package com.gijun.ticketserver.infrastructure.adapter.out.ticketevent.persistence.repository

import com.gijun.ticketserver.domain.enums.TicketEventCategory
import com.gijun.ticketserver.domain.enums.TicketEventStatus
import com.gijun.ticketserver.infrastructure.adapter.out.ticketevent.persistence.entity.TicketEventEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface TicketEventPersistenceRepository : JpaRepository<TicketEventEntity, Long> {

    @Query(
        """
        SELECT t FROM TicketEventEntity t
        WHERE (:category IS NULL OR t.ticketEventCategory = :category)
          AND (:status IS NULL OR t.ticketEventStatus = :status)
        """,
    )
    fun search(
        @Param("category") category: TicketEventCategory?,
        @Param("status") status: TicketEventStatus?,
    ): List<TicketEventEntity>
}
