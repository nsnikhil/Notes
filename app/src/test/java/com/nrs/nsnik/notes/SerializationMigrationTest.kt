package com.nrs.nsnik.notes

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * ST-008: Verifies Twitter Serial is fully replaced with kotlinx.serialization.
 */
class SerializationMigrationTest {

    private val projectRoot: File by lazy {
        var dir = File(System.getProperty("user.dir"))
        while (dir.parentFile != null && !File(dir, "settings.gradle").exists()) {
            dir = dir.parentFile
        }
        dir
    }

    private val appBuildGradle: String by lazy { File(projectRoot, "app/build.gradle").readText() }
    private val rootBuildGradle: String by lazy { File(projectRoot, "build.gradle").readText() }
    private val appSrcDir: File get() = File(projectRoot, "app/src")

    private fun mainSourceFiles(): List<File> =
        File(appSrcDir, "main").walkTopDown().filter { it.isFile && (it.extension == "kt" || it.extension == "java") }.toList()

    // --- SC1: No com.twitter.serial references ---

    @Test
    fun noTwitterSerialInSource() {
        val matches = mainSourceFiles().filter { it.readText().contains("com.twitter.serial") }
        assertTrue("Found com.twitter.serial in: ${matches.map { it.path }}", matches.isEmpty())
    }

    @Test
    fun noTwitterSerialInBuildGradle() {
        assertFalse("app/build.gradle must not reference com.twitter.serial", appBuildGradle.contains("com.twitter.serial"))
    }

    // --- SC2: No ByteBufferSerial references ---

    @Test
    fun noByteBufferSerialInSource() {
        val matches = mainSourceFiles().filter { it.readText().contains("ByteBufferSerial") }
        assertTrue("Found ByteBufferSerial in: ${matches.map { it.path }}", matches.isEmpty())
    }

    // --- kotlinx.serialization plugin applied ---

    @Test
    fun serializationPluginApplied() {
        assertTrue("app/build.gradle must apply kotlinx-serialization plugin", appBuildGradle.contains("kotlinx-serialization"))
    }

    @Test
    fun serializationClasspathInRootBuildGradle() {
        assertTrue("root build.gradle must have kotlin-serialization classpath", rootBuildGradle.contains("kotlin-serialization"))
    }

    // --- kotlinx-serialization-json dependency present ---

    @Test
    fun serializationJsonDependencyPresent() {
        assertTrue("app/build.gradle must have kotlinx-serialization-json dependency", appBuildGradle.contains("kotlinx-serialization-json"))
    }

    // --- Model classes annotated with @Serializable ---

    @Test
    fun noteEntityIsSerializable() {
        val file = File(projectRoot, "app/src/main/java/com/nrs/nsnik/notes/data/NoteEntity.kt")
        assertTrue("NoteEntity.kt must exist", file.exists())
        val content = file.readText()
        assertTrue("NoteEntity must have @Serializable annotation", content.contains("@Serializable"))
        assertTrue("NoteEntity must import kotlinx.serialization.Serializable", content.contains("import kotlinx.serialization.Serializable"))
    }

    @Test
    fun folderEntityIsSerializable() {
        val file = File(projectRoot, "app/src/main/java/com/nrs/nsnik/notes/data/FolderEntity.kt")
        assertTrue("FolderEntity.kt must exist", file.exists())
        val content = file.readText()
        assertTrue("FolderEntity must have @Serializable annotation", content.contains("@Serializable"))
        assertTrue("FolderEntity must import kotlinx.serialization.Serializable", content.contains("import kotlinx.serialization.Serializable"))
    }

    @Test
    fun checkListObjectIsSerializable() {
        val file = File(projectRoot, "app/src/main/java/com/nrs/nsnik/notes/model/CheckListObject.kt")
        assertTrue("CheckListObject.kt must exist", file.exists())
        val content = file.readText()
        assertTrue("CheckListObject must have @Serializable annotation", content.contains("@Serializable"))
    }

    // --- DateSerializer exists and uses kotlinx.serialization ---

    @Test
    fun dateSerializerExists() {
        val file = File(projectRoot, "app/src/main/java/com/nrs/nsnik/notes/util/DateSerializer.kt")
        assertTrue("DateSerializer.kt must exist", file.exists())
        val content = file.readText()
        assertTrue("DateSerializer must implement KSerializer", content.contains("KSerializer"))
        assertTrue("DateSerializer must import kotlinx.serialization", content.contains("kotlinx.serialization"))
    }

    // --- Fragments use Json.encodeToString / decodeFromString ---

    @Test
    fun listFragmentUsesKotlinxJson() {
        val file = File(projectRoot, "app/src/main/java/com/nrs/nsnik/notes/view/fragments/ListFragment.kt")
        val content = file.readText()
        assertTrue("ListFragment must use kotlinx.serialization.json.Json", content.contains("kotlinx.serialization.json.Json"))
        assertTrue("ListFragment must use Json.encodeToString", content.contains("Json.encodeToString"))
    }

    @Test
    fun newNoteFragmentUsesKotlinxJson() {
        val file = File(projectRoot, "app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt")
        val content = file.readText()
        assertTrue("NewNoteFragment must use Json.decodeFromString", content.contains("Json.decodeFromString"))
    }

    @Test
    fun imageListFragmentUsesKotlinxJson() {
        val file = File(projectRoot, "app/src/main/java/com/nrs/nsnik/notes/view/fragments/ImageListFragment.kt")
        val content = file.readText()
        assertTrue("ImageListFragment must use Json.decodeFromString", content.contains("Json.decodeFromString"))
    }

    @Test
    fun createFolderDialogUsesKotlinxJson() {
        val file = File(projectRoot, "app/src/main/java/com/nrs/nsnik/notes/view/fragments/dialogFragments/CreateFolderDialog.kt")
        val content = file.readText()
        assertTrue("CreateFolderDialog must use Json.decodeFromString", content.contains("Json.decodeFromString"))
    }

    // --- Proguard rules for kotlinx.serialization ---

    @Test
    fun proguardHasKotlinxSerializationRules() {
        val proguard = File(projectRoot, "app/proguard-rules.pro").readText()
        assertTrue("proguard-rules.pro must contain kotlinx.serialization rules", proguard.contains("kotlinx.serialization"))
    }

    // --- No leftover Twitter Serial artifacts ---

    @Test
    fun noSerialBuildConfigInSource() {
        val matches = mainSourceFiles().filter {
            val t = it.readText()
            t.contains("com.twitter.serial") || t.contains("CoreBuildSerializers") || t.contains("SerializationUtils")
        }
        assertTrue("Found Twitter Serial artifacts in: ${matches.map { it.path }}", matches.isEmpty())
    }
}
