package com.gijun.ticketserver.application.ticketevent.port.`in`

import com.gijun.ticketserver.domain.enums.TicketEventCategory
import com.gijun.ticketserver.domain.enums.TicketEventStatus
import com.gijun.ticketserver.domain.model.TicketEventModel

/**
 * 티켓 이벤트 조회(Query) 유스케이스. **1 인터페이스 = 1 함수** 규칙을 따른다.
 */

interface GetTicketEventUseCase {
    fun getById(id: Long): TicketEventModel
}

interface SearchTicketEventsUseCase {
    fun search(
        category: TicketEventCategory? = null,
        status: TicketEventStatus? = null,
    ): List<TicketEventModel>
}
