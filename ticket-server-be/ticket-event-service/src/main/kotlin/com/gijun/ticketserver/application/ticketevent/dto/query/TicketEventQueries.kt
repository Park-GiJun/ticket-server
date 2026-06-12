package com.gijun.ticketserver.application.ticketevent.dto.query

import com.gijun.ticketserver.domain.enums.TicketEventCategory
import com.gijun.ticketserver.domain.enums.TicketEventStatus

data class SearchTicketEventsQuery(
    val category: TicketEventCategory? = null,
    val status: TicketEventStatus? = null,
)
