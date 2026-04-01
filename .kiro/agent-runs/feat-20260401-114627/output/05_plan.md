# Implementation Plan: Android Notes App — Bug Fixes & Espresso UI Tests

## Overview
This plan fixes 5 confirmed bugs in the Android Notes app and adds comprehensive Espresso UI tests for 15 user flows. Bug fixes are ordered first (sub-tasks 1–4) so that Espresso tests (sub-tasks 5–6) exercise working features. A final verification sub-task (7) runs the full test suite across both flavors. Each bug fix sub-task includes a unit test proving the fix. Espresso tests use `ActivityScenarioRule`, follow the `<FeatureName>FlowTest.kt` naming convention, and run via `./gradlew connectedDemoDebugAndroidTest`.

## Sub-Tasks

### Sub-Task 1: Fix ListAdapter submitList bug across all three adapters and DiffUtil
- **Size:** S
- **Description:** Fix the mutable-list-reference `submitList()` no-op bug in `NewNoteFragment` for checklist, image, and audio lists. Every call site that passes `mCheckList`, `mImagesLocations`, or `mAudioLocations` to `submitList()` must pass a defensive copy (`list.toList()`). Fix `CheckListDiffUtil` to use meaningful identity/content comparison instead of unconditionally returning `false`. Add a unit test verifying the fixed DiffUtil behavior.
- **Dependencies:** None
- **Entry Criteria:** Repository compiles successfully with `./gradlew assembleDemoDebug`.
- **Exit Criteria:**
  - All `submitList()` calls in `NewNoteFragment` (`addCheckListItem`, `addImageToList`, `addAudioToList`, `onItemRemoved`, `setNote`) pass `list.toList()` instead of the raw mutable list reference.
  - `CheckListDiffUtil.areItemsTheSame()` returns `true` when both items have the same text and done state (identity by content since `CheckListObject` has no stable ID), and `areContentsTheSame()` compares `text` and `done` fields.
  - Manual `notifyItemInserted`/`notifyItemRemoved` calls are removed (ListAdapter handles this via DiffUtil).
  - Existing unit tests in `DiffUtilTest.kt` are updated to match the new DiffUtil behavior.
- **Success Criteria:**
  - Given two `CheckListObject` instances with identical `text` and `done` values, when `areItemsTheSame()` is called, then it returns `true`.
  - Given two `CheckListObject` instances with different `text` values, when `areItemsTheSame()` is called, then it returns `false`.
  - Given two `CheckListObject` instances with identical `text` and `done`, when `areContentsTheSame()` is called, then it returns `true`.
  - Given a `CheckListObject` where `done` differs, when `areContentsTheSame()` is called, then it returns `false`.
  - `./gradlew testDemoDebugUnitTest --tests "*.DiffUtilTest"` passes.
