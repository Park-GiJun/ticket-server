package com.gijun.ticketserver.domain.enums

/**
 * 개별 좌석의 예매 상태.
 *
 * - [AVAILABLE] 예매 가능
 * - [HELD]      결제 진행을 위한 임시 점유(홀드)
 * - [SOLD]      판매 완료
 * - [BLOCKED]   판매 대상에서 제외(시야 제한석·관계자석 등)
 */
enum class SeatStatus {
    AVAILABLE,
    HELD,
    SOLD,
    BLOCKED,
}
