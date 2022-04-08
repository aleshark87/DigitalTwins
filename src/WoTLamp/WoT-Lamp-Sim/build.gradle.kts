// Declaration of the Gradle extension to use
plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "5.2.0"
}
repositories {
    jcenter() // Contains the whole Maven Central + other stuff
}

val javaFXModules = listOf(
	"base",
	"controls",
	"fxml",
	"swing",
	"graphics"
)

val platform = "linux"

dependencies {
	for(module in javaFXModules) {
		implementation("org.openjfx:javafx-$module:17:$platform")
	}
	implementation("org.json:json:20220320")
	implementation("org.javatuples:javatuples:1.2")
	implementation("com.damnhandy:handy-uri-templates:2.1.8")
	implementation("org.eclipse.ditto:ditto-client:2.4.0-M1")
    implementation("com.fasterxml.jackson:jackson-base:2.13.0")
    implementation("org.slf4j:slf4j-simple:2.0.0-alpha5")
}

application {
    mainClassName = "application.Launcher"
}
