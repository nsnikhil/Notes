package com.nrs.nsnik.notes

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nrs.nsnik.notes.data.FolderEntity
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FolderCascadeDeleteTest {

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

        tempDir = File(System.getProperty("java.io.tmpdir"), "cascade_test_${System.nanoTime()}")
        tempDir.mkdirs()
        dbUtil = DbUtil(database, FileUtil(tempDir))
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

    private fun insertFolder(name: String, parent: String) {
        database.folderDao.insertFolders(FolderEntity().apply {
            folderName = name
            parentFolderName = parent
        })
    }

    private fun insertNote(title: String, folder: String) {
        database.noteDao.insertNotes(NoteEntity().apply {
            this.title = title
            this.folderName = folder
        })
    }

    @Test
    fun deleteFolderWithNotes_notesDeleted() {
        insertFolder("work", "root")
        insertNote("note1", "work")
        insertNote("note2", "work")

        dbUtil.deleteFolderCascade("work")

        val folders = database.folderDao.foldersList.getValueBlocking()
        val notes = database.noteDao.notesList.getValueBlocking()
        assertTrue(folders.isNullOrEmpty())
        assertTrue(notes.isNullOrEmpty())
    }

    @Test
    fun deleteFolderWithSubFolder_subFolderDeleted() {
        insertFolder("parent", "root")
        insertFolder("child", "parent")

        dbUtil.deleteFolderCascade("parent")

        val folders = database.folderDao.foldersList.getValueBlocking()
        assertTrue(folders.isNullOrEmpty())
    }

    @Test
    fun deleteFolderWithThreeLevelsDeep_allLevelsDeleted() {
        insertFolder("level1", "root")
        insertFolder("level2", "level1")
        insertFolder("level3", "level2")

        dbUtil.deleteFolderCascade("level1")

        val folders = database.folderDao.foldersList.getValueBlocking()
        assertTrue(folders.isNullOrEmpty())
    }

    @Test
    fun deleteFolderCascade_notesInSubFoldersAlsoDeleted() {
        insertFolder("parent", "root")
        insertFolder("child", "parent")
        insertFolder("grandchild", "child")
        insertNote("parentNote", "parent")
        insertNote("childNote", "child")
        insertNote("grandchildNote", "grandchild")

        dbUtil.deleteFolderCascade("parent")

        val folders = database.folderDao.foldersList.getValueBlocking()
        val notes = database.noteDao.notesList.getValueBlocking()
        assertTrue(folders.isNullOrEmpty())
        assertTrue(notes.isNullOrEmpty())
    }

    @Test
    fun deleteFolderCascade_otherFoldersNotAffected() {
        insertFolder("toDelete", "root")
        insertFolder("child", "toDelete")
        insertFolder("keep", "root")
        insertNote("keepNote", "keep")

        dbUtil.deleteFolderCascade("toDelete")

        val folders = database.folderDao.foldersList.getValueBlocking()
        val notes = database.noteDao.notesList.getValueBlocking()
        assertEquals(1, folders!!.size)
        assertEquals("keep", folders[0].folderName)
        assertEquals(1, notes!!.size)
        assertEquals("keepNote", notes[0].title)
    }

    @Test
    fun deleteFolderCascade_multipleChildrenAtSameLevel() {
        insertFolder("parent", "root")
        insertFolder("childA", "parent")
        insertFolder("childB", "parent")
        insertNote("noteA", "childA")
        insertNote("noteB", "childB")

        dbUtil.deleteFolderCascade("parent")

        val folders = database.folderDao.foldersList.getValueBlocking()
        val notes = database.noteDao.notesList.getValueBlocking()
        assertTrue(folders.isNullOrEmpty())
        assertTrue(notes.isNullOrEmpty())
    }
}
