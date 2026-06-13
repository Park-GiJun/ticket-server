# 03. 아키텍처

## 0. 시스템 구조 (MSA)

Spring Cloud 기반 **MSA**. 외부 진입점은 게이트웨이 하나이며, 서비스 위치는 Eureka 로 찾는다.

```
                        ┌──────────────────────┐
   Client ──(8080)────> │  gateway             │  단일 진입점 + 단일 인증 지점
                        │  (JwtAuthGatewayFilter)│  JWT 검증 후 X-User-* 헤더 부착
                        └───────┬──────────────┘
                 lb://USER-SERVICE │ lb://TICKET-EVENT-SERVICE  (Eureka 로 해소)
                ┌─────────────────┴───────────────────┐
                ▼                                       ▼
        ┌───────────────┐                     ┌──────────────────────┐
        │ user-service  │ (8081)              │ ticket-event-service │ (8082)
        │ 인증 발급/회원 │                     │ 티켓 이벤트 CRUD      │
        └───────────────┘                     └──────────────────────┘
                └──────────── 등록/조회 ──> discovery-server (Eureka, 8761)
```

| 모듈 | 포트 | 역할 |
|------|------|------|
| `discovery-server` | 8761 | Eureka 서버(단일 노드, 자기 등록·조회 안 함) |
| `gateway` | 8080 | Spring Cloud Gateway(webmvc). 라우팅 + JWT 검증 + `X-User-*` 신원 전달 |
| `user-service` | 8081 | 사용자/인증 도메인. **JWT 발급** |
| `ticket-event-service` | 8082 | 티켓 이벤트 도메인 |
| `common` | — | 실행 불가 `java-library`. JWT 검증기·공통 예외 핸들러·OpenAPI 설정 공유 |

- 라우팅: `/api/auth/**`,`/api/users/**` → `USER-SERVICE`, `/api/ticket-events/**` → `TICKET-EVENT-SERVICE`.
- 인증은 게이트웨이로 일원화한다. → [07 보안 & JWT](./07-security-and-jwt.md)
- 모든 모듈은 같은 베이스 패키지 `com.gijun.ticketserver` 를 공유한다(모듈이 달라도 동일).

아래는 **각 비즈니스 서비스(`user-service`/`ticket-event-service`) 내부**의 레이어 구조다.

## 개요

**헥사고날 아키텍처(Ports & Adapters)** 와 **CQRS** 를 결합했다.
도메인을 중심에 두고, 외부 기술(웹/DB/Redis/Kafka)은 어댑터로 분리해 교체 가능하게 한다.

```
        ┌─────────────────────────── inbound ───────────────────────────┐
        │  Web (REST Controller = UserWebAdapter)                        │
        └───────────────┬───────────────────────────────────────────────┘
                        │ in-port (UseCases 인터페이스)
        ┌───────────────▼───────────────────────────────────────────────┐
        │  Application (Handler = UseCase 구현, CQRS)                     │
        │   - UserCommandHandler / UserQueryHandler                      │
        └───────────────┬───────────────────────────────────────────────┘
                        │ out-port (Persistence/Memory/Message/Token)
        ┌───────────────▼───────────────────────────────────────────────┐
        │  Domain (UserModel, UserDomainService, UserException)          │  ← 의존성 없음(순수)
        └────────────────────────────────────────────────────────────────┘
                        ▲ out-adapter 구현
        ┌───────────────┴───────────────────────────────────────────────┐
        │  Infrastructure (JPA / Redis / Kafka / JWT 어댑터)             │
        └────────────────────────────────────────────────────────────────┘
```

## 의존성 규칙

- **domain** → 아무것도 의존하지 않음 (순수 Kotlin, 프레임워크 X)
- **application** → domain 에만 의존. 외부는 **포트 인터페이스**로만 통신
- **infrastructure** → application 의 포트를 구현하고 domain 을 사용. Spring/JPA/JWT 등 기술 의존

> 핵심: 화살표는 항상 **바깥 → 안쪽**. 도메인/애플리케이션은 인프라를 모른다.

## CQRS

명령(Command)과 조회(Query)를 분리한다.

| 구분 | in-port (유스케이스) | Handler | 트랜잭션 |
|------|---------|---------|----------|
| Command | `RegisterUserUseCase` / `LoginUseCase` / `RequestPasswordResetUseCase` / `ResetPasswordUseCase` | `UserCommandHandler` | `@Transactional` |
| Query | `GetUserUseCase` | `UserQueryHandler` | `@Transactional(readOnly = true)` |

> 유스케이스는 **1 인터페이스 = 1 함수** 규칙을 따른다. 한 핸들러가 같은 그룹의 여러
> 유스케이스 인터페이스를 구현하며, 명령/조회 인터페이스는 각각
> `UserCommandUseCases.kt` / `UserQueryUseCases.kt` 파일에 모은다. → [05](./05-application-layer.md)

