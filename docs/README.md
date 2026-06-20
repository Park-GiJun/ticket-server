# ticket-server 문서 인덱스

티켓 예매 서버(`ticket-server`)의 설계·구현 문서 모음입니다. **MSA 멀티모듈**(Spring Cloud:
Eureka + Gateway)이며, 각 비즈니스 서비스는 내부적으로 **헥사고날(Ports & Adapters) + CQRS** 로 구성됩니다.

문서는 **서비스(도메인)별 디렉토리**로 나누고, 각 디렉토리의 `README.md` 가 그 도메인의 인덱스를
제공합니다. 세부 사항은 레이어/관심사별 문서로 분리합니다.

> ⚠️ 이 문서들은 **작업 기록(working notes)** 입니다. 빌드/실행·아키텍처 등 시스템 전반 가이드는
> 저장소 루트 [CLAUDE.md](../CLAUDE.md) 가 단일 기준입니다(이전의 평면 `01~10` 문서는 폐기됨).

---

## 📚 서비스(도메인)별 문서

| 서비스 / 도메인 | 모듈 | 인덱스 | 상태 |
|------|------|--------|------|
| 사용자 / 인증 | `user-service` | [user/](./user/README.md) | 구현됨 |
| 티켓 이벤트 | `ticket-event-service` | [ticket-event/](./ticket-event/README.md) | 구현됨 |
| 예매 | `reservation-service` | [reservation/](./reservation/README.md) | 설계 중 |

> 게이트웨이/디스커버리/공통 모듈(`gateway`·`discovery-server`·`common`)과 인증 아키텍처,
> 빌드·실행, 기술 스택은 [CLAUDE.md](../CLAUDE.md) 를 참고한다.

## 🧩 문서 추가 규칙

서비스(도메인)가 추가되면 **도메인별 디렉토리**(`docs/{domain}/`)를 만들고, 그 안에 레이어별
하위 디렉토리(또는 레이어별 단일 파일)를 둔다. 각 디렉토리의 `README.md` 가 인덱스 역할을 하며,
세부 사항은 관심사별 문서로 나눈다. 문서 하나가 비대해지지 않게 하는 것이 목적이다.

```
docs/{domain}/
  README.md                 # 도메인 인덱스 (레이어 링크)
  domain-layer/
    README.md               # 레이어 인덱스
    <모델/enum/예외 별 세부>.md
  application-layer/
    README.md
    use-cases.md · dto.md · handlers.md · ports.md
  infrastructure-layer/
    README.md
    entities.md · repositories.md · adapters.md
  api-reference/
    README.md
    <엔드포인트 묶음 별 세부>.md · errors.md
```

- **User 도메인**은 레이어당 단일 파일(평면) 구조로 두고, 비대해지면 디렉토리로 분리한다.
- **Ticket Event 도메인**부터 위 레이어별 디렉토리 구조를 적용했다.
- **Reservation 도메인**은 설계 단계로, 도메인 레이어부터 채워 나간다.

---

## 🎯 프로젝트 개요

- **목적**: 티켓 예매 서버 (MSA: Eureka + Gateway, 서비스 내부는 헥사고날 + CQRS)
- **언어/런타임**: Kotlin 2.3.20 / JDK 25
- **프레임워크**: Spring Boot 4.0.6 (Spring Framework 7, Spring Security 7) / Spring Cloud 2025.1.x
- **영속성**: JPA(Hibernate) + PostgreSQL (로컬 dev 는 H2 PostgreSQL 호환 모드)
- **부가 인프라**: Redis(토큰 저장), Kafka(이벤트 발행)

자세한 모듈 구조·인증 아키텍처·빌드 명령은 [CLAUDE.md](../CLAUDE.md) 참고.

## ✅ 현재까지 구현된 기능

- **User**: 회원가입 / 로그인(JWT 발급) / 비밀번호 재설정 / 내 정보 조회, BCrypt 해싱, Stateless 인증
- **Gateway**: JWT 검증 일원화, `X-User-*` 신원 헤더 주입(클라이언트 헤더 차단)
- **Ticket Event**: 이벤트 CRUD·예매 상태전이(오픈/마감/취소)·조회, 셋업 워크플로우(구역→좌석→완료),
  좌석 상태 모델(AVAILABLE/HELD/SOLD/BLOCKED) 및 잔여석/매진 집계 쿼리, 구역/좌석 조회 API

## 🗺️ 앞으로 (예정)

- **Reservation**: 좌석 hold/sell/환불 흐름, 매진(SOLD_OUT) 자동 전이 연결 (→ [reservation/](./reservation/README.md))
- 잔여석 조회용 읽기 모델(Redis/CQRS 프로젝션) 분리
- 권한 기반 인가(티켓 이벤트 생성/수정은 ADMIN 전용 등)
- Refresh Token, 로그아웃(토큰 블랙리스트)
- Flyway 마이그레이션 전환, Testcontainers 기반 PostgreSQL e2e
