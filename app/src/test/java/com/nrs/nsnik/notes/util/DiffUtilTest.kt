package com.nrs.nsnik.notes.util

import com.nrs.nsnik.notes.data.FolderEntity
import com.nrs.nsnik.notes.data.NoteEntity
import com.nrs.nsnik.notes.model.CheckListObject
import com.nrs.nsnik.notes.view.adapters.diffUtil.CheckListDiffUtil
import com.nrs.nsnik.notes.view.adapters.diffUtil.FolderDiffUtil
import com.nrs.nsnik.notes.view.adapters.diffUtil.NotesDiffUtil
import com.nrs.nsnik.notes.view.adapters.diffUtil.StringDiffUtil
import org.junit.Assert.*
import org.junit.Test

class DiffUtilTest {

    private val notesDiff = NotesDiffUtil()

    @Test
    fun notesDiff_sameUid_areItemsTheSame() {
        val a = NoteEntity().apply { uid = 1; title = "A" }
        val b = NoteEntity().apply { uid = 1; title = "B" }
        assertTrue(notesDiff.areItemsTheSame(a, b))
    }

    @Test
    fun notesDiff_differentUid_areItemsNotTheSame() {
        val a = NoteEntity().apply { uid = 1 }
        val b = NoteEntity().apply { uid = 2 }
        assertFalse(notesDiff.areItemsTheSame(a, b))
    }

    @Test
    fun notesDiff_sameReference_areContentsTheSame() {
        val note = NoteEntity().apply { uid = 1; title = "Same" }
        assertTrue(notesDiff.areContentsTheSame(note, note))
    }

    private val folderDiff = FolderDiffUtil()

    @Test
    fun folderDiff_sameUid_areItemsTheSame() {
        val a = FolderEntity().apply { uid = 1; folderName = "A" }
        val b = FolderEntity().apply { uid = 1; folderName = "B" }
        assertTrue(folderDiff.areItemsTheSame(a, b))
    }

    @Test
    fun folderDiff_differentUid_areItemsNotTheSame() {
        val a = FolderEntity().apply { uid = 1 }
        val b = FolderEntity().apply { uid = 2 }
        assertFalse(folderDiff.areItemsTheSame(a, b))
    }

    @Test
    fun folderDiff_sameReference_areContentsTheSame() {
        val folder = FolderEntity().apply { uid = 1; folderName = "Same" }
        assertTrue(folderDiff.areContentsTheSame(folder, folder))
    }

    private val checkListDiff = CheckListDiffUtil()

    @Test
    fun checkListDiff_sameInstance_areItemsTheSame() {
        val a = CheckListObject().apply { text = "A"; done = false }
        assertTrue(checkListDiff.areItemsTheSame(a, a))
    }

    @Test
    fun checkListDiff_differentInstances_areItemsNotTheSame() {
        val a = CheckListObject().apply { text = "A"; done = false }
        val b = CheckListObject().apply { text = "A"; done = false }
        assertFalse(checkListDiff.areItemsTheSame(a, b))
    }

    @Test
    fun checkListDiff_sameContent_areContentsTheSame() {
        val a = CheckListObject().apply { text = "A"; done = false }
        val b = CheckListObject().apply { text = "A"; done = false }
        assertTrue(checkListDiff.areContentsTheSame(a, b))
    }

    @Test
    fun checkListDiff_differentText_areItemsNotTheSame() {
        val a = CheckListObject().apply { text = "A" }
        val b = CheckListObject().apply { text = "B" }
        assertFalse(checkListDiff.areItemsTheSame(a, b))
    }

    @Test
    fun checkListDiff_differentDoneState_areContentsNotTheSame() {
        val a = CheckListObject().apply { text = "A"; done = false }
        val b = CheckListObject().apply { text = "A"; done = true }
        assertFalse(checkListDiff.areContentsTheSame(a, b))
    }

    private val stringDiff = StringDiffUtil()

    @Test
    fun stringDiff_sameString_areItemsTheSame() {
        assertTrue(stringDiff.areItemsTheSame("hello", "hello"))
    }

    @Test
    fun stringDiff_differentStrings_areItemsNotTheSame() {
        assertFalse(stringDiff.areItemsTheSame("hello", "world"))
    }

    @Test
    fun stringDiff_sameString_areContentsTheSame() {
        assertTrue(stringDiff.areContentsTheSame("hello", "hello"))
    }

    @Test
    fun stringDiff_differentStrings_areContentsNotTheSame() {
        assertFalse(stringDiff.areContentsTheSame("hello", "world"))
    }
}
