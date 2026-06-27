plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.jpa)
}

dependencies {
    implementation(project(":shared"))

    // Spring Boot Starters (인증은 Gateway 가 담당하므로 security 미포함)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)

    // Service Discovery
    implementation(libs.spring.cloud.starter.netflix.eureka.client)

    // Kotlin / Jackson
    implementation(libs.jackson.module.kotlin)

    // Database
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.h2)

    // Testing
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.webmvc.test)
    testImplementation(libs.spring.boot.resttestclient)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.extensions.spring)
    testImplementation(libs.mockk)
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.testcontainers.postgresql)
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
