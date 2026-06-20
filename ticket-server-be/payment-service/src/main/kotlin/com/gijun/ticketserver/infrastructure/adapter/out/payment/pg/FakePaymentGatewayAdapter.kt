package com.gijun.ticketserver.infrastructure.adapter.out.payment.pg

import org.springframework.stereotype.Component

/**
 * 가짜 PG 어댑터(PaymentGatewayPort 구현체). 로컬/개발용 스텁.
 * TODO: PaymentGatewayPort 구현 (항상 승인 등). 실제 PG 연동은 별도 어댑터로 교체.
 */
@Component
class FakePaymentGatewayAdapter