- **Files:**
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/adapters/diffUtil/CheckListDiffUtil.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt` (all `submitList()` call sites, remove `notifyItem*` calls)
  - Modify: `app/src/test/java/com/nrs/nsnik/notes/util/DiffUtilTest.kt` (update assertions for fixed DiffUtil)

### Sub-Task 2: Migrate attachment flows to Activity Result API
- **Size:** M
- **Description:** Replace deprecated `startActivityForResult()`/`onActivityResult()` in `NewNoteFragment` with `registerForActivityResult()`. Register two launchers: one for gallery image picking (`ActivityResultContracts.PickVisualMedia()` or `GetContent("image/*")`), one for camera capture (`ActivityResultContracts.TakePicture()`). Remove the `onActivityResult()` override and the companion object request code constants (`ATTACH_PICTURE_REQUEST_CODE`, `TAKE_PICTURE_REQUEST_CODE`). Add a unit test verifying the deprecated code is removed.
- **Dependencies:** Sub-task 1
- **Entry Criteria:** Sub-task 1 complete. `submitList()` bug is fixed so image list updates work correctly.
- **Exit Criteria:**
  - `NewNoteFragment` has two `ActivityResultLauncher` fields registered in the class body (before `onCreateView`).
  - `startGalleryIntent()` calls `galleryLauncher.launch(...)` instead of `startActivityForResult()`.
  - `startCameraIntent()` calls `cameraLauncher.launch(photoUri)` instead of `startActivityForResult()`.
  - `onActivityResult()` override is deleted.
  - `ATTACH_PICTURE_REQUEST_CODE` and `TAKE_PICTURE_REQUEST_CODE` constants are removed.
  - `READ_EXTERNAL_STORAGE_PERMISSION` constant and its permission check for gallery are removed (PickVisualMedia doesn't need it).
  - A source-scanning unit test confirms no `startActivityForResult` or `onActivityResult` calls remain in `NewNoteFragment.kt`.
- **Success Criteria:**
  - Given the `NewNoteFragment.kt` source file, when scanned for `startActivityForResult`, then zero matches are found.
  - Given the `NewNoteFragment.kt` source file, when scanned for `override fun onActivityResult`, then zero matches are found.
  - Given the `NewNoteFragment.kt` source file, when scanned for `registerForActivityResult`, then at least two matches are found (gallery + camera launchers).
  - `./gradlew assembleDemoDebug` compiles without errors.
- **Files:**
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt` (replace startActivityForResult with registerForActivityResult, delete onActivityResult)
  - Create: `app/src/test/java/com/nrs/nsnik/notes/ActivityResultMigrationTest.kt`

### Sub-Task 3: Fix reminder past-time validation, exact alarm permission, and channel ID consistency
- **Size:** S
- **Description:** Fix three remaining issues in the reminder system: (1) Add past-time validation in `ReminderUtil.setReminder()` — if the user picks a time earlier than now, roll the calendar to the next day. (2) Add `SCHEDULE_EXACT_ALARM` permission to `AndroidManifest.xml` for API 31+ alarm reliability. (3) Align the `NotificationCompat.Builder` constructor channel ID with the `channelId` variable used in `setChannelId()` in `NotificationReceiver.buildNotification()`. Add a unit test verifying the manifest contains the permission and the source code contains past-time validation logic.
- **Dependencies:** None (independent of sub-tasks 1–2)
- **Entry Criteria:** Repository compiles successfully.
- **Exit Criteria:**
  - `ReminderUtil.setNotification()` checks if `calendar.timeInMillis` is before `System.currentTimeMillis()` and if so, adds 1 day to the calendar.
  - `AndroidManifest.xml` contains `<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />`.
  - `NotificationReceiver.buildNotification()` uses the same `channelId` variable in the `NotificationCompat.Builder(context, channelId)` constructor instead of a different string resource.
  - A unit test confirms the manifest permission and source-level past-time check.
- **Success Criteria:**
  - Given the `ReminderUtil.kt` source, when scanned for a past-time check (e.g., `calendar.timeInMillis` compared to current time), then the check is present.
  - Given the `ReminderUtil.kt` source, when scanned for `calendar.add(Calendar.DAY_OF_YEAR, 1)` or equivalent, then the roll-forward logic is present.
  - Given `AndroidManifest.xml`, when scanned for `SCHEDULE_EXACT_ALARM`, then the permission is declared.
  - Given `NotificationReceiver.kt`, when the `NotificationCompat.Builder` constructor is inspected, then it uses the `channelId` local variable (not a different string resource).
  - `./gradlew testDemoDebugUnitTest --tests "*.ReminderBugFixTest"` passes.
- **Files:**
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/util/ReminderUtil.kt` (add past-time validation in `setNotification`)
  - Modify: `app/src/main/AndroidManifest.xml` (add SCHEDULE_EXACT_ALARM permission)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/util/receiver/NotificationReceiver.kt` (fix Builder constructor channel ID)
  - Create: `app/src/test/java/com/nrs/nsnik/notes/ReminderBugFixTest.kt`

