plugins {
    `java-library`
}

// 실행 모듈이 아닌 공유 라이브러리. bootJar 가 없으므로 일반 jar 만 생성된다.
dependencies {
    api(libs.spring.boot.starter.web)
    api(libs.springdoc.openapi.starter.webmvc.ui)

    // JWT 검증(발급은 user-service, 검증은 gateway 가 공유 검증기를 사용)
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)
}
