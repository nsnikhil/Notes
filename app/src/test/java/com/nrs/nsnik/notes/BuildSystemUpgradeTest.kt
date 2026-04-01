package com.nrs.nsnik.notes

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Verifies that the build system upgrade (ST-002) was applied correctly
 * by checking build configuration files for expected values and
 * absence of removed/deprecated references.
 */
class BuildSystemUpgradeTest {

    private val projectRoot: File by lazy {
        // Navigate from test class location up to project root
        var dir = File(System.getProperty("user.dir"))
        while (dir.parentFile != null && !File(dir, "settings.gradle").exists()) {
            dir = dir.parentFile
        }
        dir
    }

    private val wrapperProps: String by lazy {
        File(projectRoot, "gradle/wrapper/gradle-wrapper.properties").readText()
    }

    private val rootBuildGradle: String by lazy {
        File(projectRoot, "build.gradle").readText()
    }

    private val appBuildGradle: String by lazy {
        File(projectRoot, "app/build.gradle").readText()
    }

    private val settingsGradle: String by lazy {
        File(projectRoot, "settings.gradle").readText()
    }

    private val gradleProperties: String by lazy {
        File(projectRoot, "gradle.properties").readText()
    }

    // --- Gradle Wrapper Version ---

    @Test
    fun gradleWrapperPointsTo8_9() {
        assertTrue(
            "gradle-wrapper.properties must reference gradle-8.9",
            wrapperProps.contains("gradle-8.9")
        )
    }

    // --- AGP and Kotlin Versions ---

    @Test
    fun agpVersionIs8_7_3() {
        assertTrue(
            "Root build.gradle must declare AGP 8.7.3",
            rootBuildGradle.contains("com.android.tools.build:gradle:8.7.3")
        )
    }

    @Test
    fun kotlinVersionIs2_1_10() {
        assertTrue(
            "Root build.gradle must set kotlin_version to 2.1.10",
            rootBuildGradle.contains("ext.kotlin_version = '2.1.10'")
        )
    }

    // --- Forbidden References Removed ---

    @Test
    fun noJcenterInBuildFiles() {
        assertFalse("Root build.gradle must not contain jcenter()", rootBuildGradle.contains("jcenter()"))
        assertFalse("App build.gradle must not contain jcenter()", appBuildGradle.contains("jcenter()"))
        assertFalse("settings.gradle must not contain jcenter()", settingsGradle.contains("jcenter()"))
    }

    @Test
    fun noMavenFabricInBuildFiles() {
        assertFalse("Root build.gradle must not reference maven.fabric.io", rootBuildGradle.contains("maven.fabric.io"))
        assertFalse("App build.gradle must not reference maven.fabric.io", appBuildGradle.contains("maven.fabric.io"))
    }

    @Test
    fun noHugoPluginInBuildFiles() {
        assertFalse("App build.gradle must not reference hugo", appBuildGradle.lowercase().contains("hugo"))
        assertFalse("Root build.gradle must not reference hugo", rootBuildGradle.lowercase().contains("hugo"))
    }

    @Test
    fun noKotlinAndroidExtensionsPlugin() {
        assertFalse(
            "App build.gradle must not apply kotlin-android-extensions",
            appBuildGradle.contains("kotlin-android-extensions")
        )
    }

    @Test
    fun noAndroidExtensionsBlock() {
        assertFalse(
            "App build.gradle must not contain androidExtensions block",
            appBuildGradle.contains("androidExtensions")
        )
    }

    @Test
    fun noFirebasePerfInBuildFiles() {
        assertFalse("App build.gradle must not reference firebase-perf", appBuildGradle.contains("firebase-perf"))
        assertFalse("Root build.gradle must not reference firebase-perf", rootBuildGradle.contains("firebase-perf"))
    }

    @Test
    fun noEnableCrashlyticsInBuildFiles() {
        assertFalse(
            "App build.gradle must not contain enableCrashlytics",
            appBuildGradle.contains("enableCrashlytics")
        )
    }

    // --- Required Features Present ---

    @Test
    fun viewBindingEnabled() {
        assertTrue(
            "App build.gradle must enable viewBinding in buildFeatures",
            appBuildGradle.contains("viewBinding true")
        )
    }

    @Test
    fun compileSdkIs35() {
        assertTrue(
            "App build.gradle must set compileSdk 35",
            appBuildGradle.contains("compileSdk 35")
        )
    }

    @Test
    fun targetSdkIs34() {
        assertTrue(
            "App build.gradle must set targetSdk 34",
            appBuildGradle.contains("targetSdk 34")
        )
    }

    @Test
    fun productFlavorsPreserved() {
        assertTrue("App build.gradle must contain demo flavor", appBuildGradle.contains("demo {"))
        assertTrue("App build.gradle must contain full flavor", appBuildGradle.contains("full {"))
    }

    @Test
    fun googleServicesPluginAppliedAtBottom() {
        val lines = appBuildGradle.trimEnd().lines()
        val lastNonBlank = lines.lastOrNull { it.isNotBlank() } ?: ""
        assertTrue(
            "google-services plugin must be applied at the bottom of app/build.gradle",
            lastNonBlank.contains("com.google.gms.google-services")
        )
    }

    @Test
    fun kotlinParcelizePluginApplied() {
        assertTrue(
            "App build.gradle must apply kotlin-parcelize plugin",
            appBuildGradle.contains("kotlin-parcelize")
        )
    }

    @Test
    fun settingsHasPluginManagementBlock() {
        assertTrue(
            "settings.gradle must contain pluginManagement block",
            settingsGradle.contains("pluginManagement")
        )
    }

    @Test
    fun gradlePropertiesNoMaxPermSize() {
        assertFalse(
            "gradle.properties must not contain MaxPermSize",
            gradleProperties.contains("MaxPermSize")
        )
    }
}
