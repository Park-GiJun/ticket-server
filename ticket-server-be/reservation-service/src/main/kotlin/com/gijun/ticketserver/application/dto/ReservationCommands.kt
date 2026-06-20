package com.gijun.ticketserver.application.dto

data class CreateReservationCommand(
    val userId: String,
    val ticketEventId: Long,
    val seatId: String,
    val price: Long
)