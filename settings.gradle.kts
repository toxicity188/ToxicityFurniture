pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.5.0")
}

rootProject.name = "ToxicityFurniture"

include("plugin")
include("api")

include("nms:v1_19_R3")
include("nms:v1_20_R1")
include("nms:v1_20_R2")