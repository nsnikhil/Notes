# Implementation Plan: Fix 5 Broken Features

## Overview
This plan fixes five confirmed broken features in the Notes Android app: (1) checklist double-add caused by redundant `notifyItemInserted`/`notifyItemRemoved` calls after `submitList()`, (2) camera crash from incorrect `<external-path>` in `file_paths.xml` and hardcoded authority/paths that don't account for flavor `applicationIdSuffix`, (3) gallery broken by unnecessary `READ_EXTERNAL_STORAGE` permission check that fails on API 33+, (4) lock first-attempt failure from `savePassword()` LiveData observer timing with `this` (DialogFragment) instead of `viewLifecycleOwner`, and (5) reminder not firing because `setInexactRepeating` is batched/deferred on API 31+ and should use `setExactAndAllowWhileIdle` for single-fire reminders. Each sub-task is a vertical slice delivering a testable fix with both unit tests and Espresso tests.

## Sub-Tasks

### Sub-Task 1: Fix Checklist/Image/Audio Double-Add (Remove Redundant notify* Calls)
- **Size:** S
- **Description:** In `NewNoteFragment.kt`, six methods call both `submitList(list.toList())` and then `notifyItemInserted()`/`notifyItemRemoved()`. `ListAdapter.submitList()` already dispatches `DiffUtil` diffs asynchronously, so the manual notify calls cause duplicate items (inserts) or index-out-of-bounds crashes (removes). Remove all six redundant notify calls.
- **Dependencies:** None
- **Entry Criteria:** Repository compiles and all existing tests pass.
- **Exit Criteria:**
  - `NewNoteFragment.kt` contains zero calls to `notifyItemInserted` or `notifyItemRemoved` in any of these methods: `addCheckListItem()`, `addAudioToList()`, `addImageToList()`, `onItemRemoved()` (all 3 branches: IMAGE_ADAPTER, AUDIO_ADAPTER, CHECKLIST_ADAPTER).
  - The six `submitList()` calls remain intact with `.toList()` copies.
  - Project compiles successfully.
- **Success Criteria:**
  - Given `NewNoteFragment.kt` source code, When scanning for `notifyItemInserted` and `notifyItemRemoved`, Then zero occurrences are found.
  - Given a new note, When the user taps the checklist tool 3 times, Then exactly 3 checklist items appear in the RecyclerView (not 6).
  - Given a note with 2 checklist items, When the user removes item at position 0, Then exactly 1 item remains and no crash occurs.
  - Given a new note, When an image is added via gallery, Then exactly 1 image appears in the image list (not 2).
- **Files:**
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt`
  - Create: `app/src/test/java/com/nrs/nsnik/notes/ChecklistDoubleAddFixTest.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/ChecklistFlowTest.kt`

### Sub-Task 2: Fix Camera FileProvider Path and Gallery Permission Check
- **Size:** M
- **Description:** Two fixes:
  (A) **Camera:** `file_paths.xml` uses `<external-path>` which maps to `Environment.getExternalStorageDirectory()`. But `getExternalFilesDir(DIRECTORY_PICTURES)` returns an app-scoped path under `/Android/data/<applicationId>/files/Pictures`. The correct tag is `<external-files-path>` with `path="Pictures/"`. Also, the hardcoded paths include `com.nrs.nsnik.notes` and `com.nrs.nsnik.notes.debug` but the actual flavors produce `com.nrs.nsnik.notes.demo` and `com.nrs.nsnik.notes.full`. Since `<external-files-path>` is relative to the app's own `getExternalFilesDir()`, a single entry with `path="Pictures/"` works for all flavors. Additionally, the FileProvider authority in `AndroidManifest.xml` is hardcoded to `com.nrs.nsnik.notes.fileprovider` but should use `${applicationId}.fileprovider` to match the flavor's applicationId. The `startCameraIntent()` in `NewNoteFragment.kt` also hardcodes this authority string.
  (B) **Gallery:** `startGalleryIntent()` is gated behind `checkPermission(READ_EXTERNAL_STORAGE, ...)` in the `toolsAttachment` click handler. `ActivityResultContracts.GetContent()` uses the system picker and does NOT require `READ_EXTERNAL_STORAGE`. On API 33+ this permission is auto-denied, blocking gallery access. Remove the permission check wrapper and call `startGalleryIntent()` directly. Also remove the `@RequiresPermission` annotation from `startGalleryIntent()`.
- **Dependencies:** None
- **Entry Criteria:** Repository compiles and all existing tests pass.
- **Exit Criteria:**
  - `file_paths.xml` uses `<external-files-path name="my_images" path="Pictures/" />` (single entry, no hardcoded applicationId).
  - `AndroidManifest.xml` FileProvider authority uses `${applicationId}.fileprovider`.
  - `NewNoteFragment.kt` `startCameraIntent()` uses `BuildConfig.APPLICATION_ID + ".fileprovider"` instead of hardcoded string.
  - `NewNoteFragment.kt` `toolsAttachment` click handler calls `startGalleryIntent()` directly without `checkPermission()`.
  - `startGalleryIntent()` has no `@RequiresPermission` annotation.
  - Project compiles successfully.
- **Success Criteria:**
  - Given `file_paths.xml`, When parsed, Then it contains exactly one `<external-files-path>` element with `path="Pictures/"` and zero `<external-path>` elements.
  - Given `AndroidManifest.xml`, When the FileProvider authority is read, Then it equals `${applicationId}.fileprovider`.
  - Given `NewNoteFragment.kt`, When scanning for `"com.nrs.nsnik.notes.fileprovider"` literal, Then zero occurrences are found.
  - Given `NewNoteFragment.kt` `toolsAttachment` click handler, When inspected, Then it does NOT call `checkPermission` and calls `startGalleryIntent()` directly.
  - Given the app running on API 35 emulator, When the user taps the gallery tool, Then the system content picker opens without a permission dialog.
  - Given the app running on API 35 emulator, When the user taps the camera tool, Then the camera app opens without a FileProvider crash.
- **Files:**
  - Modify: `app/src/main/res/xml/file_paths.xml`
  - Modify: `app/src/main/AndroidManifest.xml`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt`
  - Create: `app/src/test/java/com/nrs/nsnik/notes/CameraGalleryFixTest.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/ImageAttachmentFlowTest.kt`

