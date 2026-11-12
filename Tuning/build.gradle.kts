import io.clroot.gradle.bun.task.BunTask

plugins {
    id("com.android.library") version "8.7.3"
    id("io.clroot.gradle-bun") version "0.1.0"
    kotlin("android") version "2.3.21"
    `maven-publish`
}

// JitPack coordinates: the group is mechanically derived from the repo
// (com.github.<user>.<repo>), the version from the tag. See the FTC library
// publishing guide. The Java package / Android namespace
// (com.pedropathing.tuning) is unchanged by this.
group = property("GROUP") as String
version = property("VERSION_NAME") as String

repositories {
    mavenCentral()
    maven("https://repo.dairy.foundation/releases/")
    google()
}

android {
    namespace = "com.pedropathing.tuning"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

// Pin Kotlin's JVM target to 1.8 to match compileOptions above.
// Without this, Kotlin 2.3 defaults the JVM target to the JDK running
// Gradle (17 on JitPack), and AGP fails the build with "Inconsistent
// JVM Target Compatibility" (Java 1.8 vs Kotlin 17). Note: the old
// `kotlinOptions { jvmTarget = "1.8" }` form is an ERROR in Kotlin
// 2.3 — the target must be set through the compilerOptions DSL.
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
    }
}

dependencies {
    compileOnly("org.firstinspires.ftc:RobotCore:12.0.0")
    compileOnly("org.firstinspires.ftc:FtcCommon:12.0.0")
    compileOnly("org.firstinspires.ftc:RobotServer:12.0.0")
    implementation("com.pedropathing:revhub:3.0.2")
    implementation("dev.frozenmilk.sinister:Sloth:0.3.2")
    implementation("com.aventrix.jnanoid:jnanoid:2.0.0")
    implementation("org.nanohttpd:nanohttpd-websocket:2.3.1") {
        exclude(module = "nanohttpd")
    }
}

// JitPack publishing (FTC library publishing guide pattern):
// JitPack runs `publishToMavenLocal` on its machines and serves the result
// itself, so there is deliberately no `publishing { repositories { ... } }`
// block here. Coordinate: com.github.shlok-k720.AutoTune:tuning:1.0.3.
publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = project.group.toString()
            artifactId = "tuning"
            version = project.version.toString()
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

val frontendDir = rootDir.parentFile.resolve("web-tuner")


bun {
    version = "1.4.0"
    workingDir = frontendDir
}

tasks.register<BunTask>("buildFrontend") {
    group = "build"
    description = "Builds the React frontend"

    dependsOn("bunInstall")

    args("run", "build")

    inputs.files(
        fileTree(frontendDir).exclude("node_modules/**")
    )

    outputs.dir(layout.projectDirectory.dir("src/main/assets/pedro"))
}

tasks.named("preBuild") {
    dependsOn("buildFrontend")
}
