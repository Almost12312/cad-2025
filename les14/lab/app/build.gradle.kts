plugins {
    war
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(libs.spring.context)
    implementation(libs.spring.orm)
    implementation(libs.spring.web)
    implementation(libs.spring.webmvc)
    implementation(libs.spring.data.jpa)
    implementation(libs.spring.security.web)
    implementation(libs.spring.security.config)
    implementation(libs.hikari)
    implementation(libs.jakarta.persistence.api)
    implementation(libs.hibernate.hikaricp)
    implementation(libs.hibernate.core)
    implementation(libs.slf4j.api)
    implementation(libs.logback.core)
    implementation(libs.logback.classic)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.h2)
    implementation(libs.thymeleaf)
    implementation(libs.thymeleaf.spring6)

    compileOnly(libs.jakarta.servlet.api)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

tasks.war {
    archiveFileName.set("pet-store.war")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
