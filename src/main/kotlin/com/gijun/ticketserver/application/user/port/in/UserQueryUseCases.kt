package com.gijun.ticketserver.application.user.port.`in`

import com.gijun.ticketserver.application.user.dto.GetUserQuery
import com.gijun.ticketserver.application.user.dto.UserResult

interface UserQueryUseCases {
    fun getById(query: GetUserQuery): UserResult
}
