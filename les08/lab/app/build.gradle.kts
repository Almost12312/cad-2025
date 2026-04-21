plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(libs.spring.context)
    implementation(libs.spring.orm)
    implementation(libs.spring.data.jpa)
    implementation(libs.hikari)
    implementation(libs.jakarta.persistence.api)
    implementation(libs.hibernate.hikaricp)
    implementation(libs.hibernate.core)
    implementation(libs.slf4j.api)
    implementation(libs.logback.core)
    implementation(libs.logback.classic)
    implementation(libs.opencsv)
    runtimeOnly(libs.h2)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

application {
    mainClass = "ru.bsuedu.cad.lab.App"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
