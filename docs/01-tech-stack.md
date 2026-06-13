# 01. 기술 스택 & 버전 선정

## 핵심 버전

| 항목 | 버전 | 비고 |
|------|------|------|
| JDK | **25** | Gradle toolchain 으로 고정 |
| Kotlin | **2.3.20** | 2.3.x 최신 패치 (2026-03-16), Java 25 지원 |
| Spring Boot | **4.0.6** | 최신 안정 (2026-04-23) |
| Spring Framework | 7.0.x | Boot 4.0.6 BOM 관리 |
| Spring Security | 7.0.x | Boot 4.0.6 BOM 관리 |
| Spring Cloud | **2025.1.1** (Oakwood) | Boot 4.0 호환 릴리스 트레인. Eureka + Gateway |
| Gradle | 9.5.1 | Wrapper |

## 선정 근거

### Spring Boot 4.0.6
- 작성 시점 기준 **최신 안정 버전**.
- Java 17+ 요구, **JDK 25(최신 LTS) 권장**.
- Jackson 3(`tools.jackson`) 채택, JSpecify 기반 널 안정성 강화.
- 4.1.x 는 pre-release 라 안정 버전인 4.0.6 채택.

### Kotlin 2.3.20
- Spring Boot 4.0 의 Kotlin 베이스라인은 **2.2**(BOM 이 2.2.21 고정)이지만,
  **2.3 부터 Java 25 를 공식 지원**하므로 JDK 25 타깃에 맞춰 2.3.20 선택.
- 2.3 은 2.2 의 상위 호환이라 Boot 4.0 과 함께 사용 가능.
- ⚠️ Boot BOM 이 stdlib/reflect 를 2.2.21 로 고정하므로,
  **`kotlin.version` 프로퍼티를 2.3.20 으로 오버라이드**해 정렬함 → [02 문서](./02-build-and-gradle.md) 참고.

### JDK 25
- 최신 LTS, Spring Boot 4 권장 런타임.
- Kotlin 2.3 의 Java 25 지원과 일치.

## 주요 의존성

| 분류 | 라이브러리 |
|------|-----------|
| Web | `spring-boot-starter-web` (MVC) |
| 서비스 디스커버리 | `spring-cloud-starter-netflix-eureka-server` / `-client` |
| API 게이트웨이 | `spring-cloud-starter-gateway-server-webmvc` |
| 영속성 | `spring-boot-starter-data-jpa`, `postgresql`, `h2` |
| 보안 | `spring-boot-starter-security`, `jjwt` 0.12.6 (api/impl/jackson) |
| 캐시/메모리 | `spring-boot-starter-data-redis` |
| 메시징 | `spring-boot-starter-kafka` |
| 검색 | `spring-boot-starter-data-elasticsearch` (예정) |
| 스케줄 | `spring-boot-starter-quartz` (예정) |
| 마이그레이션 | `spring-boot-starter-flyway` (+ `flyway-database-postgresql`) |
| 문서화 | `springdoc-openapi-starter-webmvc-ui` |
| 운영 | `spring-boot-starter-actuator` |
| 직렬화 | `jackson-module-kotlin` (Jackson 3) |
| 테스트 | `spring-boot-starter-test`, `spring-kafka-test`, `kotest`, `mockk`, `testcontainers` |

> ℹ️ `spring-boot-docker-compose` 는 Version Catalog 에 정의돼 있으나 현재 어떤 모듈도
> 의존하지 않는다(MSA 분리 후 `bootRun` 자동 컨테이너 기동을 사용하지 않음). 로컬 인프라는
> `infra/compose.yaml` 로 **직접** 기동한다. → [10 문서](./10-configuration-and-run.md)
> 보안/JPA 등은 모듈별로 필요한 것만 의존한다(예: `ticket-event-service` 는 security 미포함).

> 의존성 구성은 사내 다른 프로젝트(`transfer-api`)의 백엔드 모듈 스택을 참고해 정렬했습니다.
