![KUtils Server](https://discordapp.com/api/guilds/453208597082406912/widget.png?style=banner2)

The documentation for this project is located on the [GitLab Wiki](https://gitlab.com/JakeJMattson/KUtils/wikis/home).

#### Add with Maven
```xml
<project>
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
    
    <dependencies>
        <dependency>
            <groupId>com.gitlab.jakejmattson</groupId>
            <artifactId>KUtils</artifactId>
            <version>0.17.0</version>
        </dependency>
    </dependencies>
</project>
```

#### Add with Gradle
```groovy
repositories {
    maven { url 'https://jitpack.io' }
    jcenter()
}

dependencies {
    implementation "com.gitlab.jakejmattson:KUtils:0.17.0"
}
```
