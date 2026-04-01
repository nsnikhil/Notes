# Architecture Document

## ST-001: Fix ListAdapter submitList() mutable list reference bug

### What was built/fixed
- Fixed `CheckListDiffUtil` to compare `CheckListObject` items by `text` and `done` fields instead of always returning `false`
- Fixed all 9 `submitList()` calls in `NewNoteFragment.kt` to pass a new list copy via `.toList()` instead of the same mutable reference
- Updated `DiffUtilTest.kt` to verify the corrected `CheckListDiffUtil` behavior

### Key design decisions
- `CheckListObject` lacks a stable ID field, so content equality (`text` + `done`) is used as a proxy for identity in both `areItemsTheSame` and `areContentsTheSame`
- `StringDiffUtil` (used by `ImageAdapter` and `AudioListAdapter`) was already correct — no changes needed
- The `.toList()` pattern creates a shallow copy, which is sufficient since `AsyncListDiffer` only needs a different list reference to trigger diffing

### Files modified
- `app/src/main/java/com/nrs/nsnik/notes/view/adapters/diffUtil/CheckListDiffUtil.kt`
- `app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt`
- `app/src/test/java/com/nrs/nsnik/notes/util/DiffUtilTest.kt`

## ST-002: Migrate startActivityForResult/onActivityResult to Activity Result API

### What was built/fixed
- Replaced deprecated `startActivityForResult()` / `onActivityResult()` pattern with the Activity Result API (`registerForActivityResult`) in `NewNoteFragment.kt`
- Camera capture: `ActivityResultContracts.TakePicture()` launcher registered as class-level property `cameraLauncher`
- Gallery pick: `ActivityResultContracts.GetContent()` launcher registered as class-level property `galleryLauncher`
- Removed `onActivityResult()` override entirely
- Removed unused request code constants (`ATTACH_PICTURE_REQUEST_CODE`, `TAKE_PICTURE_REQUEST_CODE`)
- Removed unused imports (`RESULT_OK`, `RESULT_CANCELED`, `Intent`)

### Key design decisions
- Launchers are registered as class-level `val` properties using `registerForActivityResult()`, which is called during fragment construction (before STARTED state) as required by the API
- `TakePicture` contract returns a `Boolean` success flag; the saved photo path (`mCurrentPhotoPath`) is used to decode the image on success
- `GetContent("image/*")` contract returns a `Uri?`; null check guards against user cancellation
- `resolveActivity()` check was removed from `startCameraIntent()` since `TakePicture` contract handles this internally
- MainActivity.kt had no `onActivityResult()` override — no changes needed there

### Files modified
- `app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt`

## ST-003: Fix Reminders (past-time, permission, notification channel)

### What was built/fixed
- **Past-time validation**: `ReminderUtil.setNotification()` now checks if the selected time has already passed today; if so, it rolls the alarm to the next day via `calendar.add(Calendar.DAY_OF_YEAR, 1)`
- **SCHEDULE_EXACT_ALARM permission**: Added `android.permission.SCHEDULE_EXACT_ALARM` to AndroidManifest.xml (required on API 31+)
- **Channel ID mismatch fix**: `NotificationReceiver.buildNotification()` now resolves the channel ID (`notificationChannelReminderId`) before constructing the `NotificationCompat.Builder`, so the Builder constructor and `createNotificationChannel()` use the same ID. Removed the redundant `setChannelId()` call.
- **Notification channel**: Already existed before posting — no change needed, just verified.

### Key design decisions
- Used `calendar.add(Calendar.DAY_OF_YEAR, 1)` for past-time rollover since the alarm is a daily repeating alarm anyway
- Zeroed out seconds (`calendar.set(Calendar.SECOND, 0)`) for consistent alarm timing
- Moved `channelId` resolution above the Builder constructor to eliminate the mismatch where the Builder was initially constructed with the channel *name* string and then patched with `setChannelId()`

### Files modified
- `app/src/main/java/com/nrs/nsnik/notes/util/ReminderUtil.kt`
- `app/src/main/java/com/nrs/nsnik/notes/util/receiver/NotificationReceiver.kt`
- `app/src/main/AndroidManifest.xml`

## ST-004: Fix Lock and Navigation bugs

### What was built/fixed
- **BUG #4 (Lock)**: `PasswordDialogFragment` now sends a `FragmentResult` via `parentFragmentManager.setFragmentResult()` after saving a new password. `NewNoteFragment` listens for this result via `setFragmentResultListener` in `onViewCreated()` and completes the lock toggle automatically — no second tap needed.
- **BUG #5 (Navigation)**: Drawer navigation in `MainActivity` now uses `NavOptions` with `setPopUpTo(R.id.navItemNotes, false)` and `setLaunchSingleTop(true)` so that Settings/About don't stack on the back stack. Pressing back from any drawer destination returns directly to the Notes list.

### Key design decisions
- Used the Fragment Result API (`setFragmentResult` / `setFragmentResultListener`) instead of EventBus for the password-created callback, since this is a direct parent-child fragment communication pattern and avoids adding another EventBus event
- `PasswordDialogFragment` defines `REQUEST_KEY` and `RESULT_PASSWORD_CREATED` as companion object constants for type-safe result passing
- For the Notes drawer item, used `controller.popBackStack(R.id.navItemNotes, false)` instead of navigate, since the start destination is already on the stack
- `NavOptions.setPopUpTo(R.id.navItemNotes, inclusive=false)` keeps the start destination on the stack while clearing everything above it

