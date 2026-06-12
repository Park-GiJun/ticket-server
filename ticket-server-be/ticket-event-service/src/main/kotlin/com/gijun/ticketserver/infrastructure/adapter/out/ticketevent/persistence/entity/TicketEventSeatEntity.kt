package com.gijun.ticketserver.infrastructure.adapter.out.ticketevent.persistence.entity

import com.gijun.ticketserver.domain.enums.SeatStatus
import com.gijun.ticketserver.domain.model.TicketEventSeatModel
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Entity
@Table(
    name = "ticket_event_seats",
    indexes = [
        // 이벤트 단위 핫쿼리(잔여석 집계·매진 판정)를 단일 인덱스로 처리하기 위한 복합 인덱스.
        Index(name = "idx_seat_event_status", columnList = "ticketEventId, status"),
        Index(name = "idx_seat_section", columnList = "sectionId"),
    ],
)
class TicketEventSeatEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var sectionId: Long,

    // section 으로부터 도달 가능하지만 이벤트 단위 조회/파티셔닝을 위해 비정규화한 값. 생성 후 불변.
    @Column(nullable = false)
    var ticketEventId: Long,

    @Column(nullable = false, length = 10)
    var rowLabel: String,

    @Column(nullable = false)
    var seatNumber: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: SeatStatus,

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: Instant? = null,
) {
    fun toModel(): TicketEventSeatModel = TicketEventSeatModel(
        id = id,
        sectionId = sectionId,
        ticketEventId = ticketEventId,
        rowLabel = rowLabel,
        seatNumber = seatNumber,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        fun fromModel(model: TicketEventSeatModel): TicketEventSeatEntity = TicketEventSeatEntity(
            id = model.id,
            sectionId = model.sectionId,
            ticketEventId = model.ticketEventId,
            rowLabel = model.rowLabel,
            seatNumber = model.seatNumber,
            status = model.status,
        )
    }
}
