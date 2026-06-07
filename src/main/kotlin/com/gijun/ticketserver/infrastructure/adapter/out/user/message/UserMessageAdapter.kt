package com.gijun.ticketserver.infrastructure.adapter.out.user.message

import com.gijun.ticketserver.application.user.port.out.UserMessagePort
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class UserMessageAdapter(
    private val kafkaTemplate: KafkaTemplate<String, String>,
) : UserMessagePort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun sendUserRegistered(userId: Long, email: String) {
        send(TOPIC_USER_REGISTERED, userId.toString(), email)
    }

    override fun sendPasswordResetRequested(email: String, resetToken: String) {
        // 실제 환경에서는 이 이벤트를 소비하는 메일 발송 서비스가 토큰을 사용자에게 전달한다.
        send(TOPIC_PASSWORD_RESET_REQUESTED, email, resetToken)
    }

    private fun send(topic: String, key: String, payload: String) {
        // 브로커 장애가 인증 흐름을 막지 않도록 발행 실패는 로깅만 한다(best-effort).
        kafkaTemplate.send(topic, key, payload).whenComplete { _, ex ->
            if (ex != null) log.warn("Kafka 발행 실패 topic={} key={}: {}", topic, key, ex.message)
        }
    }

    companion object {
        private const val TOPIC_USER_REGISTERED = "user.registered"
        private const val TOPIC_PASSWORD_RESET_REQUESTED = "user.password-reset.requested"
    }
}
