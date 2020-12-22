import org.jetbrains.dokka.Platform

group = "me.jakejmattson"
version = "0.22.0-SNAPSHOT"
val isSnapshot = version.toString().endsWith("SNAPSHOT")

plugins {
    //Core
    kotlin("jvm") version Versions.kotlin
    kotlin("plugin.serialization") version Versions.kotlin
    id("org.jetbrains.dokka") version Versions.dokka

    //Publishing
    signing
    `maven-publish`
    id("io.codearte.nexus-staging") version "0.22.0"

    //Misc
    id("com.github.ben-manes.versions") version "0.36.0"
}

repositories {
    mavenLocal()
    maven("https://dl.bintray.com/kordlib/Kord")
    mavenCentral()
    jcenter()
}

dependencies {
    //Internal Dependencies
    implementation(Dependencies.reflections)
    implementation(Dependencies.gson)
    implementation(Dependencies.slf4j)
    implementation(Dependencies.log)

    //Library Dependencies
    api(Dependencies.kord)
    api(Dependencies.emojis)
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        }
    }

    copy {
        val path = "templates/readme.md"

        from(file(path))
        into(file("."))
        rename { "README.md" }
        expand(
            "kotlin" to Versions.kotlin,
            "kord" to Versions.kord,
            "imports" to README.createImportBlock(group.toString(), version.toString(), isSnapshot)
        )
    }

    copy {
        from(file("templates/properties-template.json"))
        into(file("src/main/resources"))
        rename { "library-properties.json" }
        expand(
            "projectRepo" to Constants.projectUrl,
            "projectVersion" to version,
            "kotlinVersion" to Versions.kotlin,
            "kordVersion" to Versions.kord
        )
    }

    dokkaHtml.configure {
        outputDirectory.set(buildDir.resolve("dokka"))

        dokkaSourceSets {
            configureEach {
                platform.set(Platform.jvm)
                jdkVersion.set(8)

                includeNonPublic.set(false)
                skipEmptyPackages.set(true)
                reportUndocumented.set(true)

                suppressedFiles.from("src\\main\\kotlin\\me\\jakejmattson\\discordkt\\api\\TypeContainers.kt")
            }
        }
    }

    register("dependencySizes") {
        description = "Print dependency sizes for the default configuration"
        doLast {
            val sizes = buildString {
                val configuration = configurations["default"]
                val size = configuration.map { it.length() / (1024.0 * 1024.0) }.sum()
                val longestName = configuration.map { it.name.length }.max()
                val formatStr = "%-${longestName}s   %5d KB"

                appendln("Total Size: %.2f MB\n".format(size))

                configuration
                    .sortedBy { -it.length() }
                    .forEach {
                        appendln(formatStr.format(it.name, it.length() / 1024))
                    }
            }

            println(sizes)
        }
    }

    register("generateDocs") {
        description = "Generate documentation for DiscordKt.github.io"
        dependsOn(listOf(dokkaHtml))

        copy {
            delete(file("../DiscordKt.github.io/docs/dokka"))
            from(buildDir.resolve("dokka"))
            into(file("../DiscordKt.github.io/docs/dokka"))
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
                    connection.set("scm:git:ssh://github.com/JakeJMattson/DiscordKt.git")
                    developerConnection.set("scm:git:ssh://git@github.com:JakeJMattson/DiscordKt.git")
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