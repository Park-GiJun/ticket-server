package com.gijun.ticketserver.application.ticketevent.handler.command

import com.gijun.ticketserver.application.ticketevent.dto.command.CreateSeatsCommand
import com.gijun.ticketserver.application.ticketevent.dto.result.SeatCreationResult
import com.gijun.ticketserver.application.ticketevent.dto.result.TicketEventResult
import com.gijun.ticketserver.application.ticketevent.port.`in`.command.CreateSeatsUseCase
import com.gijun.ticketserver.application.ticketevent.port.out.persistence.TicketEventPersistencePort
import com.gijun.ticketserver.application.ticketevent.port.out.persistence.TicketEventSeatPersistencePort
import com.gijun.ticketserver.application.ticketevent.port.out.persistence.TicketEventSectionPersistencePort
import com.gijun.ticketserver.domain.exception.TicketEventException
import com.gijun.ticketserver.domain.model.TicketEventSeatModel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 좌석(Seat) 명령 핸들러. 좌석 생성 시 소속 이벤트의 생성 단계를
 * SECTION_CREATED → SEAT_CREATED 로 함께 진행한다(같은 트랜잭션).
 */
@Service
@Transactional
class TicketEventSeatCommandHandler(
    private val ticketEventPersistencePort: TicketEventPersistencePort,
    private val sectionPersistencePort: TicketEventSectionPersistencePort,
    private val seatPersistencePort: TicketEventSeatPersistencePort,
) : CreateSeatsUseCase {

    override fun createSeats(command: CreateSeatsCommand): SeatCreationResult {
        val event = ticketEventPersistencePort.findById(command.ticketEventId)
            ?: throw TicketEventException.TicketEventNotFound()

        val advanced = event.markSeatsCreated()

        val sections = sectionPersistencePort.findByTicketEventId(command.ticketEventId)
        if (sections.isEmpty()) {
            throw TicketEventException.SectionNotFound()
        }

        // 구역별 capacity 만큼 좌석을 생성한다. ticketEventId 는 구역의 값을 단일 출처로 복사.
        val seats = sections.flatMap { section ->
            val sectionId = requireNotNull(section.id) { "저장된 구역에는 id 가 존재해야 합니다" }
            (1..section.capacity).map { number ->
                TicketEventSeatModel(
                    sectionId = sectionId,
                    ticketEventId = section.ticketEventId,
                    rowLabel = "",
                    seatNumber = number,
                )
            }
        }
        val saved = seatPersistencePort.saveAll(seats)
        val updatedEvent = ticketEventPersistencePort.save(advanced)

        return SeatCreationResult(
            ticketEvent = TicketEventResult.from(updatedEvent),
            createdSeatCount = saved.size,
        )
    }
}
