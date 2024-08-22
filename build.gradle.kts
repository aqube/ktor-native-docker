val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("multiplatform") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
    id("com.gradleup.shadow") version "8.3.0"
}

group = "de.aqube.experiments"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

kotlin {

    // Native target
    val hostOs = System.getProperty("os.name")
    val arch = System.getProperty("os.arch")
    val nativeTarget = when {
        hostOs == "Mac OS X" && arch == "x86_64" -> macosX64("platform")
        hostOs == "Mac OS X" && arch == "aarch64" -> macosArm64("platform")
        hostOs == "Linux" && (arch == "x86_64" || arch == "amd64") -> linuxX64("platform")
        hostOs == "Linux" && arch == "aarch64" -> linuxArm64("platform")
        // Other supported targets are listed here: https://ktor.io/docs/native-server.html#targets
        else -> throw GradleException("Host OS is not supported in Kotlin/Native. $hostOs/$arch")
    }
    nativeTarget.apply {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }


    // JVM target
    jvmToolchain(21)
    jvm {
        compilations {
            application {
                mainClass.set("MainKt")
            }
            val main = getByName("main")
            tasks {
                shadowJar {
                    from(main.output)
                    configurations = listOf(main.compileDependencyFiles)
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation("io.ktor:ktor-server-core:$ktor_version")

            // ContentNegotiation (JSON)
            implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

            // Resource based routing
            implementation("io.ktor:ktor-server-resources:$ktor_version")

            // HTML Templating
            implementation("io.ktor:ktor-server-html-builder:$ktor_version")

            // Hashing
            implementation(kotlincrypto.hash.sha3)
            implementation(kotlincrypto.sponges.keccak)
            implementation(kotlincrypto.endians.endians)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("io.ktor:ktor-server-test-host:$ktor_version")
            implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")

        }

        nativeMain.dependencies {
            implementation("io.ktor:ktor-server-cio:$ktor_version")
        }

        nativeTest.dependencies {

        }

        jvmMain.dependencies {
            implementation("io.ktor:ktor-server-cio:$ktor_version")
            // Could use an alternative server implementation for the JVM target here
            // implementation("io.ktor:ktor-server-jetty:$ktor_version")

            implementation("ch.qos.logback:logback-classic:$logback_version")
        }

        jvmTest.dependencies {

        }
    }
}
