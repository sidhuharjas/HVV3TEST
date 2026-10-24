plugins {
    id("com.android.library")
    id("maven-publish")
}

val pedroVersion: String by project
val marrowVersion: String by project

android {
    namespace = "org.hivevisionftc.weaver"
    compileSdk = 35
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    publishing { singleVariant("release") }
}

dependencies {
    api(project(":core"))
    api("com.skeletonarmyftc.marrow:core:$marrowVersion")
    compileOnly("com.pedropathing:core:$pedroVersion")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = project.group.toString()
                artifactId = "weaver"
                version = project.version.toString()
            }
        }
    }
}
