plugins {
    `kotlin-dsl`
}
repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        register("MagiskPlugin") {
            id = "MagiskPlugin"
            implementationClass = "com.topjohnwu.magisk.build.MagiskPlugin"
        }
    }
}

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.10.0.202012080955-r")
}
