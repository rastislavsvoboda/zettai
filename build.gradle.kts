plugins {
    kotlin("jvm") version "1.9.22"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.http4k:http4k-bom:5.14.1.0"))
    implementation ("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.http4k:http4k-core")
    implementation ("org.http4k:http4k-server-jetty:4.48.0.0")

    testImplementation ("org.junit.jupiter:junit-jupiter-engine:5.7.2")
    testRuntimeOnly ("org.junit.platform:junit-platform-launcher:1.7.0")

    testImplementation ("org.http4k:http4k-client-jetty:4.48.0.0")
    testImplementation ("com.ubertob.pesticide:pesticide-core:1.6.5")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.strikt:strikt-core:0.34.0")
    testImplementation("org.jsoup:jsoup:1.12.1")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}