package com.gijun.ticketserver.application.ticketevent.handler.query

import com.gijun.ticketserver.application.ticketevent.port.out.persistence.TicketEventSeatPersistencePort
import com.gijun.ticketserver.domain.enums.SeatStatus
import com.gijun.ticketserver.domain.exception.TicketEventException
import com.gijun.ticketserver.domain.model.TicketEventSeatModel
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class TicketEventSeatQueryHandlerTest : FunSpec({

    val seatPort = mockk<TicketEventSeatPersistencePort>()
    val handler = TicketEventSeatQueryHandler(seatPort)

    fun seat(id: Long, ticketEventId: Long, status: SeatStatus = SeatStatus.AVAILABLE) =
        TicketEventSeatModel(
            id = id,
            sectionId = 5L,
            ticketEventId = ticketEventId,
            rowLabel = "A",
            seatNumber = 1,
            status = status,
        )

    context("getById") {
        test("소속 이벤트가 일치하면 좌석을 반환한다") {
            every { seatPort.findById(100L) } returns seat(id = 100L, ticketEventId = 1L)

            val result = handler.getById(ticketEventId = 1L, id = 100L)

            result.id shouldBe 100L
            result.ticketEventId shouldBe 1L
            result.sectionId shouldBe 5L
            result.status shouldBe SeatStatus.AVAILABLE
        }

        test("좌석이 없으면 SeatNotFound 를 던진다") {
            every { seatPort.findById(404L) } returns null

            shouldThrow<TicketEventException.SeatNotFound> {
                handler.getById(ticketEventId = 1L, id = 404L)
            }
        }

        test("좌석이 다른 이벤트 소속이면 SeatNotFound 를 던진다(소속 검증)") {
            every { seatPort.findById(100L) } returns seat(id = 100L, ticketEventId = 2L)

            shouldThrow<TicketEventException.SeatNotFound> {
                handler.getById(ticketEventId = 1L, id = 100L)
            }
        }
    }

    context("listByEvent") {
        test("이벤트의 좌석들을 매핑해 반환한다") {
            every { seatPort.findByTicketEventId(1L) } returns listOf(
                seat(id = 100L, ticketEventId = 1L),
                seat(id = 101L, ticketEventId = 1L, status = SeatStatus.SOLD),
            )

            val results = handler.listByEvent(1L)

            results shouldHaveSize 2
            results[1].status shouldBe SeatStatus.SOLD
        }
    }

    context("getAvailability") {
        test("상태별 집계로 합계/잔여석을 계산하고 누락 상태는 0으로 채운다") {
            every { seatPort.countByTicketEventIdGroupedByStatus(1L) } returns mapOf(
                SeatStatus.AVAILABLE to 80L,
                SeatStatus.SOLD to 20L,
            )

            val result = handler.getAvailability(1L)

            result.ticketEventId shouldBe 1L
            result.total shouldBe 100L
            result.available shouldBe 80L
            result.counts[SeatStatus.AVAILABLE] shouldBe 80L
            result.counts[SeatStatus.SOLD] shouldBe 20L
            result.counts[SeatStatus.HELD] shouldBe 0L
            result.counts[SeatStatus.BLOCKED] shouldBe 0L
            result.counts.keys shouldBe SeatStatus.entries.toSet()
        }
    }
})
