import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

val projectName = "KUtils"

group = "me.jakejmattson"
version = "0.17.0"

plugins {
    kotlin("jvm") version "1.3.72"
    `maven-publish`
    id("com.github.ben-manes.versions") version "0.28.0"
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()

    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    //Library Dependencies
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")
    implementation("org.reflections:reflections:0.9.12")
    implementation("net.dv8tion:JDA:4.1.1_155")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.google.guava:guava:29.0-jre")
    implementation("org.apache.commons:commons-text:1.8")
    implementation("org.slf4j:slf4j-simple:1.7.30")

    //Scripting Engine
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:${getKotlinPluginVersion()}")
    implementation("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:${getKotlinPluginVersion()}")
    implementation("org.jetbrains.kotlin:kotlin-script-runtime:${getKotlinPluginVersion()}")
    implementation("org.jetbrains.kotlin:kotlin-script-util:${getKotlinPluginVersion()}")

    //Test Dependencies
    testImplementation("io.mockk:mockk:1.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0-M1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0-M1")
}

tasks.compileJava {
    options.encoding = "UTF-8"

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>(projectName) {
            artifactId = projectName
            from(components["kotlin"])
            pom {
                name.set(projectName)
                description.set("A Kotlin wrapper for the Discord API.")
                url.set("https://gitlab.com/JakeJMattson/$projectName/")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("JakeJMattson")
                        name.set("Jake Mattson")
                        email.set("JakeJMattson@gmail.com")
                    }
                }
            }
        }
    }
}