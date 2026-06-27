package com.gijun.ticketserver.application.ticketevent.port.`in`

import com.gijun.ticketserver.application.ticketevent.dto.CreateSeatsCommand
import com.gijun.ticketserver.application.ticketevent.dto.SeatCreationResult

/**
 * 좌석(Seat) 명령 유스케이스. **1 인터페이스 = 1 함수** 규칙을 따른다.
 */

/** 좌석 생성(셋업 3단계): 이벤트를 SECTION_CREATED → SEAT_CREATED 로 진행. */
interface CreateSeatsUseCase {
    fun createSeats(command: CreateSeatsCommand): SeatCreationResult
}
