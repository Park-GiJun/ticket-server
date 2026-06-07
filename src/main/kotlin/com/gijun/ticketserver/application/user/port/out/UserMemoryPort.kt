package com.gijun.ticketserver.application.user.port.out

import java.time.Duration

/**
 * 휘발성 저장소(Redis) 포트. 비밀번호 재설정 토큰처럼 TTL 이 있는 데이터를 다룬다.
 */
interface UserMemoryPort {
    fun savePasswordResetToken(token: String, email: String, ttl: Duration)
    fun findEmailByPasswordResetToken(token: String): String?
    fun deletePasswordResetToken(token: String)
}
