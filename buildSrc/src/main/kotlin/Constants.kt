object Constants {
    const val projectName = "DiscordKt"
    const val projectDescription = "A Discord bot framework for Kotlin."
    const val projectUrl = "https://github.com/JakeJMattson/$projectName/"
    const val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
    const val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
}

object Versions {
    const val kotlin = "1.4.10"
    const val coroutines = "1.3.9"
    const val reflections = "0.9.12"
    const val gson = "2.8.6"
    const val slf4j = "2.0.0-alpha1"
    const val kord = "0.6.6"
    const val emojis = "0.4.0"
}

object Dependencies {
    //Internal Dependencies
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val reflections = "org.reflections:reflections:${Versions.reflections}"
    const val gson = "com.google.code.gson:gson:${Versions.gson}"
    const val slf4j = "org.slf4j:slf4j-nop:${Versions.slf4j}"

    //Library Dependencies
    const val kord = "com.gitlab.kordlib.kord:kord-core:${Versions.kord}"
    const val emojis = "com.gitlab.kordlib:kordx.emoji:${Versions.emojis}"
}

object README {
    private val kotlin = Versions.kotlin
    private val kord = Versions.kord
    private val project = Constants.projectName

    val badges =
        """
            <p align="center">
                <a href="https://discord.gg/REZVVjA">
                    <img alt="Discord Banner" src="https://discordapp.com/api/guilds/453208597082406912/widget.png?style=banner2"/>
                </a>
                <br/>
                <a href="https://kotlinlang.org/">
                    <img src="https://img.shields.io/badge/Kotlin-${kotlin}-blue.svg?logo=Kotlin" alt="Kotlin $kotlin">
                </a>
                <img alt="Kord $kord" src="https://img.shields.io/badge/Kord-${kord}-orange.svg?logo=data:image/png;base64,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">
                <a href="https://GitHub.com/JakeJMattson/DiscordKt/releases/">
                    <img src="https://img.shields.io/github/release/JakeJMattson/DiscordKt.svg?color=green?label=Release&logo=Github" alt="Release">
                </a>
                <br>
                <a href="https://discordapp.com/users/254786431656919051/">
                    <img src="https://img.shields.io/badge/Personal-JakeyWakey%231569-%2300BFFF.svg?logo=discord" alt="Discord JakeyWakey#1569">
                </a>
            </p>
        """.trimIndent()

    fun createImport(group: String, version: String) =
        """
            #### Maven
            ```xml
            <repository>
                <id>Sonatype Snapshots</id>
                <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
            </repository>

            <dependency>
                <groupId>${group}</groupId>
                <artifactId>${project}</artifactId>
                <version>${version}</version>
            </dependency>
            ```

            #### Gradle
            ```groovy
            maven {
                url 'https://oss.sonatype.org/content/repositories/snapshots/'
            }

            dependencies {
                implementation '${group}:${project}:${version}'
            }
            ```
            ```kotlin
            maven {
                url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            }

            dependencies {
                implementation("${group}:${project}:${version}")
            }
            ```
        """.trimIndent()
}