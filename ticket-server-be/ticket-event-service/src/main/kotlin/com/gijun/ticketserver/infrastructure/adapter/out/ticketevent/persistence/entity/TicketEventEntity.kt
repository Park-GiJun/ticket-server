package com.gijun.ticketserver.infrastructure.adapter.out.ticketevent.persistence.entity

import com.gijun.ticketserver.domain.enums.TicketCreationStatus
import com.gijun.ticketserver.domain.enums.TicketEventCategory
import com.gijun.ticketserver.domain.enums.TicketEventStatus
import com.gijun.ticketserver.domain.model.TicketEventModel
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Entity
@Table(name = "ticket_events")
class TicketEventEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var ticketEventName: String,

    @Column(nullable = false)
    var ticketOpenAt: Instant,

    @Column(nullable = false)
    var ticketClosedAt: Instant,

    @Column(nullable = false)
    var ticketEventAt: Instant,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var ticketEventStatus: TicketEventStatus,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var ticketCreationStatus: TicketCreationStatus,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var ticketEventCategory: TicketEventCategory,

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: Instant? = null,
) {
    fun toModel(): TicketEventModel = TicketEventModel(
        id = id,
        ticketEventName = ticketEventName,
        ticketOpenAt = ticketOpenAt,
        ticketClosedAt = ticketClosedAt,
        ticketEventAt = ticketEventAt,
        ticketEventStatus = ticketEventStatus,
        ticketCreationStatus = ticketCreationStatus,
        ticketEventCategory = ticketEventCategory,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        fun fromModel(model: TicketEventModel): TicketEventEntity = TicketEventEntity(
            id = model.id,
            ticketEventName = model.ticketEventName,
            ticketOpenAt = model.ticketOpenAt,
            ticketClosedAt = model.ticketClosedAt,
            ticketEventAt = model.ticketEventAt,
            ticketEventStatus = model.ticketEventStatus,
            ticketCreationStatus = model.ticketCreationStatus,
            ticketEventCategory = model.ticketEventCategory,
        )
    }
}
