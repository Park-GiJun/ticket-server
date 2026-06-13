package com.gijun.ticketserver.application.ticketevent.handler.command

import com.gijun.ticketserver.application.ticketevent.dto.command.CreateTicketEventCommand
import com.gijun.ticketserver.application.ticketevent.dto.result.TicketEventResult
import com.gijun.ticketserver.application.ticketevent.dto.command.UpdateTicketEventCommand
import com.gijun.ticketserver.application.ticketevent.port.`in`.command.CancelTicketEventUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.command.CloseTicketEventUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.command.CompleteTicketEventCreationUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.command.CreateTicketEventUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.command.OpenTicketEventUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.command.UpdateTicketEventUseCase
import com.gijun.ticketserver.application.ticketevent.port.out.persistence.TicketEventPersistencePort
import com.gijun.ticketserver.domain.exception.TicketEventException
import com.gijun.ticketserver.domain.model.TicketEventModel
import com.gijun.ticketserver.domain.service.TicketEventDomainService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class TicketEventCommandHandler(
    private val ticketEventPersistencePort: TicketEventPersistencePort,
    private val ticketEventDomainService: TicketEventDomainService = TicketEventDomainService(),
) : CreateTicketEventUseCase,
    UpdateTicketEventUseCase,
    OpenTicketEventUseCase,
    CloseTicketEventUseCase,
    CancelTicketEventUseCase,
    CompleteTicketEventCreationUseCase {

    override fun create(command: CreateTicketEventCommand): TicketEventResult {
        val model = ticketEventDomainService.create(
            ticketEventName = command.ticketEventName,
            ticketOpenAt = command.ticketOpenAt,
            ticketClosedAt = command.ticketClosedAt,
            ticketEventAt = command.ticketEventAt,
            ticketEventCategory = command.ticketEventCategory,
        )
        return TicketEventResult.from(ticketEventPersistencePort.save(model))
    }

    override fun update(command: UpdateTicketEventCommand): TicketEventResult {
        val existing = ticketEventPersistencePort.findById(command.id)
            ?: throw TicketEventException.TicketEventNotFound()
        val updated = ticketEventDomainService.update(
            existing = existing,
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

    override fun complete(id: Long): TicketEventResult = transition(id) { it.completeCreation() }

    private fun transition(
        id: Long,
        change: (TicketEventModel) -> TicketEventModel,
    ): TicketEventResult {
        val current = ticketEventPersistencePort.findById(id)
            ?: throw TicketEventException.TicketEventNotFound()
        return TicketEventResult.from(ticketEventPersistencePort.save(change(current)))
    }
}