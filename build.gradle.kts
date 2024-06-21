plugins {
    kotlin("jvm") version "1.9.23"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("it.skrape:skrapeit:1.1.5")
    implementation("it.skrape:skrapeit-http-fetcher:1.1.5")
    implementation("org.seleniumhq.selenium:selenium-java:4.21.0")
    implementation("org.mongodb:mongodb-driver-sync:5.1.0")
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(21))
    }
}