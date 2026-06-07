package com.gijun.ticketserver.application.user.port.out

import com.gijun.ticketserver.domain.model.UserModel

interface UserPersistencePort {
    fun save(user: UserModel): UserModel
    fun findById(id: Long): UserModel?
    fun findByEmail(email: String): UserModel?
    fun existsByEmail(email: String): Boolean
}
