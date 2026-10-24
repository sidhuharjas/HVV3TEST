plugins {
    id("com.android.library") version "8.7.0" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

// `./gradlew deployLocal` publishes both modules to ~/.m2 (add mavenLocal() in the robot project)
tasks.register("deployLocal") {
    dependsOn(subprojects.map { ":${it.name}:publishToMavenLocal" })
}
