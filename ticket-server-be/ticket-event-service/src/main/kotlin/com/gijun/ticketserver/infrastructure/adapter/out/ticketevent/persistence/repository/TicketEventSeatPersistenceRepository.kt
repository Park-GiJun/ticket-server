package com.gijun.ticketserver.infrastructure.adapter.out.ticketevent.persistence.repository

import com.gijun.ticketserver.domain.enums.SeatStatus
import com.gijun.ticketserver.infrastructure.adapter.out.ticketevent.persistence.entity.TicketEventSeatEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface TicketEventSeatPersistenceRepository : JpaRepository<TicketEventSeatEntity, Long> {
    fun findByTicketEventId(ticketEventId: Long): List<TicketEventSeatEntity>

    fun existsByTicketEventIdAndStatus(ticketEventId: Long, status: SeatStatus): Boolean

    @Query(
        """
        SELECT s.status AS status, COUNT(s) AS cnt FROM TicketEventSeatEntity s
        WHERE s.ticketEventId = :ticketEventId
        GROUP BY s.status
        """,
    )
    fun countGroupedByStatus(@Param("ticketEventId") ticketEventId: Long): List<SeatStatusCount>
}

/** [TicketEventSeatPersistenceRepository.countGroupedByStatus] 의 프로젝션. */
interface SeatStatusCount {
    val status: SeatStatus
    val cnt: Long
}
