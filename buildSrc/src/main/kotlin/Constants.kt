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
    const val kord = "0.6.3"
    const val emojis = "0.3.0"
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