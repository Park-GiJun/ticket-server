package com.gijun.ticketserver.infrastructure.adapter.`in`.ticketevent.web

import com.gijun.ticketserver.application.ticketevent.dto.CreateSeatsCommand
import com.gijun.ticketserver.application.ticketevent.dto.SearchTicketEventsQuery
import com.gijun.ticketserver.application.ticketevent.port.`in`.CancelTicketEventUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.CloseTicketEventUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.CompleteTicketEventCreationUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.CreateSeatsUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.CreateSectionsUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.CreateTicketEventUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.GetTicketEventUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.OpenTicketEventUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.SearchTicketEventsUseCase
import com.gijun.ticketserver.application.ticketevent.port.`in`.UpdateTicketEventUseCase
import com.gijun.ticketserver.domain.enums.TicketEventCategory
import com.gijun.ticketserver.domain.enums.TicketEventStatus
import com.gijun.ticketserver.infrastructure.adapter.`in`.ticketevent.web.dto.CreateSectionsRequest
import com.gijun.ticketserver.infrastructure.adapter.`in`.ticketevent.web.dto.CreateTicketEventRequest
import com.gijun.ticketserver.infrastructure.adapter.`in`.ticketevent.web.dto.SeatCreationResponse
import com.gijun.ticketserver.infrastructure.adapter.`in`.ticketevent.web.dto.SectionCreationResponse
import com.gijun.ticketserver.infrastructure.adapter.`in`.ticketevent.web.dto.TicketEventResponse
import com.gijun.ticketserver.infrastructure.adapter.`in`.ticketevent.web.dto.UpdateTicketEventRequest
import com.gijun.ticketserver.shared.openapi.OpenApiConfig
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "TicketEvent", description = "티켓 이벤트 API (생성 / 수정 / 상태 전이 / 조회)")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@RestController
@RequestMapping("/api/ticket-events")
class TicketEventWebAdapter(
    private val createTicketEventUseCase: CreateTicketEventUseCase,
    private val updateTicketEventUseCase: UpdateTicketEventUseCase,
    private val openTicketEventUseCase: OpenTicketEventUseCase,
    private val closeTicketEventUseCase: CloseTicketEventUseCase,
    private val cancelTicketEventUseCase: CancelTicketEventUseCase,
    private val createSectionsUseCase: CreateSectionsUseCase,
    private val createSeatsUseCase: CreateSeatsUseCase,
    private val completeTicketEventCreationUseCase: CompleteTicketEventCreationUseCase,
    private val getTicketEventUseCase: GetTicketEventUseCase,
    private val searchTicketEventsUseCase: SearchTicketEventsUseCase,
) {

    @Operation(summary = "티켓 이벤트 생성", description = "예매 일정/카테고리로 티켓 이벤트를 등록한다. 초기 상태는 SCHEDULED.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "생성 성공"),
        ApiResponse(responseCode = "400", description = "요청 값 검증 실패 / 일정 불변식 위반"),
        ApiResponse(responseCode = "401", description = "인증 필요"),
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateTicketEventRequest): TicketEventResponse =
        TicketEventResponse.from(createTicketEventUseCase.create(request.toCommand()))

    @Operation(summary = "티켓 이벤트 수정", description = "이름/일정/카테고리를 수정한다. 상태는 변경하지 않는다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "수정 성공"),
        ApiResponse(responseCode = "400", description = "요청 값 검증 실패 / 일정 불변식 위반"),
        ApiResponse(responseCode = "404", description = "티켓 이벤트 없음"),
    )
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateTicketEventRequest,
    ): TicketEventResponse =
        TicketEventResponse.from(updateTicketEventUseCase.update(request.toCommand(id)))

    @Operation(summary = "예매 오픈", description = "SCHEDULED → OPEN 으로 전이한다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "전이 성공"),
        ApiResponse(responseCode = "404", description = "티켓 이벤트 없음"),
        ApiResponse(responseCode = "409", description = "허용되지 않는 상태 전이"),
    )
    @PostMapping("/{id}/open")
    fun open(@PathVariable id: Long): TicketEventResponse =
        TicketEventResponse.from(openTicketEventUseCase.open(id))

    @Operation(summary = "예매 마감", description = "OPEN → CLOSED 로 전이한다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "전이 성공"),
        ApiResponse(responseCode = "404", description = "티켓 이벤트 없음"),
        ApiResponse(responseCode = "409", description = "허용되지 않는 상태 전이"),
    )
    @PostMapping("/{id}/close")
    fun close(@PathVariable id: Long): TicketEventResponse =
        TicketEventResponse.from(closeTicketEventUseCase.close(id))

    @Operation(summary = "이벤트 취소", description = "종료/취소 상태가 아니면 CANCELLED 로 전이한다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "전이 성공"),
        ApiResponse(responseCode = "404", description = "티켓 이벤트 없음"),
        ApiResponse(responseCode = "409", description = "허용되지 않는 상태 전이"),
    )
    @PostMapping("/{id}/cancel")
    fun cancel(@PathVariable id: Long): TicketEventResponse =
        TicketEventResponse.from(cancelTicketEventUseCase.cancel(id))

    @Operation(
        summary = "구역 생성 (셋업 2단계)",
        description = "이벤트에 좌석 구역을 일괄 등록한다. 생성 단계 EVENT_CREATED → SECTION_CREATED.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "생성 성공"),
        ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
        ApiResponse(responseCode = "404", description = "티켓 이벤트 없음"),
        ApiResponse(responseCode = "409", description = "허용되지 않는 생성 단계 전이"),
    )
    @PostMapping("/{id}/sections")
    @ResponseStatus(HttpStatus.CREATED)
    fun createSections(
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateSectionsRequest,
    ): SectionCreationResponse =
        SectionCreationResponse.from(createSectionsUseCase.createSections(request.toCommand(id)))

    @Operation(
        summary = "좌석 생성 (셋업 3단계)",
        description = "각 구역의 capacity 만큼 좌석을 자동 생성한다. 생성 단계 SECTION_CREATED → SEAT_CREATED.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "생성 성공"),
        ApiResponse(responseCode = "404", description = "티켓 이벤트/구역 없음"),
        ApiResponse(responseCode = "409", description = "허용되지 않는 생성 단계 전이"),
    )
    @PostMapping("/{id}/seats")
    @ResponseStatus(HttpStatus.CREATED)
    fun createSeats(@PathVariable id: Long): SeatCreationResponse =
        SeatCreationResponse.from(createSeatsUseCase.createSeats(CreateSeatsCommand(id)))

    @Operation(
        summary = "셋업 완료 (4단계)",
        description = "좌석 생성까지 끝난 이벤트의 셋업을 완료한다. 생성 단계 SEAT_CREATED → COMPLETED.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "완료 성공"),
        ApiResponse(responseCode = "404", description = "티켓 이벤트 없음"),
        ApiResponse(responseCode = "409", description = "허용되지 않는 생성 단계 전이"),
    )
    @PostMapping("/{id}/complete")
    fun complete(@PathVariable id: Long): TicketEventResponse =
        TicketEventResponse.from(completeTicketEventCreationUseCase.complete(id))

    @Operation(summary = "티켓 이벤트 단건 조회", description = "ID 로 티켓 이벤트를 조회한다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "404", description = "티켓 이벤트 없음"),
    )
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): TicketEventResponse =
        TicketEventResponse.from(getTicketEventUseCase.getById(id))

    @Operation(summary = "티켓 이벤트 목록 조회", description = "카테고리/상태로 필터링해 조회한다(미지정 시 전체).")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    fun search(
        @RequestParam(required = false) category: TicketEventCategory?,
        @RequestParam(required = false) status: TicketEventStatus?,
    ): List<TicketEventResponse> =
        searchTicketEventsUseCase.search(SearchTicketEventsQuery(category, status))
            .map { TicketEventResponse.from(it) }
}
