package com.gijun.ticketserver.application.port.`in`

interface CreateReservationCommandUseCases {
    fun createReservation(command : Unit) : List<Unit>
}