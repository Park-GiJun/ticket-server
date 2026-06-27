package com.gijun.ticketserver.application.ticketevent.handler

import com.gijun.ticketserver.application.ticketevent.port.out.TicketEventSectionPersistencePort
import com.gijun.ticketserver.domain.exception.TicketEventException
import com.gijun.ticketserver.domain.model.TicketEventSectionModel
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class TicketEventSectionQueryHandlerTest : FunSpec({

    val sectionPort = mockk<TicketEventSectionPersistencePort>()
    val handler = TicketEventSectionQueryHandler(sectionPort)

    fun section(id: Long, ticketEventId: Long) = TicketEventSectionModel(
        id = id,
        ticketEventId = ticketEventId,
        sectionName = "VIP석",
        grade = "VIP",
        price = 150_000,
        capacity = 100,
    )

    context("getById") {
        test("소속 이벤트가 일치하면 구역을 반환한다") {
            every { sectionPort.findById(10L) } returns section(id = 10L, ticketEventId = 1L)

            val result = handler.getById(ticketEventId = 1L, id = 10L)

            result.id shouldBe 10L
            result.ticketEventId shouldBe 1L
            result.sectionName shouldBe "VIP석"
            result.price shouldBe 150_000
            result.capacity shouldBe 100
        }

        test("구역이 없으면 SectionNotFound 를 던진다") {
            every { sectionPort.findById(99L) } returns null

            shouldThrow<TicketEventException.SectionNotFound> {
                handler.getById(ticketEventId = 1L, id = 99L)
            }
        }

        test("구역이 다른 이벤트 소속이면 SectionNotFound 를 던진다(소속 검증)") {
            every { sectionPort.findById(10L) } returns section(id = 10L, ticketEventId = 2L)

            shouldThrow<TicketEventException.SectionNotFound> {
                handler.getById(ticketEventId = 1L, id = 10L)
            }
        }
    }

    context("listByEvent") {
        test("이벤트의 구역들을 매핑해 반환한다") {
            every { sectionPort.findByTicketEventId(1L) } returns listOf(
                section(id = 10L, ticketEventId = 1L),
                section(id = 11L, ticketEventId = 1L),
            )

            val results = handler.listByEvent(1L)

            results shouldHaveSize 2
            results.map { it.id } shouldContainExactly listOf(10L, 11L)
        }

        test("구역이 없으면 빈 목록을 반환한다") {
            every { sectionPort.findByTicketEventId(1L) } returns emptyList()

            handler.listByEvent(1L) shouldBe emptyList()
        }
    }
})
