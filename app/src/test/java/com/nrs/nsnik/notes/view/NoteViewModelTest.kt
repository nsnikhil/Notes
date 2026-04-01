package com.nrs.nsnik.notes.view

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nrs.nsnik.notes.data.NoteEntity
import com.nrs.nsnik.notes.data.NotesDatabase
import com.nrs.nsnik.notes.util.DbUtil
import com.nrs.nsnik.notes.util.FileUtil
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Tests NoteViewModel's delegation to DbUtil by constructing DbUtil
 * directly with an in-memory Room database, bypassing Dagger and MyApplication.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NoteViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: NotesDatabase
    private lateinit var dbUtil: DbUtil
    private lateinit var tempDir: File

    @Before
    fun setUp() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NotesDatabase::class.java
        ).allowMainThreadQueries().build()

        tempDir = File(System.getProperty("java.io.tmpdir"), "note_vm_test_${System.nanoTime()}")
        tempDir.mkdirs()
        val fileUtil = FileUtil(tempDir)
        dbUtil = DbUtil(database, fileUtil)
    }

    @After
    fun tearDown() {
        database.close()
        tempDir.deleteRecursively()
        RxJavaPlugins.reset()
        RxAndroidPlugins.reset()
    }

    private fun <T> LiveData<T>.getValueBlocking(): T? {
        var value: T? = null
        val latch = CountDownLatch(1)
        val observer = Observer<T> { value = it; latch.countDown() }
        observeForever(observer)
        latch.await(2, TimeUnit.SECONDS)
        removeObserver(observer)
        return value
    }

    private fun createNote(
        title: String? = "Test",
        folderName: String? = "root",
        pinned: Int = 0,
        locked: Int = 0,
        color: String? = null,
        fileName: String? = null
    ) = NoteEntity().apply {
        this.title = title
        this.folderName = folderName
        this.pinned = pinned
        this.locked = locked
        this.color = color
        this.fileName = fileName
    }

    @Test
    fun insertNote_isRetrievableByFolder() {
        dbUtil.insertNote(createNote(title = "A", folderName = "work"))
        val notes = dbUtil.getNoteByFolderName("work").getValueBlocking()
        assertNotNull(notes)
        assertEquals(1, notes!!.size)
        assertEquals("A", notes[0].title)
    }

    @Test
    fun updateNote_changesTitle() {
        val note = createNote(title = "Old")
        database.noteDao.insertNotes(note)
        val inserted = database.noteDao.notesList.getValueBlocking()!!.first()
        inserted.title = "New"
        dbUtil.updateNote(inserted)
        val updated = dbUtil.getNoteById(inserted.uid).getValueBlocking()
        assertEquals("New", updated!!.title)
    }

    @Test
    fun deleteNote_removesFromDb() {
        val note = createNote()
        database.noteDao.insertNotes(note)
        val inserted = database.noteDao.notesList.getValueBlocking()!!.first()
        dbUtil.deleteNote(inserted)
        val remaining = dbUtil.noteList.getValueBlocking()
        assertTrue(remaining.isNullOrEmpty())
    }

    @Test
    fun deleteNoteByFolderName_removesAllInFolder() {
        database.noteDao.insertNotes(
            createNote(folderName = "trash"),
            createNote(folderName = "trash"),
            createNote(folderName = "keep")
        )
        dbUtil.deleteNoteByFolderName("trash")
        val notes = dbUtil.noteList.getValueBlocking()
        assertEquals(1, notes!!.size)
        assertEquals("keep", notes[0].folderName)
    }

    @Test
    fun getNoteById_returnsCorrectNote() {
        val ids = database.noteDao.insertNotes(createNote(title = "Find"))
        val note = dbUtil.getNoteById(ids[0].toInt()).getValueBlocking()
        assertEquals("Find", note!!.title)
    }

    @Test
    fun getNoteByFolderNameNoPinNoLock_filtersCorrectly() {
        database.noteDao.insertNotes(
            createNote(folderName = "f", pinned = 0, locked = 0, title = "match"),
            createNote(folderName = "f", pinned = 1, locked = 0, title = "pinned"),
            createNote(folderName = "f", pinned = 0, locked = 1, title = "locked")
        )
        val results = dbUtil.getNoteByFolderNameNoPinNoLock("f").getValueBlocking()
        assertEquals(1, results!!.size)
        assertEquals("match", results[0].title)
    }

    @Test
    fun searchNote_findsByTitlePrefix() {
        database.noteDao.insertNotes(
            createNote(title = "Kotlin Tips"),
            createNote(title = "Java Tips")
        )
        val results = dbUtil.searchNote("Kot").getValueBlocking()
        assertEquals(1, results!!.size)
        assertEquals("Kotlin Tips", results[0].title)
    }

    @Test
    fun getNotesByPin_returnsOnlyPinned() {
        database.noteDao.insertNotes(
            createNote(pinned = 1, title = "pinned"),
            createNote(pinned = 0, title = "not")
        )
        val pinned = dbUtil.getNotesByPin(1).getValueBlocking()
        assertEquals(1, pinned!!.size)
        assertEquals("pinned", pinned[0].title)
    }

    @Test
    fun changeNotePinStatus_updatesPinField() {
        val ids = database.noteDao.insertNotes(createNote(pinned = 0))
        dbUtil.changeNotePinStatus(ids[0].toInt(), 1)
        val note = dbUtil.getNoteById(ids[0].toInt()).getValueBlocking()
        assertEquals(1, note!!.pinned)
    }

    @Test
    fun changeNoteFolder_updatesFolder() {
        val ids = database.noteDao.insertNotes(createNote(folderName = "old"))
        dbUtil.changeNoteFolder(ids[0].toInt(), "new")
        val note = dbUtil.getNoteById(ids[0].toInt()).getValueBlocking()
        assertEquals("new", note!!.folderName)
    }

    @Test
    fun getNotesByColor_filtersCorrectly() {
        database.noteDao.insertNotes(
            createNote(color = "red"),
            createNote(color = "blue")
        )
        val results = dbUtil.getNotesByColor("red").getValueBlocking()
        assertEquals(1, results!!.size)
    }

    @Test
    fun changeNoteLockStatus_updatesLockField() {
        val ids = database.noteDao.insertNotes(createNote(locked = 0))
        dbUtil.changeNoteLockStatus(ids[0].toInt(), 1)
        val note = dbUtil.getNoteById(ids[0].toInt()).getValueBlocking()
        assertEquals(1, note!!.locked)
    }
}
