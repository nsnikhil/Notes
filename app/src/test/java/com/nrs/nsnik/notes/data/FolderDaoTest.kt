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
class FolderDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: NotesDatabase
    private lateinit var folderDao: FolderDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NotesDatabase::class.java
        ).allowMainThreadQueries().build()
        folderDao = database.folderDao
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createFolder(
        folderName: String? = "TestFolder",
        parentFolderName: String? = "root",
        pinned: Int = 0,
        locked: Int = 0,
        color: String? = null
    ): FolderEntity = FolderEntity().apply {
        this.folderName = folderName
        this.parentFolderName = parentFolderName
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
    fun insertFolder_returnsGeneratedId() {
        val ids = folderDao.insertFolders(createFolder())
        assertEquals(1, ids.size)
        assertTrue(ids[0] > 0)
    }

    @Test
    fun getAllFolders_returnsInsertedFolders() {
        folderDao.insertFolders(createFolder(folderName = "A"), createFolder(folderName = "B"))
        val folders = folderDao.foldersList.getValueBlocking()
        assertNotNull(folders)
        assertEquals(2, folders!!.size)
    }

    @Test
    fun getFolderByName_returnsCorrectFolder() {
        folderDao.insertFolders(createFolder(folderName = "Work"))
        val folder = folderDao.getFolderByName("Work").getValueBlocking()
        assertNotNull(folder)
        assertEquals("Work", folder!!.folderName)
    }

    @Test
    fun deleteFolder_removesFromDatabase() {
        val folder = createFolder()
        val ids = folderDao.insertFolders(folder)
        folder.uid = ids[0].toInt()
        folderDao.deleteFolders(folder)
        val folders = folderDao.foldersList.getValueBlocking()
        assertTrue(folders.isNullOrEmpty())
    }

    @Test
    fun updateFolder_changesFields() {
        val folder = createFolder(folderName = "Original")
        val ids = folderDao.insertFolders(folder)
        folder.uid = ids[0].toInt()
        folder.folderName = "Renamed"
        folderDao.updateFolders(folder)
        val updated = folderDao.getFolder(ids[0].toInt()).getValueBlocking()
        assertEquals("Renamed", updated!!.folderName)
    }

    @Test
    fun searchFolders_byNamePrefix() {
        folderDao.insertFolders(
            createFolder(folderName = "Kotlin"),
            createFolder(folderName = "Java")
        )
        val results = folderDao.getFolderByQuery("Kot").getValueBlocking()
        assertNotNull(results)
        assertEquals(1, results!!.size)
        assertEquals("Kotlin", results[0].folderName)
    }

    @Test
    fun getPinnedFolders_returnsOnlyPinned() {
        folderDao.insertFolders(
            createFolder(folderName = "Pinned", pinned = 1),
            createFolder(folderName = "NotPinned", pinned = 0)
        )
        val pinned = folderDao.getFolderByPin(1).getValueBlocking()
        assertNotNull(pinned)
        assertEquals(1, pinned!!.size)
        assertEquals("Pinned", pinned[0].folderName)
    }

    @Test
    fun getFolderByParent_filtersCorrectly() {
        folderDao.insertFolders(
            createFolder(folderName = "Child1", parentFolderName = "parent"),
            createFolder(folderName = "Child2", parentFolderName = "other")
        )
        val children = folderDao.getFolderByParent("parent").getValueBlocking()
        assertNotNull(children)
        assertEquals(1, children!!.size)
        assertEquals("Child1", children[0].folderName)
    }

    @Test
    fun changeFolderPinStatus_updatesPinField() {
        val ids = folderDao.insertFolders(createFolder(pinned = 0))
        folderDao.changeFolderPinStatus(ids[0].toInt(), 1)
        val folder = folderDao.getFolder(ids[0].toInt()).getValueBlocking()
        assertEquals(1, folder!!.pinned)
    }

    @Test
    fun deleteFolderByName_removesCorrectFolder() {
        folderDao.insertFolders(
            createFolder(folderName = "delete_me"),
            createFolder(folderName = "keep_me")
        )
        folderDao.deleteFolderByName("delete_me")
        val folders = folderDao.foldersList.getValueBlocking()
        assertEquals(1, folders!!.size)
        assertEquals("keep_me", folders[0].folderName)
    }

    @Test
    fun deleteFolderByParent_removesAllChildren() {
        folderDao.insertFolders(
            createFolder(folderName = "C1", parentFolderName = "trash"),
            createFolder(folderName = "C2", parentFolderName = "trash"),
            createFolder(folderName = "C3", parentFolderName = "keep")
        )
        folderDao.deleteFolderByParent("trash")
        val folders = folderDao.foldersList.getValueBlocking()
        assertEquals(1, folders!!.size)
        assertEquals("keep", folders[0].parentFolderName)
    }

    @Test
    fun getFolderByColor_filtersCorrectly() {
        folderDao.insertFolders(
            createFolder(color = "green"),
            createFolder(color = "yellow")
        )
        val greenFolders = folderDao.getFolderByColor("green").getValueBlocking()
        assertEquals(1, greenFolders!!.size)
    }
}
