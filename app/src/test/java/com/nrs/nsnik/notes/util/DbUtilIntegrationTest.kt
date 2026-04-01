package com.nrs.nsnik.notes.util

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nrs.nsnik.notes.data.FolderEntity
import com.nrs.nsnik.notes.data.NoteEntity
import com.nrs.nsnik.notes.data.NotesDatabase
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
 * Integration tests for DbUtil verifying that DAO operations and
 * FileUtil side-effects work together correctly through the RxJava pipeline.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DbUtilIntegrationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: NotesDatabase
    private lateinit var dbUtil: DbUtil
    private lateinit var fileUtil: FileUtil
    private lateinit var tempDir: File

    @Before
    fun setUp() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NotesDatabase::class.java
        ).allowMainThreadQueries().build()

        tempDir = File(System.getProperty("java.io.tmpdir"), "dbutil_test_${System.nanoTime()}")
        tempDir.mkdirs()
        fileUtil = FileUtil(tempDir)
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
        fileName: String? = null,
        pinned: Int = 0,
        locked: Int = 0
    ) = NoteEntity().apply {
        this.title = title
        this.folderName = folderName
        this.fileName = fileName
        this.pinned = pinned
        this.locked = locked
    }

    private fun createFolder(
        folderName: String? = "test",
        parentFolderName: String? = "root",
        pinned: Int = 0,
        locked: Int = 0
    ) = FolderEntity().apply {
        this.folderName = folderName
        this.parentFolderName = parentFolderName
        this.pinned = pinned
        this.locked = locked
    }

    @Test
    fun insertNote_withFileName_savesFileToFileSystem() {
        val note = createNote(title = "File Note", fileName = "note1.json")
        dbUtil.insertNote(note)

        val notes = dbUtil.noteList.getValueBlocking()
        assertNotNull(notes)
        assertEquals(1, notes!!.size)

        // insertNote calls FileUtil.saveNote which writes a file
        val savedFile = File(tempDir, "note1.json")
        assertTrue("File should be saved by FileUtil", savedFile.exists())
    }

    @Test
    fun insertNote_withoutFileName_doesNotCrash() {
        // fileName is null, so FileUtil.saveNote should be skipped (NPE guard via it.fileName!!)
        // The DAO insert still succeeds
        val note = createNote(title = "No File", fileName = null)
        dbUtil.insertNote(note)

        val notes = dbUtil.noteList.getValueBlocking()
        assertNotNull(notes)
        assertEquals(1, notes!!.size)
        assertEquals("No File", notes[0].title)
    }

    @Test
    fun updateNote_withFileName_updatesFileOnDisk() {
        val note = createNote(title = "Original", fileName = "update_test.json")
        database.noteDao.insertNotes(note)
        val inserted = database.noteDao.notesList.getValueBlocking()!!.first()

        inserted.title = "Updated"
        inserted.fileName = "update_test.json"
        dbUtil.updateNote(inserted)

        val updated = dbUtil.getNoteById(inserted.uid).getValueBlocking()
        assertEquals("Updated", updated!!.title)

        val savedFile = File(tempDir, "update_test.json")
        assertTrue("File should exist after update", savedFile.exists())
    }

    @Test
    fun deleteNote_removesFromDatabase() {
        val note = createNote(title = "ToDelete")
        database.noteDao.insertNotes(note)
        val inserted = database.noteDao.notesList.getValueBlocking()!!.first()

        dbUtil.deleteNote(inserted)

        val remaining = dbUtil.noteList.getValueBlocking()
        assertTrue(remaining.isNullOrEmpty())
    }

    @Test
    fun insertFolder_andRetrieveByParent() {
        dbUtil.insertFolder(createFolder(folderName = "child", parentFolderName = "root"))

        val folders = dbUtil.getFolderByParent("root").getValueBlocking()
        assertNotNull(folders)
        assertEquals(1, folders!!.size)
        assertEquals("child", folders[0].folderName)
    }

    @Test
    fun deleteNoteByFolderName_removesAllNotesInFolder() {
        database.noteDao.insertNotes(
            createNote(title = "A", folderName = "work"),
            createNote(title = "B", folderName = "work"),
            createNote(title = "C", folderName = "personal")
        )

        dbUtil.deleteNoteByFolderName("work")

        val remaining = dbUtil.noteList.getValueBlocking()
        assertEquals(1, remaining!!.size)
        assertEquals("personal", remaining[0].folderName)
    }

    @Test
    fun noteAndFolderOperations_areIndependent() {
        dbUtil.insertNote(createNote(title = "Note1"))
        dbUtil.insertFolder(createFolder(folderName = "Folder1"))

        val notes = dbUtil.noteList.getValueBlocking()
        val folders = dbUtil.folderList.getValueBlocking()

        assertEquals(1, notes!!.size)
        assertEquals(1, folders!!.size)
    }

    @Test
    fun changeNotePinAndLock_updatesCorrectly() {
        val ids = database.noteDao.insertNotes(createNote(pinned = 0, locked = 0))
        val noteId = ids[0].toInt()

        dbUtil.changeNotePinStatus(noteId, 1)
        dbUtil.changeNoteLockStatus(noteId, 1)

        val note = dbUtil.getNoteById(noteId).getValueBlocking()
        assertEquals(1, note!!.pinned)
        assertEquals(1, note.locked)
    }

    @Test
    fun changeFolderParent_updatesParentField() {
        val ids = database.folderDao.insertFolders(createFolder(parentFolderName = "old"))
        dbUtil.changeFolderParent(ids[0].toInt(), "new")

        val folder = dbUtil.getFolderById(ids[0].toInt()).getValueBlocking()
        assertEquals("new", folder!!.parentFolderName)
    }
}
