package com.gijun.ticketserver.domain.model

data class TicketEventSectionModel(
    val id: Long? = null,
    val ticketEventModelId: Long,
    val ticketEventSectionName: String,
    val ticketEventSectionOrder: Int
) {
    init {
        require(ticketEventSectionName.isNotBlank()) { "ticketEventSectionName cannot be blank" }
    }
}