### Files modified
- `app/src/main/java/com/nrs/nsnik/notes/view/fragments/dialogFragments/PasswordDialogFragment.kt`
- `app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt`
- `app/src/main/java/com/nrs/nsnik/notes/view/MainActivity.kt`

### Files created
- `app/src/test/java/com/nrs/nsnik/notes/LockAndNavigationBugFixTest.kt`

## ST-005: Espresso Tests for Core User Flows

### What was built
- 7 new Espresso instrumentation test files covering core user flows:
  1. `AppLaunchFlowTest.kt` — Verifies app launches correctly, toolbar/drawer/nav host/recycler/FAB are displayed
  2. `NavigationFlowTest.kt` — Drawer open/close, navigate to Settings/About, back navigation returns to notes list
  3. `NoteCrudFlowTest.kt` — FAB reveals add-note/add-folder, opens new note fragment, enter title/content, save returns to list
  4. `SearchFlowTest.kt` — Search icon displayed, tap expands SearchView, type query with list still visible
  5. `FolderFlowTest.kt` — FAB reveals add-folder, opens create folder dialog, dialog has name/create/cancel/color/lock/star
  6. `PinFlowTest.kt` — New note menu shows star option, tap star toggles pin, pin+save returns to list
  7. `ColorPickerFlowTest.kt` — Bottom sheet tools visible, expand shows all tools (checklist/camera/attachment/reminder/audio/color)

### Key design decisions
- Used `ActivityTestRule` (deprecated but consistent with existing 4 test files) instead of `ActivityScenarioRule`
- Used `IdlingResource` pattern matching existing tests for synchronization
- Tests are independent — each starts from a fresh activity launch
- Used real view IDs from layout XMLs (`R.id.fabAdd`, `R.id.fabAddNote`, `R.id.newNoteTitle`, etc.)
- Used `NavigationViewActions.navigateTo()` from espresso-contrib for drawer navigation
- Search tests use `SearchView.SearchAutoComplete` matcher since the SearchView is a collapsible action view
- Did NOT modify any existing test files

### Files created
- `app/src/androidTest/java/com/nrs/nsnik/notes/AppLaunchFlowTest.kt`
- `app/src/androidTest/java/com/nrs/nsnik/notes/NavigationFlowTest.kt`
- `app/src/androidTest/java/com/nrs/nsnik/notes/NoteCrudFlowTest.kt`
- `app/src/androidTest/java/com/nrs/nsnik/notes/SearchFlowTest.kt`
- `app/src/androidTest/java/com/nrs/nsnik/notes/FolderFlowTest.kt`
- `app/src/androidTest/java/com/nrs/nsnik/notes/PinFlowTest.kt`
- `app/src/androidTest/java/com/nrs/nsnik/notes/ColorPickerFlowTest.kt`

## ST-006: Espresso Tests for Remaining User Flows

### What was built
- 7 new Espresso instrumentation test files covering remaining user flows:
  1. `LockFlowTest.kt` — Lock icon displayed in new note menu, tap shows password dialog with text/create/cancel
  2. `ReminderFlowTest.kt` — Reminder tool displayed in bottom sheet, clickable
  3. `ImageAttachmentFlowTest.kt` — Camera/gallery tools displayed, uses espresso-intents to stub camera/gallery results
  4. `AudioFlowTest.kt` — Audio tool displayed in bottom sheet, uses GrantPermissionRule for RECORD_AUDIO
  5. `SettingsFlowTest.kt` — Navigate via drawer, verify security heading and change password option
  6. `AboutFlowTest.kt` — Navigate via drawer, verify author info, libraries button, license button
  7. `ChecklistFlowTest.kt` — Checklist tool displayed, tap shows checklist RecyclerView

### Key design decisions
- Used `ActivityTestRule` (deprecated but consistent with existing ST-005 test files) for pattern consistency
- Used `IdlingResource` pattern matching existing tests for synchronization
- `ImageAttachmentFlowTest` uses `Intents.init()`/`Intents.release()` in @Before/@After to stub camera/gallery intents
- `AudioFlowTest` uses `GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO)` to auto-grant permission
- Tests are independent — each starts from a fresh activity launch
- Used real view IDs verified from layout XMLs

### Files created
- `app/src/androidTest/java/com/nrs/nsnik/notes/LockFlowTest.kt`
- `app/src/androidTest/java/com/nrs/nsnik/notes/ReminderFlowTest.kt`
- `app/src/androidTest/java/com/nrs/nsnik/notes/ImageAttachmentFlowTest.kt`
- `app/src/androidTest/java/com/nrs/nsnik/notes/AudioFlowTest.kt`
- `app/src/androidTest/java/com/nrs/nsnik/notes/SettingsFlowTest.kt`
- `app/src/androidTest/java/com/nrs/nsnik/notes/AboutFlowTest.kt`
- `app/src/androidTest/java/com/nrs/nsnik/notes/ChecklistFlowTest.kt`