### Sub-Task 3: Fix Lock First-Attempt Failure (LiveData Observer Lifecycle)
- **Size:** S
- **Description:** In `PasswordDialogFragment.savePassword()`, the `encryptAndGet()` LiveData result is observed with `.observe(this, ...)` where `this` is the DialogFragment. On a DialogFragment, `this` as LifecycleOwner can have timing issues — the observer may not receive the value if the fragment's lifecycle state isn't STARTED yet when the value is posted. The fix is to use `viewLifecycleOwner` instead of `this` in the `observe()` call inside `savePassword()`. Similarly, in `initialize()`, `decryptAndGet().observe(this, ...)` should use `viewLifecycleOwner`. This ensures the observer is tied to the view lifecycle which is guaranteed to be in STARTED state during `onViewCreated`.
- **Dependencies:** None
- **Entry Criteria:** Repository compiles and all existing tests pass.
- **Exit Criteria:**
  - `PasswordDialogFragment.kt` `savePassword()` uses `.observe(viewLifecycleOwner, ...)` instead of `.observe(this, ...)`.
  - `PasswordDialogFragment.kt` `initialize()` uses `.observe(viewLifecycleOwner, ...)` instead of `.observe(this, ...)`.
  - No other occurrences of `.observe(this,` remain in `PasswordDialogFragment.kt`.
  - Project compiles successfully.
- **Success Criteria:**
  - Given `PasswordDialogFragment.kt` source, When scanning for `.observe(this,`, Then zero occurrences are found.
  - Given `PasswordDialogFragment.kt` source, When scanning for `.observe(viewLifecycleOwner,`, Then exactly 2 occurrences are found (one in `initialize()`, one in `savePassword()`).
  - Given a new note with no existing password, When the user taps lock icon, enters a password in both fields, and taps Save, Then the password is saved and the lock icon toggles to locked state on the first attempt.
  - Given a note with an existing password, When the user taps lock icon and enters the correct password, Then the lock toggles on the first attempt.
- **Files:**
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/dialogFragments/PasswordDialogFragment.kt`
  - Create: `app/src/test/java/com/nrs/nsnik/notes/LockFirstAttemptFixTest.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/LockFlowTest.kt`

### Sub-Task 4: Fix Reminder Not Firing (Use Exact Alarm on API 31+)
- **Size:** S
- **Description:** `ReminderUtil.setNotification()` uses `alarmManager.setInexactRepeating()` which on API 31+ is heavily batched and may not fire at the user-selected time. Since the app sets a reminder for a specific time (not a repeating interval), the correct API is `setExactAndAllowWhileIdle()` for a one-shot alarm. The `SCHEDULE_EXACT_ALARM` permission is already declared in the manifest. The fix: replace `setInexactRepeating()` with `setExactAndAllowWhileIdle()` using `AlarmManager.RTC_WAKEUP` and the calendar's `timeInMillis`. On API 31+, also check `canScheduleExactAlarms()` before scheduling; if not permitted, fall back to `setAndAllowWhileIdle()`.
- **Dependencies:** None
- **Entry Criteria:** Repository compiles and all existing tests pass.
- **Exit Criteria:**
  - `ReminderUtil.setNotification()` no longer calls `setInexactRepeating()`.
  - `ReminderUtil.setNotification()` calls `setExactAndAllowWhileIdle()` (with `canScheduleExactAlarms()` guard on API 31+, falling back to `setAndAllowWhileIdle()`).
  - The alarm is a one-shot (no repeat interval parameter).
  - Project compiles successfully.
- **Success Criteria:**
  - Given `ReminderUtil.kt` source, When scanning for `setInexactRepeating`, Then zero occurrences are found.
  - Given `ReminderUtil.kt` source, When scanning for `setExactAndAllowWhileIdle`, Then at least one occurrence is found.
  - Given the app on API 35 emulator, When the user sets a reminder for 2 minutes from now, Then a notification fires within 2 minutes (±30 seconds).
  - Given `ReminderUtil.kt` source, When scanning for `canScheduleExactAlarms`, Then at least one occurrence is found (API 31+ guard).
- **Files:**
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/util/ReminderUtil.kt`
  - Create: `app/src/test/java/com/nrs/nsnik/notes/ReminderExactAlarmFixTest.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/ReminderFlowTest.kt`

