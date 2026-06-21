# 🎫 ticket-server

티켓 예매 서버. **MSA 멀티모듈**(Spring Cloud: Eureka + Gateway)로 구성되며, 각 비즈니스 서비스는
내부적으로 **헥사고날 아키텍처(Ports & Adapters) + CQRS** 패턴을 따릅니다. 프론트엔드는 React SPA입니다.

> 개인 학습/포트폴리오 프로젝트입니다. 이슈/일정의 정본은 Jira(KAN) 보드이며, 설계·구현 메모는 [`docs/`](./docs/README.md)에 도메인별로 정리되어 있습니다. **코드가 항상 정답입니다.**

---

## 기술 스택

**백엔드** (`ticket-server-be/`)
- Kotlin 2.3.20 / JDK 25 (Gradle toolchain)
- Spring Boot 4.0.6 (Spring Framework 7, Spring Security 7) / Spring Cloud 2025.1.x
- 영속성: JPA(Hibernate) + PostgreSQL (로컬 dev는 H2 PostgreSQL 호환 모드)
- 부가 인프라: Redis(토큰 저장), Kafka(이벤트 발행)
- 테스트: JUnit5 + Kotest + MockK + Testcontainers

**프론트엔드** (`ticket-server-fe/`)
- React 19 / TypeScript 5.7 / Vite 6
- 라우팅: react-router-dom v7 (`React.lazy` + `Suspense` 코드 스플리팅)
- 서버 상태: @tanstack/react-query v5 · 클라이언트 상태: zustand v5(persist)
- HTTP: axios (`/api` baseURL, Bearer 토큰 자동 부착, 401 시 자동 로그아웃)
- 스타일: CSS Modules + 전역 디자인 토큰

---

## 모듈 구조

빌드 루트는 **`ticket-server-be/`** 입니다(`gradlew`, `settings.gradle.kts` 위치). 저장소 루트가 아닙니다.

| 모듈 | 포트 | 역할 |
|------|------|------|
| `discovery-server` | 18761 | Eureka 서버(단일 노드) |
| `gateway` | 18080 | Spring Cloud Gateway — **유일한 외부 진입점 + 단일 인증 지점** |
| `user-service` | 18081 | 사용자/인증 (회원가입·로그인·비밀번호 재설정·내 정보) |
| `ticket-event-service` | 18082 | 티켓 이벤트(공연/경기) CRUD·상태전이·조회 |
| `reservation-service` | 18083 | 예매 도메인 (진행 중) |
| `payment-service` | 18084 | 결제 도메인 (예정) |
| `common` | — | 실행 불가 `java-library`. JWT 검증기·공통 예외 핸들러·OpenApi 설정 공유 |

모든 모듈은 베이스 패키지 `com.gijun.ticketserver` 를 공유합니다.

### 서비스 내부 레이어 (헥사고날 + CQRS)

의존성 화살표는 항상 **바깥 → 안쪽**. domain/application 은 infrastructure 를 모릅니다.

```
domain/            순수 Kotlin. model / service / enums / exception(sealed)
application.<도메인>/
  port.in/         유스케이스 인터페이스 (Command / Query 분리, 1 인터페이스 = 1 함수)
  port.out/        영속성 / Memory(Redis) / Message(Kafka) / Token 포트
  dto/             계층 간 DTO (Commands / Queries / Results)
  handler/         유스케이스 구현 = CommandHandler / QueryHandler
infrastructure/
  adapter.in.<도메인>.web/    REST 컨트롤러 + 요청/응답 DTO
  adapter.out.<도메인>/        포트 구현체 (persistence / memory / message / token)
  config/                      예외 핸들러, security 등
```

- **CQRS**: 명령은 `@Transactional`, 조회는 `@Transactional(readOnly = true)`. 핸들러도 Command/Query로 분리.

---

## 인증 아키텍처

JWT **발급은 user-service**, **검증은 gateway** 가 담당하며, 양쪽은 `common` 의 `JwtTokenValidator` 와
동일한 secret/issuer 를 공유합니다.

1. `gateway` 의 `JwtAuthGatewayFilter` 가 모든 요청의 `Authorization: Bearer` 를 검증합니다.
2. 검증 성공 시 신원을 **`X-User-Id` / `X-User-Email` / `X-User-Role` 헤더로 백엔드에 전달**합니다.
   클라이언트가 보낸 `X-User-*` 헤더는 신뢰하지 않고 게이트웨이가 덮어쓰거나 제거합니다.
