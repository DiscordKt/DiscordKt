group = "me.jakejmattson"
version = "0.23.0-SNAPSHOT"
val isSnapshot = version.toString().endsWith("SNAPSHOT")

plugins {
    //Core
    kotlin("jvm") version Constants.kotlin
    kotlin("plugin.serialization") version Constants.kotlin
    id("org.jetbrains.dokka") version Constants.dokka

    //Publishing
    signing
    `maven-publish`
    id("io.codearte.nexus-staging") version "0.30.0"

    //Misc
    id("com.github.ben-manes.versions") version "0.39.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.reflections:reflections:0.9.12")
    implementation("com.google.code.gson:gson:2.8.8")

    api("dev.kord:kord-core:0.8.0-M5")
    api("dev.kord.x:emoji:0.5.0")
    api("org.slf4j:slf4j-simple:2.0.0-alpha5")

    testImplementation(platform("org.junit:junit-bom:5.8.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("io.mockk:mockk:1.12.0")
}

tasks {
    kotlin {
        explicitApi()
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        }
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    test {
        useJUnitPlatform()
    }

    dokkaHtml.configure {
        outputDirectory.set(buildDir.resolve("dokka"))

        dokkaSourceSets {
            configureEach {
                platform.set(org.jetbrains.dokka.Platform.jvm)

                includeNonPublic.set(false)
                skipEmptyPackages.set(true)
                reportUndocumented.set(true)

                includes.from("packages.md")
                suppressedFiles.from("src\\main\\kotlin\\me\\jakejmattson\\discordkt\\api\\TypeContainers.kt")
            }
        }
    }

    copy {
        val path = "templates/readme.md"

        from(file(path))
        into(file("."))
        rename { "README.md" }
        expand(
            "kotlin" to Constants.kotlin.replace("-", "--"),
            "kord" to Constants.kord.replace("-", "--"),
            "discordkt" to version.toString().replace("-", "--"),
            "imports" to Docs.generateImports(group.toString(), version.toString(), isSnapshot)
        )
    }

    copy {
        from(file("templates/properties-template.json"))
        into(file("src/main/resources"))
        rename { "library-properties.json" }
        expand(
            "projectRepo" to Constants.projectUrl,
            "projectVersion" to version,
            "kotlinVersion" to Constants.kotlin,
            "kordVersion" to Constants.kord
        )
    }

    register("generateDocs") {
        description = "Generate documentation for discordkt.github.io"
        dependsOn(dokkaHtml)

        copy {
            val docsPath = "../discordkt.github.io/docs/${if (isSnapshot) "snapshot" else "release"}"

            delete(file("$docsPath/dokka"))
            from(buildDir.resolve("dokka"))
            into(file("$docsPath/dokka"))

            file("$docsPath/index.md").writeText(
                Docs.generateImports(group.toString(), version.toString(), isSnapshot, true)
            )
        }
    }

    register("dependencySizes") {
        description = "Print dependency sizes for the default configuration"
        doLast {
            val sizes = buildString {
                val configuration = configurations["default"]
                val size = configuration.sumOf { it.length() / (1024.0 * 1024.0) }
                val longestName = configuration.map { it.name.length }.maxOrNull()
                val formatStr = "%-${longestName}s   %5d KB"

                appendLine("Total Size: %.2f MB\n".format(size))

                configuration
                    .sortedBy { -it.length() }
                    .forEach {
                        appendLine(formatStr.format(it.name, it.length() / 1024))
                    }
            }

            println(sizes)
        }
    }
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    archiveClassifier.set("javadoc")
    from(tasks.dokkaJavadoc)
    dependsOn(tasks.dokkaJavadoc)
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
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                scm {
                    connection.set("scm:git:ssh://github.com/discordkt/discordkt.git")
                    developerConnection.set("scm:git:ssh://git@github.com:discordkt/discordkt.git")
                    url.set(Constants.projectUrl)
                }
            }
            repositories {
                maven {
                    url = if (isSnapshot) uri(Constants.snapshotsRepoUrl) else uri(Constants.releasesRepoUrl)

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