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
    implementation("org.eclipse.ditto:ditto-client:2.2.0")
    implementation("org.eclipse.ditto:ditto-model-base:2.0.0-M2")
    implementation("org.eclipse.ditto:ditto-model-base:2.0.0-M2")
    implementation("com.fasterxml.jackson:jackson-base:2.13.0")
    implementation("org.slf4j:slf4j-simple:2.0.0-alpha5")
    implementation("com.andrebreves:java-tuple:1.2.0")
}

application {
    mainClassName = "application.Launcher"
}
