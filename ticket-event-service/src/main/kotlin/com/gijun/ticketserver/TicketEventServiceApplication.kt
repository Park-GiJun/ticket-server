package com.gijun.ticketserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TicketEventServiceApplication

fun main(args: Array<String>) {
    runApplication<TicketEventServiceApplication>(*args)
}
