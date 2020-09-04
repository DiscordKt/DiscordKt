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
    const val reflections = "0.11.7"
    const val gson = "2.8.6"
    const val kord = "0.6.1"
    const val emojis = "0.2.0"
}

object Dependencies {
    //Internal Dependencies
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val reflections = "net.oneandone.reflections8:reflections8:${Versions.reflections}"

    //Library Dependencies
    const val kord = "com.gitlab.kordlib.kord:kord-core:${Versions.kord}"
    const val emojis = "com.gitlab.kordlib:kordx.emoji:${Versions.emojis}"
    const val gson = "com.google.code.gson:gson:${Versions.gson}"
}