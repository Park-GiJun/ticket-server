package com.gijun.ticketserver.application.payment.handler.query

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 결제 조회 핸들러(CQRS Query). @Transactional(readOnly = true).
 * TODO: PaymentQueryUseCases 구현 (getById 등).
 */
@Service
@Transactional(readOnly = true)
class PaymentQueryHandler
