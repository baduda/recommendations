plugins {
    java
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
    jacoco
}

group = "com.epam.xm"
version = "0.0.1-SNAPSHOT"
description = "recommendations"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jspecify:jspecify:1.0.0")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    // Добавляем зависимость для автоконфигурации, если она не подтянулась
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("net.javacrumbs.shedlock:shedlock-spring:6.3.0")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:6.3.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")
    implementation("org.mapstruct:mapstruct:1.6.3")
    implementation("com.bucket4j:bucket4j-core:8.10.1")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter:1.20.5")
    testImplementation("org.testcontainers:postgresql:1.20.5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:all")
    options.compilerArgs.add("-Xlint:-processing")
    options.compilerArgs.add("-Xlint:-removal")
}

tasks.withType<Test> {
    useJUnitPlatform()
    environment("DOCKER_HOST", "unix://${System.getProperty("user.home")}/.colima/default/docker.sock")
    environment("TESTCONTAINERS_RYUK_DISABLED", "true")
    finalizedBy("jacocoTestReport")
}

val jacocoExcludes = listOf(
    "**/dto/**",
    "**/persistence/*Entity*",
    "**/config/**",
    "**/*Application*",
    "**/infrastructure/error/ApiError*",
    "**/domain/PricePoint*",
    "**/domain/CryptoStats*"
)

tasks.withType<JacocoReport> {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(
        sourceSets.main.get().output.asFileTree.matching {
            exclude(jacocoExcludes)
        }
    )
}

tasks.withType<JacocoCoverageVerification> {
    dependsOn("jacocoTestReport")
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
        rule {
            element = "CLASS"
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
            excludes = listOf(
                "*.dto.*",
                "*.persistence.*Entity*",
                "*.config.*",
                "*Application*",
                "*.infrastructure.error.ApiError*",
                "*.domain.PricePoint*",
                "*.domain.CryptoStats*"
            )
        }
    }
}

tasks.check {
    dependsOn("jacocoTestCoverageVerification")
}
