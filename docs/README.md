# ticket-server 문서 인덱스

티켓 예매 서버(`ticket-server`)의 설계·구현 문서 모음입니다.
현재까지는 **사용자(User) 도메인의 인증 기능**(회원가입 / 로그인 / 비밀번호 재설정)을
헥사고날 아키텍처 + CQRS 구조로 구현했고, **티켓 이벤트(Ticket Event) 도메인**은
이벤트·구역(Section)·좌석(Seat) 모델부터 셋업 워크플로우(생성→구역→좌석→완료)·예매 상태전이·
조회까지 REST API 한 벌을 구현한 단계입니다.

> ⚠️ 이 문서들은 **작업 기록(working notes)** 입니다. 추후 포트폴리오용으로 재구성할 예정입니다.

---

## 📚 문서 목록

> 번호는 작성 순서이자 읽기 순서. **공통 기반** 문서와 **도메인별** 문서로 나뉘며,
> 도메인이 추가될 때마다 해당 도메인의 문서가 뒤에 이어 붙는다(아래 [문서 추가 규칙](#-문서-추가-규칙) 참고).

### 공통 기반 (모든 도메인 공유)

| # | 문서 | 내용 |
|---|------|------|
| 00 | [README (이 문서)](./README.md) | 인덱스 / 전체 개요 |
| 01 | [기술 스택 & 버전 선정](./01-tech-stack.md) | JDK 25 / Kotlin 2.3.20 / Spring Boot 4.0.6 선정 근거 |
| 02 | [빌드 구성](./02-build-and-gradle.md) | Gradle Version Catalog, 플러그인, `kotlin.version` 오버라이드 |
| 03 | [아키텍처](./03-architecture.md) | 헥사고날 + CQRS, 패키지 구조, 의존성 규칙 |
| 10 | [설정 & 로컬 실행](./10-configuration-and-run.md) | `application.yml`, `compose.yaml`, 실행 가이드 |

### User 도메인

| # | 문서 | 내용 |
|---|------|------|
| 04 | [도메인 레이어](./04-domain-layer.md) | `UserModel`, `UserDomainService`, 도메인 예외 |
| 05 | [애플리케이션 레이어](./05-application-layer.md) | 포트(in/out), DTO, 유스케이스 핸들러 |
| 06 | [인프라스트럭처 레이어](./06-infrastructure-layer.md) | JPA / Redis / Kafka 어댑터 |
| 07 | [보안 & JWT](./07-security-and-jwt.md) | Spring Security 설정, JWT 발급/검증, 인증 필터 |
| 08 | [인증 플로우](./08-auth-flows.md) | 회원가입 / 로그인 / 비밀번호 재설정 시퀀스 |
| 09 | [API 레퍼런스](./09-api-reference.md) | 엔드포인트, 요청/응답, 에러 코드, Swagger |

### Ticket Event 도메인

문서가 비대해져 **레이어별 디렉토리**로 분리했다. → [ticket-event/](./ticket-event/README.md) (도메인 인덱스)

| 레이어 | 문서 | 내용 |
|--------|------|------|
| 11 | [도메인 레이어](./ticket-event/domain-layer/README.md) | `TicketEventModel`/`Section`/`Seat`, 예매·셋업·좌석 상태 전이, enum, 도메인 예외 |
| 12 | [애플리케이션 레이어](./ticket-event/application-layer/README.md) | 도메인별 유스케이스/핸들러(이벤트·구역·좌석), DTO, 영속성 포트 3종 |
| 13 | [인프라스트럭처 레이어](./ticket-event/infrastructure-layer/README.md) | 이벤트·구역·좌석 JPA 엔티티/리포지토리/어댑터, 인덱스 |
| 14 | [API 레퍼런스](./ticket-event/api-reference/README.md) | 엔드포인트(셋업 워크플로우 포함), 요청/응답, 상태 전이, 에러 코드 |

---

## 🧩 문서 추가 규칙

도메인이 추가되면 **도메인별 디렉토리**(`docs/{domain}/`)를 만들고, 그 안에 레이어별
하위 디렉토리를 둔다. 각 디렉토리의 `README.md` 가 인덱스 역할을 하며, 세부 사항은
관심사별 문서로 나눈다. 문서 하나가 비대해지지 않게 하는 것이 목적이다.

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

- **User 도메인**(04~09)은 아직 평면 파일(`NN-*.md`) 구조로 남아 있다(레거시).
- **Ticket Event 도메인**부터 위 디렉토리 구조를 적용했다 → [ticket-event/](./ticket-event/README.md).
- 도메인 표의 `#` 열은 읽기 순서를 위한 참고 번호로만 유지한다(파일명 접두사 아님).
- 공통 기반(01~03, 10)은 도메인이 늘어도 한 벌로 유지한다.

---

## 🎯 프로젝트 개요

- **목적**: 티켓 예매 서버 (현재는 사용자 인증 기반 구축 단계)
- **언어/런타임**: Kotlin 2.3.20 / JDK 25
- **프레임워크**: Spring Boot 4.0.6 (Spring Framework 7, Spring Security 7)
- **아키텍처**: 헥사고날(Ports & Adapters) + CQRS
- **영속성**: JPA(Hibernate) + PostgreSQL (로컬은 H2)
- **부가 인프라**: Redis(토큰 저장), Kafka(이벤트 발행), Elasticsearch(예정)

## ✅ 현재까지 구현된 기능

- 회원가입 (`POST /api/auth/register`)
- 로그인 → JWT 액세스 토큰 발급 (`POST /api/auth/login`)
- 비밀번호 재설정 요청/확정 (`POST /api/auth/password-reset/{request,confirm}`)
- 내 정보 조회 (`GET /api/users/me`, 인증 필요)
- JWT 기반 Stateless 인증, BCrypt 비밀번호 해싱
- 전역 예외 처리, 요청 검증(Bean Validation)
- 티켓 이벤트(공연/경기) CRUD·예매 상태전이(오픈/마감/취소)·조회 REST API (`/api/ticket-events`)
- 티켓 이벤트 셋업 워크플로우: 구역(Section) 생성 → 좌석(Seat) 자동 생성 → 완료 (4단계 생성 단계 추적)
- 좌석 상태 모델(AVAILABLE/HELD/SOLD/BLOCKED) 및 매진 판정·잔여석 집계용 영속성 쿼리
- 구역/좌석 조회 API (목록·단건·좌석 잔여 현황), 단건 조회 시 이벤트 소속 검증(404)
- 티켓 이벤트 조회 레이어 테스트(Kotest + MockK): 단위(핸들러/DTO)·웹 슬라이스(`@WebMvcTest`)·e2e(`@SpringBootTest`, H2)

## 🗺️ 앞으로 (예정)

- 티켓 예매(`reservation`) 도메인: 좌석 hold/sell/환불 흐름, 매진(SOLD_OUT) 자동 전이 연결
- 잔여석 조회용 읽기 모델(Redis/CQRS 프로젝션) 분리
- 권한 기반 인가(티켓 이벤트 생성/수정은 ADMIN 전용 등)
- Refresh Token, 로그아웃(토큰 블랙리스트)
- 권한 기반 인가(ADMIN 전용 API)
- Flyway 마이그레이션 전환, 테스트 커버리지 확대(Testcontainers 기반 PostgreSQL e2e, user-service 테스트)
