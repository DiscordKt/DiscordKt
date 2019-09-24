<p align="center">
  <a href="https://discord.gg/REZVVjA">
    <img src="https://img.shields.io/discord/453208597082406912?logo=discord" alt="chat on Discord">
  </a>
</p>

##### Purpose
The purpose of this is to provide a nice kotlin wrapper over JDA and to add a bunch of extension functions for utility 
purposes/cleaner code.

#### Index

1. Starting up the bot and basic configuration
2. Creating commands
3. Command arguments
4. Listening to events
5. Creating and using conversations
6. Creating and using a Service
7. Creating and using auto-injected data objects

#### Add to your project with Maven
Under the dependencies tag, add

```xml
<dependency>
    <groupId>com.gitlab.aberrantfox</groupId>
    <artifactId>Kutils</artifactId>
    <version>0.10.1</version>
</dependency>
```

Under the repositories tag, add

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

#### Add to your project with Gradle
```groovy
repositories {
    //...
    maven { url 'https://jitpack.io' }
    jcenter()
    //...
}

dependencies {
    //...
    implementation "com.gitlab.aberrantfox:Kutils:0.10.1"
    //...
}
```
