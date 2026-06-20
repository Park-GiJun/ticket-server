package com.gijun.ticketserver.infrastructure.adapter.`in`.payment.web

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 결제 REST 컨트롤러(WebAdapter). 게이트웨이가 보장한 X-User-* 헤더를 신뢰한다.
 * TODO: 결제 요청/취소/조회 엔드포인트 정의. 유스케이스 주입 후 위임.
 */
@RestController
@RequestMapping("/api/payments")
class PaymentWebAdapter
