package com.nrs.nsnik.notes

import org.junit.Assert.*
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * ST-002: Verifies the camera FileProvider path fix and gallery permission removal.
 */
class CameraAndGalleryFixTest {

    private val projectRoot: File by lazy {
        var dir = File(System.getProperty("user.dir"))
        while (dir.parentFile != null && !File(dir, "settings.gradle").exists()) {
            dir = dir.parentFile
        }
        dir
    }

    @Test
    fun filePathsXml_usesExternalFilesPath_notExternalPath() {
        val filePathsXml = File(projectRoot, "app/src/main/res/xml/file_paths.xml")
        assertTrue("file_paths.xml must exist", filePathsXml.exists())

        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(filePathsXml)
        val root = doc.documentElement

        assertEquals(
            "file_paths.xml must NOT contain <external-path> (restricted on API 29+)",
            0, root.getElementsByTagName("external-path").length
        )

        val entries = root.getElementsByTagName("external-files-path")
        assertTrue("file_paths.xml must contain <external-files-path>", entries.length > 0)
        assertEquals("Pictures", (entries.item(0) as Element).getAttribute("path"))
    }

    @Test
    fun filePathsXml_doesNotHardcodeApplicationId() {
        val content = File(projectRoot, "app/src/main/res/xml/file_paths.xml").readText()
        assertFalse("Must not hardcode .debug path", content.contains("com.nrs.nsnik.notes.debug"))
        assertFalse("Must not hardcode .demo path", content.contains("com.nrs.nsnik.notes.demo"))
        assertFalse("Must not hardcode .full path", content.contains("com.nrs.nsnik.notes.full"))
    }

    @Test
    fun manifest_fileProviderAuthority_usesApplicationIdPlaceholder() {
        val content = File(projectRoot, "app/src/main/AndroidManifest.xml").readText()
        assertTrue(
            "Authority must use \${applicationId}.fileprovider",
            content.contains("\${applicationId}.fileprovider")
        )
        assertFalse(
            "Authority must NOT be hardcoded",
            content.contains("\"com.nrs.nsnik.notes.fileprovider\"")
        )
    }

    @Test
    fun newNoteFragment_galleryLaunch_hasNoPermissionCheck() {
        val content = File(projectRoot,
            "app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt").readText()

        assertFalse(
            "Gallery must not use checkPermission with READ_EXTERNAL_STORAGE",
            content.contains("READ_EXTERNAL_STORAGE_PERMISSION, ::startGalleryIntent")
        )
        assertFalse(
            "startGalleryIntent() should be removed",
            content.contains("fun startGalleryIntent()")
        )
        assertTrue(
            "Gallery must call galleryLauncher.launch directly",
            content.contains("galleryLauncher.launch(\"image/*\")")
        )
    }

    @Test
    fun newNoteFragment_cameraIntent_usesBuildConfigAuthority() {
        val content = File(projectRoot,
            "app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt").readText()

        assertTrue(
            "Camera must use BuildConfig.APPLICATION_ID for authority",
            content.contains("BuildConfig.APPLICATION_ID")
        )
        assertFalse(
            "Camera must NOT hardcode authority string",
            content.contains("\"com.nrs.nsnik.notes.fileprovider\"")
        )
    }
}
