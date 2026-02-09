import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.serialization") version "2.3.10"
    id("com.gradleup.shadow") version "9.3.0"
    application
}

group = "me.igorunderplayer"
version = "1.0-SNAPSHOT"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {

    // Kotlin
    testImplementation(kotlin("test"))

    // Dependency Injection
    implementation("io.insert-koin:koin-core:4.0.2")

    // Ktor
    implementation("io.ktor:ktor-server-core-jvm:3.2.3")
    implementation("io.ktor:ktor-server-netty-jvm:3.2.3")
    implementation("io.ktor:ktor-server-content-negotiation:3.2.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.2.3")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // Discord
    implementation("dev.kord:kord-core:0.17.0")

    // Riot
    implementation("com.github.stelar7:R4J:2.7.0")

    // Database
    implementation("org.ktorm:ktorm-core:4.1.1")
    implementation("org.ktorm:ktorm-support-postgresql:4.1.1")
    implementation("org.postgresql:postgresql:42.7.5")
}

tasks {
    test {
        useJUnitPlatform()
    }

    withType<KotlinCompile> {
        compilerOptions.jvmTarget.set(
            org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
        )
    }

    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        archiveFileName.set("KonoBot.jar")
        mergeServiceFiles()
    }
}

tasks.register("stage") {
    dependsOn("shadowJar")
}

application {
    mainClass.set("me.igorunderplayer.kono.Launcher")
}
