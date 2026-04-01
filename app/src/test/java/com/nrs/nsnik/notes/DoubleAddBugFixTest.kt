package com.nrs.nsnik.notes

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * ST-001: Verifies that redundant notifyItemInserted/notifyItemRemoved calls
 * have been removed from NewNoteFragment.kt. submitList() already triggers
 * DiffUtil which handles adapter notification; extra notify* calls cause
 * double-add/double-remove visual bugs.
 */
class DoubleAddBugFixTest {

    private val projectRoot: File by lazy {
        var dir = File(System.getProperty("user.dir"))
        while (dir.parentFile != null && !File(dir, "settings.gradle").exists()) {
            dir = dir.parentFile
        }
        dir
    }

    private val fragmentSource: String by lazy {
        File(projectRoot, "app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt").readText()
    }

    @Test
    fun noNotifyItemInsertedCallsInNewNoteFragment() {
        val codeLines = fragmentSource.lines()
            .filter { !it.trimStart().startsWith("//") }
        val matches = codeLines.filter { it.contains("notifyItemInserted") }
        assertTrue(
            "NewNoteFragment.kt must not contain notifyItemInserted calls (found: $matches)",
            matches.isEmpty()
        )
    }

    @Test
    fun noNotifyItemRemovedCallsInNewNoteFragment() {
        val codeLines = fragmentSource.lines()
            .filter { !it.trimStart().startsWith("//") }
        val matches = codeLines.filter { it.contains("notifyItemRemoved") }
        assertTrue(
            "NewNoteFragment.kt must not contain notifyItemRemoved calls (found: $matches)",
            matches.isEmpty()
        )
    }

    @Test
    fun submitListCallsStillPresent() {
        val submitListCalls = fragmentSource.lines()
            .filter { it.contains(".submitList(") && !it.trimStart().startsWith("//") }
        assertTrue(
            "NewNoteFragment.kt must still contain submitList() calls for adapter updates",
            submitListCalls.size >= 6
        )
    }
}