### Sub-Task 4: Fix note locking callback and drawer navigation stacking
- **Size:** M
- **Description:** Fix two bugs: (1) **Lock callback**: After `PasswordDialogFragment.savePassword()` succeeds, post a `PasswordEvent` via EventBus so the calling fragment (`NewNoteFragment.setLock()` or `ListFragment.setLock()`) can complete the lock toggle. Currently, password creation dismisses the dialog without notifying the caller. The `PasswordEvent` needs the `itemType`, `position`, and `eventType` from the dialog's arguments bundle. For `NewNoteFragment`, which doesn't use `PasswordEvent` (it uses a direct `checkLock()` return value), add a `FragmentResultListener` that listens for a `"password_created"` key and completes the lock toggle. (2) **Navigation stacking**: In `MainActivity.setNavigationItemSelectedListener`, add `NavOptions` with `popUpTo(R.id.navItemNotes)` and `launchSingleTop = true` when navigating to Settings or About. This prevents fragment stacking on repeated drawer taps. Add unit tests verifying the source-level fixes.
- **Dependencies:** None (independent of sub-tasks 1–3)
- **Entry Criteria:** Repository compiles successfully.
- **Exit Criteria:**
  - `PasswordDialogFragment.savePassword()` posts a `PasswordEvent` after successful encryption and SharedPreferences save, using `itemType`, `position`, and `eventType` from its arguments bundle.
  - `NewNoteFragment.setLock()` registers a `FragmentResultListener` for `"password_created"` that toggles `mIsLocked` and updates the menu icon when the password dialog completes password creation.
  - `MainActivity.setNavigationItemSelectedListener` uses `NavOptions.Builder().setPopUpTo(R.id.navItemNotes, false).setLaunchSingleTop(true).build()` for Settings and About navigation.
  - Unit tests confirm the source-level changes.
- **Success Criteria:**
  - Given `PasswordDialogFragment.kt` source, when scanned for `EventBus.getDefault().post` inside or after `savePassword()`, then at least one `PasswordEvent` post is found.
  - Given `NewNoteFragment.kt` source, when scanned for `setFragmentResultListener` or `parentFragmentManager.setFragmentResultListener`, then a listener for password creation is registered.
  - Given `MainActivity.kt` source, when the drawer navigation for Settings is inspected, then `popUpTo` and `launchSingleTop` are present in the navigation call.
  - Given `MainActivity.kt` source, when the drawer navigation for About is inspected, then `popUpTo` and `launchSingleTop` are present in the navigation call.
  - `./gradlew testDemoDebugUnitTest --tests "*.LockAndNavBugFixTest"` passes.
