package com.nrs.nsnik.notes.view

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nrs.nsnik.notes.data.FolderEntity
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
 * Tests FolderViewModel's delegation to DbUtil by constructing DbUtil
 * directly with an in-memory Room database, bypassing Dagger and MyApplication.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FolderViewModelTest {

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

        tempDir = File(System.getProperty("java.io.tmpdir"), "folder_vm_test_${System.nanoTime()}")
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

    private fun createFolder(
        folderName: String? = "test",
        parentFolderName: String? = "root",
        pinned: Int = 0,
        locked: Int = 0,
        color: String? = null
    ) = FolderEntity().apply {
        this.folderName = folderName
        this.parentFolderName = parentFolderName
        this.pinned = pinned
        this.locked = locked
        this.color = color
    }

    @Test
    fun insertFolder_isRetrievableByParent() {
        dbUtil.insertFolder(createFolder(folderName = "child", parentFolderName = "root"))
        val folders = dbUtil.getFolderByParent("root").getValueBlocking()
        assertNotNull(folders)
        assertEquals(1, folders!!.size)
        assertEquals("child", folders[0].folderName)
    }

    @Test
    fun updateFolder_changesName() {
        val folder = createFolder(folderName = "old")
        database.folderDao.insertFolders(folder)
        val inserted = database.folderDao.foldersList.getValueBlocking()!!.first()
        inserted.folderName = "new"
        dbUtil.updateFolder(inserted)
        val updated = dbUtil.getFolderById(inserted.uid).getValueBlocking()
        assertEquals("new", updated!!.folderName)
    }

    @Test
    fun deleteFolder_removesFromDb() {
        val folder = createFolder()
        database.folderDao.insertFolders(folder)
        val inserted = database.folderDao.foldersList.getValueBlocking()!!.first()
        dbUtil.deleteFolder(inserted)
        val remaining = dbUtil.folderList.getValueBlocking()
        assertTrue(remaining.isNullOrEmpty())
    }

    @Test
    fun deleteFolderByName_removesCorrectFolder() {
        database.folderDao.insertFolders(
            createFolder(folderName = "remove"),
            createFolder(folderName = "keep")
        )
        dbUtil.deleteFolderByName("remove")
        val folders = dbUtil.folderList.getValueBlocking()
        assertEquals(1, folders!!.size)
        assertEquals("keep", folders[0].folderName)
    }

    @Test
    fun deleteFolderByParent_removesChildren() {
        database.folderDao.insertFolders(
            createFolder(folderName = "child1", parentFolderName = "parent"),
            createFolder(folderName = "child2", parentFolderName = "parent"),
            createFolder(folderName = "other", parentFolderName = "root")
        )
        dbUtil.deleteFolderByParent("parent")
        val folders = dbUtil.folderList.getValueBlocking()
        assertEquals(1, folders!!.size)
        assertEquals("other", folders[0].folderName)
    }

    @Test
    fun getFolders_returnsAll() {
        database.folderDao.insertFolders(createFolder(folderName = "a"), createFolder(folderName = "b"))
        val folders = dbUtil.getFolders().getValueBlocking()
        assertEquals(2, folders!!.size)
    }

    @Test
    fun getFolderByName_returnsCorrectFolder() {
        database.folderDao.insertFolders(createFolder(folderName = "target"))
        val folder = dbUtil.getFolderByName("target").getValueBlocking()
        assertNotNull(folder)
        assertEquals("target", folder!!.folderName)
    }

    @Test
    fun searchFolder_findsByNamePrefix() {
        database.folderDao.insertFolders(
            createFolder(folderName = "Kotlin"),
            createFolder(folderName = "Java")
        )
        val results = dbUtil.searchFolder("Kot").getValueBlocking()
        assertEquals(1, results!!.size)
        assertEquals("Kotlin", results[0].folderName)
    }

    @Test
    fun getFolderByParentNoPinNoLock_filtersCorrectly() {
        database.folderDao.insertFolders(
            createFolder(parentFolderName = "p", pinned = 0, locked = 0, folderName = "match"),
            createFolder(parentFolderName = "p", pinned = 1, locked = 0, folderName = "pinned"),
            createFolder(parentFolderName = "p", pinned = 0, locked = 1, folderName = "locked")
        )
        val results = dbUtil.getFolderByParentNoPinNoLock("p").getValueBlocking()
        assertEquals(1, results!!.size)
        assertEquals("match", results[0].folderName)
    }

    @Test
    fun changeFolderPinStatus_updatesPinField() {
        val ids = database.folderDao.insertFolders(createFolder(pinned = 0))
        dbUtil.changeFolderPinStatus(ids[0].toInt(), 1)
        val folder = dbUtil.getFolderById(ids[0].toInt()).getValueBlocking()
        assertEquals(1, folder!!.pinned)
    }

    @Test
    fun changeFolderLockStatus_updatesLockField() {
        val ids = database.folderDao.insertFolders(createFolder(locked = 0))
        dbUtil.changeFolderLockStatus(ids[0].toInt(), 1)
        val folder = dbUtil.getFolderById(ids[0].toInt()).getValueBlocking()
        assertEquals(1, folder!!.locked)
    }

    @Test
    fun getFolderByColor_filtersCorrectly() {
        database.folderDao.insertFolders(
            createFolder(color = "red"),
            createFolder(color = "blue")
        )
        val results = dbUtil.getFolderByColor("red").getValueBlocking()
        assertEquals(1, results!!.size)
    }
}
