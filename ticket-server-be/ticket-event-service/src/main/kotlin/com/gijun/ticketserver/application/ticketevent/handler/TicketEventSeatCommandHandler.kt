package com.gijun.ticketserver.application.ticketevent.handler

import com.gijun.ticketserver.application.ticketevent.dto.CreateSeatsCommand
import com.gijun.ticketserver.application.ticketevent.dto.SeatCreationResult
import com.gijun.ticketserver.application.ticketevent.dto.TicketEventResult
import com.gijun.ticketserver.application.ticketevent.port.`in`.CreateSeatsUseCase
import com.gijun.ticketserver.application.ticketevent.port.out.TicketEventPersistencePort
import com.gijun.ticketserver.application.ticketevent.port.out.TicketEventSeatPersistencePort
import com.gijun.ticketserver.application.ticketevent.port.out.TicketEventSectionPersistencePort
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

        // 구역별 capacity 만큼 좌석을 행×열 그리드로 생성한다(레이아웃).
        // 행 = ceil(capacity / seatsPerRow), 행 라벨 A,B,…,Z,AA,… / 열 = 1..seatsPerRow.
        val seats = sections.flatMap { section ->
            val sectionId = requireNotNull(section.id) { "저장된 구역에는 id 가 존재해야 합니다" }
            val perRow = section.seatsPerRow.coerceAtLeast(1)
            (0 until section.capacity).map { index ->
                TicketEventSeatModel(
                    sectionId = sectionId,
                    ticketEventId = section.ticketEventId,
                    rowLabel = rowLabelOf(index / perRow),
                    seatNumber = index % perRow + 1,
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

/** 행 인덱스 → 라벨(bijective base-26): 0→A, 1→B, …, 25→Z, 26→AA, … */
private fun rowLabelOf(index: Int): String {
    var n = index + 1
    val sb = StringBuilder()
    while (n > 0) {
        val rem = (n - 1) % 26
        sb.insert(0, 'A' + rem)
        n = (n - 1) / 26
    }
    return sb.toString()
}
