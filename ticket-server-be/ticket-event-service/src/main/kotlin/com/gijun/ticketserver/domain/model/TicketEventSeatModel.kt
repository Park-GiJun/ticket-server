package com.gijun.ticketserver.domain.model

import com.gijun.ticketserver.domain.enums.SeatStatus
import com.gijun.ticketserver.domain.exception.TicketEventException
import java.time.Instant

/**
 * 티켓 이벤트의 개별 좌석 도메인 모델. 하나의 [TicketEventSectionModel] 에 소속된다.
 *
 * 좌석은 예매 흐름의 최소 단위이며, [AVAILABLE][SeatStatus.AVAILABLE] →
 * [HELD][SeatStatus.HELD] → [SOLD][SeatStatus.SOLD] 의 상태 전이를 가진다.
 *
 * @property sectionId     소속 구역 식별자
 * @property ticketEventId 소속 이벤트 식별자. `sectionId` 로부터 도달 가능한 값이지만,
 *                         이벤트 단위 핫쿼리(잔여석·매진 판정)와 파티셔닝을 위해 의도적으로 비정규화한다.
 *                         좌석↔이벤트는 생성 후 불변이라 갱신 이상이 없다 — 생성 시 소속 구역의 값을 복사한다.
 * @property rowLabel      행 표기 (예: "A", "12"). 비좌석형(스탠딩) 구역에서는 비어 있을 수 있다.
 * @property seatNumber    행 내 좌석 번호. 1 이상.
 * @property status        현재 좌석 상태
 */
data class TicketEventSeatModel(
    val id: Long? = null,
    val sectionId: Long,
    val ticketEventId: Long,
    val rowLabel: String,
    val seatNumber: Int,
    val status: SeatStatus = SeatStatus.AVAILABLE,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    init {
        require(seatNumber >= 1) { "seatNumber must be at least 1" }
    }

    /** 좌석을 예매 가능 상태로 다룰 수 있는지 여부. */
    fun isAvailable(): Boolean = status == SeatStatus.AVAILABLE

    /** 결제 진행을 위한 임시 점유: `AVAILABLE` 상태에서만 가능. */
    fun hold(): TicketEventSeatModel = transitionTo(
        SeatStatus.HELD,
        allowedFrom = setOf(SeatStatus.AVAILABLE),
    )

    /** 점유 해제: `HELD` 상태에서만 가능. */
    fun release(): TicketEventSeatModel = transitionTo(
        SeatStatus.AVAILABLE,
        allowedFrom = setOf(SeatStatus.HELD),
    )

    /** 판매 확정: `HELD` 상태에서만 가능. */
    fun sell(): TicketEventSeatModel = transitionTo(
        SeatStatus.SOLD,
        allowedFrom = setOf(SeatStatus.HELD),
    )

    /** 예매 취소(환불): `SOLD` 상태에서만 가능하며 다시 예매 가능 상태로 돌린다. */
    fun cancel(): TicketEventSeatModel = transitionTo(
        SeatStatus.AVAILABLE,
        allowedFrom = setOf(SeatStatus.SOLD),
    )

    /** 판매 제외 처리: `AVAILABLE` 상태에서만 가능(시야 제한석·관계자석 등). */
    fun block(): TicketEventSeatModel = transitionTo(
        SeatStatus.BLOCKED,
        allowedFrom = setOf(SeatStatus.AVAILABLE),
    )

    /** 판매 제외 해제: `BLOCKED` 상태에서만 가능. */
    fun unblock(): TicketEventSeatModel = transitionTo(
        SeatStatus.AVAILABLE,
        allowedFrom = setOf(SeatStatus.BLOCKED),
    )

    private fun transitionTo(
        target: SeatStatus,
        allowedFrom: Set<SeatStatus>,
    ): TicketEventSeatModel {
        if (status !in allowedFrom) {
            throw TicketEventException.InvalidSeatStatusTransition(
                "$status 상태에서는 $target 로 전이할 수 없습니다",
            )
        }
        return copy(status = target)
    }
}
