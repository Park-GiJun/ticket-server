package com.gijun.ticketserver.infrastructure.adapter.`in`.ticketevent.web

import com.gijun.ticketserver.application.ticketevent.dto.TicketEventSectionResult
import com.gijun.ticketserver.application.ticketevent.port.`in`.GetSectionUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.ListSectionsByEventUseCase
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

@WebMvcTest(TicketEventSectionWebAdapter::class)
@Import(TicketEventSectionWebAdapterTest.MockConfig::class)
class TicketEventSectionWebAdapterTest(
    private val mockMvc: MockMvc,
    private val getSectionUseCase: GetSectionUseCase,
    private val listSectionsByEventUseCase: ListSectionsByEventUseCase,
) : FunSpec({

    extension(SpringExtension)

    beforeTest { clearMocks(getSectionUseCase, listSectionsByEventUseCase) }

    fun sectionResult(id: Long, eventId: Long) = TicketEventSectionResult(
        id = id,
        ticketEventId = eventId,
        sectionName = "VIP석",
        grade = "VIP",
        price = 150_000,
        capacity = 100,
        seatsPerRow = 10,
    )

    test("GET /{eventId}/sections — 구역 목록을 200 으로 반환한다") {
        every { listSectionsByEventUseCase.listByEvent(1L) } returns listOf(
            sectionResult(id = 10L, eventId = 1L),
            sectionResult(id = 11L, eventId = 1L),
        )

        mockMvc.get("/api/ticket-events/1/sections").andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(2) }
            jsonPath("$[0].id") { value(10) }
            jsonPath("$[1].id") { value(11) }
        }
    }

    test("GET /{eventId}/sections/{sectionId} — 단건을 200 으로 반환하고 경로의 eventId 를 위임한다") {
        every { getSectionUseCase.getById(1L, 10L) } returns sectionResult(id = 10L, eventId = 1L)

        mockMvc.get("/api/ticket-events/1/sections/10").andExpect {
            status { isOk() }
            jsonPath("$.id") { value(10) }
            jsonPath("$.ticketEventId") { value(1) }
            jsonPath("$.sectionName") { value("VIP석") }
        }

        verify { getSectionUseCase.getById(1L, 10L) }
    }

    test("GET 단건 — 소속이 아니거나 없으면 404 를 반환한다") {
        every { getSectionUseCase.getById(1L, 99L) } throws TicketEventException.SectionNotFound()

        mockMvc.get("/api/ticket-events/1/sections/99").andExpect {
            status { isNotFound() }
        }
    }
}) {
    @TestConfiguration
    class MockConfig {
        @Bean
        fun getSectionUseCase(): GetSectionUseCase = mockk()

        @Bean
        fun listSectionsByEventUseCase(): ListSectionsByEventUseCase = mockk()
    }
}
