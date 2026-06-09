package com.gijun.ticketserver.infrastructure.adapter.out.user.persistence.adapter

import com.gijun.ticketserver.application.user.port.out.UserPersistencePort
import com.gijun.ticketserver.domain.model.UserModel
import com.gijun.ticketserver.infrastructure.adapter.out.user.persistence.entity.UserEntity
import com.gijun.ticketserver.infrastructure.adapter.out.user.persistence.repository.UserPersistenceRepository
import org.springframework.stereotype.Component

@Component
class UserPersistenceAdapter(
    private val repository: UserPersistenceRepository,
) : UserPersistencePort {

    override fun save(user: UserModel): UserModel {
        val entity = user.id
            ?.let { id -> repository.findById(id).orElse(null)?.applyFrom(user) }
            ?: UserEntity.fromModel(user)
        return repository.save(entity).toModel()
    }

    override fun findById(id: Long): UserModel? =
        repository.findById(id).orElse(null)?.toModel()

    override fun findByEmail(email: String): UserModel? =
        repository.findByEmail(email)?.toModel()

    override fun existsByEmail(email: String): Boolean =
        repository.existsByEmail(email)

    /** 기존 영속 엔티티에 도메인 변경분을 반영한다(비밀번호/상태 등 갱신용). */
    private fun UserEntity.applyFrom(model: UserModel): UserEntity = apply {
        password = model.encodedPassword
        name = model.name
        role = model.role
        status = model.status
    }
}
