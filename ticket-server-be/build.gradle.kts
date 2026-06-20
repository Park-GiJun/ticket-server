import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.kotlin.jpa) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
}

// version catalog 접근자(libs)는 subprojects {} 컨텍스트에서 쓸 수 없으므로 루트에서 미리 추출한다.
val kotlinVersion = libs.versions.kotlin.get()
val springBootVersion = libs.versions.spring.boot.get()
val springCloudVersion = libs.versions.spring.cloud.get()
val kotlinReflectLib = libs.kotlin.reflect
val micrometerPrometheusLib = libs.micrometer.registry.prometheus

allprojects {
    group = "com.gijun"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    // 모든 모듈 공통: Kotlin(JVM) + Spring 보정 + 의존성 BOM 관리
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "io.spring.dependency-management")

    // Spring Boot 4.0 BOM 이 Kotlin 을 2.2.x 로 고정하므로 플러그인(2.3.x) 버전으로 덮어쓴다.
    extra["kotlin.version"] = kotlinVersion

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    the<DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
        }
    }

    dependencies {
        "implementation"(kotlinReflectLib)
        // Prometheus 메트릭(/actuator/prometheus). 노출은 management.endpoints exposure 로 제어.
        "runtimeOnly"(micrometerPrometheusLib)
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    configure<KotlinJvmProjectExtension> {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
