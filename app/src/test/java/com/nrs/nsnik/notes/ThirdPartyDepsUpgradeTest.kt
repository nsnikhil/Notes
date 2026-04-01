package com.nrs.nsnik.notes

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * ST-007: Verifies EventBus, Timber, Glide, Dagger, and remaining third-party deps
 * are at target versions, no duplicates exist, and Branch SDK is removed.
 */
class ThirdPartyDepsUpgradeTest {

    private val projectRoot: File by lazy {
        var dir = File(System.getProperty("user.dir"))
        while (dir.parentFile != null && !File(dir, "settings.gradle").exists()) {
            dir = dir.parentFile
        }
        dir
    }

    private val buildGradle: String by lazy {
        File(projectRoot, "app/build.gradle").readText()
    }

    // --- SC1: Target versions ---

    @Test
    fun eventBusAtTargetVersion() {
        assertTrue("EventBus must be 3.3.1", buildGradle.contains("org.greenrobot:eventbus:3.3.1"))
    }

    @Test
    fun timberAtTargetVersion() {
        assertTrue("Timber must be 5.0.1", buildGradle.contains("com.jakewharton.timber:timber:5.0.1"))
    }

    @Test
    fun glideAtTargetVersion() {
        assertTrue("Glide must be 4.16.0", buildGradle.contains("com.github.bumptech.glide:glide:4.16.0"))
        assertTrue("Glide compiler must be 4.16.0", buildGradle.contains("com.github.bumptech.glide:compiler:4.16.0"))
    }

    @Test
    fun daggerAtTargetVersion() {
        val daggerArtifacts = listOf("dagger:", "dagger-compiler:", "dagger-android:", "dagger-android-support:", "dagger-android-processor:")
        daggerArtifacts.forEach { artifact ->
            assertTrue("$artifact must be at 2.53.1", buildGradle.contains("com.google.dagger:${artifact}2.53.1"))
        }
    }

    @Test
    fun okhttpAtTargetVersion() {
        assertTrue("OkHttp must be 4.12.0", buildGradle.contains("com.squareup.okhttp3:logging-interceptor:4.12.0"))
    }

    @Test
    fun retrofitAtTargetVersion() {
        assertTrue("Retrofit must be 2.11.0", buildGradle.contains("com.squareup.retrofit2:retrofit:2.11.0"))
        assertTrue("Retrofit converter-gson must be 2.11.0", buildGradle.contains("com.squareup.retrofit2:converter-gson:2.11.0"))
        assertTrue("Retrofit adapter-rxjava3 must be 2.11.0", buildGradle.contains("com.squareup.retrofit2:adapter-rxjava3:2.11.0"))
    }

    @Test
    fun gsonAtTargetVersion() {
        assertTrue("Gson must be 2.11.0", buildGradle.contains("com.google.code.gson:gson:2.11.0"))
    }

    @Test
    fun junitAtTargetVersion() {
        assertTrue("JUnit must be 4.13.2", buildGradle.contains("junit:junit:4.13.2"))
    }

    @Test
    fun mockitoAtTargetVersion() {
        assertTrue("Mockito must be 5.14.2", buildGradle.contains("org.mockito:mockito-core:5.14.2"))
    }

    @Test
    fun robolectricAtTargetVersion() {
        assertTrue("Robolectric must be 4.14.1", buildGradle.contains("org.robolectric:robolectric:4.14.1"))
    }

    // --- SC2: No duplicate dependency entries ---

    @Test
    fun noDuplicateDependencies() {
        val depLines = buildGradle.lines()
            .map { it.trim() }
            .filter { it.matches(Regex("^(implementation|kapt|testImplementation|androidTestImplementation|debugImplementation|androidTestUtil)\\s.*")) }
        val duplicates = depLines.groupBy { it }.filter { it.value.size > 1 }.keys
        assertTrue("No duplicate dependency entries allowed: $duplicates", duplicates.isEmpty())
    }

    // --- SC3: Branch SDK removed ---

    @Test
    fun noBranchSdkInBuildGradle() {
        assertFalse("build.gradle must not reference io.branch", buildGradle.contains("io.branch"))
    }

    @Test
    fun noBranchSdkInSource() {
        val mainDir = File(projectRoot, "app/src/main")
        val matches = mainDir.walkTopDown()
            .filter { it.isFile && (it.extension == "kt" || it.extension == "java" || it.extension == "xml") }
            .filter { it.readText().contains("io.branch") }
            .toList()
        assertTrue("Main source must not reference io.branch: ${matches.map { it.name }}", matches.isEmpty())
    }

    // --- Consistency: all Dagger deps use same version ---

    @Test
    fun daggerDepsUseSameVersion() {
        val regex = Regex("""com\.google\.dagger:dagger[\w-]*:(\d+\.\d+\.\d+)""")
        val versions = regex.findAll(buildGradle).map { it.groupValues[1] }.toSet()
        assertTrue("All Dagger deps must use the same version, found: $versions", versions.size == 1)
    }

    // --- Consistency: all Retrofit deps use same version ---

    @Test
    fun retrofitDepsUseSameVersion() {
        val regex = Regex("""com\.squareup\.retrofit2:[\w-]+:(\d+\.\d+\.\d+)""")
        val versions = regex.findAll(buildGradle).map { it.groupValues[1] }.toSet()
        assertTrue("All Retrofit deps must use the same version, found: $versions", versions.size == 1)
    }
}
