object Constants {
    const val projectName = "DiscordKt"
    const val projectDescription = "A Discord bot framework for Kotlin."
    const val projectUrl = "https://github.com/discordkt/$projectName/"
    const val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
    const val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"

    const val kotlin = "1.5.30"
    const val dokka = "1.5.30"
    const val kord = "0.8.0-M5"
}

object README {
    fun createImportBlock(group: String, version: String, isSnapshot: Boolean) = buildString {
        val gradleTag = "${group}:${Constants.projectName}:${version}"
        val snapshotUrl = "https://oss.sonatype.org/content/repositories/snapshots/"

        appendLine("### Gradle (Kotlin)\n```kotlin")

        if (isSnapshot)
            appendLine("""
                repositories {
                    mavenCentral()
                    maven("$snapshotUrl")
                }
                
            """.trimIndent())

        appendLine("""
            dependencies {
                implementation("$gradleTag")
            }
            ```
        """.trimIndent())

        appendLine("### Gradle (Groovy)\n```groovy")

        if (isSnapshot)
            appendLine("""
                repositories {
                    mavenCentral()
                    maven {
                        url '${snapshotUrl}'
                    }
                }
                
            """.trimIndent())

        appendLine("""
            dependencies {
                implementation '${gradleTag}'
            }
            ```
            
        """.trimIndent())

        appendLine("### Maven\n```xml")

        if (isSnapshot)
            appendLine("""
                <repositories>
                    <repository>
                        <id>Sonatype Snapshots</id>
                        <url>$snapshotUrl</url>
                    </repository>
                </repositories>
                
            """.trimIndent())

        appendLine("""
            <dependencies>
                <dependency>
                    <groupId>${group}</groupId>
                    <artifactId>${Constants.projectName}</artifactId>
                    <version>${version}</version>
                </dependency>
            </dependencies>
            ```
        """.trimIndent())
    }
}