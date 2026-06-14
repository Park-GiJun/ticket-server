package com.gijun.ticketserver.e2e

import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Section/Seat 조회 레이어 end-to-end 시나리오.
 *
 * 전체 컨텍스트(웹+JPA)를 RANDOM_PORT 로 기동하고 실제 HTTP 로 셋업(이벤트→구역→좌석 생성) 후
 * 조회 엔드포인트와 소속 검증(404)을 검증한다. CLAUDE.md 방침에 따라 인프라는 H2(인메모리)를 쓰며,
 * Eureka 등록은 비활성화한다.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["eureka.client.enabled=false"],
)
@AutoConfigureTestRestTemplate
class TicketEventQueryE2ETest(
    private val restTemplate: TestRestTemplate,
) : FunSpec({

    extension(SpringExtension)

    @Suppress("UNCHECKED_CAST")
    fun idOf(body: Any?): Long = ((body as Map<String, Any?>)["id"] as Number).toLong()

    test("이벤트 셋업 후 구역/좌석을 조회하고 소속 검증이 동작한다") {
        val now = Instant.now()

        // 1) 이벤트 생성
        val createEvent = mapOf(
            "ticketEventName" to "E2E 콘서트",
            "ticketOpenAt" to now.plus(1, ChronoUnit.DAYS),
            "ticketClosedAt" to now.plus(2, ChronoUnit.DAYS),
            "ticketEventAt" to now.plus(3, ChronoUnit.DAYS),
            "ticketEventCategory" to "CONCERT",
        )
        val eventResp = restTemplate.postForEntity("/api/ticket-events", createEvent, Map::class.java)
        eventResp.statusCode shouldBe HttpStatus.CREATED
        val eventId = idOf(eventResp.body)

        // 2) 구역 생성 (capacity 3 + 2 = 좌석 5)
        val createSections = mapOf(
            "sections" to listOf(
                mapOf("sectionName" to "A구역", "grade" to "R", "price" to 100_000, "capacity" to 3),
                mapOf("sectionName" to "B구역", "grade" to "S", "price" to 80_000, "capacity" to 2),
            ),
        )
        restTemplate.postForEntity(
            "/api/ticket-events/$eventId/sections", createSections, Map::class.java,
        ).statusCode shouldBe HttpStatus.CREATED

        // 3) 좌석 생성
        val seatCreateResp = restTemplate.postForEntity(
            "/api/ticket-events/$eventId/seats", null, Map::class.java,
        )
        seatCreateResp.statusCode shouldBe HttpStatus.CREATED
        (seatCreateResp.body!!["createdSeatCount"] as Number).toInt() shouldBe 5

        // 4) 구역 목록 조회
        val sectionsResp = restTemplate.getForEntity(
            "/api/ticket-events/$eventId/sections", List::class.java,
        )
        sectionsResp.statusCode shouldBe HttpStatus.OK
        val sections = sectionsResp.body!!
        sections shouldHaveSize 2
        val sectionId = idOf(sections[0])

        // 5) 구역 단건 조회
        restTemplate.getForEntity(
            "/api/ticket-events/$eventId/sections/$sectionId", Map::class.java,
        ).statusCode shouldBe HttpStatus.OK

        // 6) 다른 이벤트 id 로 구역 단건 조회 → 소속 검증으로 404
        restTemplate.getForEntity(
            "/api/ticket-events/9999/sections/$sectionId", Map::class.java,
        ).statusCode shouldBe HttpStatus.NOT_FOUND

        // 7) 좌석 목록 조회 (총 5)
        val seatsResp = restTemplate.getForEntity(
            "/api/ticket-events/$eventId/seats", List::class.java,
        )
        seatsResp.statusCode shouldBe HttpStatus.OK
        val seats = seatsResp.body!!
        seats shouldHaveSize 5
        val seatId = idOf(seats[0])

        // 8) 잔여 현황 — 전부 AVAILABLE
        val availResp = restTemplate.getForEntity(
            "/api/ticket-events/$eventId/seats/availability", Map::class.java,
        )
        availResp.statusCode shouldBe HttpStatus.OK
        (availResp.body!!["total"] as Number).toLong() shouldBe 5L
        (availResp.body!!["available"] as Number).toLong() shouldBe 5L

        // 9) 좌석 단건 조회 + 잘못된 이벤트 id → 404
        restTemplate.getForEntity(
            "/api/ticket-events/$eventId/seats/$seatId", Map::class.java,
        ).statusCode shouldBe HttpStatus.OK
        restTemplate.getForEntity(
            "/api/ticket-events/9999/seats/$seatId", Map::class.java,
        ).statusCode shouldBe HttpStatus.NOT_FOUND
    }
})
