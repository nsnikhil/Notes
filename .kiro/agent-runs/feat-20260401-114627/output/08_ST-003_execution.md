# ST-003 Execution Report: Fix Reminders

## Status: ✅ PASSED

## Issues Found & Fixed

### 1. Past-time validation (ReminderUtil.kt)
- **Problem**: `setNotification()` set the calendar to the user-chosen hour/minute but never checked if that time had already passed today. If it had, `calendar.timeInMillis` was in the past and the alarm would fire immediately.
- **Fix**: Added a check after setting hour/minute/second — if `calendar.timeInMillis <= System.currentTimeMillis()`, roll forward by one day. Also zeroed out seconds for consistent timing.

### 2. SCHEDULE_EXACT_ALARM permission (AndroidManifest.xml)
- **Problem**: Missing from manifest. Required on API 31+ for exact alarm scheduling.
- **Fix**: Added `<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />`.

### 3. Notification channel creation (NotificationReceiver.kt)
- **Problem**: None — channel was already created before `notify()`. Verified correct.

### 4. Channel ID mismatch (NotificationReceiver.kt)
- **Problem**: `NotificationCompat.Builder` constructor used `R.string.notificationChannelReminder` (the channel *name*: `"notificationChannelReminder"`), but the actual channel was created with `R.string.notificationChannelReminderId` (the channel *ID*: `"notificationChannelReminderId"`). A later `setChannelId()` call patched it, but the initial constructor was wrong.
- **Fix**: Moved `channelId` resolution above the Builder constructor so both the Builder and `createNotificationChannel()` use the same `notificationChannelReminderId` string. Removed the redundant `setChannelId()` call.

## Files Modified
| File | Change |
|------|--------|
| `app/src/main/java/com/nrs/nsnik/notes/util/ReminderUtil.kt` | Past-time roll-forward, zero seconds |
| `app/src/main/java/com/nrs/nsnik/notes/util/receiver/NotificationReceiver.kt` | Channel ID mismatch fix |
| `app/src/main/AndroidManifest.xml` | SCHEDULE_EXACT_ALARM permission |

## Verification
- `./gradlew assembleDebug` — ✅ BUILD SUCCESSFUL
- `./gradlew testDemoDebugUnitTest` — ✅ BUILD SUCCESSFUL

## Exit Criteria Checklist
- [x] ReminderUtil handles past-time (rolls to next day)
- [x] SCHEDULE_EXACT_ALARM permission in AndroidManifest.xml
- [x] NotificationReceiver creates notification channel before posting (was already correct)
- [x] Channel IDs match between channel creation and notification builder
- [x] `./gradlew assembleDebug` succeeds
- [x] `./gradlew testDemoDebugUnitTest` passes
