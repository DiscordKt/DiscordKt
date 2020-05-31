group = "me.jakejmattson"
version = "0.17.0"

plugins {
    kotlin("jvm") version Versions.kotlin
    `java-library`
    `maven-publish`
    id("com.github.ben-manes.versions") version "0.28.0"
}

repositories {
    mavenCentral()
    mavenLocal()
    jcenter()

    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    //Internal Dependencies
    implementation(Dependencies.coroutines)
    implementation(Dependencies.reflections)
    implementation(Dependencies.gson)
    implementation(Dependencies.guava)
    implementation(Dependencies.commons)
    implementation(Dependencies.slf4j)

    //Library Dependencies
    implementation(Dependencies.jda)

    //Scripting Engine
    implementation(Dependencies.kotlinCompiler)
    implementation(Dependencies.script_compiler)
    implementation(Dependencies.script_runtime)
    implementation(Dependencies.script_util)

    //Test Dependencies
    testImplementation(Dependencies.mockk)
    testImplementation(Dependencies.junit_api)
    testRuntimeOnly(Dependencies.junit_engine)
}

tasks {
    compileJava {
        options.encoding = "UTF-8"

        java {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
    }

    test {
        useJUnitPlatform()
    }
}

publishing {
    publications {
        create<MavenPublication>(Constants.projectName) {
            artifactId = Constants.projectName
            from(components["kotlin"])
            pom {
                name.set(Constants.projectName)
                description.set(Constants.projectDescription)
                url.set(Constants.projectUrl)
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