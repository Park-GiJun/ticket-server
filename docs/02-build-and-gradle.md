# 02. 빌드 구성

## 멀티모듈 + Version Catalog

백엔드는 `ticket-server-be/` 를 **Gradle 빌드 루트**로 하는 멀티모듈이며, 의존성은 **Version Catalog**(`gradle/libs.versions.toml`)로 중앙 관리한다. 저장소 루트에는 빌드 산출물이 아닌 `docs/` 와 `infra/` 만 둔다.

```
ticket-server/                  # 저장소 루트
├─ docs/
├─ infra/
│  └─ compose.yaml              # 로컬 인프라(수동 docker compose)
└─ ticket-server-be/            # Gradle 빌드 루트
   ├─ build.gradle.kts
   ├─ settings.gradle.kts
   ├─ gradlew / gradlew.bat
   ├─ gradle/
   │  ├─ libs.versions.toml
   │  └─ wrapper/
   ├─ common/                   # 공유 라이브러리
   ├─ discovery-server/         # Eureka (8761)
   ├─ gateway/                  # API Gateway (8080)
   ├─ user-service/             # (8081)
   └─ ticket-event-service/     # (8082)
```

## 플러그인 (루트 선언 → 서브프로젝트 적용)

루트 `build.gradle.kts` 는 모든 플러그인을 **`apply false`** 로 선언만 하고, `subprojects {}` 블록이
공통 플러그인을 모든 모듈에 적용한다. **Spring Boot·kotlin-jpa 는 모듈별로** 필요한 곳에만 붙인다.

```kotlin
// 루트 build.gradle.kts — 선언만(apply false)
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.kotlin.jpa) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")            // 모든 모듈 공통
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "io.spring.dependency-management")
    // ...
}
```

| 플러그인 | 적용 범위 |
|----------|-----------|
| `kotlin.jvm` / `kotlin.spring` / `dependency-management` | 전 모듈(`subprojects`) |
| `spring.boot` (bootJar) | 실행 모듈만(discovery/gateway/user/ticket-event). `common` 은 `java-library` |
| `kotlin.jpa` + `allOpen` | JPA 사용 모듈만(`user-service`, `ticket-event-service`) |

## JDK 25 Toolchain

`subprojects {}` 에서 전 모듈 공통으로 지정한다.

```kotlin
subprojects {
    configure<JavaPluginExtension> {
        toolchain { languageVersion = JavaLanguageVersion.of(25) }
    }
}
```

## ⭐ `kotlin.version` 오버라이드 (핵심)

Spring Boot 4.0.6 BOM 은 Kotlin 을 **2.2.21** 로 고정한다. 플러그인만 2.3.20 으로 올리면
`kotlin-stdlib`/`kotlin-reflect` 가 2.2.21 로 끌려와 **버전이 어긋난다.**
이를 막기 위해 Boot 의 관리 버전 프로퍼티를 카탈로그 값으로 덮어쓴다(역시 `subprojects {}` 에서).

```kotlin
// Spring Boot BOM 의 Kotlin 고정(2.2.21)을 플러그인 버전(2.3.20)에 맞춰 정렬
// version catalog 접근자(libs)는 subprojects 컨텍스트에서 못 쓰므로 루트에서 미리 추출해 둔다.
subprojects {
    extra["kotlin.version"] = kotlinVersion   // = libs.versions.kotlin.get()
}
```

**검증** (`gradlew dependencies --configuration runtimeClasspath`):

```
+--- org.jetbrains.kotlin:kotlin-stdlib:2.3.20
+--- org.jetbrains.kotlin:kotlin-reflect -> 2.3.20
+--- org.springframework.boot:spring-boot-starter-web -> 4.0.6
```

## Kotlin 컴파일러 옵션

```kotlin
kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}
```

- `-Xjsr305=strict` : JSR-305/JSpecify 널 애너테이션을 엄격히 적용(Spring 7 널 안정성과 정합).
- `-Xannotation-default-target=param-property` : 생성자 프로퍼티의 애너테이션 기본 타깃 처리.

## JPA용 all-open (JPA 모듈 한정)

`user-service` / `ticket-event-service` 의 모듈 `build.gradle.kts` 에만 둔다(`kotlin-jpa` 플러그인 동반).

```kotlin
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
```

Kotlin 클래스는 기본 `final` 이라 Hibernate 프록시가 불가능하다.
`kotlin-jpa` 플러그인이 no-arg 생성자를, `allOpen` 이 위 애너테이션 클래스를 open 으로 만든다.

## settings.gradle.kts

```kotlin
rootProject.name = "ticket-server"

pluginManagement {
    repositories { gradlePluginPortal(); mavenCentral(); google() }
}
dependencyResolutionManagement {
    repositories { mavenCentral(); google() }
}

include("common", "discovery-server", "gateway", "user-service", "ticket-event-service")
```

## 빌드/실행 명령

| 목적 | 명령 |
|------|------|
| 메인 컴파일 | `./gradlew compileKotlin` |
| 전체 빌드 | `./gradlew build` |
| 모듈 실행 | `./gradlew :user-service:bootRun` (루트엔 `bootRun` 없음 — 모듈 지정) |
| 단일 모듈 테스트 | `./gradlew :user-service:test` |

> ℹ️ 루트 프로젝트에는 `bootRun` 태스크가 없다(실행은 각 서비스 모듈에 있음). 기동 순서·인프라는
> [10 문서](./10-configuration-and-run.md) 참고. `bootRun` 은 더 이상 컨테이너를 자동 기동하지 않는다.

> ⚠️ `gradlew` 는 빌드 루트인 `ticket-server-be/` 에서 실행한다.
> 저장소 루트에서 돌릴 때는 `-p ticket-server-be` 를 붙인다 — 예: `./gradlew -p ticket-server-be build`
>
> ⚠️ 로컬에 `JAVA_HOME` 이 비어 있으면 `gradlew` 가 실패한다.
> JDK 25 경로를 지정해 실행한다 — 예: PowerShell
> `$env:JAVA_HOME = 'C:\Users\<user>\.jdks\temurin-25.0.2'; .\ticket-server-be\gradlew.bat -p ticket-server-be build`

> ℹ️ Gradle Wrapper 는 9.5.1. Kotlin 2.3.20 의 공식 테스트 범위는 ~9.3.0 이라
> 상위 버전에서 경고가 날 수 있으나 빌드는 정상 동작한다.
