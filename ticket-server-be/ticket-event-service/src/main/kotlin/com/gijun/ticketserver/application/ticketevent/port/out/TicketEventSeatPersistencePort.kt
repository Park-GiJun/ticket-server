package com.gijun.ticketserver.application.ticketevent.port.out

import com.gijun.ticketserver.domain.enums.SeatStatus
import com.gijun.ticketserver.domain.model.TicketEventSeatModel

interface TicketEventSeatPersistencePort {
    fun saveAll(seats: List<TicketEventSeatModel>): List<TicketEventSeatModel>
    fun findById(id: Long): TicketEventSeatModel?
    fun findByTicketEventId(ticketEventId: Long): List<TicketEventSeatModel>

    /**
     * 이벤트 내 특정 상태의 좌석이 하나라도 존재하는지. 매진(SOLD_OUT) 판정에 사용 —
     * 마지막 판매 후 `AVAILABLE` 좌석이 남았는지를 COUNT 보다 싸게 확인한다.
     */
    fun existsByTicketEventIdAndStatus(ticketEventId: Long, status: SeatStatus): Boolean

    /**
     * 이벤트 내 상태별 좌석 수 집계(잔여석/판매 수 표시용 폴백). 진실 원천은 좌석 상태이며,
     * 고빈도 조회는 추후 Redis 프로젝션/읽기 모델로 분리할 수 있다.
     */
    fun countByTicketEventIdGroupedByStatus(ticketEventId: Long): Map<SeatStatus, Long>
}
