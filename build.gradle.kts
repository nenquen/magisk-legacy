import com.android.build.gradle.BaseExtension
import com.topjohnwu.magisk.build.Config

plugins {
    id("MagiskPlugin")
    kotlin("android") version "1.7.20" apply false
    kotlin("kapt") version "1.7.20" apply false
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    val vNav: String by project

    dependencies {
        classpath("com.android.tools.build:gradle:7.1.3")
        classpath(kotlin("gradle-plugin", version = "1.7.20"))
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:${vNav}")
    }
}

val vNav: String by project
extra["vNav"] = vNav

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

fun Project.android(configuration: BaseExtension.() -> Unit) =
    extensions.getByName<BaseExtension>("android").configuration()

subprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }

    afterEvaluate {
        if (plugins.hasPlugin("com.android.library") ||
            plugins.hasPlugin("com.android.application")) {
            android {
                compileSdkVersion(33)
                buildToolsVersion = "30.0.3"
                val magiskNdk = file("${sdkDirectory.absolutePath}/ndk/magisk")
                if (magiskNdk.exists()) {
                    ndkPath = magiskNdk.absolutePath
                }

                defaultConfig {
                    if (minSdkVersion == null)
                        minSdkVersion(16)
                    targetSdkVersion(33)
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_1_8
                    targetCompatibility = JavaVersion.VERSION_1_8
                }
            }
        }

        if (plugins.hasPlugin("java")) {
            tasks.withType<JavaCompile> {
                // If building with JDK 9+, we need additional flags to generate compatible bytecode
                if (JavaVersion.current() > JavaVersion.VERSION_1_8) {
                    options.compilerArgs.addAll(listOf("--release", "8"))
                }
            }
        }

        if (name == "app") {
            android {
                signingConfigs {
                    create("config") {
                        Config["keyStore"]?.also {
                            storeFile = rootProject.file(it)
                            storePassword = Config["keyStorePass"]
                            keyAlias = Config["keyAlias"]
                            keyPassword = Config["keyPass"]
                        }
                    }
                }

                buildTypes {
                    signingConfigs.getByName("config").also {
                        getByName("debug") {
                            signingConfig = if (it.storeFile?.exists() == true) it
                            else signingConfigs.getByName("debug")
                        }
                        getByName("release") {
                            signingConfig = if (it.storeFile?.exists() == true) it
                            else signingConfigs.getByName("debug")
                        }
                    }
                }

                lintOptions {
                    disable("MissingTranslation")
                }
            }
        }
    }
}
