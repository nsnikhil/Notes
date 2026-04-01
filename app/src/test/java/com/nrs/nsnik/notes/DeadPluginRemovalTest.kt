package com.nrs.nsnik.notes

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * ST-003: Verifies dead plugins, Crashlytics/Fabric, and Hugo are fully removed from source.
 */
class DeadPluginRemovalTest {

    private val projectRoot: File by lazy {
        var dir = File(System.getProperty("user.dir"))
        while (dir.parentFile != null && !File(dir, "settings.gradle").exists()) {
            dir = dir.parentFile
        }
        dir
    }

    private val appSrcDir: File get() = File(projectRoot, "app/src")
    private val appBuildGradle: String by lazy { File(projectRoot, "app/build.gradle").readText() }
    private val proguardRules: String by lazy { File(projectRoot, "app/proguard-rules.pro").readText() }

    private fun allSourceFiles(): List<File> =
        appSrcDir.walkTopDown().filter { it.isFile && (it.extension == "kt" || it.extension == "java") }.toList()

    private fun mainSourceFiles(): List<File> =
        File(appSrcDir, "main").walkTopDown().filter { it.isFile && (it.extension == "kt" || it.extension == "java") }.toList()

    private fun sourceContent(): String = mainSourceFiles().joinToString("\n") { it.readText() }

    // --- SC1: Zero hugo.weaving / @DebugLog references ---

    @Test
    fun noHugoWeaving() {
        val matches = mainSourceFiles().filter { it.readText().contains("hugo.weaving") }
        assertTrue("Found hugo.weaving in: ${matches.map { it.path }}", matches.isEmpty())
    }

    @Test
    fun noDebugLogAnnotation() {
        val matches = mainSourceFiles().filter { it.readText().contains("@DebugLog") }
        assertTrue("Found @DebugLog in: ${matches.map { it.path }}", matches.isEmpty())
    }

    // --- SC2: Zero Fabric/Crashlytics/BlockCanary/Stetho/Rollbar references ---

    @Test
    fun noFabricInSource() {
        val matches = mainSourceFiles().filter { it.readText().contains("Fabric") }
        assertTrue("Found Fabric in: ${matches.map { it.path }}", matches.isEmpty())
    }

    @Test
    fun noCrashlyticsInSource() {
        val matches = mainSourceFiles().filter { it.readText().contains("com.crashlytics") }
        assertTrue("Found com.crashlytics in: ${matches.map { it.path }}", matches.isEmpty())
    }

    @Test
    fun noBlockCanaryInSource() {
        val matches = mainSourceFiles().filter { val t = it.readText(); t.contains("BlockCanary") || t.contains("blockcanary") }
        assertTrue("Found BlockCanary in: ${matches.map { it.path }}", matches.isEmpty())
    }

    @Test
    fun noStethoInSource() {
        val matches = mainSourceFiles().filter { val t = it.readText(); t.contains("Stetho") || t.contains("stetho") }
        assertTrue("Found Stetho in: ${matches.map { it.path }}", matches.isEmpty())
    }

    @Test
    fun noRollbarInSource() {
        val matches = mainSourceFiles().filter { val t = it.readText(); t.contains("Rollbar") || t.contains("rollbar") }
        assertTrue("Found Rollbar in: ${matches.map { it.path }}", matches.isEmpty())
    }

    // --- SC3: Zero getRefWatcher references ---

    @Test
    fun noGetRefWatcher() {
        val matches = mainSourceFiles().filter { it.readText().contains("getRefWatcher") }
        assertTrue("Found getRefWatcher in: ${matches.map { it.path }}", matches.isEmpty())
    }

    // --- SC4: LeakCanary v2 debugImplementation only ---

    @Test
    fun leakCanaryIsDebugImplementationOnly() {
        val lines = appBuildGradle.lines().filter { it.contains("leakcanary") }
        assertTrue("LeakCanary must be declared", lines.isNotEmpty())
        lines.forEach { line ->
            assertTrue("LeakCanary must use debugImplementation, found: $line", line.trimStart().startsWith("debugImplementation"))
        }
    }

    @Test
    fun noLeakCanaryNoOp() {
        assertFalse("build.gradle must not contain leakcanary-android-no-op", appBuildGradle.contains("leakcanary-android-no-op"))
    }

    @Test
    fun noManualLeakCanaryInit() {
        val content = sourceContent()
        assertFalse("Source must not contain LeakCanary.install", content.contains("LeakCanary.install"))
        assertFalse("Source must not contain RefWatcher", content.contains("RefWatcher"))
    }

    // --- SC5: Proguard clean of ButterKnife/Dart ---

    @Test
    fun noButterKnifeInProguard() {
        assertFalse("proguard-rules.pro must not reference butterknife", proguardRules.lowercase().contains("butterknife"))
    }

    @Test
    fun noDartInProguard() {
        assertFalse("proguard-rules.pro must not reference dart (henson/dart)", proguardRules.lowercase().contains("dart"))
    }

    // --- SC6: kotlinx.serialization keep rules present ---

    @Test
    fun kotlinxSerializationRulesPresent() {
        assertTrue("proguard-rules.pro must contain kotlinx.serialization rules", proguardRules.contains("kotlinx.serialization"))
        assertTrue("proguard-rules.pro must keep serializer", proguardRules.contains("serializer"))
    }

    // --- SC7: AppBlockCanaryContext.kt deleted ---

    @Test
    fun appBlockCanaryContextDeleted() {
        val found = projectRoot.walkTopDown().any { it.name == "AppBlockCanaryContext.kt" }
        assertFalse("AppBlockCanaryContext.kt must not exist anywhere in the project", found)
    }
}
