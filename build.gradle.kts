plugins {
    java
}

group = "me.coblaz"
version = "0.5.3-1.1.2"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    compileOnly(files("libs/HytaleServer.jar"))
    implementation("org.joml:joml:1.10.5")
    implementation("io.github.archipelagomw:Java-Client:0.2.1")
    implementation("com.google.code.gson:gson:2.13.1")
}

tasks.jar {
    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveBaseName.set("hytelago")
    archiveVersion.set("0.5.3-1.1.2")
    archiveClassifier.set("")
}

tasks.test {
    useJUnitPlatform()
}