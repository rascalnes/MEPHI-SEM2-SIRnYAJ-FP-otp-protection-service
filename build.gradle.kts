plugins {
    id("java")
    id("application")
}

group = "ru.nes"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // Database
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("com.zaxxer:HikariCP:5.1.0")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")

    // Password hashing
    implementation("org.mindrot:jbcrypt:0.4")

    // JSON processing
    implementation("com.google.code.gson:gson:2.10.1")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("org.slf4j:slf4j-api:2.0.11")

    // Email (Jakarta Mail / Angus Mail)
    implementation("com.sun.mail:jakarta.mail:2.0.1")
    implementation("jakarta.activation:jakarta.activation-api:2.1.3")

    // SMS (JSMPP)
    implementation("org.jsmpp:jsmpp:3.0.1")

    // Utils
    implementation("org.apache.commons:commons-lang3:3.14.0")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("ru.nes.otp.OTPApplication")
}

tasks.test {
    useJUnitPlatform()
}