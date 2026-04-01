package com.nrs.nsnik.notes

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * ST-003: Verifies all 8 runtime crash fixes are correctly applied.
 */
class RuntimeCrashFixTest {

    private val projectRoot: File by lazy {
        var dir = File(System.getProperty("user.dir"))
        while (dir.parentFile != null && !File(dir, "settings.gradle").exists()) {
            dir = dir.parentFile
        }
        dir
    }

    // CRASH-01: FragmentContainerView replaces <fragment> tag
    @Test
    fun crash01_activityMainUsesFragmentContainerView() {
        val content = File(projectRoot, "app/src/main/res/layout/activity_main.xml").readText()
        assertTrue("activity_main.xml must contain FragmentContainerView", content.contains("FragmentContainerView"))
        assertFalse(
            "activity_main.xml must not contain <fragment with NavHostFragment",
            content.contains("<fragment") && content.contains("NavHostFragment")
        )
    }

    // CRASH-02: ContextModule not initialized as field
    @Test
    fun crash02_contextModuleNotFieldInitializer() {
        val content = File(projectRoot, "app/src/main/java/com/nrs/nsnik/notes/MyApplication.kt").readText()
        assertFalse(
            "MyApplication.kt must not have ContextModule(this) as a field initializer",
            Regex("""=\s*ContextModule\(this\)""").containsMatchIn(
                content.substringBefore("override fun onCreate")
            )
        )
    }

    // CRASH-03: FirebaseApp.initializeApp wrapped in try-catch
    @Test
    fun crash03_firebaseInitWrappedInTryCatch() {
        val content = File(projectRoot, "app/src/main/java/com/nrs/nsnik/notes/MyApplication.kt").readText()
        assertTrue("MyApplication.kt must contain FirebaseApp.initializeApp", content.contains("FirebaseApp.initializeApp"))
        val firebaseIdx = content.indexOf("FirebaseApp.initializeApp")
        val precedingBlock = content.substring(0, firebaseIdx)
        val lastTry = precedingBlock.lastIndexOf("try")
        assertTrue("FirebaseApp.initializeApp must be inside a try block", lastTry >= 0)
        val betweenTryAndFirebase = content.substring(lastTry, firebaseIdx)
        assertFalse("No closing brace between try and FirebaseApp call", betweenTryAndFirebase.contains("}"))
    }

    // CRASH-04: NetworkComponent is an interface, not abstract class
    @Test
    fun crash04_networkComponentIsInterface() {
        val content = File(projectRoot, "app/src/main/java/com/nrs/nsnik/notes/dagger/components/NetworkComponent.kt").readText()
        assertTrue("NetworkComponent.kt must contain 'interface NetworkComponent'", content.contains("interface NetworkComponent"))
        assertFalse("NetworkComponent.kt must not contain 'abstract class NetworkComponent'", content.contains("abstract class NetworkComponent"))
    }

    // CRASH-05: fallbackToDestructiveMigration present in DatabaseModule
    @Test
    fun crash05_databaseModuleHasFallbackMigration() {
        val content = File(projectRoot, "app/src/main/java/com/nrs/nsnik/notes/dagger/modules/DatabaseModule.kt").readText()
        assertTrue("DatabaseModule.kt must contain fallbackToDestructiveMigration", content.contains("fallbackToDestructiveMigration"))
    }

    // CRASH-06: NoteEntity does not implement java.io.Serializable
    @Test
    fun crash06_noteEntityNoJavaSerializable() {
        val content = File(projectRoot, "app/src/main/java/com/nrs/nsnik/notes/data/NoteEntity.kt").readText()
        assertFalse("NoteEntity.kt must not contain java.io.Serializable", content.contains("java.io.Serializable"))
    }

    // CRASH-07: No setTargetFragment in any .kt file under app/src/main/
    @Test
    fun crash07_noSetTargetFragmentInSource() {
        val mainDir = File(projectRoot, "app/src/main")
        val matches = mainDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filter { it.readText().contains("setTargetFragment") }
            .toList()
        assertTrue("Found setTargetFragment in: ${matches.map { it.path }}", matches.isEmpty())
    }

    // CRASH-08: MainActivity does not use findNavController(R.id.mainNavHost)
    @Test
    fun crash08_mainActivityNoFindNavControllerById() {
        val content = File(projectRoot, "app/src/main/java/com/nrs/nsnik/notes/view/MainActivity.kt").readText()
        assertFalse(
            "MainActivity.kt must not contain findNavController(R.id.mainNavHost)",
            content.contains("findNavController(R.id.mainNavHost)")
        )
    }
}
