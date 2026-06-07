package com.gijun.ticketserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class TicketServerApplication

fun main(args: Array<String>) {
    runApplication<TicketServerApplication>(*args)
}
