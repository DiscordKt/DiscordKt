object Constants {
    const val projectName = "KUtils"
    const val projectDescription = "A Discord bot framework for Kotlin."
    const val projectUrl = "https://gitlab.com/JakeJMattson/$projectName/"
    const val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
    const val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
}

object Versions {
    const val kotlin = "1.3.72"
    const val reflections = "0.9.12"
    const val gson = "2.8.6"
    const val guava = "29.0-jre"
    const val commons = "1.8"
    const val slf4j = "2.0.0-alpha1"
    const val jda = "4.2.0_172"

    //Test Dependencies
    const val mockk = "1.10.0"
    const val junit = "5.7.0-M1"
}

object Dependencies {
    //Internal Dependencies
    const val reflections = "org.reflections:reflections:${Versions.reflections}"
    const val commons = "org.apache.commons:commons-text:${Versions.commons}"
    const val slf4j = "org.slf4j:slf4j-simple:${Versions.slf4j}"

    //Library Dependencies
    const val jda = "net.dv8tion:JDA:${Versions.jda}"
    const val guava = "com.google.guava:guava:${Versions.guava}"
    const val gson = "com.google.code.gson:gson:${Versions.gson}"

    //Scripting Engine
    const val kotlinCompiler = "org.jetbrains.kotlin:kotlin-compiler-embeddable:${Versions.kotlin}"
    const val script_compiler = "org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:${Versions.kotlin}"
    const val script_runtime = "org.jetbrains.kotlin:kotlin-script-runtime:${Versions.kotlin}"
    const val script_util = "org.jetbrains.kotlin:kotlin-script-util:${Versions.kotlin}"

    //Test Dependencies
    const val mockk = "io.mockk:mockk:${Versions.mockk}"
    const val junit_api = "org.junit.jupiter:junit-jupiter-api:${Versions.junit}"
    const val junit_engine = "org.junit.jupiter:junit-jupiter-engine:${Versions.junit}"
}