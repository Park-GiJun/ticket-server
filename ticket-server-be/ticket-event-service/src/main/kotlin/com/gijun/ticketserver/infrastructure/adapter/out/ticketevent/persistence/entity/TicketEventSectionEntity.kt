package com.gijun.ticketserver.infrastructure.adapter.out.ticketevent.persistence.entity

import com.gijun.ticketserver.domain.model.TicketEventSectionModel
import jakarta.persistence.Column
import jakarta.persistence.Entity
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
    name = "ticket_event_sections",
    indexes = [Index(name = "idx_section_event", columnList = "ticketEventId")],
)
class TicketEventSectionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var ticketEventId: Long,

    @Column(nullable = false)
    var sectionName: String,

    @Column(nullable = false, length = 30)
    var grade: String,

    @Column(nullable = false)
    var price: Long,

    @Column(nullable = false)
    var capacity: Int,

    // 좌석 배치도 행당 좌석 수. 기존 행 호환을 위해 nullable, 매핑 시 기본 20.
    @Column
    var seatsPerRow: Int? = null,

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: Instant? = null,
) {
    fun toModel(): TicketEventSectionModel = TicketEventSectionModel(
        id = id,
        ticketEventId = ticketEventId,
        sectionName = sectionName,
        grade = grade,
        price = price,
        capacity = capacity,
        seatsPerRow = seatsPerRow ?: 20,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        fun fromModel(model: TicketEventSectionModel): TicketEventSectionEntity = TicketEventSectionEntity(
            id = model.id,
            ticketEventId = model.ticketEventId,
            sectionName = model.sectionName,
            grade = model.grade,
            price = model.price,
            capacity = model.capacity,
            seatsPerRow = model.seatsPerRow,
        )
    }
}
