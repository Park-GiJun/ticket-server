plugins {
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.spring.cloud.starter.gateway.server.webmvc)
    implementation(libs.spring.cloud.starter.netflix.eureka.client)
    implementation(libs.spring.boot.starter.actuator)

    testImplementation(libs.spring.boot.starter.test)
}
