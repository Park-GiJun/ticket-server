# 02. 빌드 구성

## 단일 모듈 + Version Catalog

`ticket-server` 는 단일 Gradle 모듈이며, 의존성은 **Version Catalog**(`gradle/libs.versions.toml`)로 중앙 관리한다.

```
ticket-server/
├─ build.gradle.kts
├─ settings.gradle.kts
├─ compose.yaml
└─ gradle/
   └─ libs.versions.toml
```

## 플러그인

`build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)               // all-open: @Component 등
    alias(libs.plugins.kotlin.jpa)                  // no-arg/all-open: @Entity 등
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}
```

## JDK 25 Toolchain

```kotlin
java {
    toolchain { languageVersion = JavaLanguageVersion.of(25) }
}
```

## ⭐ `kotlin.version` 오버라이드 (핵심)

Spring Boot 4.0.6 BOM 은 Kotlin 을 **2.2.21** 로 고정한다. 플러그인만 2.3.20 으로 올리면
`kotlin-stdlib`/`kotlin-reflect` 가 2.2.21 로 끌려와 **버전이 어긋난다.**
이를 막기 위해 Boot 의 관리 버전 프로퍼티를 카탈로그 값으로 덮어쓴다.

```kotlin
// Spring Boot BOM 의 Kotlin 고정(2.2.21)을 플러그인 버전(2.3.20)에 맞춰 정렬
extra["kotlin.version"] = libs.versions.kotlin.get()
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

## JPA용 all-open

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
```

## 빌드/실행 명령

| 목적 | 명령 |
|------|------|
| 메인 컴파일 | `./gradlew compileKotlin` |
| 전체 빌드 | `./gradlew build` |
| 실행(컨테이너 자동 기동) | `./gradlew bootRun` |

> ⚠️ 로컬에 `JAVA_HOME` 이 비어 있으면 `gradlew` 가 실패한다.
> JDK 25 경로를 지정해 실행한다 — 예: PowerShell
> `$env:JAVA_HOME = 'C:\Users\<user>\.jdks\temurin-25.0.2'; .\gradlew.bat build`

> ℹ️ Gradle Wrapper 는 9.5.1. Kotlin 2.3.20 의 공식 테스트 범위는 ~9.3.0 이라
> 상위 버전에서 경고가 날 수 있으나 빌드는 정상 동작한다.
