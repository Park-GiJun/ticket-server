package com.gijun.ticketserver.domain.model

import com.gijun.ticketserver.domain.enums.ReservationStatus
import java.time.Instant

data class ReservationModel(
    val id: Long? = null,
    val reservationCode: String,
    val transactionId: String,
    val userId: Long,
    val ticketEventId: Long,
    val seatId: Long,
    val status: ReservationStatus = ReservationStatus.PENDING,
    val price: Long,
    val holdExpiresAt: Instant,
    val paymentId: String? = null,
    val confirmedAt: Instant? = null,
    val failureReason: String? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    init {
        require(reservationCode.isNotBlank()) { "reservationCode must not be blank" }
        require(transactionId.isNotBlank()) { "transactionId must not be blank" }
        require(price >= 0) { "price must not be negative" }
    }

    fun isPending(): Boolean = status == ReservationStatus.PENDING
    fun isHeld(): Boolean = status == ReservationStatus.HELD
    fun isExpired(at: Instant): Boolean = isHeld() && !at.isBefore(holdExpiresAt)
}