import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "me.jakejmattson"
version = "0.17.0-SNAPSHOT"
val isSnapshot = version.toString().endsWith("SNAPSHOT")

plugins {
    kotlin("jvm") version Versions.kotlin
    `maven-publish`
    id("com.github.ben-manes.versions") version "0.28.0"
}

repositories {
    mavenCentral()
    mavenLocal()
    jcenter()
}

dependencies {
    //Internal Dependencies
    implementation(Dependencies.coroutines)
    implementation(Dependencies.reflections)
    implementation(Dependencies.commons)
    implementation(Dependencies.slf4j)

    //Library Dependencies
    api(Dependencies.jda)
    api(Dependencies.guava)
    api(Dependencies.gson)

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
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    test {
        useJUnitPlatform()
    }
}

publishing {
    publications {
        create<MavenPublication>(Constants.projectName) {
            from(components["kotlin"])
            pom {
                description.set(Constants.projectDescription)
                url.set(Constants.projectUrl)
                developers {
                    developer {
                        id.set("JakeJMattson")
                        name.set("Jake Mattson")
                        email.set("JakeJMattson@gmail.com")
                    }
                }
                withXml {
                    val repoNode = asNode().appendNode("repositories").appendNode("repository")

                    with(repoNode) {
                        appendNode("id", "jcenter")
                        appendNode("name", "jcenter-bintray")
                        appendNode("url", "https://jcenter.bintray.com")
                    }
                }
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
            }
            repositories {
                val repoName = if (isSnapshot) "Snapshots" else "Releases"
                val repoUrl = if (isSnapshot) Constants.snapshotsRepoUrl else Constants.releasesRepoUrl

                maven {
                    name = repoName
                    url = uri(repoUrl)
                    credentials {
                        username = project.properties["nexusUsername"] as String
                        password = project.properties["nexusPassword"] as String
                    }
                }
            }
        }
    }
}