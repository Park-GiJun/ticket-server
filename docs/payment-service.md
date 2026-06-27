# 💳 payment-service — 결제

> `payment-service` · 포트 **18084** · 상태 **⬜ 골격만**
> PG(결제 대행) 연동으로 결제 요청/취소/조회를 처리할 결제 도메인. **현재 헥사고날 패키지 골격만**
> 존재하고 비즈니스 로직은 전부 TODO 입니다.

> [!warning] 구현 현황 (코드 기준)
> 21개 소스 파일이 **헥사고날+CQRS 구조의 빈 골격**으로 존재합니다. 모든 클래스가 비어 있거나
> TODO 주석이 달린 스켈레톤입니다. 실행은 되지만(부팅 가능) 동작하는 엔드포인트는 없습니다.
> 이 문서는 **의도된 구조(blueprint)** 를 안내합니다.

---

## 1. 한눈에 보기

| 항목 | 내용 |
|------|------|
| 책임 | 결제 요청 → PG 승인 → 확정/취소, 예매(reservation)와 연계 |
| 설정 | PostgreSQL, Eureka 등록 (`application.yml`: port 18084, `ddl-auto: update`) |
| 라우트 | `/api/payments/**` → `lb://PAYMENT-SERVICE` |

---

## 2. 패키지 골격 (존재하는 것)

표준 헥사고날+CQRS 구조가 미리 잡혀 있습니다. 각 파일은 비어 있거나 TODO 스켈레톤입니다.

```
domain/
  model/PaymentModel.kt              (빈 클래스 — 필드·불변식 TODO)
  enums/PaymentStatus.kt             (값 TODO: REQUESTED/APPROVED/FAILED/CANCELLED 등)
  enums/PaymentMethod.kt             (값 TODO: CARD/BANK_TRANSFER 등)
  exception/PaymentException.kt      (sealed, 하위 예외 TODO)
  service/PaymentDomainService.kt    (빈 클래스)
application.payment/
  port.in.command / port.in.query    (빈 인터페이스 — 유스케이스 TODO)
  port.out.persistence               (PaymentPersistencePort, 빈 인터페이스)
  port.out.pg                        (PaymentGatewayPort, 빈 인터페이스)
  dto.command / dto.query / dto.result  (모두 TODO)
  handler.command / handler.query    (@Service 스켈레톤, 메서드 없음)
infrastructure/
  adapter.in.payment.web/
    PaymentWebAdapter.kt             (@RestController("/api/payments"), 엔드포인트 없음)
    dto/PaymentRequests · PaymentResponses (TODO)
  adapter.out.payment.persistence/
    PaymentEntity.kt                 (⚠️ @Entity 미선언 — 아래 주의 참고)
    PaymentPersistenceRepository.kt  (주석 처리됨, 엔티티 완성 후 활성화)
    PaymentPersistenceAdapter.kt     (@Component 스켈레톤)
  adapter.out.payment.pg/
    FakePaymentGatewayAdapter.kt     (로컬/개발용 가짜 PG 스텁)
  config/PaymentExceptionHandler.kt  (@RestControllerAdvice 스켈레톤)
```

> [!caution] PaymentEntity 는 의도적으로 @Entity 미선언
> `@Id` 없는 엔티티가 스캔되면 부팅이 깨지므로, `PaymentEntity` 를 완성할 때
> `@Entity`/`@Id`/컬럼 매핑과 `PaymentPersistenceRepository` 활성화를 **함께** 해야 합니다.

---

## 3. 설계 방향 (예정)

표준 구조([architecture.md](./architecture.md#3-서비스-내부-구조--헥사고날--cqrs))를 따라 다음을 채울 예정입니다.

- **도메인**: `PaymentModel`(금액·상태·PG 참조 + 불변식), 상태 전이(`approve`/`fail`/`cancel`),
  `PaymentStatus`/`PaymentMethod` enum, `PaymentException` 하위 예외
- **포트**:
  - `PaymentGatewayPort` — PG 승인/취소 (구현체: 우선 `FakePaymentGatewayAdapter`, 이후 실제 PG 어댑터로 교체)
  - `PaymentPersistencePort` — `payments` 테이블 영속성
- **유스케이스**: 결제 요청 · 취소 · 조회 (Command/Query 분리)
- **인프라**: `PaymentWebAdapter` 엔드포인트(`/api/payments`), `PaymentEntity`+리포지토리,
  `PaymentExceptionHandler`(sealed 예외 → HTTP 상태 `when` 매핑)
- **연계**: 예매(reservation) 확정 흐름과 맞물려 결제 성공 시 좌석 `SOLD` 확정

---

## 4. 인프라 설정

`application.yml`: 포트 18084, PostgreSQL(`DB_URL` 등 환경변수), Eureka 등록, `ddl-auto: update`,
`open-in-view: false`. 게이트웨이가 `X-User-*` 헤더로 신원을 전달합니다(자체 보안 의존성 없음).
