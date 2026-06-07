# 10. 설정 & 로컬 실행

## application.yml

`src/main/resources/application.yml`

| 영역 | 설정 | 비고 |
|------|------|------|
| Datasource | H2 인메모리 (`MODE=PostgreSQL`) | 로컬 기본값 |
| JPA | `ddl-auto: update`, `open-in-view: false` | |
| Flyway | `enabled: false` | dev 는 ddl-auto 사용, 운영 전환 시 활성화 |
| Redis | `localhost:6379` | 재설정 토큰 저장 |
| Kafka | `localhost:9092` + String 직렬화 | `max.block.ms: 3000` (브로커 미가동 시 블로킹 제한) |
| JWT | `jwt.secret/access-token-expiration-millis/issuer` | `@ConfigurationProperties` 바인딩 |

```yaml
jwt:
  secret: ticket-server-dev-secret-please-change-in-production-0123456789abcdef
  access-token-expiration-millis: 3600000
  issuer: ticket-server
```

> ⚠️ `jwt.secret` 은 dev 기본값. 운영에서는 **환경변수 등으로 반드시 교체**(32바이트 이상).

## compose.yaml

루트의 `compose.yaml` 로 로컬 인프라(PostgreSQL / Redis / Elasticsearch / Kafka)를 정의.

| 서비스 | 이미지 | 포트 |
|--------|--------|------|
| postgres | `postgres:latest` | 동적 (DB `ticket`) |
| redis | `redis:latest` | 동적 |
| elasticsearch | `elasticsearch:9.2.8` | 동적 (security off, single-node) |
| kafka | `confluentinc/cp-kafka:7.6.1` | **9092 고정** (KRaft, advertised=localhost:9092) |

## Spring Boot Docker Compose 연동

`spring-boot-docker-compose`(developmentOnly)가 핵심.

- **`bootRun` 시** `compose.yaml` 의 컨테이너를 **자동 기동**하고,
  각 서비스의 연결 정보(`ConnectionDetails`)를 자동 주입한다.
- 이 연결 정보는 `application.yml` 값보다 **우선**한다.
  → bootRun 에서는 H2 대신 **PostgreSQL 컨테이너**로 연결되고, Redis/Kafka 도 컨테이너로 향한다.
- 포트를 동적 매핑해도 Boot 가 실제 포트를 읽어 연결한다(Kafka 만 advertised listener 때문에 9092 고정).

```
bootRun ──> docker-compose 기동 ──> ConnectionDetails 주입(우선)
                                         ├─ postgres (H2 대체)
                                         ├─ redis
                                         ├─ kafka
                                         └─ elasticsearch
```

> 테스트(`@SpringBootTest`)에서는 `developmentOnly` 의존성이 테스트 클래스패스에 없어
> docker-compose 가 **동작하지 않는다** → 테스트는 `application.yml` 의 H2 를 사용(컨테이너 불필요).

## 실행 가이드

### 1) 전체 스택 실행 (Docker 필요)

```powershell
$env:JAVA_HOME = 'C:\Users\<user>\.jdks\temurin-25.0.2'
.\gradlew.bat bootRun
```
→ postgres/redis/kafka/elasticsearch 컨테이너가 함께 뜨고 앱이 연결된다.

### 2) Docker 없이 H2 로만 실행

`bootRun` 은 `compose.yaml` 을 감지해 Docker 를 요구하므로, 비활성화가 필요하다.

```yaml
# application.yml 에 추가
spring:
  docker:
    compose:
      enabled: false
```
- 이 경우 회원가입/로그인은 H2 만으로 동작한다.
- **비밀번호 재설정**은 Redis/Kafka 가 필요하므로 별도 기동이 요구된다.

## 빌드/검증 명령

| 목적 | 명령 |
|------|------|
| 컴파일 | `./gradlew compileKotlin` |
| 전체 빌드 | `./gradlew build` |
| 실행 | `./gradlew bootRun` |
| 의존성 트리 | `./gradlew dependencies --configuration runtimeClasspath` |

> 로컬 JDK: `C:\Users\<user>\.jdks\` 하위(예: `temurin-25.0.2`). `JAVA_HOME` 이 비어 있으면 지정 필요.
