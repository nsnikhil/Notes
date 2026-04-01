package com.nrs.nsnik.notes.util

import com.nrs.nsnik.notes.data.NoteEntity
import kotlinx.serialization.json.Json
import okio.ByteString.Companion.toByteString
import okio.buffer
import okio.sink
import okio.source
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FileUtilTest {

    private lateinit var tempDir: File
    private lateinit var fileUtil: FileUtil

    @Before
    fun setUp() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "fileutil_test_${System.nanoTime()}")
        tempDir.mkdirs()
        fileUtil = FileUtil(tempDir)
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun serialize_deserialize_roundTrip() {
        val note = NoteEntity().apply {
            title = "Test Title"
            noteContent = "Some content"
            folderName = "root"
            pinned = 1
            locked = 0
            dateModified = Date(1700000000000L)
            color = "#FF0000"
        }

        val file = File(tempDir, "test_note.dat")
        val jsonString = Json.encodeToString(NoteEntity.serializer(), note)
        val byteString = jsonString.encodeToByteArray().toByteString()
        file.sink().buffer().use { it.write(byteString) }

        val readData = file.source().buffer().use { it.readByteString() }
        val restored = Json.decodeFromString(NoteEntity.serializer(), readData.utf8())

        assertEquals("Test Title", restored.title)
        assertEquals("root", restored.folderName)
        assertEquals(1, restored.pinned)
        assertEquals(0, restored.locked)
        assertEquals(1700000000000L, restored.dateModified!!.time)
        assertEquals("#FF0000", restored.color)
    }

    @Test
    fun serialize_withNullFields() {
        val note = NoteEntity().apply {
            title = null
            folderName = null
            dateModified = null
            color = null
        }
        val jsonString = Json.encodeToString(NoteEntity.serializer(), note)
        val restored = Json.decodeFromString(NoteEntity.serializer(), jsonString)

        assertNull(restored.title)
        assertNull(restored.folderName)
        assertNull(restored.dateModified)
        assertNull(restored.color)
    }

    @Test
    fun readFromMissingFile_throwsException() {
        val missingFile = File(tempDir, "nonexistent.dat")
        try {
            missingFile.source().buffer().use { it.readByteString() }
            fail("Expected exception for missing file")
        } catch (expected: Exception) {
            // FileNotFoundException is expected
        }
    }

    @Test
    fun rootFolder_isAccessible() {
        assertEquals(tempDir, fileUtil.rootFolder)
        assertTrue(fileUtil.rootFolder.exists())
    }
}
