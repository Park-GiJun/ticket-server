# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

티켓 예매 서버. **MSA 멀티모듈**(Spring Cloud: Eureka + Gateway)이며, 각 비즈니스 서비스는
내부적으로 **헥사고날 아키텍처(Ports & Adapters) + CQRS** 로 구성된다.

- Kotlin 2.3.20 / JDK 25 (Gradle toolchain) / Spring Boot 4.0.6 / Spring Cloud 2025.1.x
- 영속성: JPA(Hibernate) + PostgreSQL (로컬 dev 는 H2 PostgreSQL 호환 모드)
- 부가 인프라: Redis(토큰 저장), Kafka(이벤트 발행)

> `docs/`(저장소 루트)에 **서비스(도메인)별 설계·구현 문서**가 있다(`docs/user/`·`docs/ticket-event/`·
> `docs/reservation/`, 인덱스는 `docs/README.md`). 작업 기록이라 코드와 어긋날 수 있다 — **코드가 정답이다.**

## 모듈 구조

빌드 루트는 **`ticket-server-be/`** (여기에 `gradlew`, `settings.gradle.kts` 가 있다). 저장소 루트가 아니다.

| 모듈 | 포트 | 역할 |
|------|------|------|
| `discovery-server` | 18761 | Eureka 서버(단일 노드, 자기 등록 안 함) |
| `gateway` | 18080 | Spring Cloud Gateway. **유일한 외부 진입점 + 단일 인증 지점** |
| `user-service` | 18081 | 사용자/인증 도메인 (회원가입·로그인·비밀번호 재설정·내 정보) |
| `ticket-event-service` | 18082 | 티켓 이벤트(공연/경기) CRUD·상태전이·조회 |
| `reservation-service` | 18083 | 예매 도메인 (진행 중) |
| `payment-service` | 18084 | 결제 도메인 (진행 중) |
| `common` | — | 실행 불가 `java-library`. JWT 검증기·공통 예외 핸들러·OpenApi 설정 공유 |

모든 모듈은 패키지 루트 `com.gijun.ticketserver` 를 공유한다(모듈이 달라도 같은 베이스 패키지).

## 인증 아키텍처 (핵심)

JWT **발급은 user-service**, **검증은 gateway** 가 담당하며, 양쪽은 `common` 의 `JwtTokenValidator`
와 동일한 `jwt.secret`/`jwt.issuer` 를 공유한다.

1. `gateway` 의 `JwtAuthGatewayFilter` 가 모든 요청의 `Authorization: Bearer` 를 검증한다.
2. 검증 성공 시 신원을 **`X-User-Id` / `X-User-Email` / `X-User-Role` 헤더로 백엔드에 전달**한다.
   클라이언트가 보낸 `X-User-*` 헤더는 신뢰하지 않고 게이트웨이가 덮어쓰거나 제거한다(`IdentityHeaderRequest`).
3. 공개 경로(`/api/auth/**`, `/actuator/**`)는 인증 없이 통과한다.
4. `ticket-event-service` 는 보안 의존성이 없다 — 게이트웨이가 보장한 `X-User-*` 헤더를 신뢰한다.
5. `user-service` 는 자체 `SecurityConfig` + `JwtAuthenticationFilter` 도 가진다(현재 `Authorization`
   토큰을 직접 검증). 게이트웨이를 우회한 직접 호출 시에도 인증이 걸린다.

JWT secret/issuer 를 바꿀 때는 **gateway 와 user-service 의 `application.yml` 을 함께** 수정해야 한다.

## 서비스 내부 구조 (헥사고날 + CQRS)

각 비즈니스 서비스(`user-service`, `ticket-event-service`)는 동일한 레이어 규칙을 따른다.
의존성 화살표는 항상 **바깥 → 안쪽**. domain/application 은 infrastructure 를 모른다.

```
domain/            순수 Kotlin (프레임워크 의존 X). model / service / enums / exception(sealed)
application.<도메인>/
  port.in/         유스케이스 인터페이스 (Command / Query 파일 분리)
  port.out/        영속성/Memory(Redis)/Message(Kafka)/Token 포트
  dto/             계층 간 DTO (Commands / Queries / Results)
  handler/         유스케이스 구현 = CommandHandler / QueryHandler
infrastructure/
  adapter.in.<도메인>.web/    REST 컨트롤러(=WebAdapter) + 요청/응답 DTO
  adapter.out.<도메인>/        포트 구현체 (persistence / memory / message / token)
  config/                      예외 핸들러, security 등
```

- **CQRS**: 명령은 `@Transactional`, 조회는 `@Transactional(readOnly = true)`. 핸들러도 Command/Query 로 분리.
- **유스케이스는 1 인터페이스 = 1 함수** 규칙. 한 핸들러가 같은 그룹의 여러 단일함수 인터페이스를 구현하고,
  명령/조회 인터페이스는 각각 `<도메인>CommandUseCases.kt` / `<도메인>QueryUseCases.kt` 에 모은다.
