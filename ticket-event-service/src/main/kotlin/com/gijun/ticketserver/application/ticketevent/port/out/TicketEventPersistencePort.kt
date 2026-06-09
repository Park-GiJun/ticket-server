package com.gijun.ticketserver.application.ticketevent.port.out

import com.gijun.ticketserver.domain.enums.TicketEventCategory
import com.gijun.ticketserver.domain.enums.TicketEventStatus
import com.gijun.ticketserver.domain.model.TicketEventModel

interface TicketEventPersistencePort {
    fun save(ticketEvent: TicketEventModel): TicketEventModel
    fun findById(id: Long): TicketEventModel?
    fun existsById(id: Long): Boolean

    /** 카테고리/상태로 필터링해 조회한다(둘 다 null 이면 전체). */
    fun search(
        category: TicketEventCategory?,
        status: TicketEventStatus?,
    ): List<TicketEventModel>
}
