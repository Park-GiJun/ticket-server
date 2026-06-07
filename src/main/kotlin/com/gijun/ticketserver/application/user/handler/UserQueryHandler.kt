package com.gijun.ticketserver.application.user.handler

import com.gijun.ticketserver.application.user.dto.GetUserQuery
import com.gijun.ticketserver.application.user.dto.UserResult
import com.gijun.ticketserver.application.user.port.`in`.UserQueryUseCases
import com.gijun.ticketserver.application.user.port.out.UserPersistencePort
import com.gijun.ticketserver.domain.exception.UserException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserQueryHandler(
    private val userPersistencePort: UserPersistencePort,
) : UserQueryUseCases {

    override fun getById(query: GetUserQuery): UserResult {
        val user = userPersistencePort.findById(query.id)
            ?: throw UserException.UserNotFound()
        return UserResult.from(user)
    }
}
