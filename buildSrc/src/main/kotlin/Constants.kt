object Constants {
    const val projectName = "DiscordKt"
    const val projectDescription = "A Discord bot framework for Kotlin."
    const val projectUrl = "https://github.com/discordkt/$projectName/"
    const val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
    const val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
}

object Versions {
    const val kotlin = "1.4.32"
    const val dokka = "1.4.30"
    const val reflections = "0.9.12"
    const val gson = "2.8.6"
    const val slf4j = "2.0.0-alpha1"
    const val kord = "0.7.x-SNAPSHOT"
    const val emojis = "0.5.0-SNAPSHOT"
}

object Dependencies {
    //Internal Dependencies
    const val reflections = "org.reflections:reflections:${Versions.reflections}"
    const val gson = "com.google.code.gson:gson:${Versions.gson}"
    const val slf4j = "org.slf4j:slf4j-nop:${Versions.slf4j}"

    //Library Dependencies
    const val kord = "dev.kord:kord-core:${Versions.kord}"
    const val emojis = "dev.kord.x:emoji:${Versions.emojis}"
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