package com.nrs.nsnik.notes.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NoteDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: NotesDatabase
    private lateinit var noteDao: NoteDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NotesDatabase::class.java
        ).allowMainThreadQueries().build()
        noteDao = database.noteDao
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createNote(
        title: String? = "Test Note",
        folderName: String? = "root",
        pinned: Int = 0,
        locked: Int = 0,
        color: String? = null
    ): NoteEntity = NoteEntity().apply {
        this.title = title
        this.folderName = folderName
        this.pinned = pinned
        this.locked = locked
        this.color = color
    }

    private fun <T> LiveData<T>.getValueBlocking(): T? {
        var value: T? = null
        val latch = CountDownLatch(1)
        val observer = Observer<T> {
            value = it
            latch.countDown()
        }
        observeForever(observer)
        latch.await(2, TimeUnit.SECONDS)
        removeObserver(observer)
        return value
    }

    @Test
    fun insertNote_returnsGeneratedId() {
        val note = createNote()
        val ids = noteDao.insertNotes(note)
        assertEquals(1, ids.size)
        assertTrue(ids[0] > 0)
    }

    @Test
    fun getAllNotes_returnsInsertedNotes() {
        noteDao.insertNotes(createNote(title = "A"), createNote(title = "B"))
        val notes = noteDao.notesList.getValueBlocking()
        assertNotNull(notes)
        assertEquals(2, notes!!.size)
    }

    @Test
    fun getNoteById_returnsCorrectNote() {
        val ids = noteDao.insertNotes(createNote(title = "Find Me"))
        val note = noteDao.getNote(ids[0].toInt()).getValueBlocking()
        assertNotNull(note)
        assertEquals("Find Me", note!!.title)
    }

    @Test
    fun deleteNote_removesFromDatabase() {
        val note = createNote()
        val ids = noteDao.insertNotes(note)
        note.uid = ids[0].toInt()
        noteDao.deleteNotes(note)
        val notes = noteDao.notesList.getValueBlocking()
        assertTrue(notes.isNullOrEmpty())
    }

    @Test
    fun updateNote_changesFields() {
        val note = createNote(title = "Original")
        val ids = noteDao.insertNotes(note)
        note.uid = ids[0].toInt()
        note.title = "Updated"
        noteDao.updateNote(note)
        val updated = noteDao.getNote(ids[0].toInt()).getValueBlocking()
        assertEquals("Updated", updated!!.title)
    }

    @Test
    fun searchNotes_byTitlePrefix() {
        noteDao.insertNotes(createNote(title = "Kotlin Guide"), createNote(title = "Java Guide"))
        val results = noteDao.getNoteByQuery("Kot").getValueBlocking()
        assertNotNull(results)
        assertEquals(1, results!!.size)
        assertEquals("Kotlin Guide", results[0].title)
    }

    @Test
    fun getNotesByFolder_filtersCorrectly() {
        noteDao.insertNotes(
            createNote(title = "In Work", folderName = "work"),
            createNote(title = "In Home", folderName = "home")
        )
        val workNotes = noteDao.getNoteByFolderName("work").getValueBlocking()
        assertNotNull(workNotes)
        assertEquals(1, workNotes!!.size)
        assertEquals("In Work", workNotes[0].title)
    }

    @Test
    fun getPinnedNotes_returnsOnlyPinned() {
        noteDao.insertNotes(
            createNote(title = "Pinned", pinned = 1),
            createNote(title = "Not Pinned", pinned = 0)
        )
        val pinned = noteDao.getNoteByPin(1).getValueBlocking()
        assertNotNull(pinned)
        assertEquals(1, pinned!!.size)
        assertEquals("Pinned", pinned[0].title)
    }

    @Test
    fun changeNotePinStatus_updatesPinField() {
        val ids = noteDao.insertNotes(createNote(pinned = 0))
        noteDao.changeNotePinStatus(ids[0].toInt(), 1)
        val note = noteDao.getNote(ids[0].toInt()).getValueBlocking()
        assertEquals(1, note!!.pinned)
    }

    @Test
    fun deleteNoteByFolderName_removesAllInFolder() {
        noteDao.insertNotes(
            createNote(folderName = "trash"),
            createNote(folderName = "trash"),
            createNote(folderName = "keep")
        )
        noteDao.deleteNoteByFolderName("trash")
        val notes = noteDao.notesList.getValueBlocking()
        assertEquals(1, notes!!.size)
        assertEquals("keep", notes[0].folderName)
    }

    @Test
    fun getNoteByColor_filtersCorrectly() {
        noteDao.insertNotes(
            createNote(color = "red"),
            createNote(color = "blue")
        )
        val redNotes = noteDao.getNoteByColor("red").getValueBlocking()
        assertEquals(1, redNotes!!.size)
    }

    @Test
    fun changeNoteFolder_updatesFolder() {
        val ids = noteDao.insertNotes(createNote(folderName = "old"))
        noteDao.changeNoteFolder(ids[0].toInt(), "new")
        val note = noteDao.getNote(ids[0].toInt()).getValueBlocking()
        assertEquals("new", note!!.folderName)
    }
}
