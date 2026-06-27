package com.gijun.ticketserver.application.ticketevent.dto

import com.gijun.ticketserver.domain.enums.SeatStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SeatAvailabilityResultTest : FunSpec({

    test("집계에 없는 상태는 0으로 채우고 항상 모든 상태를 포함한다") {
        val result = SeatAvailabilityResult.from(
            ticketEventId = 7L,
            counts = mapOf(SeatStatus.AVAILABLE to 3L),
        )

        result.counts.keys shouldBe SeatStatus.entries.toSet()
        result.counts[SeatStatus.HELD] shouldBe 0L
        result.counts[SeatStatus.SOLD] shouldBe 0L
        result.counts[SeatStatus.BLOCKED] shouldBe 0L
    }

    test("total 은 상태별 좌석 수의 합이다") {
        val result = SeatAvailabilityResult.from(
            ticketEventId = 7L,
            counts = mapOf(
                SeatStatus.AVAILABLE to 10L,
                SeatStatus.HELD to 2L,
                SeatStatus.SOLD to 8L,
                SeatStatus.BLOCKED to 5L,
            ),
        )

        result.total shouldBe 25L
        result.available shouldBe 10L
    }

    test("빈 집계면 total 과 available 모두 0이다") {
        val result = SeatAvailabilityResult.from(ticketEventId = 7L, counts = emptyMap())

        result.total shouldBe 0L
        result.available shouldBe 0L
        result.counts.values.toSet() shouldBe setOf(0L)
    }
})
