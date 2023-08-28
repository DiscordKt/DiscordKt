object Constants {
    const val projectName = "DiscordKt"
    const val projectDescription = "A Discord bot framework for Kotlin."
    const val projectUrl = "https://github.com/discordkt/$projectName/"

    const val kotlin = "1.9.10"
    const val dokka = "1.8.20"
    const val kord = "0.8.0-M16"
}

object Docs {
    fun generateImports(group: String, version: String, isDocs: Boolean = false) = buildString {
        val gradleTag = "${group}:${Constants.projectName}:${version}"
        val snapshotUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
        val isSnapshot = version.endsWith("SNAPSHOT")

        createGradleKts(snapshotUrl, gradleTag, isSnapshot, isDocs)
        createGradleGroovy(snapshotUrl, gradleTag, isSnapshot, isDocs)
        createMaven(snapshotUrl, group, version, isSnapshot, isDocs)
    }

    private fun StringBuilder.createGradleKts(snapshotUrl: String, gradleTag: String, isSnapshot: Boolean, isDocs: Boolean) = apply {
        appendLine(if (isDocs) "=== \"build.gradle.kts\"" else "### Gradle (Kotlin)")

        val block = StringBuilder()

        block.appendLine("```kotlin")

        if (isSnapshot)
            block.appendLine("""
                repositories {
                    mavenCentral()
                    maven("$snapshotUrl")
                }
                
            """.trimIndent())

        block.appendLine("""
            dependencies {
                implementation("$gradleTag")
            }
            ```
        """.trimIndent())

        appendLine(
            if (isDocs)
                block.split("\n").joinToString("\n") { "    $it" }
            else
                block.toString()
        )
    }

    private fun StringBuilder.createGradleGroovy(snapshotUrl: String, gradleTag: String, isSnapshot: Boolean, isDocs: Boolean) = apply {
        appendLine(if (isDocs) "=== \"build.gradle\"" else "### Gradle (Groovy)")

        val block = StringBuilder()

        block.appendLine("```groovy")

        if (isSnapshot)
            block.appendLine("""
                repositories {
                    mavenCentral()
                    maven {
                        url '${snapshotUrl}'
                    }
                }
                
            """.trimIndent())

        block.appendLine("""
            dependencies {
                implementation '${gradleTag}'
            }
            ```
        """.trimIndent())

        appendLine(
            if (isDocs)
                block.split("\n").joinToString("\n") { "    $it" }
            else
                block.toString()
        )
    }

    private fun StringBuilder.createMaven(snapshotUrl: String, group: String, version: String, isSnapshot: Boolean, isDocs: Boolean) = apply {
        appendLine(if (isDocs) "=== \"pom.xml\"" else "### Maven")

        val block = StringBuilder()

        block.appendLine("```xml")

        if (isSnapshot)
            block.appendLine("""
                <repositories>
                    <repository>
                        <id>Sonatype Snapshots</id>
                        <url>$snapshotUrl</url>
                    </repository>
                </repositories>
                
            """.trimIndent())

        block.appendLine("""
            <dependencies>
                <dependency>
                    <groupId>${group}</groupId>
                    <artifactId>${Constants.projectName}</artifactId>
                    <version>${version}</version>
                </dependency>
            </dependencies>
            ```
        """.trimIndent())

        append(
            if (isDocs)
                block.split("\n").joinToString("\n") { "    $it" }
            else
                block.toString()
        )
    }
}