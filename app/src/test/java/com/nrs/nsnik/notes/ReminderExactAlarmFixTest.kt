package com.nrs.nsnik.notes

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * ST-004: Verifies ReminderUtil uses exact alarm scheduling instead of
 * setInexactRepeating, and includes a canScheduleExactAlarms fallback.
 */
class ReminderExactAlarmFixTest {

    private val projectRoot: File by lazy {
        var dir = File(System.getProperty("user.dir"))
        while (dir.parentFile != null && !File(dir, "settings.gradle").exists()) {
            dir = dir.parentFile
        }
        dir
    }

    private val reminderUtilSource: String by lazy {
        File(projectRoot, "app/src/main/java/com/nrs/nsnik/notes/util/ReminderUtil.kt").readText()
    }

    @Test
    fun reminderUtil_doesNotUseSetInexactRepeating() {
        // Check that setInexactRepeating is not called as a method (ignore comments)
        val nonCommentLines = reminderUtilSource.lines().filter { !it.trimStart().startsWith("//") && !it.trimStart().startsWith("*") }
        val codeOnly = nonCommentLines.joinToString("\n")
        assertFalse(
            "ReminderUtil must not call setInexactRepeating (heavily batched on API 31+)",
            codeOnly.contains("setInexactRepeating")
        )
    }

    @Test
    fun reminderUtil_usesSetExactAndAllowWhileIdle() {
        assertTrue(
            "ReminderUtil must use setExactAndAllowWhileIdle for precise alarm delivery",
            reminderUtilSource.contains("setExactAndAllowWhileIdle")
        )
    }

    @Test
    fun reminderUtil_checksCanScheduleExactAlarms() {
        assertTrue(
            "ReminderUtil must check canScheduleExactAlarms() for API 31+ permission gate",
            reminderUtilSource.contains("canScheduleExactAlarms()")
        )
    }

    @Test
    fun reminderUtil_fallsBackToSetAndAllowWhileIdle() {
        assertTrue(
            "ReminderUtil must fall back to setAndAllowWhileIdle when exact alarms are not permitted",
            reminderUtilSource.contains("setAndAllowWhileIdle")
        )
    }

    @Test
    fun manifest_declaresScheduleExactAlarmPermission() {
        val manifest = File(projectRoot, "app/src/main/AndroidManifest.xml").readText()
        assertTrue(
            "AndroidManifest must declare SCHEDULE_EXACT_ALARM permission",
            manifest.contains("android.permission.SCHEDULE_EXACT_ALARM")
        )
    }

    @Test
    fun notificationReceiver_postsNotification() {
        val receiverSource = File(
            projectRoot,
            "app/src/main/java/com/nrs/nsnik/notes/util/receiver/NotificationReceiver.kt"
        ).readText()
        assertTrue(
            "NotificationReceiver must call notificationManager.notify() to post the notification",
            receiverSource.contains("notificationManager?.notify(") || receiverSource.contains("notificationManager.notify(")
        )
        assertTrue(
            "NotificationReceiver must create a NotificationChannel",
            receiverSource.contains("createNotificationChannel")
        )
    }
}
