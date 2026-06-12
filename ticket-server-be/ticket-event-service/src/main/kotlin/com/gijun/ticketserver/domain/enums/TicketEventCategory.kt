package com.gijun.ticketserver.domain.enums

/**
 * 티켓 이벤트의 분류.
 *
 * - [CONCERT] 콘서트
 * - [MUSICAL] 뮤지컬
 * - [PLAY]    연극
 * - [SPORTS]  스포츠 경기
 * - [EXHIBITION] 전시
 * - [FESTIVAL] 축제
 * - [ETC] 기타
 */
enum class TicketEventCategory {
    CONCERT, MUSICAL, PLAY, SPORTS, EXHIBITION, FESTIVAL, ETC,
}
