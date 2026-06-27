package com.gijun.ticketserver.infrastructure.adapter.`in`.ticketevent.web

import com.gijun.ticketserver.application.ticketevent.port.`in`.GetSectionUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.ListSectionsByEventUseCase
import com.gijun.ticketserver.infrastructure.adapter.`in`.ticketevent.web.dto.TicketEventSectionResponse
import com.gijun.ticketserver.shared.openapi.OpenApiConfig
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "TicketEventSection", description = "티켓 이벤트 구역 조회 API")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@RestController
@RequestMapping("/api/ticket-events")
class TicketEventSectionWebAdapter(
    private val getSectionUseCase: GetSectionUseCase,
    private val listSectionsByEventUseCase: ListSectionsByEventUseCase,
) {

    @Operation(summary = "이벤트 구역 목록 조회", description = "특정 티켓 이벤트에 속한 구역을 모두 조회한다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{eventId}/sections")
    fun listByEvent(@PathVariable eventId: Long): List<TicketEventSectionResponse> =
        listSectionsByEventUseCase.listByEvent(eventId)
            .map { TicketEventSectionResponse.from(it) }

    @Operation(summary = "구역 단건 조회", description = "구역 ID 로 단건 조회한다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "404", description = "구역 없음"),
    )
    @GetMapping("/{eventId}/sections/{sectionId}")
    fun getById(
        @PathVariable eventId: Long,
        @PathVariable sectionId: Long,
    ): TicketEventSectionResponse =
        TicketEventSectionResponse.from(getSectionUseCase.getById(eventId, sectionId))
}
