package com.nrs.nsnik.notes

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * ST-004: Structural verification tests that validate Dagger components,
 * ViewModels, navigation graph, and MyApplication wiring by reading source files.
 */
class AppStructureVerificationTest {

    private val projectRoot: File by lazy {
        var dir = File(System.getProperty("user.dir"))
        while (dir.parentFile != null && !File(dir, "settings.gradle").exists()) {
            dir = dir.parentFile
        }
        dir
    }

    private val componentsDir = "app/src/main/java/com/nrs/nsnik/notes/dagger/components"

    private val expectedComponents = listOf(
        "DatabaseComponent", "FileComponent", "FolderComponent",
        "GlideComponent", "SharedPrefComponent", "NetworkComponent", "MediaComponent"
    )

    // --- Dagger Components ---

    @Test
    fun allSevenDaggerComponentFilesExist() {
        for (name in expectedComponents) {
            val file = File(projectRoot, "$componentsDir/$name.kt")
            assertTrue("$name.kt must exist", file.exists())
        }
    }

    @Test
    fun allDaggerComponentsAnnotatedWithComponent() {
        for (name in expectedComponents) {
            val content = File(projectRoot, "$componentsDir/$name.kt").readText()
            assertTrue(
                "$name.kt must contain @Component annotation",
                content.contains("@Component")
            )
        }
    }

    @Test
    fun networkComponentIsInterface() {
        val content = File(projectRoot, "$componentsDir/NetworkComponent.kt").readText()
        assertTrue(
            "NetworkComponent must be declared as interface",
            content.contains("interface NetworkComponent")
        )
        assertFalse(
            "NetworkComponent must not be abstract class",
            content.contains("abstract class NetworkComponent")
        )
    }

    // --- ViewModel Completeness ---

    @Test
    fun noteViewModelExistsWithKeyMethods() {
        val content = File(
            projectRoot,
            "app/src/main/java/com/nrs/nsnik/notes/viewmodel/NoteViewModel.kt"
        ).readText()
        val requiredMethods = listOf("insertNote", "deleteNote", "updateNote", "searchNote")
        for (method in requiredMethods) {
            assertTrue(
                "NoteViewModel.kt must contain $method",
                content.contains(method)
            )
        }
    }

    @Test
    fun folderViewModelExistsWithKeyMethods() {
        val content = File(
            projectRoot,
            "app/src/main/java/com/nrs/nsnik/notes/viewmodel/FolderViewModel.kt"
        ).readText()
        val requiredMethods = listOf("insertFolder", "deleteFolder", "updateFolder", "searchFolder")
        for (method in requiredMethods) {
            assertTrue(
                "FolderViewModel.kt must contain $method",
                content.contains(method)
            )
        }
    }

    // --- Navigation Graph ---

    @Test
    fun navGraphFragmentReferencesPointToExistingFiles() {
        val navContent = File(projectRoot, "app/src/main/res/navigation/nav_graph.xml").readText()
        val fragmentClassPattern = Regex("""android:name="(com\.nrs\.nsnik\.notes\.[^"]+)"""")
        val matches = fragmentClassPattern.findAll(navContent).toList()
        assertTrue("nav_graph.xml must reference at least one fragment", matches.isNotEmpty())

        for (match in matches) {
            val fqcn = match.groupValues[1]
            val relativePath = fqcn.replace('.', '/') + ".kt"
            val file = File(projectRoot, "app/src/main/java/$relativePath")
            assertTrue("Fragment class file must exist: $relativePath", file.exists())
        }
    }

    // --- MyApplication Dagger Initialization ---

    @Test
    fun myApplicationReferencesCoreDaggerComponents() {
        val content = File(
            projectRoot,
            "app/src/main/java/com/nrs/nsnik/notes/MyApplication.kt"
        ).readText()
        // These 6 components are initialized in MyApplication.moduleSetter()
        val coreComponents = listOf(
            "DaggerDatabaseComponent", "DaggerFileComponent", "DaggerFolderComponent",
            "DaggerGlideComponent", "DaggerSharedPrefComponent", "DaggerNetworkComponent"
        )
        for (component in coreComponents) {
            assertTrue(
                "MyApplication.kt must reference $component",
                content.contains(component)
            )
        }
    }

    @Test
    fun contextModuleInitializedInsideOnCreate() {
        val content = File(
            projectRoot,
            "app/src/main/java/com/nrs/nsnik/notes/MyApplication.kt"
        ).readText()
        // ContextModule(this) must appear AFTER "override fun onCreate"
        val onCreateIdx = content.indexOf("override fun onCreate")
        assertTrue("MyApplication.kt must contain onCreate", onCreateIdx >= 0)
        val afterOnCreate = content.substring(onCreateIdx)
        assertTrue(
            "ContextModule must be initialized inside onCreate",
            afterOnCreate.contains("ContextModule(this)")
        )
        // Must NOT appear as a field initializer before onCreate
        val beforeOnCreate = content.substring(0, onCreateIdx)
        assertFalse(
            "ContextModule(this) must not be a field initializer",
            Regex("""=\s*ContextModule\(this\)""").containsMatchIn(beforeOnCreate)
        )
    }
}
