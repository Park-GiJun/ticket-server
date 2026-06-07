package com.gijun.ticketserver.infrastructure.adapter.out.user.persistence.repository

import com.gijun.ticketserver.infrastructure.adapter.out.user.persistence.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserPersistenceRepository : JpaRepository<UserEntity, Long> {
    fun findByEmail(email: String): UserEntity?
    fun existsByEmail(email: String): Boolean
}
