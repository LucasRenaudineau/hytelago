plugins {
    id("java")
}

group = "me.coblaz"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    compileOnly(files("libs/HytaleServer.jar"))
    implementation("io.github.archipelagomw:Java-Client:0.2.1")
}

tasks.test {
    useJUnitPlatform()
}

// ← this block was missing; without it the .ui file never ends up in the JAR
tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveBaseName.set("hytelago_achievements")
    archiveVersion.set("1.0.0")
    from("src/main/resources")
}