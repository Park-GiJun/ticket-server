package com.gijun.ticketserver.domain.enums

/**
 * 티켓 이벤트(공연/경기 등)의 예매 진행 상태.
 *
 * - [SCHEDULED] 예매 오픈 전 (등록만 된 상태)
 * - [OPEN]      예매 진행 중
 * - [CLOSED]    예매 마감 (오픈 기간 종료)
 * - [SOLD_OUT]  전 좌석 매진
 * - [CANCELLED] 이벤트 취소
 * - [COMPLETED] 이벤트 종료(공연/경기 완료)
 */
enum class TicketEventStatus {
    SCHEDULED,
    OPEN,
    CLOSED,
    SOLD_OUT,
    CANCELLED,
    COMPLETED,
}
