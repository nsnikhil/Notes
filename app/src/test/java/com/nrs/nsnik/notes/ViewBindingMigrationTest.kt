package com.nrs.nsnik.notes

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * ST-004: Verifies Kotlin Synthetics → View Binding migration is complete and correct.
 */
class ViewBindingMigrationTest {

    private val projectRoot: File by lazy {
        var dir = File(System.getProperty("user.dir"))
        while (dir.parentFile != null && !File(dir, "settings.gradle").exists()) {
            dir = dir.parentFile
        }
        dir
    }

    private val appSrcMain: File get() = File(projectRoot, "app/src/main")
    private val appBuildGradle: String by lazy { File(projectRoot, "app/build.gradle").readText() }

    private fun allKotlinFiles(): List<File> =
        appSrcMain.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()

    private fun fragmentFiles(): List<File> =
        File(appSrcMain, "java/com/nrs/nsnik/notes/view/fragments").walkTopDown()
            .filter { it.isFile && it.extension == "kt" && it.readText().let { t ->
                t.contains(": Fragment(") || t.contains(": DialogFragment(") ||
                t.contains(": Fragment()") || t.contains(": DialogFragment()") ||
                t.contains("AppCompatDialogFragment")
            }}.toList()

    private fun adapterFiles(): List<File> =
        File(appSrcMain, "java/com/nrs/nsnik/notes/view/adapters").listFiles()
            ?.filter { it.isFile && it.extension == "kt" && (it.readText().contains("RecyclerView.Adapter") || it.readText().contains("ListAdapter<")) }
            ?.toList() ?: emptyList()

    // --- SC1: Zero kotlinx.android.synthetic imports ---

    @Test
    fun noSyntheticImportsInSource() {
        val matches = allKotlinFiles().filter { it.readText().contains("kotlinx.android.synthetic") }
        assertTrue("Found kotlinx.android.synthetic in: ${matches.map { it.name }}", matches.isEmpty())
    }

    // --- SC2: All migrated files have view binding imports ---

    @Test
    fun allFragmentsHaveViewBindingImport() {
        val missing = fragmentFiles().filter { !it.readText().contains("com.nrs.nsnik.notes.databinding") }
        assertTrue("Fragments missing databinding import: ${missing.map { it.name }}", missing.isEmpty())
    }

    @Test
    fun allAdaptersHaveViewBindingImport() {
        val missing = adapterFiles().filter { !it.readText().contains("com.nrs.nsnik.notes.databinding") }
        assertTrue("Adapters missing databinding import: ${missing.map { it.name }}", missing.isEmpty())
    }

    @Test
    fun mainActivityHasViewBindingImport() {
        val mainActivity = File(appSrcMain, "java/com/nrs/nsnik/notes/view/MainActivity.kt")
        assertTrue("MainActivity must have databinding import", mainActivity.readText().contains("com.nrs.nsnik.notes.databinding"))
    }

    @Test
    fun atLeast18FilesHaveViewBindingImports() {
        val count = allKotlinFiles().count { it.readText().contains("import com.nrs.nsnik.notes.databinding") }
        assertTrue("Expected >= 18 files with databinding imports, found $count", count >= 18)
    }

    // --- SC3: No synthetic import lines remain ---

    @Test
    fun noImportSyntheticLines() {
        val matches = allKotlinFiles().filter { it.readText().contains("import kotlinx.android.synthetic") }
        assertTrue("Found 'import kotlinx.android.synthetic' in: ${matches.map { it.name }}", matches.isEmpty())
    }

    // --- SC4: Fragment _binding nullable pattern and onDestroyView cleanup ---

    @Test
    fun fragmentsHaveNullableBindingPattern() {
        val missing = fragmentFiles().filter { f ->
            val text = f.readText()
            !text.contains("_binding") || !text.contains("get() = _binding!!")
        }
        assertTrue("Fragments missing _binding nullable pattern: ${missing.map { it.name }}", missing.isEmpty())
    }

    @Test
    fun fragmentsHaveOnDestroyViewCleanup() {
        val missing = fragmentFiles().filter { f ->
            val text = f.readText()
            !text.contains("_binding = null") || !text.contains("onDestroyView")
        }
        assertTrue("Fragments missing onDestroyView cleanup: ${missing.map { it.name }}", missing.isEmpty())
    }

    // --- SC5: Activity uses lateinit var binding ---

    @Test
    fun mainActivityUsesLateinitBinding() {
        val mainActivity = File(appSrcMain, "java/com/nrs/nsnik/notes/view/MainActivity.kt")
        assertTrue("MainActivity must use 'lateinit var binding'", mainActivity.readText().contains("lateinit var binding"))
    }

    @Test
    fun mainActivityInflatesBinding() {
        val text = File(appSrcMain, "java/com/nrs/nsnik/notes/view/MainActivity.kt").readText()
        assertTrue("MainActivity must call Binding.inflate", text.contains("ActivityMainBinding.inflate"))
        assertTrue("MainActivity must call setContentView(binding.root)", text.contains("setContentView(binding.root)"))
    }

    // --- SC6: Adapter files pass binding to ViewHolder ---

    @Test
    fun adapterViewHoldersAcceptBinding() {
        val failing = adapterFiles().filter { f ->
            val text = f.readText()
            val hasViewHolder = text.contains("ViewHolder")
            val bindingInVH = text.contains("ViewHolder(binding") || text.contains("ViewHolder internal constructor(binding")
            hasViewHolder && !bindingInVH
        }
        assertTrue("Adapters not passing binding to ViewHolder: ${failing.map { it.name }}", failing.isEmpty())
    }

    @Test
    fun adapterOnCreateViewHolderUsesBindingInflate() {
        val failing = adapterFiles().filter { f ->
            val text = f.readText()
            text.contains("onCreateViewHolder") && !text.contains("Binding.inflate")
        }
        assertTrue("Adapters not using Binding.inflate in onCreateViewHolder: ${failing.map { it.name }}", failing.isEmpty())
    }

    // --- Build config ---

    @Test
    fun viewBindingEnabledInBuildGradle() {
        assertTrue("viewBinding must be enabled in build.gradle", appBuildGradle.contains("viewBinding true"))
    }

    @Test
    fun noKotlinAndroidExtensionsPlugin() {
        assertFalse("kotlin-android-extensions must not be in build.gradle", appBuildGradle.contains("kotlin-android-extensions"))
    }
}
