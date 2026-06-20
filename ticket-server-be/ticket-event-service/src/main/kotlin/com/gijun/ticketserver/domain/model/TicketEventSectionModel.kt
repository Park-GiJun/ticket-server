package com.gijun.ticketserver.domain.model

import java.time.Instant

/**
 * 티켓 이벤트의 좌석 구역(섹션) 도메인 모델. 하나의 [TicketEventModel] 에 여러 구역이 속한다.
 *
 * 구역은 등급/가격 정책의 단위다. 개별 좌석([TicketEventSeatModel])은 구역에 소속되며,
 * 좌석의 가격·등급은 소속 구역으로부터 결정된다.
 *
 * @property ticketEventId 소속 티켓 이벤트 식별자
 * @property sectionName   구역 이름 (예: "VIP석", "1층 A구역", "스탠딩")
 * @property grade         구역 등급 표기 (예: "VIP", "R", "S"). 이벤트마다 체계가 달라 자유 문자열로 둔다.
 * @property price         좌석 1매 가격(원). 0 이상.
 * @property capacity      구역 전체 좌석 수. 1 이상.
 */
data class TicketEventSectionModel(
    val id: Long? = null,
    val ticketEventId: Long,
    val sectionName: String,
    val grade: String,
    val price: Long,
    val capacity: Int,
    /** 좌석 배치도(레이아웃)의 행당 좌석 수. 좌석 생성 시 행×열 그리드로 배치된다. */
    val seatsPerRow: Int = 20,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    init {
        require(sectionName.isNotBlank()) { "sectionName must not be blank" }
        require(grade.isNotBlank()) { "grade must not be blank" }
        require(price >= 0) { "price must not be negative" }
        require(capacity >= 1) { "capacity must be at least 1" }
        require(seatsPerRow >= 1) { "seatsPerRow must be at least 1" }
    }
}
