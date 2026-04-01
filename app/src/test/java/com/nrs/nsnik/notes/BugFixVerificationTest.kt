package com.nrs.nsnik.notes

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * ST-001: Verifies BUG-001, BUG-002, BUG-003 fixes are correctly applied.
 */
class BugFixVerificationTest {

    private val projectRoot: File by lazy {
        var dir = File(System.getProperty("user.dir"))
        while (dir.parentFile != null && !File(dir, "settings.gradle").exists()) {
            dir = dir.parentFile
        }
        dir
    }

    // BUG-001: ReminderUtil PendingIntent calls must include FLAG_IMMUTABLE
    @Test
    fun bug001_reminderUtilPendingIntentHasFlagImmutable() {
        val content = File(projectRoot, "app/src/main/java/com/nrs/nsnik/notes/util/ReminderUtil.kt").readText()
        val pendingIntentCalls = Regex("""PendingIntent\.getBroadcast\([^)]+\)""").findAll(content).toList()
        assertTrue("ReminderUtil.kt must contain PendingIntent.getBroadcast calls", pendingIntentCalls.isNotEmpty())
        pendingIntentCalls.forEach { match ->
            assertTrue(
                "PendingIntent.getBroadcast must include FLAG_IMMUTABLE: ${match.value}",
                match.value.contains("FLAG_IMMUTABLE")
            )
        }
    }

    // BUG-002: NotificationReceiver PendingIntent calls must include FLAG_IMMUTABLE
    @Test
    fun bug002_notificationReceiverPendingIntentHasFlagImmutable() {
        val content = File(projectRoot, "app/src/main/java/com/nrs/nsnik/notes/util/receiver/NotificationReceiver.kt").readText()
        val pendingIntentCalls = Regex("""PendingIntent\.getActivity\([^)]+\)""").findAll(content).toList()
        assertTrue("NotificationReceiver.kt must contain PendingIntent.getActivity calls", pendingIntentCalls.isNotEmpty())
        pendingIntentCalls.forEach { match ->
            assertTrue(
                "PendingIntent.getActivity must include FLAG_IMMUTABLE: ${match.value}",
                match.value.contains("FLAG_IMMUTABLE")
            )
        }
    }

    // BUG-003: CheckListObject must not implement java.io.Serializable
    @Test
    fun bug003_checkListObjectNoJavaSerializable() {
        val content = File(projectRoot, "app/src/main/java/com/nrs/nsnik/notes/model/CheckListObject.kt").readText()
        assertFalse(
            "CheckListObject.kt must not contain java.io.Serializable",
            content.contains("java.io.Serializable")
        )
        assertTrue(
            "CheckListObject.kt must retain @Serializable annotation",
            content.contains("@Serializable")
        )
    }
}
