Fix the remaining broken features in the Android Notes app. The app builds, launches, and Espresso tests pass, but several features are still broken when used manually. The agent must fix each broken feature, then verify the fix with unit tests (Robolectric where needed) AND Espresso tests on the connected emulator.

The project is at the repo_path. An emulator/device is connected and available.

## Broken Features

### 1. Checklist: pressing Enter creates two items instead of one
When a user adds a checklist item and presses Enter/Next on the keyboard, two new items are created instead of one, and the cursor lands on the second item. The agent must trace the full code path: `CheckListAdapter` → `editorActions()` RxBinding subscription → `OnAddClickListener.addClickListener()` → `NewNoteFragment.addCheckListItem()` → `mCheckListAdapter.submitList()` + `notifyItemInserted()`. The likely cause is that `submitList()` with a mutable list reference AND `notifyItemInserted()` together cause a double-add — `submitList` detects the change and inserts, then `notifyItemInserted` inserts again. Or the RxBinding `editorActions()` fires multiple times for a single Enter press. Find the root cause and fix it.

### 2. Lock feature does not work on first attempt
Tapping the lock icon the first time does nothing (or shows the password dialog but doesn't toggle the lock state). Tapping it a second time works. The agent must trace: `NewNoteFragment.onOptionsItemSelected` → `setLock()` → `PasswordUtil.checkLock()` → `PasswordDialogFragment`. The likely issue is that `checkLock()` returns `false` on the first call (because no password exists yet, so it shows the dialog), and the lock toggle only happens when `checkLock()` returns `true`. But after the dialog sets the password, the lock state isn't toggled — the user has to tap again. The fix should toggle the lock state after the password is successfully set, not require a second tap.

### 3. Camera does not work
Tapping the camera tool in the bottom sheet does nothing or crashes. The agent must trace: `NewNoteFragment.listeners()` → `toolsCamera.clicks()` → `checkPermission()` → `startCameraIntent()` → `createImageFile()` → `FileProvider.getUriForFile()`. Common issues after modernization: `FileProvider` authority mismatch, `WRITE_EXTERNAL_STORAGE` permission changes on API 29+, `resolveActivity()` returning null on API 30+ due to package visibility restrictions, or `onActivityResult` not being called (deprecated in favor of Activity Result API).

### 4. Attachment (gallery) does not work
Tapping the attachment/gallery tool does nothing or crashes. Same code path as camera but via `startGalleryIntent()`. Same potential issues: `resolveActivity()` returning null on API 30+, `READ_EXTERNAL_STORAGE` vs `READ_MEDIA_IMAGES` permission on API 33+, or `onActivityResult` issues.

### 5. Reminder notification never appears
Setting a reminder via the time picker works (the dialog shows, user picks a time, dialog closes), but no notification is ever delivered at the scheduled time. The agent must trace the full notification delivery path: `ReminderUtil.setReminder()` → `TimePickerDialog` callback → `setNotification()` → `AlarmManager.setInexactRepeating()` → `PendingIntent` → `NotificationReceiver.onReceive()` → notification creation and display. The agent must verify: (a) the PendingIntent uses `FLAG_IMMUTABLE` (required API 31+), (b) `NotificationReceiver` creates a notification channel (required API 26+), (c) `NotificationReceiver.onReceive()` actually builds and posts a `Notification` via `NotificationManager`, (d) the alarm is scheduled with the correct time, (e) `setInexactRepeating` is not restricted on the target API level. Read `NotificationReceiver.kt` carefully — it may receive the broadcast but not create/show the notification.

## What To Do

For EACH broken feature:

1. Read the source code for the entire code path (UI → logic → data)
2. Identify the root cause of the bug
3. Fix the bug in the app source code (`app/src/main/`)
4. Write a unit test (in `app/src/test/`) that verifies the fix — use Robolectric if Android framework is needed
5. Write or update an Espresso test (in `app/src/androidTest/`) that exercises the feature end-to-end on the emulator
6. Run `./gradlew testDemoDebugUnitTest` to verify unit tests pass
7. Run `./gradlew connectedDemoDebugAndroidTest` to verify Espresso tests pass
8. Repeat until the feature works

## Hard Requirements

- ALL 5 broken features must be fixed and verified
- `./gradlew connectedDemoDebugAndroidTest` MUST pass with 0 failures
- `./gradlew testDemoDebugUnitTest` MUST pass with 0 failures
- `./gradlew assembleDebug` MUST succeed
- Do NOT remove any existing tests
- Do NOT change the app's intended functionality — only fix bugs
- Both product flavors must still build
- The agent MUST run both test commands to verify — do not claim success without running them

## Constraints

- Fix app bugs in `app/src/main/`, test bugs in `app/src/androidTest/` or `app/src/test/`
- Use Robolectric for unit tests that need Android framework
- Use Room in-memory database for DAO tests
- Use Espresso with the connected emulator for UI tests
- For camera/gallery intents, use espresso-intents to stub the result (do not depend on external apps)
- For permissions, use GrantPermissionRule
- Work iteratively: fix → run tests → check → repeat
- Add inline comments explaining each bug fix
