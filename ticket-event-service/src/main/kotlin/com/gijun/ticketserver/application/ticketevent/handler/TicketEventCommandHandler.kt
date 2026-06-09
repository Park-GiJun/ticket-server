package com.gijun.ticketserver.application.ticketevent.handler

import com.gijun.ticketserver.application.ticketevent.dto.CreateTicketEventCommand
import com.gijun.ticketserver.application.ticketevent.dto.TicketEventResult
import com.gijun.ticketserver.application.ticketevent.dto.UpdateTicketEventCommand
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

    override fun create(command: CreateTicketEventCommand): TicketEventResult {
        val model = TicketEventModel(
            ticketEventName = command.ticketEventName,
            ticketOpenAt = command.ticketOpenAt,
            ticketClosedAt = command.ticketClosedAt,
            ticketEventAt = command.ticketEventAt,
            ticketEventCategory = command.ticketEventCategory,
        )
        return TicketEventResult.from(ticketEventPersistencePort.save(model))
    }

    override fun update(command: UpdateTicketEventCommand): TicketEventResult {
        // 상태/생성시각은 보존하고 편집 가능한 필드만 갱신한다(상태는 open/close/cancel 로만 전이).
        val existing = ticketEventPersistencePort.findById(command.id)
            ?: throw TicketEventException.TicketEventNotFound()
        val updated = existing.copy(
            ticketEventName = command.ticketEventName,
            ticketOpenAt = command.ticketOpenAt,
            ticketClosedAt = command.ticketClosedAt,
            ticketEventAt = command.ticketEventAt,
            ticketEventCategory = command.ticketEventCategory,
        )
        return TicketEventResult.from(ticketEventPersistencePort.save(updated))
    }

    override fun open(id: Long): TicketEventResult = transition(id) { it.open() }

    override fun close(id: Long): TicketEventResult = transition(id) { it.close() }

    override fun cancel(id: Long): TicketEventResult = transition(id) { it.cancel() }

    private fun transition(
        id: Long,
        change: (TicketEventModel) -> TicketEventModel,
    ): TicketEventResult {
        val current = ticketEventPersistencePort.findById(id)
            ?: throw TicketEventException.TicketEventNotFound()
        return TicketEventResult.from(ticketEventPersistencePort.save(change(current)))
    }
}
