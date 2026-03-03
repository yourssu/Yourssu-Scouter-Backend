plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.4.1"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "1.9.25"
	id("org.flywaydb.flyway") version "10.20.1"
}

group = "com.yourssu"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

val jwtVersion = "0.12.6"

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:2024.0.0")
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-mysql")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.apache.poi:poi-ooxml:5.2.3")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("software.amazon.awssdk:s3:2.30.33")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	// mail
	implementation("com.sun.mail:jakarta.mail:2.0.1")
//	implementation("javax.activation:activation:1.1.1")
	implementation("jakarta.activation:jakarta.activation-api:2.1.3")
	implementation("com.sun.activation:jakarta.activation:2.0.1")

	// jwt
	implementation ("io.jsonwebtoken:jjwt-api:$jwtVersion")
	runtimeOnly ("io.jsonwebtoken:jjwt-impl:$jwtVersion")
	runtimeOnly ("io.jsonwebtoken:jjwt-jackson:$jwtVersion")
	runtimeOnly("com.h2database:h2")
	runtimeOnly("com.mysql:mysql-connector-j")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.assertj:assertj-core")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:mysql")
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

flyway {
	url = System.getenv("DB_URL")
	user = System.getenv("DB_USERNAME")
	password = System.getenv("DB_PASSWORD")
	baselineOnMigrate = true
	baselineVersion = "1"
	locations = arrayOf("filesystem:src/main/resources/db/migration")
}
