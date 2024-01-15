group = "me.jakejmattson"
version = "0.23.5"
val projectGroup = group.toString()

plugins {
    //Core
    kotlin("jvm") version Constants.kotlin
    kotlin("plugin.serialization") version Constants.kotlin
    id("org.jetbrains.dokka") version Constants.dokka

    //Publishing
    signing
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"

    //Misc
    id("com.github.ben-manes.versions") version "0.42.0"
}

repositories {
    mavenCentral()
}

dependencies {
    api("dev.kord:kord-core:${Constants.kord}")
    api("dev.kord.x:emoji:0.5.0")
    api("org.slf4j:slf4j-simple:2.0.0")

    implementation("org.reflections:reflections:0.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")

    testImplementation(platform("org.junit:junit-bom:5.9.0"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("io.mockk:mockk:1.12.5")
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

        dependsOn("writeProperties")
    }

    register<WriteProperties>("writeProperties") {
        property("version", project.version.toString())
        property("kotlin", Constants.kotlin)
        property("kord", Constants.kord)
        setOutputFile("src/main/resources/library.properties")
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
                suppressedFiles.from("src\\main\\kotlin\\me\\jakejmattson\\discordkt\\TypeContainers.kt")
            }
        }
    }

    copy {
        from(file("templates/readme.md"))
        into(file("."))
        rename { "README.md" }
        expand(
            "kotlin" to Constants.kotlin.replace("-", "--"),
            "kord" to Constants.kord.replace("-", "--"),
            "discordkt" to version.toString().replace("-", "--"),
            "imports" to Docs.generateImports(projectGroup, version.toString())
        )
    }

    register("generateDocs") {
        description = "Generate documentation for discordkt.github.io"
        dependsOn(dokkaHtml)

        copy {
            val docsPath = "../discordkt.github.io/docs/"

            delete(file("$docsPath/api"))
            from(buildDir.resolve("dokka"))
            into(file("$docsPath/api"))

            file("$docsPath/install.md").writeText(
                Docs.generateImports(projectGroup, version.toString(), true)
            )
        }
    }

    register("dependencySizes") {
        description = "Print dependency sizes for the default configuration"
        doLast {
            val sizes = buildString {
                val configuration = configurations["default"]
                val size = configuration.sumOf { it.length() / (1024.0 * 1024.0) }
                val longestName = configuration.maxOfOrNull { it.name.length }
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
    dependsOn("writeProperties")
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
        }
    }
}

signing {
    setRequired({
        gradle.taskGraph.hasTask("publish")
    })

    sign(publishing.publications[Constants.projectName])
}

nexusPublishing {
    repositories {
        sonatype()
    }
}