3. 공개 경로(`/api/auth/**`, `/actuator/**`)는 인증 없이 통과합니다.
4. 백엔드 서비스는 게이트웨이가 보장한 `X-User-*` 헤더를 신뢰합니다.

---

## 빌드 & 실행

### 백엔드

> `JAVA_HOME` 이 비어 있으면 JDK 25 를 지정해야 합니다.

```powershell
# (필요 시) JDK 지정
$env:JAVA_HOME = 'C:\Users\<user>\.jdks\temurin-25.0.2'

cd ticket-server-be

# 전체 빌드 / 컴파일
.\gradlew.bat build
.\gradlew.bat compileKotlin

# 모듈별 실행 (루트에 bootRun 없음 → 모듈 지정)
.\gradlew.bat :discovery-server:bootRun
.\gradlew.bat :gateway:bootRun
.\gradlew.bat :user-service:bootRun
.\gradlew.bat :ticket-event-service:bootRun

# 테스트
.\gradlew.bat test                         # 전체
.\gradlew.bat :user-service:test           # 특정 모듈
```

**전체 스택 기동 순서**: discovery-server → gateway → user-service / ticket-event-service

로컬 인프라(Redis/Kafka/PostgreSQL)는 [`infra/compose.yaml`](./infra/compose.yaml) 으로 띄울 수 있습니다.

```powershell
docker compose -f infra/compose.yaml up -d
```

### 프론트엔드

```bash
cd ticket-server-fe
npm install
npm run dev        # http://localhost:5173 (개발 서버)
npm run build      # tsc --noEmit 후 vite build -> dist/
npm run lint
```

개발 시 Vite dev proxy 가 `/api/**` 요청을 게이트웨이(`http://localhost:18080`)로 전달하므로,
프론트는 상대경로 `/api/...` 로만 호출합니다(별도 환경변수 불필요). 자세한 내용은
[`ticket-server-fe/README.md`](./ticket-server-fe/README.md) 참고.

---

## 진행 현황

| 도메인 | 모듈 | 상태 |
|--------|------|------|
| 🔐 사용자 / 인증 | `user-service` | ✅ 구현됨 |
| 🎫 티켓 이벤트 | `ticket-event-service` | ✅ 구현됨 |
| 🖥️ 프론트엔드 | `ticket-server-fe` | 🟡 진행 중 |
| 🪑 예매 | `reservation-service` | 🟡 진행 중 |
| 💳 결제 | `payment-service` | ⬜ 예정 |

**구현된 기능**
- **User**: 회원가입 / 로그인(JWT 발급) / 비밀번호 재설정 / 내 정보 조회, BCrypt 해싱, Stateless 인증
- **Gateway**: JWT 검증 일원화, `X-User-*` 신원 헤더 주입(클라이언트 헤더 차단)
- **Ticket Event**: 이벤트 CRUD·예매 상태전이(오픈/마감/취소)·조회, 셋업 워크플로우(구역→좌석→완료),
  좌석 상태 모델(AVAILABLE/HELD/SOLD/BLOCKED)·잔여석/매진 집계 쿼리, 좌석 배치도(seatsPerRow) 기반 그리드 좌석 생성
- **Frontend**: 홈/이벤트 목록·상세, 좌석 선택 예매 → 결제 플로우, 인증(로그인/회원가입), 마이페이지, 공통 에러 처리

**로드맵** (예매 → 결제 → 프론트엔드 → 배포/CI·CD → 관측성 → QA 자동화 → 하드닝)
- **예매**: 좌석 hold/sell/환불 흐름, 동시성 제어, TTL 만료, 매진(SOLD_OUT) 자동 전이, Kafka 이벤트
- **결제**: 결제수단·PG 포트, SAGA·보상 트랜잭션, 웹훅, 멱등성, 취소/환불
- **공통**: 권한 기반 인가(ADMIN 전용), Refresh Token·로그아웃, Flyway 마이그레이션, Testcontainers 기반 e2e

---

## 문서

- [`docs/`](./docs/README.md) — 서비스(도메인)별 설계·구현 문서
  - [user/](./docs/user/README.md) · [ticket-event/](./docs/ticket-event/README.md) · [reservation/](./docs/reservation/README.md)
- [`CLAUDE.md`](./CLAUDE.md) — 아키텍처·빌드·일정관리 파이프라인 등 시스템 전반 가이드(단일 기준)
</content>
</invoke>
