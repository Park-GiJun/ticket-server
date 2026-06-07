plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "com.gijun"
version = "0.0.1-SNAPSHOT"
description = "ticket-server"

// Spring Boot 4.0.6 BOM 은 Kotlin 을 2.2.21 로 고정하므로, stdlib/reflect 등이
// Kotlin 플러그인(2.3.20)과 어긋나지 않도록 관리 버전을 2.3 으로 덮어쓴다.
extra["kotlin.version"] = libs.versions.kotlin.get()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.data.elasticsearch)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.quartz)

    // Kafka (Boot 4 스타터: spring-kafka + KafkaTemplate/리스너 컨테이너 자동설정)
    implementation(libs.spring.boot.starter.kafka)

    // Kotlin
    implementation(libs.kotlin.reflect)
    implementation(libs.jackson.module.kotlin)

    // JWT
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    // API Docs
    implementation(libs.springdoc.openapi.starter.webmvc.ui)

    // Database
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.h2)

    // Flyway (스타터가 flyway-core + Spring Boot 자동설정을 포함)
    implementation(libs.spring.boot.starter.flyway)
    runtimeOnly(libs.flyway.database.postgresql)

    // Docker Compose
    developmentOnly(libs.spring.boot.docker.compose)

    // Testing
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.kafka.test)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.extensions.spring)
    testImplementation(libs.mockk)

    // Testcontainers
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.kafka)
    testImplementation(libs.testcontainers.elasticsearch)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
