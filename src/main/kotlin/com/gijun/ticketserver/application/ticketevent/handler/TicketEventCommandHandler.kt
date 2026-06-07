package com.gijun.ticketserver.application.ticketevent.handler

import com.gijun.ticketserver.application.ticketevent.port.`in`.CancelTicketEventUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.CloseTicketEventUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.CreateTicketEventUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.OpenTicketEventUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.UpdateTicketEventUseCase
import com.gijun.ticketserver.application.ticketevent.port.out.TicketEventPersistencePort
import com.gijun.ticketserver.domain.exception.TicketEventException
import com.gijun.ticketserver.domain.model.TicketEventModel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class TicketEventCommandHandler(
    private val ticketEventPersistencePort: TicketEventPersistencePort,
) : CreateTicketEventUseCase,
    UpdateTicketEventUseCase,
    OpenTicketEventUseCase,
    CloseTicketEventUseCase,
    CancelTicketEventUseCase {

    override fun create(ticketEvent: TicketEventModel): TicketEventModel =
        // 신규 생성이므로 식별자는 영속 계층이 부여한다.
        ticketEventPersistencePort.save(ticketEvent.copy(id = null))

    override fun update(ticketEvent: TicketEventModel): TicketEventModel {
        val id = ticketEvent.id ?: throw TicketEventException.TicketEventNotFound()
        if (!ticketEventPersistencePort.existsById(id)) {
            throw TicketEventException.TicketEventNotFound()
        }
        return ticketEventPersistencePort.save(ticketEvent)
    }

    override fun open(id: Long): TicketEventModel = transition(id) { it.open() }

    override fun close(id: Long): TicketEventModel = transition(id) { it.close() }

    override fun cancel(id: Long): TicketEventModel = transition(id) { it.cancel() }

    private fun transition(
        id: Long,
        change: (TicketEventModel) -> TicketEventModel,
    ): TicketEventModel {
        val current = ticketEventPersistencePort.findById(id)
            ?: throw TicketEventException.TicketEventNotFound()
        return ticketEventPersistencePort.save(change(current))
    }
}
