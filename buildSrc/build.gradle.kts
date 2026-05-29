plugins {
    `kotlin-dsl`
}
repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        register("RegiskPlugin") {
            id = "RegiskPlugin"
            implementationClass = "com.regisk.legacy.build.RegiskPlugin"
        }
    }
}

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.10.0.202012080955-r")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
