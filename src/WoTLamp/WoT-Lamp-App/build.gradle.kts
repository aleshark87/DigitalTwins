// Declaration of the Gradle extension to use
plugins {
    java
    application
    /*
     * Adds tasks to export a runnable jar.
     * In order to create it, launch the "shadowJar" task.
     * The runnable jar will be found in build/libs/projectname-all.jar
     */
    id("com.github.johnrengelman.shadow") version "5.2.0"
}
repositories {
    jcenter() // Contains the whole Maven Central + other stuff
}

dependencies {
// https://mvnrepository.com/artifact/org.json/json
implementation("org.json:json:20220320")
implementation("org.javatuples:javatuples:1.2")
implementation("com.damnhandy:handy-uri-templates:2.1.8")

}

application {
    mainClassName = "application.Launcher"
}