## Implementation Order
1. Sub-task 1 — Fix checklist double-add (no dependencies, isolated to NewNoteFragment notify calls)
2. Sub-task 2 — Fix camera + gallery (no dependencies, touches NewNoteFragment but different methods/lines than sub-task 1; can run in parallel with sub-task 1 if file merge is handled)
3. Sub-task 3 — Fix lock first-attempt (no dependencies, isolated to PasswordDialogFragment)
4. Sub-task 4 — Fix reminder not firing (no dependencies, isolated to ReminderUtil)

Note: Sub-tasks 1–4 have no inter-dependencies and touch non-overlapping code regions (except sub-tasks 1 and 2 both modify `NewNoteFragment.kt` — sub-task 1 modifies `addCheckListItem()`, `addAudioToList()`, `addImageToList()`, `onItemRemoved()` while sub-task 2 modifies the `toolsAttachment` click handler in `listeners()` and `startCameraIntent()`). They should be executed sequentially to avoid merge conflicts in `NewNoteFragment.kt`.

## Integration Test Plan
After all sub-tasks are complete, the IntegrationTester should verify:

- **Checklist end-to-end:** Create a new note → add 3 checklist items → verify exactly 3 items displayed → remove 1 item → verify exactly 2 items remain → save note → reopen note → verify 2 items persist. No duplicate items at any point.
- **Camera end-to-end:** Create a new note → tap camera tool → verify camera intent fires without FileProvider crash → cancel camera → verify no crash. (On emulator with stubbed intent.)
- **Gallery end-to-end:** Create a new note → tap gallery/attachment tool → verify content picker opens immediately with no permission dialog on API 33+ → cancel picker → verify no crash.
- **Lock end-to-end:** Create a new note → tap lock icon → password dialog appears → enter matching passwords in both fields → tap Save → verify lock icon shows locked state immediately (first attempt) → save note → reopen note → verify note shows as locked.
- **Reminder end-to-end:** Create a new note with title and content → tap reminder tool → select a time 2 minutes from now → verify reminder is set → wait for notification → verify notification appears with correct title and content.
- **Cross-feature regression:** Create a note with all features (checklist + image + lock + reminder) → save → reopen → verify all features persist correctly.
- **Error/edge cases:**
  - Add and immediately remove a checklist item → no crash, empty list hidden.
  - Tap lock icon, cancel password dialog → lock state unchanged.
  - Set reminder, delete note → alarm should still fire (documented known gap BUG-005, not fixed in this plan).
  - Gallery on API 23 device → should still work (GetContent doesn't need permissions on any API level).

## Requirements Traceability
| Requirement | Sub-Task(s) |
|---|---|
| REQ-1: Checklist items must not duplicate on add | Sub-task 1 |
| REQ-2: Checklist items must not crash on remove | Sub-task 1 |
| REQ-3: Image list must not duplicate on add | Sub-task 1 |
| REQ-4: Audio list must not duplicate on add | Sub-task 1 |
| REQ-5: Camera must not crash with FileProvider error | Sub-task 2 |
| REQ-6: Camera must work across all build flavors (demo/full) | Sub-task 2 |
| REQ-7: FileProvider authority must be flavor-aware | Sub-task 2 |
| REQ-8: file_paths.xml must use external-files-path | Sub-task 2 |
| REQ-9: Gallery must open without permission dialog on API 33+ | Sub-task 2 |
| REQ-10: Gallery GetContent must not require READ_EXTERNAL_STORAGE | Sub-task 2 |
| REQ-11: Lock must toggle on first password creation attempt | Sub-task 3 |
| REQ-12: Lock must toggle on first password entry attempt | Sub-task 3 |
| REQ-13: PasswordDialogFragment must use viewLifecycleOwner | Sub-task 3 |
| REQ-14: Reminder must fire at the user-selected time | Sub-task 4 |
| REQ-15: Reminder must use exact alarm API on API 31+ | Sub-task 4 |
| REQ-16: Reminder must check canScheduleExactAlarms on API 31+ | Sub-task 4 |
| REQ-17: Reminder must fall back to setAndAllowWhileIdle if exact not permitted | Sub-task 4 |
| REQ-18: Reminder must be one-shot, not repeating | Sub-task 4 |
| REQ-19: All existing 27 unit + 18 androidTest tests must continue passing | Sub-task 1, 2, 3, 4 |
