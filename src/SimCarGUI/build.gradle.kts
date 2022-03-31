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
// List of JavaFX modules you need. Comment out things you are not using.
val javaFXModules = listOf(
    "base",
    "controls",
    "fxml",
    "swing",
    "graphics"
)
val supportedPlatforms = listOf("linux")

dependencies {
    for (platform in supportedPlatforms) {
        for (module in javaFXModules) {
            implementation("org.openjfx:javafx-$module:17:$platform")
        }
    }
}

tasks.withType<Test> {
    // Enables JUnit 5 Jupiter module
    useJUnitPlatform()
}

application {
    mainClassName = "application.Launcher"
}
