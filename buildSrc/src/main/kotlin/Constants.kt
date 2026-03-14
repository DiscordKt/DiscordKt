object Constants {
    const val projectName = "DiscordKt"
    const val projectDescription = "A Discord bot framework for Kotlin."
    const val projectUrl = "https://github.com/discordkt/$projectName/"

    const val kotlin = "2.3.10"
    const val dokka = "2.1.0"
    const val kord = "0.18.1"
}

object Docs {
    fun generateImports(group: String, version: String, isDocs: Boolean = false) = buildString {
        val gradleTag = "${group}:${Constants.projectName}:${version}"

        createGradleKts(gradleTag, isDocs)
        createGradleGroovy(gradleTag, isDocs)
        createMaven(group, version, isDocs)
    }

    private fun StringBuilder.createGradleKts(gradleTag: String, isDocs: Boolean) = apply {
        appendLine(if (isDocs) "=== \"build.gradle.kts\"" else "### Gradle (Kotlin)")

        val block = StringBuilder()

        block.appendLine("```kotlin")

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

    private fun StringBuilder.createGradleGroovy(gradleTag: String, isDocs: Boolean) = apply {
        appendLine(if (isDocs) "=== \"build.gradle\"" else "### Gradle (Groovy)")

        val block = StringBuilder()

        block.appendLine("```groovy")

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

    private fun StringBuilder.createMaven(group: String, version: String, isDocs: Boolean) = apply {
        appendLine(if (isDocs) "=== \"pom.xml\"" else "### Maven")

        val block = StringBuilder()

        block.appendLine("```xml")

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