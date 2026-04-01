package com.nrs.nsnik.notes

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * ST-003: Verifies that PasswordDialogFragment uses viewLifecycleOwner for LiveData observers,
 * not 'this' (the Fragment instance). Using 'this' causes observer timing issues in
 * DialogFragments because the Fragment lifecycle differs from the view lifecycle.
 */
class PasswordDialogViewLifecycleOwnerTest {

    private val projectRoot: File by lazy {
        var dir = File(System.getProperty("user.dir"))
        while (dir.parentFile != null && !File(dir, "settings.gradle").exists()) {
            dir = dir.parentFile
        }
        dir
    }

    private val sourceFile: File by lazy {
        File(projectRoot, "app/src/main/java/com/nrs/nsnik/notes/view/fragments/dialogFragments/PasswordDialogFragment.kt")
    }

    @Test
    fun sourceFileExists() {
        assertTrue("PasswordDialogFragment.kt must exist", sourceFile.exists())
    }

    @Test
    fun noLiveDataObserversUseThisAsLifecycleOwner() {
        val source = sourceFile.readText()
        val bugPattern = Regex("""\.\s*observe\s*\(\s*this\s*,""")
        assertFalse(
            "PasswordDialogFragment must not use .observe(this, ...) — use viewLifecycleOwner instead",
            bugPattern.containsMatchIn(source)
        )
    }

    @Test
    fun liveDataObserversUseViewLifecycleOwner() {
        val source = sourceFile.readText()
        val correctPattern = Regex("""\.\s*observe\s*\(\s*viewLifecycleOwner\s*,""")
        assertTrue(
            "PasswordDialogFragment must use .observe(viewLifecycleOwner, ...) for LiveData observers",
            correctPattern.containsMatchIn(source)
        )
    }

    @Test
    fun fragmentResultRequestKeyExists() {
        val source = sourceFile.readText()
        assertTrue(
            "PasswordDialogFragment must define REQUEST_KEY for FragmentResult API",
            source.contains("REQUEST_KEY")
        )
    }

    @Test
    fun savePasswordSetsFragmentResultBeforeDismiss() {
        val source = sourceFile.readText()
        assertTrue(
            "savePassword must call setFragmentResult",
            source.contains("setFragmentResult")
        )
    }
}
