package com.gijun.ticketserver.domain.service

import com.gijun.ticketserver.domain.enums.TicketEventCategory
import com.gijun.ticketserver.domain.model.TicketEventModel
import java.time.Instant

/**
 * 티켓 이벤트 도메인 규칙(생성 불변식, 편집 가능 필드 정책)을 담당한다.
 * 상태가 없는 순수 클래스이므로 Spring 빈으로 등록하지 않고 직접 생성해서 사용한다.
 *
 * 상태 전이(open/close/cancel)는 [TicketEventModel] 의 행위 메서드가 담당한다.
 */
class TicketEventDomainService {

    /** 신규 티켓 이벤트 생성. 상태는 모델 기본값(SCHEDULED)으로 시작한다. */
    fun create(
        ticketEventName: String,
        ticketOpenAt: Instant,
        ticketClosedAt: Instant,
        ticketEventAt: Instant,
        ticketEventCategory: TicketEventCategory,
    ): TicketEventModel = TicketEventModel(
        ticketEventName = ticketEventName,
        ticketOpenAt = ticketOpenAt,
        ticketClosedAt = ticketClosedAt,
        ticketEventAt = ticketEventAt,
        ticketEventCategory = ticketEventCategory,
    )

    /**
     * 편집 가능한 필드만 갱신한다. 상태·생성시각은 보존하며(상태는 open/close/cancel 로만 전이),
     * 갱신 결과는 [TicketEventModel] 의 생성 불변식(시각 순서 등)으로 다시 검증된다.
     */
    fun update(
        existing: TicketEventModel,
        ticketEventName: String,
        ticketOpenAt: Instant,
        ticketClosedAt: Instant,
        ticketEventAt: Instant,
        ticketEventCategory: TicketEventCategory,
    ): TicketEventModel = existing.copy(
        ticketEventName = ticketEventName,
        ticketOpenAt = ticketOpenAt,
        ticketClosedAt = ticketClosedAt,
        ticketEventAt = ticketEventAt,
        ticketEventCategory = ticketEventCategory,
    )
}
