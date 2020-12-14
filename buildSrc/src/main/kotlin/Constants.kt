object Constants {
    const val projectName = "DiscordKt"
    const val projectDescription = "A Discord bot framework for Kotlin."
    const val projectUrl = "https://github.com/JakeJMattson/$projectName/"
    const val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
    const val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
}

object Versions {
    const val kotlin = "1.4.21"
    const val dokka = "1.4.20"
    const val reflections = "0.9.12"
    const val gson = "2.8.6"
    const val slf4j = "2.0.0-alpha1"
    const val log = "2.0.3"
    const val kord = "0.7.0-RC"
    const val emojis = "0.5.0"
}

object Dependencies {
    //Internal Dependencies
    const val reflections = "org.reflections:reflections:${Versions.reflections}"
    const val gson = "com.google.code.gson:gson:${Versions.gson}"
    const val slf4j = "org.slf4j:slf4j-nop:${Versions.slf4j}"
    const val log = "io.github.microutils:kotlin-logging:${Versions.log}"

    //Library Dependencies
    const val kord = "dev.kord:kord-core:${Versions.kord}"
    const val emojis = "dev.kord:kordx.emoji:${Versions.emojis}"
}

object README {
    fun createImportBlock(group: String, version: String, isSnapshot: Boolean) = buildString {
        val gradleTag = "${group}:${Constants.projectName}:${version}"
        val snapshotUrl = "https://oss.sonatype.org/content/repositories/snapshots/"

        appendln("### Gradle (Kotlin)\n```kotlin")

        if (isSnapshot)
            appendln("""
                repositories {
                    mavenCentral()
                    jcenter()
                    maven("$snapshotUrl")
                }
                
            """.trimIndent())

        appendln("""
            dependencies {
                implementation("$gradleTag")
            }
            ```
        """.trimIndent())

        appendln("### Gradle (Groovy)\n```groovy")

        if (isSnapshot)
            appendln("""
                repositories {
                    mavenCentral()
                    jcenter()
                    maven {
                        url '${snapshotUrl}'
                    }
                }
                
            """.trimIndent())

        appendln("""
            dependencies {
                implementation '${gradleTag}'
            }
            ```
            
        """.trimIndent())

        appendln("### Maven\n```xml")

        if (isSnapshot)
            appendln("""
                <repositories>
                    <repository>
                        <id>Sonatype Snapshots</id>
                        <url>$snapshotUrl</url>
                    </repository>
                </repositories>
                
            """.trimIndent())

        appendln("""
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