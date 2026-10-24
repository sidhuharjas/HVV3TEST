plugins {
    id("com.android.library")
    id("maven-publish")
}

val ftcVersion: String by project

android {
    namespace = "org.hivevisionftc.core"
    compileSdk = 35
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    publishing { singleVariant("release") }
}

dependencies {
    compileOnly("org.firstinspires.ftc:RobotCore:$ftcVersion")
    compileOnly("org.firstinspires.ftc:Hardware:$ftcVersion")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = project.group.toString()
                artifactId = "core"
                version = project.version.toString()
            }
        }
    }
}
