import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "me.jakejmattson"
version = "0.18.0"

plugins {
    kotlin("jvm") version Versions.kotlin
    `maven-publish`
    signing
    id("io.codearte.nexus-staging") version "0.21.2"
    id("com.github.ben-manes.versions") version "0.29.0"
    id("org.jetbrains.dokka") version "0.10.1"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    //Internal Dependencies
    implementation(kotlin("stdlib-jdk8"))
    implementation(Dependencies.coroutines)
    implementation(Dependencies.reflections)
    implementation(Dependencies.commons)
    implementation(Dependencies.slf4j)

    //Library Dependencies
    api(Dependencies.jda)
    api(Dependencies.guava)
    api(Dependencies.gson)

    //Test Dependencies
    testImplementation(Dependencies.mockk)
    testImplementation(Dependencies.junit_api)
    testRuntimeOnly(Dependencies.junit_engine)
}

tasks {
    val resourcePath = "src/main/resources"

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    test {
        useJUnitPlatform()
    }

    copy {
        from(file("$resourcePath/templates/readme-template.md"))
        into(file("."))
        rename{ "README.md" }
        expand(
            "group" to group,
            "project" to Constants.projectName,
            "version" to version
        )
    }

    copy {
        from(file("$resourcePath/templates/properties-template.json"))
        into(file(resourcePath))
        rename{ "kutils-properties.json" }
        expand(
            "projectRepo" to Constants.projectUrl,
            "projectVersion" to version,
            "kotlinVersion" to Versions.kotlin,
            "jdaVersion" to Versions.jda
        )
    }

    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"

        configuration {
            includeNonPublic = false
            skipEmptyPackages = true
            reportUndocumented = true

            targets = listOf("JVM")
            platform = "JVM"

            sourceLink {
                path = "src/main/kotlin"
                url = "https://github.com/JakeJMattson/KUtils/tree/master/src/main/kotlin"
            }
        }
    }
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    archiveClassifier.set("javadoc")
    from(tasks.dokka)
    dependsOn(tasks.dokka)
}

publishing {
    publications {
        create<MavenPublication>(Constants.projectName) {
            from(components["kotlin"])
            artifact(sourcesJar)
            artifact(dokkaJar)

            pom {
                name.set(Constants.projectName)
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
                scm {
                    connection.set("scm:git:ssh://github.com/JakeJMattson/KUtils.git")
                    developerConnection.set("scm:git:ssh://git@github.com:JakeJMattson/KUtils.git")
                    url.set(Constants.projectUrl)
                }
            }
            repositories {
                maven {
                    url = if (version.toString().endsWith("SNAPSHOT"))
                        uri(Constants.snapshotsRepoUrl)
                    else
                        uri(Constants.releasesRepoUrl)

                    credentials {
                        username = project.properties["nexusUsername"] as String?
                        password = project.properties["nexusPassword"] as String?
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications[Constants.projectName])
}

nexusStaging { }