- **Files:**
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/dialogFragments/PasswordDialogFragment.kt` (post PasswordEvent after savePassword)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt` (add FragmentResultListener for password_created)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/MainActivity.kt` (add NavOptions with popUpTo + launchSingleTop to drawer navigation)
  - Create: `app/src/test/java/com/nrs/nsnik/notes/LockAndNavBugFixTest.kt`

### Sub-Task 5: Write Espresso tests for core flows (launch, CRUD, checklist, search, folders)
- **Size:** L
- **Description:** Create Espresso UI tests for the 7 most critical user flows. Each test class uses `ActivityScenarioRule<MainActivity>`, follows `<FeatureName>FlowTest.kt` naming, and is fully independent with `@Before`/`@After` cleanup. Tests cover: (1) App launch + drawer navigation (FR-007), (2) Note CRUD — create, read, update, delete (FR-008), (3) Checklist add/remove/toggle (FR-009, validates FR-001 fix), (4) Search (FR-013), (5) Folder create/navigate/delete (FR-012), (6) Pin/Star toggle (FR-010), (7) Color picker (FR-014). This sub-task cannot be split further because all 7 test files are independent, non-overlapping Espresso test classes that share no code — they are simply grouped for executor efficiency. Each test file is a standalone class.
- **Dependencies:** Sub-task 1, Sub-task 4
- **Entry Criteria:** Sub-tasks 1 and 4 complete. Checklist submitList bug is fixed. Navigation stacking bug is fixed. App compiles with `./gradlew assembleDemoDebug`.
- **Exit Criteria:**
  - 7 new test files exist in `app/src/androidTest/java/com/nrs/nsnik/notes/`.
  - Each test class uses `ActivityScenarioRule<MainActivity>` (not deprecated `ActivityTestRule`).
  - Each test has `@Before` setup and `@After` teardown for test isolation.
  - All tests use Espresso matchers (`onView`, `ViewMatchers`, `ViewActions`, `ViewAssertions`).
- **Success Criteria:**
  - Given the app is launched, when the test checks for the main toolbar and FAB, then they are displayed (AppLaunchFlowTest).
  - Given the drawer is opened, when Settings is tapped then back pressed, then the app returns to the note list without stacking (NavigationFlowTest).
  - Given a new note is created with title "Test Note" and content "Test Content", when the user saves and returns to the list, then the note appears in the list (NoteCrudFlowTest).
  - Given an existing note, when the user opens it, changes the title, and saves, then the updated title is visible in the list (NoteCrudFlowTest).
  - Given an existing note, when the user deletes it via the menu, then it no longer appears in the list (NoteCrudFlowTest).
  - Given a note is open, when the user adds a checklist item via the bottom sheet, then the checklist RecyclerView becomes visible with one item (ChecklistFlowTest).
  - Given the search icon is tapped, when a query is typed, then the search view is displayed and accepts input (SearchFlowTest).
  - Given the FAB is tapped, when "Add Folder" is selected and a name entered, then the folder appears in the list (FolderFlowTest).
  - Given a note exists, when the user long-presses and taps Star, then the note's pinned state toggles (PinStarFlowTest).
  - Given a note is open, when the color picker is opened and a color selected, then the title color changes (ColorPickerFlowTest).
- **Files:**
  - Create: `app/src/androidTest/java/com/nrs/nsnik/notes/AppLaunchFlowTest.kt`
  - Create: `app/src/androidTest/java/com/nrs/nsnik/notes/NavigationFlowTest.kt`
  - Create: `app/src/androidTest/java/com/nrs/nsnik/notes/NoteCrudFlowTest.kt`
  - Create: `app/src/androidTest/java/com/nrs/nsnik/notes/ChecklistFlowTest.kt`
  - Create: `app/src/androidTest/java/com/nrs/nsnik/notes/SearchFlowTest.kt`
  - Create: `app/src/androidTest/java/com/nrs/nsnik/notes/FolderFlowTest.kt`
  - Create: `app/src/androidTest/java/com/nrs/nsnik/notes/PinStarFlowTest.kt`
  - Create: `app/src/androidTest/java/com/nrs/nsnik/notes/ColorPickerFlowTest.kt`

### Sub-Task 6: Write Espresso tests for remaining flows (lock, reminders, attachments, settings, about, edge cases)
- **Size:** L
- **Description:** Create Espresso UI tests for the remaining 7 user flows. Each test class uses `ActivityScenarioRule<MainActivity>`, follows `<FeatureName>FlowTest.kt` naming, and is fully independent. Tests cover: (1) Lock/password-protect flow (FR-011, validates FR-005 fix), (2) Reminder flow (FR-015, validates FR-004 fix), (3) Image attachment flow (FR-016, validates FR-003 fix), (4) Audio recording flow (FR-017), (5) Settings screen (FR-018), (6) About screen (FR-019), (7) Edge cases — empty note save, rapid navigation, back press from various states (FR-020). Attachment tests use `espresso-intents` with `Intents.init()`/`Intents.release()` and `intending()` to stub external activity results. This sub-task cannot be split further because all 7 test files are independent, non-overlapping classes grouped for executor efficiency.
- **Dependencies:** Sub-task 2, Sub-task 3, Sub-task 4
- **Entry Criteria:** Sub-tasks 2, 3, and 4 complete. Activity Result API migration done. Reminder fix done. Lock callback fix done. App compiles with `./gradlew assembleDemoDebug`.
- **Exit Criteria:**
  - 7 new test files exist in `app/src/androidTest/java/com/nrs/nsnik/notes/`.
  - Attachment tests use `espresso-intents` to stub gallery/camera results.
  - Lock test exercises the password creation → lock toggle flow.
  - All tests have `@Before`/`@After` for isolation.
- **Success Criteria:**
  - Given no password exists, when the user taps lock on a note, then the password creation dialog appears, and after setting a password the note becomes locked (LockFlowTest).
  - Given a note is open, when the user taps the reminder tool, then a TimePickerDialog is displayed (ReminderFlowTest).
  - Given a note is open and espresso-intents stubs a gallery result, when the user taps the attachment tool, then the image attachment flow is initiated via the Activity Result API (ImageAttachmentFlowTest).
  - Given a note is open, when the user taps the audio tool, then the audio recording UI appears (AudioFlowTest).
  - Given the drawer is opened, when Settings is tapped, then the settings screen displays with change password, backup, restore, and dark mode options (SettingsFlowTest).
  - Given the drawer is opened, when About is tapped, then the about screen displays with source code link, libraries, and license options (AboutFlowTest).
  - Given the note editor is open with empty title and content, when save is tapped, then a toast or error is shown (EdgeCaseFlowTest).
  - Given the user rapidly navigates drawer → Settings → back → About → back, then no crash occurs and the note list is displayed (EdgeCaseFlowTest).
- **Files:**
  - Create: `app/src/androidTest/java/com/nrs/nsnik/notes/LockFlowTest.kt`
  - Create: `app/src/androidTest/java/com/nrs/nsnik/notes/ReminderFlowTest.kt`
  - Create: `app/src/androidTest/java/com/nrs/nsnik/notes/ImageAttachmentFlowTest.kt`
  - Create: `app/src/androidTest/java/com/nrs/nsnik/notes/AudioFlowTest.kt`
  - Create: `app/src/androidTest/java/com/nrs/nsnik/notes/SettingsFlowTest.kt`
  - Create: `app/src/androidTest/java/com/nrs/nsnik/notes/AboutFlowTest.kt`
  - Create: `app/src/androidTest/java/com/nrs/nsnik/notes/EdgeCaseFlowTest.kt`

### Sub-Task 7: Final verification — full test suite across both flavors
- **Size:** S
- **Description:** Run the complete test suite to verify all bug fixes and Espresso tests pass together. Execute unit tests for both flavors (`testDemoDebugUnitTest`, `testFullDebugUnitTest`) and connected tests (`connectedDemoDebugAndroidTest`). Verify no regressions in the existing 26 unit test files. Fix any compilation errors or test failures discovered during the full run.
- **Dependencies:** Sub-task 1, Sub-task 2, Sub-task 3, Sub-task 4, Sub-task 5, Sub-task 6
- **Entry Criteria:** All sub-tasks 1–6 complete. Emulator is running.
- **Exit Criteria:**
  - `./gradlew testDemoDebugUnitTest` passes (all existing + new unit tests).
  - `./gradlew testFullDebugUnitTest` passes.
  - `./gradlew connectedDemoDebugAndroidTest` passes (all Espresso tests).
  - No compilation warnings related to the changed files.
- **Success Criteria:**
  - Given the full unit test suite, when `./gradlew testDemoDebugUnitTest` is executed, then all tests pass with 0 failures.
  - Given the full unit test suite, when `./gradlew testFullDebugUnitTest` is executed, then all tests pass with 0 failures.
  - Given the full Espresso test suite, when `./gradlew connectedDemoDebugAndroidTest` is executed on the emulator, then all tests pass with 0 failures.
  - Given the existing 26 unit test files, when they are run, then none have regressed.
- **Files:**
  - Modify: (any files needing minor fixes discovered during full verification — no new files expected)

## Implementation Order
1. Sub-task 1 — Fix ListAdapter submitList bug + DiffUtil (no dependencies)
2. Sub-task 3 — Fix reminder past-time + permission + channel ID (no dependencies, parallel with 1)
3. Sub-task 4 — Fix lock callback + navigation stacking (no dependencies, parallel with 1 and 3)
4. Sub-task 2 — Migrate attachments to Activity Result API (depends on 1)
5. Sub-task 5 — Espresso tests: core flows (depends on 1, 4)
6. Sub-task 6 — Espresso tests: remaining flows (depends on 2, 3, 4)
7. Sub-task 7 — Final verification (depends on all)

**Parallelization opportunities:**
- Sub-tasks 1, 3, and 4 can execute in parallel (no shared file modifications).
- Sub-tasks 5 and 6 can execute in parallel once their dependencies are met (no overlapping test files).

## Integration Test Plan
After all sub-tasks are complete, the IntegrationTester agent should verify:

- **E2E Scenario 1 — Checklist round-trip:** Launch app → create note → add 3 checklist items → save → reopen note → verify exactly 3 items displayed → remove 1 item → save → reopen → verify exactly 2 items. No duplicates at any step.
- **E2E Scenario 2 — Image attachment via Activity Result API:** Launch app → create note → tap attachment tool → stub gallery result with test image → verify image appears in the note's image RecyclerView → save → reopen → verify image persists.
- **E2E Scenario 3 — Lock on first use:** Launch app → create and save a note → long-press note → tap Lock → password creation dialog appears → enter and confirm password → dialog dismisses → note is now locked (lock icon visible) → navigate away → return → long-press locked note → tap Open → password entry dialog appears → enter correct password → note opens.
- **E2E Scenario 4 — Navigation no-stacking:** Open drawer → tap Settings → press back → on note list → open drawer → tap About → press back → on note list → open drawer → tap Settings → open drawer → tap About → press back → on note list (not on Settings). Verify back stack depth never exceeds 2.
- **E2E Scenario 5 — Reminder past-time handling:** Open note → tap reminder → pick a time 1 hour in the past → verify the calendar rolls to the next day (alarm time is in the future).
- **E2E Scenario 6 — Full CRUD cycle:** Create note with title + content → verify in list → open and edit title → save → verify updated title → delete note → verify removed from list.
- **E2E Scenario 7 — Folder operations:** Create folder → navigate into folder → create note inside folder → navigate back → verify folder shows in list → delete folder → verify folder and its note are removed.
- **Error/Edge Case Scenarios:**
  - Attempt to save a note with empty title and content → verify toast/error message.
  - Rapidly toggle drawer navigation between Settings and About 5 times → verify no crash and final state is note list.
  - Open note editor → press back without saving → verify no orphan data.

## Requirements Traceability
| Requirement | Sub-Task(s) | Acceptance Criteria |
|-------------|-------------|---------------------|
| FR-001 (Checklist duplicates) | 1 | AC-001 |
| FR-002 (Image/Audio submitList) | 1 | AC-011 |
| FR-003 (Attachment Activity Result) | 2 | AC-002, AC-003 |
| FR-004 (Reminder delivery) | 3 | AC-004, AC-012 |
| FR-005 (Lock callback) | 4 | AC-005 |
| FR-006 (Navigation stacking) | 4 | AC-006 |
| FR-007 (App Launch test) | 5 | AC-007 |
| FR-008 (Note CRUD test) | 5 | AC-007 |
| FR-009 (Checklist test) | 5 | AC-001, AC-007 |
| FR-010 (Pin/Star test) | 5 | AC-007 |
| FR-011 (Lock test) | 6 | AC-005, AC-007 |
| FR-012 (Folder test) | 5 | AC-007 |
| FR-013 (Search test) | 5 | AC-007 |
| FR-014 (Color Picker test) | 5 | AC-007 |
| FR-015 (Reminder test) | 6 | AC-004, AC-007 |
| FR-016 (Image Attachment test) | 6 | AC-002, AC-007 |
| FR-017 (Audio test) | 6 | AC-003, AC-007 |
| FR-018 (Settings test) | 6 | AC-007 |
| FR-019 (About test) | 6 | AC-007 |
| FR-020 (Edge Cases test) | 6 | AC-007 |
| FR-021 (Bug fix test coverage) | 5, 6 | AC-007, AC-010 |
| NFR-001 (No flakiness) | 7 | AC-007 |
| NFR-002 (Deterministic tests) | 5, 6 | AC-008 |
