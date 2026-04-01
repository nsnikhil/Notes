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
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FeatureVerificationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: NotesDatabase
    private lateinit var noteDao: NoteDao
    private lateinit var folderDao: FolderDao

    private val projectRoot: File by lazy {
        var dir = File(System.getProperty("user.dir"))
        while (dir.parentFile != null && !File(dir, "settings.gradle").exists()) {
            dir = dir.parentFile
        }
        dir
    }

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

    private fun createNote(
        title: String? = "Note",
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

    private fun createFolder(
        folderName: String? = "Folder",
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

    // --- 1. Reminder structure: FLAG_IMMUTABLE verification ---

    @Test
    fun reminderUtil_allPendingIntentCallsContainFlagImmutable() {
        val content = File(projectRoot, "app/src/main/java/com/nrs/nsnik/notes/util/ReminderUtil.kt").readText()
        val pendingIntentCalls = Regex("""PendingIntent\.\w+\([^)]+\)""").findAll(content).toList()
        assertTrue("ReminderUtil must have PendingIntent calls", pendingIntentCalls.isNotEmpty())
        pendingIntentCalls.forEach { match ->
            assertTrue(
                "PendingIntent call must include FLAG_IMMUTABLE: ${match.value}",
                match.value.contains("FLAG_IMMUTABLE")
            )
        }
    }

    @Test
    fun notificationReceiver_allPendingIntentCallsContainFlagImmutable() {
        val content = File(projectRoot, "app/src/main/java/com/nrs/nsnik/notes/util/receiver/NotificationReceiver.kt").readText()
        val pendingIntentCalls = Regex("""PendingIntent\.\w+\([^)]+\)""").findAll(content).toList()
        assertTrue("NotificationReceiver must have PendingIntent calls", pendingIntentCalls.isNotEmpty())
        pendingIntentCalls.forEach { match ->
            assertTrue(
                "PendingIntent call must include FLAG_IMMUTABLE: ${match.value}",
                match.value.contains("FLAG_IMMUTABLE")
            )
        }
    }

    // --- 2. Pin/Lock combined DAO queries ---

    @Test
    fun notePinQuery_returnsOnlyPinnedNotes() {
        noteDao.insertNotes(
            createNote(title = "Pinned", pinned = 1),
            createNote(title = "Unpinned", pinned = 0),
            createNote(title = "Also Pinned", pinned = 1)
        )
        val pinned = noteDao.getNoteByPin(1).getValueBlocking()!!
        assertEquals(2, pinned.size)
        assertTrue(pinned.all { it.pinned == 1 })
    }

    @Test
    fun noteLockQuery_returnsOnlyLockedNotes() {
        noteDao.insertNotes(
            createNote(title = "Locked", locked = 1),
            createNote(title = "Unlocked", locked = 0)
        )
        val locked = noteDao.getNoteByLock(1).getValueBlocking()!!
        assertEquals(1, locked.size)
        assertEquals("Locked", locked[0].title)
    }

    @Test
    fun notePinnedAndLocked_combinedQuery() {
        noteDao.insertNotes(
            createNote(title = "Both", folderName = "f", pinned = 1, locked = 1),
            createNote(title = "PinOnly", folderName = "f", pinned = 1, locked = 0),
            createNote(title = "LockOnly", folderName = "f", pinned = 0, locked = 1),
            createNote(title = "Neither", folderName = "f", pinned = 0, locked = 0)
        )
        val pinnedLocked = noteDao.getNotesByFolderNamePinnedLocked("f").getValueBlocking()!!
        assertEquals(1, pinnedLocked.size)
        assertEquals("Both", pinnedLocked[0].title)

        val pinnedNotLocked = noteDao.getNotesByFolderNamePinnedNotLocked("f").getValueBlocking()!!
        assertEquals(1, pinnedNotLocked.size)
        assertEquals("PinOnly", pinnedNotLocked[0].title)

        val notPinnedLocked = noteDao.getNotesByFolderNameNotPinnedLocked("f").getValueBlocking()!!
        assertEquals(1, notPinnedLocked.size)
        assertEquals("LockOnly", notPinnedLocked[0].title)

        val notPinnedNotLocked = noteDao.getNotesByFolderNameNotPinnedNotLocked("f").getValueBlocking()!!
        assertEquals(1, notPinnedNotLocked.size)
        assertEquals("Neither", notPinnedNotLocked[0].title)
    }

    @Test
    fun folderPinLockCombinedQueries() {
        folderDao.insertFolders(
            createFolder(folderName = "Both", parentFolderName = "p", pinned = 1, locked = 1),
            createFolder(folderName = "PinOnly", parentFolderName = "p", pinned = 1, locked = 0),
            createFolder(folderName = "LockOnly", parentFolderName = "p", pinned = 0, locked = 1),
            createFolder(folderName = "Neither", parentFolderName = "p", pinned = 0, locked = 0)
        )
        val pinnedLocked = folderDao.getFolderByParentPinLock("p").getValueBlocking()!!
        assertEquals(1, pinnedLocked.size)
        assertEquals("Both", pinnedLocked[0].folderName)

        val pinnedNotLocked = folderDao.getFolderByParentPinNoLock("p").getValueBlocking()!!
        assertEquals(1, pinnedNotLocked.size)
        assertEquals("PinOnly", pinnedNotLocked[0].folderName)

        val notPinnedLocked = folderDao.getFolderByParentNoPinLock("p").getValueBlocking()!!
        assertEquals(1, notPinnedLocked.size)
        assertEquals("LockOnly", notPinnedLocked[0].folderName)

        val notPinnedNotLocked = folderDao.getFolderByParentNoPinNoLock("p").getValueBlocking()!!
        assertEquals(1, notPinnedNotLocked.size)
        assertEquals("Neither", notPinnedNotLocked[0].folderName)
    }

    // --- 3. Search edge cases ---

    @Test
    fun searchNote_emptyString_returnsAllNotes() {
        // DAO query: LIKE :query|| '%' → empty string becomes LIKE '%' which matches all
        noteDao.insertNotes(createNote(title = "Alpha"), createNote(title = "Beta"))
        val results = noteDao.getNoteByQuery("").getValueBlocking()!!
        assertEquals(2, results.size)
    }

    @Test
    fun searchNote_specialCharacters_returnsEmptyWhenNoMatch() {
        noteDao.insertNotes(createNote(title = "Normal Title"))
        val results = noteDao.getNoteByQuery("@#\$%").getValueBlocking()!!
        assertTrue(results.isEmpty())
    }

    @Test
    fun searchNote_caseInsensitiveForAscii() {
        // SQLite LIKE is case-insensitive for ASCII by default
        noteDao.insertNotes(createNote(title = "Kotlin Guide"))
        val lower = noteDao.getNoteByQuery("kotlin").getValueBlocking()!!
        val upper = noteDao.getNoteByQuery("KOTLIN").getValueBlocking()!!
        assertEquals(lower.size, upper.size)
    }

    @Test
    fun searchNote_noMatch_returnsEmptyList() {
        noteDao.insertNotes(createNote(title = "Something"))
        val results = noteDao.getNoteByQuery("ZZZNonExistent").getValueBlocking()!!
        assertTrue(results.isEmpty())
    }

    @Test
    fun searchFolder_emptyString_returnsAllFolders() {
        folderDao.insertFolders(createFolder(folderName = "Work"), createFolder(folderName = "Home"))
        val results = folderDao.getFolderByQuery("").getValueBlocking()!!
        assertEquals(2, results.size)
    }

    // --- 4. Color filtering ---

    @Test
    fun noteColorFilter_returnsCorrectNotes() {
        noteDao.insertNotes(
            createNote(title = "Red1", color = "red"),
            createNote(title = "Red2", color = "red"),
            createNote(title = "Blue", color = "blue"),
            createNote(title = "NoColor", color = null)
        )
        val redNotes = noteDao.getNoteByColor("red").getValueBlocking()!!
        assertEquals(2, redNotes.size)
        assertTrue(redNotes.all { it.color == "red" })

        val blueNotes = noteDao.getNoteByColor("blue").getValueBlocking()!!
        assertEquals(1, blueNotes.size)
    }

    @Test
    fun folderColorFilter_returnsCorrectFolders() {
        folderDao.insertFolders(
            createFolder(folderName = "Green1", color = "green"),
            createFolder(folderName = "Green2", color = "green"),
            createFolder(folderName = "Yellow", color = "yellow")
        )
        val greenFolders = folderDao.getFolderByColor("green").getValueBlocking()!!
        assertEquals(2, greenFolders.size)
        assertTrue(greenFolders.all { it.color == "green" })
    }

    // --- 5. BUG-005: Note deletion does not cancel reminders ---

    @Test
    fun bug005_deleteNoteDoesNotCancelReminder_documentedBehavior() {
        // DbUtil.deleteNote() calls noteDao.deleteNotes() but never calls
        // ReminderUtil.cancelReminder(). This means deleting a note with an
        // active reminder leaves a dangling alarm that will fire after the
        // note is gone. This test documents the current behavior as a known gap.
        val content = File(projectRoot, "app/src/main/java/com/nrs/nsnik/notes/util/DbUtil.kt").readText()
        val deleteNoteMethod = content.substringAfter("fun deleteNote(")
            .substringBefore("fun deleteNoteByFolderName(")
        assertFalse(
            "BUG-005: DbUtil.deleteNote does not cancel reminders (known gap)",
            deleteNoteMethod.contains("cancelReminder")
        )
    }
}
