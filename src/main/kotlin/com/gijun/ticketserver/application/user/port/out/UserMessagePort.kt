package com.gijun.ticketserver.application.user.port.out

/**
 * 메시지 브로커(Kafka) 포트. 도메인 이벤트를 외부로 발행한다.
 */
interface UserMessagePort {
    fun sendUserRegistered(userId: Long, email: String)
    fun sendPasswordResetRequested(email: String, resetToken: String)
}
