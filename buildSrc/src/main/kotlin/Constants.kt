object Constants {
    const val projectName = "DiscordKt"
    const val projectDescription = "A Discord bot framework for Kotlin."
    const val projectUrl = "https://github.com/JakeJMattson/$projectName/"
    const val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
    const val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
}

object Versions {
    const val kotlin = "1.4.0"
    const val coroutines = "1.3.9"
    const val reflections = "0.9.12"
    const val gson = "2.8.6"
    const val slf4j = "1.7.30"
    const val kord = "0.5.11"
    const val emojis = "0.2.0"

    //Test Dependencies
    const val mockk = "1.10.0"
    const val junit = "5.7.0-M1"
}

object Dependencies {
    //Internal Dependencies
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val reflections = "org.reflections:reflections:${Versions.reflections}"
    const val slf4j = "org.slf4j:slf4j-nop:${Versions.slf4j}"

    //Library Dependencies
    const val kord = "com.gitlab.kordlib.kord:kord-core:${Versions.kord}"
    const val emojis = "com.gitlab.kordlib:kordx.emoji:${Versions.emojis}"
    const val gson = "com.google.code.gson:gson:${Versions.gson}"

    //Test Dependencies
    const val mockk = "io.mockk:mockk:${Versions.mockk}"
    const val junit_api = "org.junit.jupiter:junit-jupiter-api:${Versions.junit}"
    const val junit_engine = "org.junit.jupiter:junit-jupiter-engine:${Versions.junit}"
}