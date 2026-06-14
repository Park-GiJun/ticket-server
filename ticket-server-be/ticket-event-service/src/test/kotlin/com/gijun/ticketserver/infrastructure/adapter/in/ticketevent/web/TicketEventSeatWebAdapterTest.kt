package com.gijun.ticketserver.infrastructure.adapter.`in`.ticketevent.web

import com.gijun.ticketserver.application.ticketevent.dto.result.SeatAvailabilityResult
import com.gijun.ticketserver.application.ticketevent.dto.result.TicketEventSeatResult
import com.gijun.ticketserver.application.ticketevent.port.`in`.query.GetSeatAvailabilityUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.query.GetSeatUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.query.ListSeatsByEventUseCase
import com.gijun.ticketserver.domain.enums.SeatStatus
import com.gijun.ticketserver.domain.exception.TicketEventException
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(TicketEventSeatWebAdapter::class)
@Import(TicketEventSeatWebAdapterTest.MockConfig::class)
class TicketEventSeatWebAdapterTest(
    private val mockMvc: MockMvc,
    private val getSeatUseCase: GetSeatUseCase,
    private val listSeatsByEventUseCase: ListSeatsByEventUseCase,
    private val getSeatAvailabilityUseCase: GetSeatAvailabilityUseCase,
) : FunSpec({

    extension(SpringExtension)

    beforeTest {
        clearMocks(getSeatUseCase, listSeatsByEventUseCase, getSeatAvailabilityUseCase)
    }

    fun seatResult(id: Long, eventId: Long, status: SeatStatus = SeatStatus.AVAILABLE) =
        TicketEventSeatResult(
            id = id,
            sectionId = 5L,
            ticketEventId = eventId,
            rowLabel = "A",
            seatNumber = 1,
            status = status,
        )

    test("GET /{eventId}/seats — 좌석 목록을 200 으로 반환한다") {
        every { listSeatsByEventUseCase.listByEvent(1L) } returns listOf(
            seatResult(id = 100L, eventId = 1L),
            seatResult(id = 101L, eventId = 1L, status = SeatStatus.SOLD),
        )

        mockMvc.get("/api/ticket-events/1/seats").andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(2) }
            jsonPath("$[1].status") { value("SOLD") }
        }
    }

    test("GET /{eventId}/seats/availability — 리터럴 경로가 {seatId} 보다 우선 매칭된다") {
        every { getSeatAvailabilityUseCase.getAvailability(1L) } returns SeatAvailabilityResult.from(
            ticketEventId = 1L,
            counts = mapOf(SeatStatus.AVAILABLE to 80L, SeatStatus.SOLD to 20L),
        )

        mockMvc.get("/api/ticket-events/1/seats/availability").andExpect {
            status { isOk() }
            jsonPath("$.total") { value(100) }
            jsonPath("$.available") { value(80) }
            jsonPath("$.counts.AVAILABLE") { value(80) }
            jsonPath("$.counts.HELD") { value(0) }
        }

        verify { getSeatAvailabilityUseCase.getAvailability(1L) }
        verify(exactly = 0) { getSeatUseCase.getById(any(), any()) }
    }

    test("GET /{eventId}/seats/{seatId} — 단건을 200 으로 반환하고 경로의 eventId 를 위임한다") {
        every { getSeatUseCase.getById(1L, 100L) } returns seatResult(id = 100L, eventId = 1L)

        mockMvc.get("/api/ticket-events/1/seats/100").andExpect {
            status { isOk() }
            jsonPath("$.id") { value(100) }
            jsonPath("$.ticketEventId") { value(1) }
        }

        verify { getSeatUseCase.getById(1L, 100L) }
    }

    test("GET 단건 — 소속이 아니거나 없으면 404 를 반환한다") {
        every { getSeatUseCase.getById(1L, 404L) } throws TicketEventException.SeatNotFound()

        mockMvc.get("/api/ticket-events/1/seats/404").andExpect {
            status { isNotFound() }
        }
    }
}) {
    @TestConfiguration
    class MockConfig {
        @Bean
        fun getSeatUseCase(): GetSeatUseCase = mockk()

        @Bean
        fun listSeatsByEventUseCase(): ListSeatsByEventUseCase = mockk()

        @Bean
        fun getSeatAvailabilityUseCase(): GetSeatAvailabilityUseCase = mockk()
    }
}
