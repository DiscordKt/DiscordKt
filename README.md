<p align="center">
  <a href="https://discord.gg/REZVVjA">
    <img src="https://img.shields.io/discord/453208597082406912?logo=discord" alt="chat on Discord">
  </a>
</p>

The documentation for this project is located on the [GitLab Wiki](https://gitlab.com/Aberrantfox/KUtils/wikis/home).

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
            <groupId>com.gitlab.aberrantfox</groupId>
            <artifactId>KUtils</artifactId>
            <version>0.11.1</version>
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
    implementation "com.gitlab.aberrantfox:KUtils:0.11.1"
}
```
