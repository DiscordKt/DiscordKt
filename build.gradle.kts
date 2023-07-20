group = "me.jakejmattson"
version = "0.24.0-SNAPSHOT"
val projectGroup = group.toString()

plugins {
    //Core
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)

    //Publishing
    signing
    `maven-publish`
    alias(libs.plugins.publish)

    //Misc
    alias(libs.plugins.versions)
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.kord.core)
    api(libs.kord.emoji)
    api(libs.slf4j)

    implementation(libs.reflections)
    implementation(libs.kotlinx.serialization)

    testImplementation(libs.kotest)
    testImplementation(libs.mockk)
}

tasks {
    kotlin {
        explicitApi()
    }

    java {
        targetCompatibility = JavaVersion.VERSION_11
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        }
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }

    test {
        useJUnitPlatform()
    }

    build {
        finalizedBy("writeProperties")
    }

    register<WriteProperties>("writeProperties") {
        outputs.upToDateWhen { false }
        property("version", project.version.toString())
        property("kotlin", libs.versions.kotlin.get())
        property("kord", libs.versions.kord.core.get())
        setOutputFile("src/main/resources/library.properties")
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
            "kotlin" to libs.versions.kotlin.get().replace("-", "--"),
            "kord" to libs.versions.kord.core.get().replace("-", "--"),
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
                val configuration = configurations["runtimeClasspath"]
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
    this.repositories {
        sonatype()
    }
}