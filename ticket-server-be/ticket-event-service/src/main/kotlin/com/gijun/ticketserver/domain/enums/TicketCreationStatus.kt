package com.gijun.ticketserver.domain.enums

/**
 * 티켓 이벤트의 셋업(생성) 진행 단계. 예매 진행 상태([TicketEventStatus])와는 별개로,
 * 이벤트 등록이 어디까지 구성됐는지를 추적한다. 단계는 순방향으로만 진행된다.
 *
 * 1. [EVENT_CREATED]   이벤트 기본 정보만 생성된 상태
 * 2. [SECTION_CREATED] 좌석 구역 구성 완료
 * 3. [SEAT_CREATED]    개별 좌석 생성 완료
 * 4. [COMPLETED]       셋업 완료 — 예매 오픈 가능
 */
enum class TicketCreationStatus {
    EVENT_CREATED,
    SECTION_CREATED,
    SEAT_CREATED,
    COMPLETED,
}
