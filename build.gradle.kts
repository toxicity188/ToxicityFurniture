plugins {
    `java-library`
    kotlin("jvm") version "1.9.0"
    id("com.github.johnrengelman.shadow") version("8.1.1")
    id("maven-publish")
    id("org.jetbrains.dokka") version("1.9.0")
}

group = "kor.toxicity.furniture"
version = "1.0.4"

val pluginName = "ToxicityFurniture"

val adventureVersion = "4.14.0"
val platformVersion = "4.3.1"

allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    repositories {
        mavenCentral()
        maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven(url = "https://jitpack.io")
        maven(url = "https://mvn.lumine.io/repository/maven-public/")
    }
    dependencies {
        testImplementation(kotlin("test"))

        compileOnly("net.kyori:adventure-api:$adventureVersion")
        compileOnly("net.kyori:adventure-platform-bukkit:$platformVersion")
    }

    tasks {
        compileJava {
            options.encoding = Charsets.UTF_8.name()
            options.release.set(targetJavaVersion)
        }
        test {
            useJUnitPlatform()
        }
        processResources {
            val props = mapOf(
                "version" to version.toString()
            )
            filteringCharset = Charsets.UTF_8.name()
            inputs.properties(props)
            filesMatching("plugin.yml") {
                expand(props)
            }
        }
    }
}
subprojects {
    apply(plugin = "org.jetbrains.dokka")
}

dependencies {
    implementation(project(path = ":nms:v1_19_R3", configuration = "reobf"))
    implementation(project(path = ":nms:v1_20_R1", configuration = "reobf"))
    implementation(project(path = ":nms:v1_20_R2", configuration = "reobf"))
    implementation(project(":plugin"))
}

tasks {
    jar {
        finalizedBy(shadowJar)
        archiveFileName.set("$pluginName.jar")
    }
    shadowJar {
        finalizedBy(dokkaHtmlMultiModule)
        archiveFileName.set("$pluginName.jar")
        fun applyPrefix(pattern: String) {
            relocate(pattern, "kor.toxicity.furniture.shaded.$pattern")
        }
        applyPrefix("kotlin")
    }
    dokkaHtmlMultiModule {
        outputDirectory.set(file("${layout.buildDirectory.get()}/dokka"))
    }
}

val targetJavaVersion = 17

val sourcesJar by tasks.creating(Jar::class.java) {
    dependsOn(tasks.classes)
    fun getProjectSource(project: Project): Array<File> {
        return if (project.subprojects.isEmpty()) project.sourceSets.main.get().allSource.srcDirs.toTypedArray() else ArrayList<File>().apply {
            project.subprojects.forEach {
                addAll(getProjectSource(it))
            }
        }.toTypedArray()
    }
    archiveFileName.set("$pluginName-sources.jar")
    from(*getProjectSource(project))
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
val dokkaJar by tasks.creating(Jar::class.java) {
    dependsOn(tasks.dokkaHtmlMultiModule)
    archiveFileName.set("$pluginName-docs.jar")
    from(file("${layout.buildDirectory.get()}/dokka"))
}
artifacts {
    archives(dokkaJar)
    archives(sourcesJar)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
}

kotlin {
    jvmToolchain(targetJavaVersion)
}
