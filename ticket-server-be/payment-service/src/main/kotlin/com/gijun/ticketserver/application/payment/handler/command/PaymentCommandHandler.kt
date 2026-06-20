package com.gijun.ticketserver.application.payment.handler.command

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 결제 명령 핸들러(CQRS Command). @Transactional.
 * TODO: PaymentCommandUseCases 구현 (request/cancel 등). 포트 주입 후 도메인 호출.
 */
@Service
@Transactional
class PaymentCommandHandler
