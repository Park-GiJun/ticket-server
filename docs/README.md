# ticket-server 문서 인덱스

티켓 예매 서버(`ticket-server`)의 설계·구현 문서 모음입니다.
현재까지는 **사용자(User) 도메인의 인증 기능**(회원가입 / 로그인 / 비밀번호 재설정)을
헥사고날 아키텍처 + CQRS 구조로 구현한 단계입니다.

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

---

## 🧩 문서 추가 규칙

도메인이 추가되면(예: `ticket`, `booking`) **다음 번호부터 이어서** 문서를 추가한다.
도메인별로 아래 묶음을 기본 템플릿으로 삼는다(필요한 것만 취사선택).

```
NN-{domain}-domain-layer.md          # 도메인 모델/서비스/예외
NN-{domain}-application-layer.md     # 포트, DTO, 유스케이스 핸들러
NN-{domain}-infrastructure-layer.md # JPA/Redis/Kafka 등 어댑터
NN-{domain}-api-reference.md         # 엔드포인트/요청·응답/에러
NN-{domain}-flows.md                 # (선택) 주요 시퀀스
```

예) 다음 도메인이 `ticket` 이면 `11-ticket-domain-layer.md` … 식으로 11번부터 추가하고,
이 인덱스의 "### Ticket 도메인" 섹션 표에 행을 더한다.
공통 기반(01~03, 10)은 도메인이 늘어도 한 벌로 유지한다.

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

## 🗺️ 앞으로 (예정)

- 티켓/공연/예매 도메인
- Refresh Token, 로그아웃(토큰 블랙리스트)
- 권한 기반 인가(ADMIN 전용 API)
- Flyway 마이그레이션 전환, 테스트 코드(Kotest + Testcontainers)
