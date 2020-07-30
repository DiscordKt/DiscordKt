<a href="https://discord.gg/REZVVjA">
    <img alt="KUtils Banner" src="https://discordapp.com/api/guilds/453208597082406912/widget.png?style=banner2"/>
</a>

The documentation for this project is currently WIP. 
The best source for learning the framework is by joining the Discord, or checking existing bots.

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