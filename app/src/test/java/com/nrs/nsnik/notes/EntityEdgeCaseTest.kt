package com.nrs.nsnik.notes

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nrs.nsnik.notes.data.*
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
class EntityEdgeCaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: NotesDatabase
    private lateinit var noteDao: NoteDao
    private lateinit var folderDao: FolderDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NotesDatabase::class.java
        ).allowMainThreadQueries().build()
        noteDao = database.noteDao
        folderDao = database.folderDao
    }

    @After
    fun tearDown() {
        database.close()
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

    // --- NoteEntity edge cases ---

    @Test
    fun noteEntity_nullTitle_persistsSuccessfully() {
        val note = NoteEntity().apply { title = null; folderName = "root" }
        val ids = noteDao.insertNotes(note)
        val retrieved = noteDao.getNote(ids[0].toInt()).getValueBlocking()
        assertNotNull(retrieved)
        assertNull(retrieved!!.title)
    }

    @Test
    fun noteEntity_emptyStringTitle_persistsSuccessfully() {
        val note = NoteEntity().apply { title = ""; folderName = "root" }
        val ids = noteDao.insertNotes(note)
        val retrieved = noteDao.getNote(ids[0].toInt()).getValueBlocking()
        assertEquals("", retrieved!!.title)
    }

    @Test
    fun noteEntity_specialCharactersInTitle_persistsSuccessfully() {
        val specialTitle = "Note: <test> & 'quotes' \"double\" @#\$%"
        val note = NoteEntity().apply { title = specialTitle; folderName = "root" }
        val ids = noteDao.insertNotes(note)
        val retrieved = noteDao.getNote(ids[0].toInt()).getValueBlocking()
        assertEquals(specialTitle, retrieved!!.title)
    }

    @Test
    fun noteEntity_unicodeInTitle_persistsSuccessfully() {
        val unicodeTitle = "笔记 📝 Ñoté"
        val note = NoteEntity().apply { title = unicodeTitle; folderName = "root" }
        val ids = noteDao.insertNotes(note)
        val retrieved = noteDao.getNote(ids[0].toInt()).getValueBlocking()
        assertEquals(unicodeTitle, retrieved!!.title)
    }

    @Test
    fun noteEntity_nullFolderName_persistsSuccessfully() {
        val note = NoteEntity().apply { title = "Orphan"; folderName = null }
        val ids = noteDao.insertNotes(note)
        val retrieved = noteDao.getNote(ids[0].toInt()).getValueBlocking()
        assertNull(retrieved!!.folderName)
    }

    @Test
    fun noteEntity_nullColor_persistsSuccessfully() {
        val note = NoteEntity().apply { title = "NoColor"; color = null }
        val ids = noteDao.insertNotes(note)
        val retrieved = noteDao.getNote(ids[0].toInt()).getValueBlocking()
        assertNull(retrieved!!.color)
    }

    @Test
    fun noteEntity_allNullableFieldsNull_persistsSuccessfully() {
        val note = NoteEntity().apply {
            title = null; fileName = null; folderName = null
            color = null; dateModified = null
        }
        val ids = noteDao.insertNotes(note)
        val retrieved = noteDao.getNote(ids[0].toInt()).getValueBlocking()
        assertNotNull(retrieved)
        assertNull(retrieved!!.title)
        assertNull(retrieved.fileName)
        assertNull(retrieved.folderName)
        assertNull(retrieved.color)
    }

    @Test
    fun noteEntity_defaultPinnedAndLocked_areZero() {
        val note = NoteEntity()
        assertEquals(0, note.pinned)
        assertEquals(0, note.locked)
    }

    // --- FolderEntity edge cases ---

    @Test
    fun folderEntity_nullFolderName_persistsSuccessfully() {
        val folder = FolderEntity().apply { folderName = null; parentFolderName = "root" }
        val ids = folderDao.insertFolders(folder)
        val retrieved = folderDao.getFolder(ids[0].toInt()).getValueBlocking()
        assertNotNull(retrieved)
        assertNull(retrieved!!.folderName)
    }

    @Test
    fun folderEntity_emptyStringFolderName_persistsSuccessfully() {
        val folder = FolderEntity().apply { folderName = ""; parentFolderName = "root" }
        val ids = folderDao.insertFolders(folder)
        val retrieved = folderDao.getFolder(ids[0].toInt()).getValueBlocking()
        assertEquals("", retrieved!!.folderName)
    }

    @Test
    fun folderEntity_specialCharactersInName_persistsSuccessfully() {
        val specialName = "Folder: <test> & 'quotes' \"double\" @#\$%"
        val folder = FolderEntity().apply { folderName = specialName; parentFolderName = "root" }
        val ids = folderDao.insertFolders(folder)
        val retrieved = folderDao.getFolder(ids[0].toInt()).getValueBlocking()
        assertEquals(specialName, retrieved!!.folderName)
    }

    @Test
    fun folderEntity_unicodeInName_persistsSuccessfully() {
        val unicodeName = "文件夹 📁 Földér"
        val folder = FolderEntity().apply { folderName = unicodeName; parentFolderName = "root" }
        val ids = folderDao.insertFolders(folder)
        val retrieved = folderDao.getFolder(ids[0].toInt()).getValueBlocking()
        assertEquals(unicodeName, retrieved!!.folderName)
    }

    @Test
    fun folderEntity_nullParentFolderName_persistsSuccessfully() {
        val folder = FolderEntity().apply { folderName = "TopLevel"; parentFolderName = null }
        val ids = folderDao.insertFolders(folder)
        val retrieved = folderDao.getFolder(ids[0].toInt()).getValueBlocking()
        assertNull(retrieved!!.parentFolderName)
    }

    @Test
    fun folderEntity_allNullableFieldsNull_persistsSuccessfully() {
        val folder = FolderEntity().apply {
            folderName = null; parentFolderName = null; color = null
        }
        val ids = folderDao.insertFolders(folder)
        val retrieved = folderDao.getFolder(ids[0].toInt()).getValueBlocking()
        assertNotNull(retrieved)
        assertNull(retrieved!!.folderName)
        assertNull(retrieved.parentFolderName)
        assertNull(retrieved.color)
    }

    @Test
    fun folderEntity_defaultPinnedAndLocked_areZero() {
        val folder = FolderEntity()
        assertEquals(0, folder.pinned)
        assertEquals(0, folder.locked)
    }
}