- 아웃바운드 포트 구현체는 **모두** `infrastructure/adapter/out/<도메인>/<관심사>/` 아래 둔다.

## 일정관리 파이프라인 (Jira · Notion · Slack · GitHub)

커밋/푸시는 **네 서비스를 한 번에 동기화**한다. 진입점은 Claude Code 스킬 **`/ship`**
(`.claude/commands/ship.md`). 직접 커밋만 할 때도 아래 규칙을 따른다.

- **Jira `KAN`("내 칸반 스페이스", gijun.atlassian.net) 이 일정의 정본.** 에픽=도메인 마일스톤
  (인프라=`KAN-1`, 인증=`KAN-2`, 티켓이벤트=`KAN-3`, 예매=`KAN-4`, 결제=`KAN-5`), 작업=커밋 단위 이슈, 라벨=`<모듈>`+`<커밋타입>`.
- **커밋 메시지 본문에 `Refs: KAN-xx`** 를 넣어 GitHub↔Jira 를 연결한다.
- **Notion** `커밋 로그` DB(허브: ticket-server 일정관리 허브)에 커밋당 1행 기록.
- **Slack `새-채널`** 로 변경 알림 전송(봇 미초대 시 `not_in_channel` → 초대 필요).
- 흐름: **Jira 이슈 확보 → 커밋(Refs 포함) → GitHub 푸시 → Notion 기록 → Jira 전이 + Slack 알림.**
  커밋/푸시 후의 동기화는 실패해도 롤백하지 않고 부분 성공으로 처리한다.
- 리소스 ID(cloudId·data_source_id·channel_id·에픽 키)는 모두 `ship.md` 상단 표에 고정돼 있다.

## 빌드 & 실행 명령

**반드시 빌드 루트 `ticket-server-be/` 에서 실행**(또는 `-p ticket-server-be`).
`JAVA_HOME` 이 비어 있으면 `~/.jdks` 하위 JDK 25 를 지정해야 한다(PowerShell).

```powershell
# (필요 시) JDK 지정
$env:JAVA_HOME = 'C:\Users\<user>\.jdks\temurin-25.0.2'

# 전체 빌드 / 컴파일
.\gradlew.bat build
.\gradlew.bat compileKotlin

# 특정 모듈만 실행 — 루트에는 bootRun 이 없으므로 모듈을 지정한다
.\gradlew.bat :discovery-server:bootRun
.\gradlew.bat :gateway:bootRun
.\gradlew.bat :user-service:bootRun
.\gradlew.bat :ticket-event-service:bootRun

# 테스트
.\gradlew.bat test                         # 전체
.\gradlew.bat :user-service:test           # 특정 모듈
.\gradlew.bat :user-service:test --tests "com.gijun.ticketserver.*SomeTest"   # 단일 테스트
```

전체 스택 기동 순서: **discovery-server → gateway → user-service / ticket-event-service**.

### 로컬 인프라 (Redis / Kafka / PostgreSQL)

`infra/compose.yaml`(저장소 루트의 `infra/`)에 PostgreSQL/Redis/Elasticsearch/Kafka 가 정의돼 있다.
현재 빌드에는 `spring-boot-docker-compose` 자동 기동 의존성이 **없으므로** Redis/Kafka 가 필요한
기능(비밀번호 재설정)을 쓰려면 컨테이너를 직접 띄운다:

```powershell
docker compose -f ..\infra\compose.yaml up -d   # ticket-server-be 기준 상위의 infra/
```

**개발도 별도 dev 인프라 없이 공용 홈서버 인프라를 직접 쓴다**(개인 프로젝트 정책): 각 서비스
`application.yml` 의 datasource/redis/kafka 가 `210.121.177.150` 의 `infra-postgres`(DB `ticketserver`)/
`infra-redis`(6380)/`infra-kafka`(9094) 를 가리킨다. 따라서 로컬에서 인프라 컨테이너를 띄울 필요가 없다
(Eureka 만 로컬 `discovery-server` 18761 로 구동). 배포·서버 토폴로지는 `ticket-server-be/deploy/README.md` 참고.

## 기술 스택 세부

- **Version Catalog**: 모든 의존성/플러그인 버전은 `ticket-server-be/gradle/libs.versions.toml` 에서 관리.
- 루트 `build.gradle.kts` 의 `subprojects {}` 가 공통 설정(Kotlin JVM, JDK 25 toolchain,
  Spring Boot/Cloud BOM, JSR305 strict)을 모든 모듈에 적용한다.
- Spring Boot 4.0 BOM 이 Kotlin 을 2.2.x 로 고정하므로 `extra["kotlin.version"]` 로 2.3.x 를 강제한다.
- JPA 엔티티가 있는 모듈은 `kotlin-jpa` 플러그인 + `allOpen`(@Entity/@MappedSuperclass/@Embeddable) 설정.
- 테스트 스택: JUnit5 플랫폼 + Kotest + MockK + Testcontainers. (테스트는 docker-compose 미동작 → H2 사용)
