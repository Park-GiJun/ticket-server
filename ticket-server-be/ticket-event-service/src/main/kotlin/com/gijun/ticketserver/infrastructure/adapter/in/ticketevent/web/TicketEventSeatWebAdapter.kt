package com.gijun.ticketserver.infrastructure.adapter.`in`.ticketevent.web

import com.gijun.ticketserver.application.ticketevent.port.`in`.GetSeatAvailabilityUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.GetSeatUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.ListSeatsByEventUseCase
import com.gijun.ticketserver.infrastructure.adapter.`in`.ticketevent.web.dto.SeatAvailabilityResponse
import com.gijun.ticketserver.infrastructure.adapter.`in`.ticketevent.web.dto.TicketEventSeatResponse
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

@Tag(name = "TicketEventSeat", description = "티켓 이벤트 좌석 조회 API")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@RestController
@RequestMapping("/api/ticket-events")
class TicketEventSeatWebAdapter(
    private val getSeatUseCase: GetSeatUseCase,
    private val listSeatsByEventUseCase: ListSeatsByEventUseCase,
    private val getSeatAvailabilityUseCase: GetSeatAvailabilityUseCase,
) {

    @Operation(summary = "이벤트 좌석 목록 조회", description = "특정 티켓 이벤트에 속한 좌석을 모두 조회한다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{eventId}/seats")
    fun listByEvent(@PathVariable eventId: Long): List<TicketEventSeatResponse> =
        listSeatsByEventUseCase.listByEvent(eventId)
            .map { TicketEventSeatResponse.from(it) }

    @Operation(
        summary = "좌석 잔여 현황 조회",
        description = "이벤트 좌석을 상태별로 집계해 합계/잔여석과 함께 반환한다.",
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{eventId}/seats/availability")
    fun getAvailability(@PathVariable eventId: Long): SeatAvailabilityResponse =
        SeatAvailabilityResponse.from(getSeatAvailabilityUseCase.getAvailability(eventId))

    @Operation(summary = "좌석 단건 조회", description = "좌석 ID 로 단건 조회한다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "404", description = "좌석 없음"),
    )
    @GetMapping("/{eventId}/seats/{seatId}")
    fun getById(
        @PathVariable eventId: Long,
        @PathVariable seatId: Long,
    ): TicketEventSeatResponse =
        TicketEventSeatResponse.from(getSeatUseCase.getById(eventId, seatId))
}
