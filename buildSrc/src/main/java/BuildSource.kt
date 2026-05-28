package com.topjohnwu.magisk.build

import org.eclipse.jgit.internal.storage.file.FileRepository
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.provideDelegate
import java.io.File
import java.util.*

private val props = Properties()
private var commitHash = ""

object Config {
    operator fun get(key: String): String? {
        val v = props[key] as? String ?: return null
        return if (v.isBlank()) null else v
    }

    fun contains(key: String) = get(key) != null

    val version: String get() = get("version") ?: commitHash
    val versionCode: Int get() = get("magisk.versionCode")?.toInt() ?: 0
    val stubVersion: String get() = get("magisk.stubVersion") ?: ""
}

class MagiskPlugin : Plugin<Project> {
    override fun apply(project: Project) = project.applyPlugin()

    private fun Project.applyPlugin() {
        props.clear()
        rootProject.file("gradle.properties").inputStream().use { props.load(it) }
        val configPath = findProperty("configPath") as String?
        val config = configPath?.let { File(it) } ?: rootProject.file("config.prop")
        if (config.exists())
            config.inputStream().use { props.load(it) }

        val gitDir = rootProject.file(".git")
        if (gitDir.exists()) {
            val repo = FileRepository(gitDir)
            repo.refDatabase.exactRef("HEAD")?.let {
                val refId = it.objectId
                commitHash = repo.newObjectReader().abbreviate(refId, 8).name()
            }
        }
    }
}
