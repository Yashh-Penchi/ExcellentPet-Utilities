plugins {
    java
    id("com.gradleup.shadow") version "9.4.3"
}

group = "com.yashhpenchi"
version = "1.0.0"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    implementation("org.xerial:sqlite-jdbc:3.46.1.3")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    jar {
        enabled = false
    }

    shadowJar {
        archiveClassifier.set("")
        // relocate sqlite-jdbc so it never collides with other plugins shading the same lib
        relocate("org.sqlite", "com.yashhpenchi.excellentpetutilities.libs.sqlite")
    }

    build {
        dependsOn(shadowJar)
    }
}
