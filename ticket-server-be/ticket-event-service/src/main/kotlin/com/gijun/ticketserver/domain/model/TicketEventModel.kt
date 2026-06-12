package com.gijun.ticketserver.domain.model

import com.gijun.ticketserver.domain.enums.TicketCreationStatus
import com.gijun.ticketserver.domain.enums.TicketEventCategory
import com.gijun.ticketserver.domain.enums.TicketEventStatus
import com.gijun.ticketserver.domain.exception.TicketEventException
import java.time.Instant

/**
 * 티켓 이벤트 도메인 모델. 영속/웹 등 인프라 관심사로부터 독립적이다.
 *
 * @property ticketOpenAt   예매 오픈 시각
 * @property ticketClosedAt 예매 마감 시각
 * @property ticketEventAt  실제 이벤트(공연/경기) 시각
 */
data class TicketEventModel(
    val id: Long? = null,
    val ticketEventName: String,
    val ticketOpenAt: Instant,
    val ticketClosedAt: Instant,
    val ticketEventAt: Instant,
    val ticketEventStatus: TicketEventStatus = TicketEventStatus.SCHEDULED,
    val ticketCreationStatus: TicketCreationStatus = TicketCreationStatus.EVENT_CREATED,
    val ticketEventCategory: TicketEventCategory,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    init {
        require(ticketEventName.isNotBlank()) { "ticketEventName must not be blank" }
        require(ticketClosedAt.isAfter(ticketOpenAt)) {
            "ticketClosedAt must be after ticketOpenAt"
        }
        require(!ticketEventAt.isBefore(ticketClosedAt)) {
            "ticketEventAt must not be before ticketClosedAt"
        }
    }

    /** 주어진 시각 기준으로 예매 가능 기간인지 여부. */
    fun isBookable(at: Instant): Boolean =
        ticketEventStatus == TicketEventStatus.OPEN &&
            !at.isBefore(ticketOpenAt) &&
            at.isBefore(ticketClosedAt)

    fun withStatus(newStatus: TicketEventStatus): TicketEventModel =
        copy(ticketEventStatus = newStatus)

    /** 예매 오픈: `SCHEDULED` 상태에서만 가능. */
    fun open(): TicketEventModel = transitionTo(
        TicketEventStatus.OPEN,
        allowedFrom = setOf(TicketEventStatus.SCHEDULED),
    )

    /** 예매 마감: `OPEN` 또는 `SOLD_OUT` 상태에서 가능. */
    fun close(): TicketEventModel = transitionTo(
        TicketEventStatus.CLOSED,
        allowedFrom = setOf(TicketEventStatus.OPEN, TicketEventStatus.SOLD_OUT),
    )

    /**
     * 매진 처리: `OPEN` 상태에서만 가능. 잔여(AVAILABLE) 좌석이 0이 되는 시점에
     * 마지막 판매를 처리하는 흐름이 호출한다. 카운트는 좌석 상태(단일 진실 원천)에서 파생하며
     * 이벤트 모델은 보유하지 않는다.
     */
    fun markSoldOut(): TicketEventModel = transitionTo(
        TicketEventStatus.SOLD_OUT,
        allowedFrom = setOf(TicketEventStatus.OPEN),
    )

    /** 매진 해제: `SOLD_OUT` 상태에서 환불 등으로 좌석이 다시 풀릴 때 `OPEN` 으로 복귀. */
    fun reopen(): TicketEventModel = transitionTo(
        TicketEventStatus.OPEN,
        allowedFrom = setOf(TicketEventStatus.SOLD_OUT),
    )

    /** 이벤트 취소: 종료/취소된 이벤트가 아니면 가능. */
    fun cancel(): TicketEventModel = transitionTo(
        TicketEventStatus.CANCELLED,
        allowedFrom = setOf(
            TicketEventStatus.SCHEDULED,
            TicketEventStatus.OPEN,
            TicketEventStatus.CLOSED,
            TicketEventStatus.SOLD_OUT,
        ),
    )

    private fun transitionTo(
        target: TicketEventStatus,
        allowedFrom: Set<TicketEventStatus>,
    ): TicketEventModel {
        if (ticketEventStatus !in allowedFrom) {
            throw TicketEventException.InvalidStatusTransition(
                "$ticketEventStatus 상태에서는 $target 로 전이할 수 없습니다",
            )
        }
        return withStatus(target)
    }

    /** 셋업 완료(`COMPLETED`) 여부. 예매 오픈 가능 조건의 하나. */
    fun isCreationCompleted(): Boolean = ticketCreationStatus == TicketCreationStatus.COMPLETED

    /** 구역 구성 완료: `EVENT_CREATED` 단계에서만 가능. */
    fun markSectionsCreated(): TicketEventModel = advanceCreationTo(
        TicketCreationStatus.SECTION_CREATED,
        allowedFrom = setOf(TicketCreationStatus.EVENT_CREATED),
    )

    /** 좌석 생성 완료: `SECTION_CREATED` 단계에서만 가능. */
    fun markSeatsCreated(): TicketEventModel = advanceCreationTo(
        TicketCreationStatus.SEAT_CREATED,
        allowedFrom = setOf(TicketCreationStatus.SECTION_CREATED),
    )

    /** 셋업 완료: `SEAT_CREATED` 단계에서만 가능. */
    fun completeCreation(): TicketEventModel = advanceCreationTo(
        TicketCreationStatus.COMPLETED,
        allowedFrom = setOf(TicketCreationStatus.SEAT_CREATED),
    )

    private fun advanceCreationTo(
        target: TicketCreationStatus,
        allowedFrom: Set<TicketCreationStatus>,
    ): TicketEventModel {
        if (ticketCreationStatus !in allowedFrom) {
            throw TicketEventException.InvalidStatusTransition(
                "생성 단계 $ticketCreationStatus 에서는 $target 로 진행할 수 없습니다",
            )
        }
        return copy(ticketCreationStatus = target)
    }
}
