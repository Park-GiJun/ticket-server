rootProject.name = "ticket-server"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

include(
    "shared",
    "discovery-server",
    "gateway",
    "user-service",
    "ticket-event-service",
    "reservation-service",
    "payment-service",
)
