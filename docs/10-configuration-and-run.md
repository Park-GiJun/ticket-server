# 10. 설정 & 로컬 실행

## 모듈별 application.yml

MSA 분리로 설정도 **모듈마다** 따로 있다(`<module>/src/main/resources/application.yml`). 모든 서비스는
`eureka.client.service-url.defaultZone: http://localhost:8761/eureka/` 로 디스커버리에 등록한다.

| 모듈 | 포트 | 주요 설정 |
|------|------|-----------|
| `discovery-server` | 8761 | Eureka 서버(`register-with-eureka:false`, `fetch-registry:false`) |
| `gateway` | 8080 | 라우팅(`spring.cloud.gateway.server.webmvc.routes`) + `jwt.secret`/`jwt.issuer`(검증 전용) |
| `user-service` | 8081 | H2/JPA + Redis(6379) + Kafka(9092) + `jwt.*`(발급) |
| `ticket-event-service` | 8082 | H2/JPA (security·Redis·Kafka 없음) |

JPA 서비스 공통: Datasource = H2 인메모리(`MODE=PostgreSQL`), `ddl-auto: update`, `open-in-view: false`.
user-service 의 Kafka 는 String 직렬화 + `max.block.ms: 3000`(브로커 미가동 시 블로킹 제한).

```yaml
# user-service / gateway 공통(발급=user-service, 검증=gateway) — 값이 일치해야 한다
jwt:
  secret: ticket-server-dev-secret-please-change-in-production-0123456789abcdef
  access-token-expiration-millis: 3600000   # user-service(발급)만 사용
  issuer: ticket-server
```

> ⚠️ `jwt.secret` 은 dev 기본값. 운영에서는 **환경변수 등으로 반드시 교체**(32바이트 이상).
> gateway 와 user-service 양쪽 `application.yml` 을 **함께** 바꿔야 한다. → [07](./07-security-and-jwt.md)

## compose.yaml

`infra/compose.yaml`(저장소 루트의 `infra/`)로 로컬 인프라(PostgreSQL / Redis / Elasticsearch / Kafka)를 정의.

| 서비스 | 이미지 | 포트 |
|--------|--------|------|
| postgres | `postgres:latest` | 동적 (DB `ticket`) |
| redis | `redis:latest` | 동적 |
| elasticsearch | `elasticsearch:9.2.8` | 동적 (security off, single-node) |
| kafka | `confluentinc/cp-kafka:7.6.1` | **9092 고정** (KRaft, advertised=localhost:9092) |

> ℹ️ MSA 분리 이후 빌드에는 `spring-boot-docker-compose`(자동 컨테이너 기동) 의존성이 **없다.**
> `compose.yaml` 은 `bootRun` 이 자동으로 띄우지 않으므로 인프라는 **수동으로** 기동한다.

## 실행 가이드

### 0) (선택) 로컬 인프라 기동 — Redis/Kafka 가 필요한 경우

```powershell
docker compose -f .\infra\compose.yaml up -d   # 저장소 루트 기준
```
기본 dev 는 H2 인메모리라, **회원가입/로그인/티켓이벤트 CRUD 는 인프라 없이** 동작한다.
**비밀번호 재설정**만 Redis/Kafka 가 필요하다.

### 1) 서비스 기동 (순서 중요)

디스커버리를 먼저 띄우고, 그다음 게이트웨이·서비스를 띄운다. 각 모듈은 자기 `bootRun` 으로 실행한다.

```powershell
$env:JAVA_HOME = 'C:\Users\<user>\.jdks\temurin-25.0.2'
cd ticket-server-be

.\gradlew.bat :discovery-server:bootRun        # 8761  (먼저)
.\gradlew.bat :gateway:bootRun                 # 8080
.\gradlew.bat :user-service:bootRun            # 8081
.\gradlew.bat :ticket-event-service:bootRun    # 8082
```
> 각 `bootRun` 은 포그라운드로 점유하므로 **터미널을 나눠** 실행한다. 외부 호출은 게이트웨이(8080)로 한다.
> `gradlew` 는 빌드 루트 `ticket-server-be/` 에서 실행(또는 `-p ticket-server-be`).

## 빌드/검증 명령

| 목적 | 명령 |
|------|------|
| 컴파일 | `./gradlew compileKotlin` |
| 전체 빌드 | `./gradlew build` |
| 모듈 실행 | `./gradlew :user-service:bootRun` (루트엔 `bootRun` 없음) |
| 모듈 테스트 | `./gradlew :user-service:test` |
| 의존성 트리 | `./gradlew :user-service:dependencies --configuration runtimeClasspath` |

> 로컬 JDK: `C:\Users\<user>\.jdks\` 하위(예: `temurin-25.0.2`). `JAVA_HOME` 이 비어 있으면 지정 필요.
