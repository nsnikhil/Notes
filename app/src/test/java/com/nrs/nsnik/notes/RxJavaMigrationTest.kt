package com.nrs.nsnik.notes

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * ST-006: Verifies RxJava 2→3 full cascade migration is complete.
 */
class RxJavaMigrationTest {

    private val projectRoot: File by lazy {
        var dir = File(System.getProperty("user.dir"))
        while (dir.parentFile != null && !File(dir, "settings.gradle").exists()) {
            dir = dir.parentFile
        }
        dir
    }

    private val appBuildGradle: String by lazy {
        File(projectRoot, "app/build.gradle").readText()
    }

    private val mainSourceFiles: List<File> by lazy {
        File(projectRoot, "app/src/main").walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .toList()
    }

    // --- SC1: No RxJava 2 imports in source ---

    @Test
    fun noRxJava2ImportsInSource() {
        val rx2Matches = mainSourceFiles.filter { f ->
            f.readText().lines().any { line ->
                line.trimStart().startsWith("import ") &&
                line.contains("io.reactivex.") &&
                !line.contains("io.reactivex.rxjava3")
            }
        }
        assertTrue(
            "Found RxJava 2 imports in: ${rx2Matches.map { it.name }}",
            rx2Matches.isEmpty()
        )
    }

    // --- SC2: No rxbinding2 references ---

    @Test
    fun noRxBinding2InSource() {
        val matches = mainSourceFiles.filter { it.readText().contains("rxbinding2") }
        assertTrue(
            "Found rxbinding2 references in: ${matches.map { it.name }}",
            matches.isEmpty()
        )
    }

    @Test
    fun noRxBinding2InBuildGradle() {
        assertFalse(
            "build.gradle must not contain rxbinding2",
            appBuildGradle.contains("rxbinding2")
        )
    }

    // --- SC3: No rxjava2 in build.gradle ---

    @Test
    fun noRxJava2InBuildGradle() {
        assertFalse(
            "build.gradle must not contain rxjava2",
            appBuildGradle.contains("rxjava2")
        )
    }

    // --- RxJava 3 dependencies present ---

    @Test
    fun rxJava3DependenciesPresent() {
        assertTrue("Must have rxjava3:rxandroid", appBuildGradle.contains("io.reactivex.rxjava3:rxandroid"))
        assertTrue("Must have rxjava3:rxjava", appBuildGradle.contains("io.reactivex.rxjava3:rxjava"))
        assertTrue("Must have rxjava3:rxkotlin", appBuildGradle.contains("io.reactivex.rxjava3:rxkotlin"))
    }

    @Test
    fun rxBinding4DependenciesPresent() {
        assertTrue("Must have rxbinding4:rxbinding", appBuildGradle.contains("rxbinding4:rxbinding"))
    }

    @Test
    fun roomRxJava3AdapterPresent() {
        assertTrue("Must have room-rxjava3", appBuildGradle.contains("room-rxjava3"))
    }

    @Test
    fun retrofitRxJava3AdapterPresent() {
        assertTrue("Must have adapter-rxjava3", appBuildGradle.contains("adapter-rxjava3"))
    }

    @Test
    fun pagingRxJava3Present() {
        assertTrue("Must have paging-rxjava3", appBuildGradle.contains("paging-rxjava3"))
    }

    // --- Source uses RxJava 3 imports ---

    @Test
    fun sourceUsesRxJava3Imports() {
        val rx3Files = mainSourceFiles.filter { it.readText().contains("io.reactivex.rxjava3") }
        assertTrue("At least one source file must use RxJava 3 imports", rx3Files.isNotEmpty())
    }

    @Test
    fun sourceUsesRxBinding4Imports() {
        val rb4Files = mainSourceFiles.filter { it.readText().contains("com.jakewharton.rxbinding4") }
        assertTrue("At least one source file must use RxBinding 4 imports", rb4Files.isNotEmpty())
    }

    // --- No stale RxJava 2 patterns ---

    @Test
    fun noRxBinding3InBuildGradle() {
        assertFalse(
            "build.gradle must not contain rxbinding3 (intermediate version)",
            appBuildGradle.contains("rxbinding3")
        )
    }

    @Test
    fun noOldRxJavaPackageInSource() {
        val matches = mainSourceFiles.filter { f ->
            f.readText().lines().any { line ->
                line.trimStart().startsWith("import ") &&
                (line.contains("io.reactivex.Observable") ||
                 line.contains("io.reactivex.Single") ||
                 line.contains("io.reactivex.Completable") ||
                 line.contains("io.reactivex.Flowable")) &&
                !line.contains("rxjava3")
            }
        }
        assertTrue(
            "Found old RxJava 2 type imports in: ${matches.map { it.name }}",
            matches.isEmpty()
        )
    }
}
