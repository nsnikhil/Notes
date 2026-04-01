package com.nrs.nsnik.notes

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * ST-004: Verifies BUG-004 (lock) and BUG-005 (navigation) fixes.
 */
class LockAndNavigationBugFixTest {

    private val projectRoot: File by lazy {
        var dir = File(System.getProperty("user.dir"))
        while (dir.parentFile != null && !File(dir, "settings.gradle").exists()) {
            dir = dir.parentFile
        }
        dir
    }

    // BUG-004: PasswordDialogFragment must define FragmentResult constants and call setFragmentResult
    @Test
    fun bug004_passwordDialogFragmentSendsFragmentResult() {
        val content = File(projectRoot,
            "app/src/main/java/com/nrs/nsnik/notes/view/fragments/dialogFragments/PasswordDialogFragment.kt"
        ).readText()

        assertTrue(
            "PasswordDialogFragment must define REQUEST_KEY constant",
            content.contains("const val REQUEST_KEY")
        )
        assertTrue(
            "PasswordDialogFragment must define RESULT_PASSWORD_CREATED constant",
            content.contains("const val RESULT_PASSWORD_CREATED")
        )
        assertTrue(
            "PasswordDialogFragment.savePassword must call setFragmentResult",
            content.contains("setFragmentResult")
        )
    }

    // BUG-004: NewNoteFragment must listen for password creation result
    @Test
    fun bug004_newNoteFragmentListensForPasswordResult() {
        val content = File(projectRoot,
            "app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt"
        ).readText()

        assertTrue(
            "NewNoteFragment must register a FragmentResultListener for PasswordDialogFragment.REQUEST_KEY",
            content.contains("setFragmentResultListener")
                    && content.contains("PasswordDialogFragment.REQUEST_KEY")
        )
        assertTrue(
            "NewNoteFragment must check RESULT_PASSWORD_CREATED in the listener",
            content.contains("PasswordDialogFragment.RESULT_PASSWORD_CREATED")
        )
    }

    // BUG-005: Drawer navigation must use NavOptions with popUpTo to prevent stacking
    @Test
    fun bug005_drawerNavigationUsesPopUpTo() {
        val content = File(projectRoot,
            "app/src/main/java/com/nrs/nsnik/notes/view/MainActivity.kt"
        ).readText()

        assertTrue(
            "MainActivity must import NavOptions",
            content.contains("import androidx.navigation.NavOptions")
        )
        assertTrue(
            "Drawer navigation must use setPopUpTo to prevent fragment stacking",
            content.contains("setPopUpTo")
        )
        assertTrue(
            "Drawer navigation must use setLaunchSingleTop(true)",
            content.contains("setLaunchSingleTop(true)")
        )
    }
}
