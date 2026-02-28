import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier

group = "me.jakejmattson"
version = "0.24.0"

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
    id("com.github.ben-manes.versions") version "0.51.0"
}

repositories {
    mavenCentral()
}

dependencies {
    api("dev.kord:kord-core:${Constants.kord}")
    api("dev.kord.x:emoji:0.5.0")
    api("org.slf4j:slf4j-simple:2.0.9")

    implementation("org.reflections:reflections:0.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.mockk:mockk:1.13.9")
}

tasks {
    kotlin {
        explicitApi()
        jvmToolchain(11)
    }

    compileKotlin {
        compilerOptions {
            freeCompilerArgs.add("-Xopt-in=kotlin.RequiresOptIn")
        }

        doLast("writeProperties") {}
    }

    register<WriteProperties>("writeProperties") {
        dependsOn(processResources)
        property("version", project.version.toString())
        property("kotlin", Constants.kotlin)
        property("kord", Constants.kord)
        destinationFile = file("src/main/resources/library.properties")
    }

    test {
        useJUnitPlatform()
    }

    copy {
        from(file("templates/readme.md"))
        into(file("."))
        rename { "README.md" }
        expand(
            "kotlin" to Constants.kotlin.replace("-", "--"),
            "kord" to Constants.kord.replace("-", "--"),
            "discordkt" to version.toString().replace("-", "--"),
            "imports" to Docs.generateImports(group.toString(), version.toString())
        )
    }

    register("generateDocs") {
        description = "Generate documentation for discordkt.github.io"
        dependsOn(dokka.dokkaPublications.html)

        copy {
            val docsPath = "../discordkt.github.io/docs/"

            delete(file("$docsPath/api"))
            from(layout.buildDirectory.dir("dokka"))
            into(file("$docsPath/api"))

            file("$docsPath/install.md").writeText(
                Docs.generateImports(group.toString(), version.toString(), true)
            )
        }
    }

    register("dependencySizes") {
        description = "Print dependency sizes for the default configuration"
        doLast {
            val sizes = buildString {
                val configuration = configurations.first()
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

dokka {
    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.dir("dokka"))
    }

    dokkaSourceSets.main {
        documentedVisibilities.set(setOf(VisibilityModifier.Public))

        skipEmptyPackages = true
        reportUndocumented = true

        includes.from("packages.md")
        suppressedFiles.from("src\\main\\kotlin\\me\\jakejmattson\\discordkt\\TypeContainers.kt")
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    dependsOn("writeProperties")
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

val dokkaJar by tasks.registering(Jar::class) {
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
    this.repositories {
        sonatype()
    }
}