## 패키지 구조

서비스마다 자기 `*ServiceApplication.kt` 와 자기 도메인만 갖는다(예: `user-service`).
`ticket-event-service` 도 같은 골격(`domain`/`application.ticketevent`/`infrastructure`)을 따른다.

```
# user-service (모듈)
com.gijun.ticketserver
├─ UserServiceApplication.kt              # @SpringBootApplication, @ConfigurationPropertiesScan
│
├─ domain                                 # [순수] 비즈니스 핵심
│  ├─ enums/                              #   UserRole, UserStatus
│  ├─ model/UserModel.kt                  #   UserModel
│  ├─ service/UserDomainService.kt        #   도메인 규칙(이메일/비밀번호 정책)
│  └─ exception/UserException.kt          #   sealed 도메인 예외
│
├─ application.user                        # [유스케이스]
│  ├─ port.in/                            #   인바운드 포트(UseCase 인터페이스)
│  │   ├─ UserCommandUseCases.kt
│  │   └─ UserQueryUseCases.kt
│  ├─ port.out/                           #   아웃바운드 포트
│  │   ├─ UserPersistencePort.kt          #     영속성
│  │   ├─ UserMemoryPort.kt               #     Redis(토큰)
│  │   ├─ UserMessagePort.kt              #     Kafka(이벤트)
│  │   └─ UserTokenPort.kt                #     JWT 발급/검증
│  ├─ dto/                                #   계층 간 DTO (Command/Query/Result)
│  └─ handler/                            #   유스케이스 구현
│      ├─ UserCommandHandler.kt
│      └─ UserQueryHandler.kt
│
└─ infrastructure                          # [어댑터]
   ├─ adapter.in.user.web/                #   인바운드: REST
   │   ├─ UserWebAdapter.kt
   │   └─ dto/ (UserRequests, UserResponses)
   ├─ adapter.out.user/                   #   아웃바운드: 포트 구현체
   │   ├─ persistence/ (entity/repository/adapter)
   │   ├─ memory/UserMemoryAdapter.kt     #     Redis
   │   ├─ message/UserMessageAdapter.kt   #     Kafka
   │   └─ token/JwtTokenProvider.kt       #     JWT (UserTokenPort 구현)
   └─ config/                             #   설정
       ├─ UserExceptionHandler.kt         #     이 서비스 전용 예외 매핑
       └─ security/                       #     보안 설정 + 전역 보안 인프라
           ├─ SecurityConfig.kt           #       Spring Security 설정
           ├─ JwtAuthenticationFilter.kt
           └─ AuthenticatedUser.kt
```

> 공통 인프라는 `common` 모듈로 빠졌다: `OpenApiConfig`, `CommonExceptionHandler`,
> 그리고 보안 공유물 `config/security/{JwtProperties, JwtTokenValidator}`. JWT **검증기**(`JwtTokenValidator`)
> 는 게이트웨이와 user-service 가 함께 쓰며, **발급**은 user-service 의 `JwtTokenProvider` 뿐이다.

## 어댑터 배치 규칙

아웃바운드 포트의 구현체는 **모두** `infrastructure/adapter/out/user/{관심사}/` 아래 둔다.

| 포트 | 구현 어댑터 | 위치 |
|------|------------|------|
| `UserPersistencePort` | `UserPersistenceAdapter` | `adapter/out/user/persistence/adapter` |
| `UserMemoryPort` | `UserMemoryAdapter` | `adapter/out/user/memory` |
| `UserMessagePort` | `UserMessageAdapter` | `adapter/out/user/message` |
| `UserTokenPort` | `JwtTokenProvider` | `adapter/out/user/token` |

> `JwtTokenProvider` 는 처음 `infrastructure/security` 에 두었다가, **out 포트 구현체라는 점에서
> 다른 어댑터와 동일한 규칙(`adapter/out/user/token`)으로 이동**해 일관성을 맞췄다.
> 반면 `JwtAuthenticationFilter`/`AuthenticatedUser` 는 특정 포트의 어댑터가 아니라
> **웹 보안 전역 인프라**이다. 이들은 처음 `infrastructure/security` 에 흩어져 있었으나,
> `SecurityConfig` 와 같은 보안 관심사이므로 **`config/security/` 한 곳으로 모아** 응집도를 높였다.
> MSA 분리 후 발급/검증 양쪽이 공유하는 `JwtProperties`/`JwtTokenValidator` 는 **`common` 모듈**의
> `config/security/` 로 옮겨 게이트웨이와 user-service 가 같은 비밀키·issuer 를 쓰도록 했다.
