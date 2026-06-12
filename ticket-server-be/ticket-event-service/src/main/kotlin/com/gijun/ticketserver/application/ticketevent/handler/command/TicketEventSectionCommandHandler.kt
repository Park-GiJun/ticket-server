package com.gijun.ticketserver.application.ticketevent.handler.command

import com.gijun.ticketserver.application.ticketevent.dto.command.CreateSectionsCommand
import com.gijun.ticketserver.application.ticketevent.dto.result.SectionCreationResult
import com.gijun.ticketserver.application.ticketevent.dto.result.TicketEventResult
import com.gijun.ticketserver.application.ticketevent.dto.result.TicketEventSectionResult
import com.gijun.ticketserver.application.ticketevent.port.`in`.command.CreateSectionsUseCase
import com.gijun.ticketserver.application.ticketevent.port.out.persistence.TicketEventPersistencePort
import com.gijun.ticketserver.application.ticketevent.port.out.persistence.TicketEventSectionPersistencePort
import com.gijun.ticketserver.domain.exception.TicketEventException
import com.gijun.ticketserver.domain.model.TicketEventSectionModel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 구역(Section) 명령 핸들러. 구역 생성 시 소속 이벤트의 생성 단계를
 * EVENT_CREATED → SECTION_CREATED 로 함께 진행한다(같은 트랜잭션).
 */
@Service
@Transactional
class TicketEventSectionCommandHandler(
    private val ticketEventPersistencePort: TicketEventPersistencePort,
    private val sectionPersistencePort: TicketEventSectionPersistencePort,
) : CreateSectionsUseCase {

    override fun createSections(command: CreateSectionsCommand): SectionCreationResult {
        val event = ticketEventPersistencePort.findById(command.ticketEventId)
            ?: throw TicketEventException.TicketEventNotFound()

        // 도메인이 생성 단계(EVENT_CREATED)에서만 전이를 허용하므로 먼저 검증·전이한다.
        val advanced = event.markSectionsCreated()

        val sections = command.sections.map {
            TicketEventSectionModel(
                ticketEventId = command.ticketEventId,
                sectionName = it.sectionName,
                grade = it.grade,
                price = it.price,
                capacity = it.capacity,
            )
        }
        val saved = sectionPersistencePort.saveAll(sections)
        val updatedEvent = ticketEventPersistencePort.save(advanced)

        return SectionCreationResult(
            ticketEvent = TicketEventResult.from(updatedEvent),
            sections = saved.map { TicketEventSectionResult.from(it) },
        )
    }
}
