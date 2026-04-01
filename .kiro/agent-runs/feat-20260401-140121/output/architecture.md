# Architecture Document

## ST-001: Fix checklist/image/audio double-add bug

### Problem
In `NewNoteFragment.kt`, after calling `adapter.submitList(list.toList())`, there were redundant `notifyItemInserted()` or `notifyItemRemoved()` calls. `submitList()` already triggers DiffUtil which handles adapter notification. The extra `notifyItem*` calls caused double-add/double-remove visual bugs.

### Solution
Removed all 6 redundant `notifyItem*` calls:
- 3 `notifyItemInserted` in `addCheckListItem()`, `addAudioToList()`, `addImageToList()`
- 3 `notifyItemRemoved` in `onItemRemoved()` (IMAGE_ADAPTER, AUDIO_ADAPTER, CHECKLIST_ADAPTER branches)

### Files Modified
- `app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt`

### Files Created
- `app/src/test/java/com/nrs/nsnik/notes/DoubleAddBugFixTest.kt`

### Key Details
- `submitList()` on `ListAdapter`/`AsyncListDiffer` handles all adapter notifications via DiffUtil — no manual `notifyItem*` calls needed
- All 6 removal sites have inline BUG FIX comments

## ST-002: Fix camera FileProvider path + gallery permission check

### Problem 1 — Camera FileProvider
- `file_paths.xml` used `<external-path>` which maps to `Environment.getExternalStorageDirectory()`, restricted on API 29+ (scoped storage)
- Paths were hardcoded for `.notes` and `.notes.debug` suffixes, but actual flavors use `.demo` and `.full`
- `createImageFile()` uses `getExternalFilesDir(DIRECTORY_PICTURES)` which maps to `<external-files-path>`, not `<external-path>`
- FileProvider authority was hardcoded to `com.nrs.nsnik.notes.fileprovider` in both manifest and Kotlin code, breaking for flavored builds

### Problem 2 — Gallery Permission
- Gallery launch was wrapped in `checkPermission(READ_EXTERNAL_STORAGE, ...)` but `ActivityResultContracts.GetContent()` uses the system picker which grants temporary URI access — no permission needed
- `READ_EXTERNAL_STORAGE` is broken on API 33+ (replaced by granular `READ_MEDIA_IMAGES`)

### Solution
1. `file_paths.xml`: Replaced two `<external-path>` entries with a single `<external-files-path name="my_images" path="Pictures" />` — matches `getExternalFilesDir(DIRECTORY_PICTURES)` and works for all flavors
2. `AndroidManifest.xml`: Changed FileProvider authority from `com.nrs.nsnik.notes.fileprovider` to `${applicationId}.fileprovider`
3. `NewNoteFragment.kt`:
   - `startCameraIntent()`: Uses `BuildConfig.APPLICATION_ID` + `.fileprovider` instead of hardcoded string
   - Gallery handler: Calls `galleryLauncher.launch("image/*")` directly, removed `checkPermission()` wrapper
   - Removed unused `startGalleryIntent()` method and `READ_EXTERNAL_STORAGE_PERMISSION` constant
   - Added `BuildConfig` import

### Files Modified
- `app/src/main/res/xml/file_paths.xml`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt`

### Files Created
- `app/src/test/java/com/nrs/nsnik/notes/CameraAndGalleryFixTest.kt`

### Key Details
- `<external-files-path>` maps to `context.getExternalFilesDir()` — app-private, no scoped storage issues
- `${applicationId}` in manifest is replaced at build time with the flavor-specific applicationId
- `BuildConfig.APPLICATION_ID` in Kotlin resolves to the same flavor-specific value at runtime
- `ActivityResultContracts.GetContent()` grants temporary read via content URI — no storage permission needed

## ST-003: Fix lock not working on first attempt

### Problem
In `PasswordDialogFragment.kt`, `savePassword()` and `initialize()` used `.observe(this, ...)` instead of `.observe(viewLifecycleOwner, ...)`. In a DialogFragment, `this` refers to the Fragment's lifecycle, which differs from the view's lifecycle. This caused LiveData observer timing issues — the observer might not fire correctly on the first attempt because the Fragment lifecycle owner may not be in the correct state when the view is ready.

### Solution
Changed both `.observe(this, Observer {` calls to `.observe(viewLifecycleOwner, Observer {`:
1. In `initialize()` — the `decryptAndGet` observer (line ~95)
2. In `savePassword()` — the `encryptAndGet` observer (line ~177)

### Files Modified
- `app/src/main/java/com/nrs/nsnik/notes/view/fragments/dialogFragments/PasswordDialogFragment.kt`

### Files Created
- `app/src/test/java/com/nrs/nsnik/notes/PasswordDialogViewLifecycleOwnerTest.kt`

### Key Details
- `viewLifecycleOwner` is the correct lifecycle owner for LiveData observers in Fragments/DialogFragments because it tracks the view's lifecycle (onCreateView → onDestroyView), not the Fragment's lifecycle (onCreate → onDestroy)
- The FragmentResult API callback (`setFragmentResultListener` in NewNoteFragment, `setFragmentResult` in PasswordDialogFragment) was already correctly wired from a previous run
- The `REQUEST_KEY` and `RESULT_PASSWORD_CREATED` companion constants in PasswordDialogFragment are intact

## ST-004: Fix reminder notification not firing (exact alarm scheduling)

### Problem
`ReminderUtil.setNotification()` used `alarmManager.setInexactRepeating()` which is heavily batched on API 31+ (Android 12). Alarms could be delayed by minutes or hours, making reminders unreliable. Additionally, on API 31+, exact alarms require the `SCHEDULE_EXACT_ALARM` permission AND the user must grant it in system Settings.

### Solution
1. Replaced `setInexactRepeating()` with a call to a new `scheduleAlarm()` helper
2. `scheduleAlarm()` checks `canScheduleExactAlarms()` on API 31+:
   - If permitted: uses `setExactAndAllowWhileIdle()` for precise one-time delivery
   - If not permitted: falls back to `setAndAllowWhileIdle()` (inexact but Doze-safe, much better than `setInexactRepeating`)
3. `SCHEDULE_EXACT_ALARM` permission was already declared in AndroidManifest.xml from a previous run
4. Added `import android.os.Build` for the SDK version check

### Files Modified
- `app/src/main/java/com/nrs/nsnik/notes/util/ReminderUtil.kt`

### Files Created
- `app/src/test/java/com/nrs/nsnik/notes/ReminderExactAlarmFixTest.kt`

### Key Details
- `scheduleAlarm()` is `internal` visibility so it can be tested directly
- `setExactAndAllowWhileIdle()` fires at the exact time even in Doze mode
- `setAndAllowWhileIdle()` is the fallback — not exact but still Doze-safe and far better than `setInexactRepeating`
- The alarm is one-time (not repeating) — the original `setInexactRepeating` with `INTERVAL_DAY` was replaced with a single exact trigger
- NotificationReceiver.kt was verified: it correctly creates a NotificationChannel and posts the notification via `notificationManager.notify()`
