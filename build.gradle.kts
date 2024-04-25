plugins {
    kotlin("jvm") version "1.9.23"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("it.skrape:skrapeit:1.1.5")
    implementation("it.skrape:skrapeit-http-fetcher:1.1.5")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}