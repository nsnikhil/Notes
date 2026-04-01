package com.nrs.nsnik.notes

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * ST-005: Verifies AndroidX, Room, Lifecycle, Navigation, Paging dependency upgrade.
 */
class DependencyUpgradeTest {

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
            .filter { it.isFile && (it.extension == "kt" || it.extension == "java") }
            .toList()
    }

    // --- SC2: No android.arch. references ---

    @Test
    fun noAndroidArchReferences() {
        assertFalse(
            "build.gradle must not contain android.arch. references",
            appBuildGradle.contains("android.arch.")
        )
    }

    // --- SC3: No lifecycle-extensions ---

    @Test
    fun noLifecycleExtensions() {
        assertFalse(
            "build.gradle must not contain lifecycle-extensions",
            appBuildGradle.contains("lifecycle-extensions")
        )
    }

    // --- SC4: No work-runtime or work-firebase ---

    @Test
    fun noWorkRuntimeOrFirebase() {
        assertFalse("build.gradle must not contain work-runtime", appBuildGradle.contains("work-runtime"))
        assertFalse("build.gradle must not contain work-firebase", appBuildGradle.contains("work-firebase"))
    }

    // --- SC5: Modern AndroidX dependency versions ---

    @Test
    fun lifecycleVersionIsModern() {
        val regex = Regex("""lifecycle-\w+(-ktx)?:(\d+\.\d+\.\d+)""")
        val versions = regex.findAll(appBuildGradle).map { it.groupValues[2] }.toSet()
        assertTrue("Lifecycle deps must exist", versions.isNotEmpty())
        versions.forEach { v ->
            val major = v.split(".")[0].toInt()
            assertTrue("Lifecycle version $v must be >= 2.6.0", major >= 2)
            if (major == 2) {
                val minor = v.split(".")[1].toInt()
                assertTrue("Lifecycle version $v must be >= 2.6.0", minor >= 6)
            }
        }
    }

    @Test
    fun roomVersionIsModern() {
        val regex = Regex("""room-\w+:(\d+\.\d+\.\d+)""")
        val versions = regex.findAll(appBuildGradle).map { it.groupValues[1] }.toSet()
        assertTrue("Room deps must exist", versions.isNotEmpty())
        versions.forEach { v ->
            val parts = v.split(".")
            val major = parts[0].toInt()
            val minor = parts[1].toInt()
            assertTrue("Room version $v must be >= 2.5.0", major >= 2 && (major > 2 || minor >= 5))
        }
    }

    @Test
    fun navigationVersionIsModern() {
        val regex = Regex("""navigation-\w+(-ktx)?:(\d+\.\d+\.\d+)""")
        val versions = regex.findAll(appBuildGradle).map { it.groupValues[2] }.toSet()
        assertTrue("Navigation deps must exist", versions.isNotEmpty())
        versions.forEach { v ->
            val parts = v.split(".")
            val major = parts[0].toInt()
            val minor = parts[1].toInt()
            assertTrue("Navigation version $v must be >= 2.7.0", major >= 2 && (major > 2 || minor >= 7))
        }
    }

    @Test
    fun pagingVersionIsModern() {
        val regex = Regex("""paging-\w+:(\d+\.\d+\.\d+)""")
        val versions = regex.findAll(appBuildGradle).map { it.groupValues[1] }.toSet()
        assertTrue("Paging deps must exist", versions.isNotEmpty())
        versions.forEach { v ->
            val major = v.split(".")[0].toInt()
            assertTrue("Paging version $v must be >= 3.0.0", major >= 3)
        }
    }

    // --- SC6: No ViewModelProviders usage ---

    @Test
    fun noViewModelProvidersInSource() {
        val matches = mainSourceFiles.filter { it.readText().contains("ViewModelProviders") }
        assertTrue(
            "Source must not contain ViewModelProviders: ${matches.map { it.name }}",
            matches.isEmpty()
        )
    }

    // --- Consistency: all lifecycle deps use same version ---

    @Test
    fun lifecycleDepsUseSameVersion() {
        val regex = Regex("""lifecycle-\w+(-ktx)?:(\d+\.\d+\.\d+)""")
        val versions = regex.findAll(appBuildGradle).map { it.groupValues[2] }.toSet()
        assertTrue("All lifecycle deps must use the same version, found: $versions", versions.size == 1)
    }

    @Test
    fun roomDepsUseSameVersion() {
        val regex = Regex("""room-\w+:(\d+\.\d+\.\d+)""")
        val versions = regex.findAll(appBuildGradle).map { it.groupValues[1] }.toSet()
        assertTrue("All room deps must use the same version, found: $versions", versions.size == 1)
    }

    @Test
    fun navigationDepsUseSameVersion() {
        val regex = Regex("""navigation-\w+(-ktx)?:(\d+\.\d+\.\d+)""")
        val versions = regex.findAll(appBuildGradle).map { it.groupValues[2] }.toSet()
        assertTrue("All navigation deps must use the same version, found: $versions", versions.size == 1)
    }

    @Test
    fun pagingDepsUseSameVersion() {
        val regex = Regex("""paging-\w+:(\d+\.\d+\.\d+)""")
        val versions = regex.findAll(appBuildGradle).map { it.groupValues[1] }.toSet()
        assertTrue("All paging deps must use the same version, found: $versions", versions.size == 1)
    }

    // --- No deprecated lifecycle patterns ---

    @Test
    fun noLifecycleExtensionsImportInSource() {
        val matches = mainSourceFiles.filter { it.readText().contains("androidx.lifecycle.extensions") }
        assertTrue(
            "Source must not import lifecycle-extensions: ${matches.map { it.name }}",
            matches.isEmpty()
        )
    }
}
