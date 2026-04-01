# Implementation Plan: Notes App Bug Fixes and Comprehensive Espresso Test Suite

## Overview
This plan fixes three confirmed bugs (checklist DiffUtil identity, camera permission gate, lock FragmentResult timing) and then adds comprehensive Espresso tests for five features. Bug fixes are in Sub-Task 1 so all subsequent test sub-tasks run against corrected code. The plan is structured as 4 sub-tasks: (1) all three bug fixes + their verification Espresso tests, (2) comprehensive Espresso tests for checklist and lock, (3) comprehensive Espresso tests for camera, gallery, reminder, and cross-feature interactions, (4) full build and test verification on emulator.

## Sub-Tasks

### Sub-Task 1: Fix Three Bugs + Verification Espresso Tests
- **Size:** M
- **Description:** Fix the three confirmed bugs and add one Espresso verification test per bug. Specifically:
  1. **Checklist fix (FR-001):** Add `val id: String = UUID.randomUUID().toString()` to `CheckListObject` with `@Serializable` support. Update `CheckListDiffUtil.areItemsTheSame()` to compare by `id` only. `areContentsTheSame()` continues comparing `text` and `done`. The `id` field uses a default value, so kotlinx.serialization handles old data without migration.
  2. **Camera fix (FR-002):** In `NewNoteFragment.listeners()`, replace `checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, EXTERNAL_STORAGE_PERMISSION_CODE, ::startCameraIntent)` with a direct call to `startCameraIntent()`. Also remove the `@RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)` annotation from `startCameraIntent()`.
  3. **Lock verification (FR-003):** Write an Espresso test that taps the lock icon, enters a password in both fields, taps Save, and asserts the lock icon changes state — confirming `FragmentResultListener` fires on first attempt. If the test fails, add a short `Thread.sleep()` or `IdlingResource` wait before the assertion to handle async `FragmentResult` delivery.
  4. **Update existing unit tests:** Update `DiffUtilTest` to test `areItemsTheSame` by `id` field. Update `CheckListObjectTest` to verify `id` field serialization round-trip.
- **Dependencies:** None
- **Entry Criteria:** Emulator running, project builds successfully with `assembleDebug`
- **Exit Criteria:**
  - `CheckListObject.kt` has `val id: String = UUID.randomUUID().toString()` field
  - `CheckListDiffUtil.areItemsTheSame()` compares by `id` only
  - `NewNoteFragment.kt` camera click handler calls `startCameraIntent()` directly (no `checkPermission` wrapper)
  - `startCameraIntent()` has no `@RequiresPermission(WRITE_EXTERNAL_STORAGE)` annotation
  - Three new Espresso test methods exist verifying each bug fix
  - Existing unit tests updated for new `id` field
  - `testDemoDebugUnitTest` passes
- **Success Criteria:**
  - Given two `CheckListObject` instances with different `id` values but same `text` and `done`, when `areItemsTheSame()` is called, then it returns `false`
  - Given two `CheckListObject` instances with the same `id` but different `text`, when `areItemsTheSame()` is called, then it returns `true`
  - Given a `CheckListObject` serialized to JSON and deserialized back, when the `id` field is checked, then it matches the original
  - Given a `CheckListObject` deserialized from JSON without an `id` field, when the object is created, then `id` is a non-empty auto-generated UUID string
  - Given a user on API 29+ emulator tapping the camera tool, when the camera intent fires, then no permission denial occurs (verified via espresso-intents stub)
  - Given a user tapping the lock icon and entering matching passwords in both fields, when Save is tapped, then the lock icon state changes to locked on the first attempt
- **Files:**
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/model/CheckListObject.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/adapters/diffUtil/CheckListDiffUtil.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt`
  - Modify: `app/src/test/java/com/nrs/nsnik/notes/util/DiffUtilTest.kt`
  - Modify: `app/src/test/java/com/nrs/nsnik/notes/model/CheckListObjectTest.kt`
  - Create: `app/src/androidTest/java/com/nrs/nsnik/notes/BugFixVerificationEspressoTest.kt`

### Sub-Task 2: Comprehensive Espresso Tests — Checklist and Lock
- **Size:** M
- **Description:** Add comprehensive Espresso tests for the checklist and lock features covering happy paths, edge cases, and error scenarios. These expand the existing `ChecklistFlowTest.kt` and `LockFlowTest.kt` files with additional test methods.
  - **Checklist tests (FR-005, FR-010, FR-011, FR-012):**
    - Happy path: add a checklist item, type text, verify it displays; toggle checkbox, verify strikethrough styling
    - Happy path: add multiple items via Enter key, verify each appears as a separate row
    - Edge case: add item, delete it via remove button, verify list updates
    - Edge case: add item with empty text, verify it persists (no crash)
    - Error scenario: rapidly add 10+ items, verify no duplicates and no crash
  - **Lock tests (FR-006, FR-010, FR-011, FR-012):**
    - Happy path: set password, verify lock icon changes; save note, reopen, verify lock state persisted
    - Edge case: tap lock with no password set, verify dialog appears with "create" mode (two password fields visible)
    - Edge case: cancel password dialog, verify lock state unchanged
    - Error scenario: enter mismatched passwords, verify error message shown
    - Error scenario: submit empty password, verify error message shown
- **Dependencies:** Sub-task 1
- **Entry Criteria:** Sub-task 1 complete, all three bug fixes applied, `testDemoDebugUnitTest` passes
- **Exit Criteria:**
  - `ChecklistFlowTest.kt` has ≥5 new test methods covering happy path, edge cases, error scenarios
  - `LockFlowTest.kt` has ≥5 new test methods covering happy path, edge cases, error scenarios
  - All new tests compile and pass on emulator
- **Success Criteria:**
  - Given a new note with the bottom sheet open, when the user taps the checklist tool and types "Buy milk", then the checklist area is visible and contains "Buy milk"
  - Given a checklist with one item, when the user checks the checkbox, then the text displays with strikethrough styling
  - Given a checklist with one item, when the user taps the remove button, then the item is removed and the checklist area hides
  - Given a checklist, when the user rapidly adds 10 items by pressing Enter, then exactly 10 items are displayed with no duplicates
  - Given a new note, when the user taps lock, enters "test123" in both password fields, and taps Save, then the lock icon shows the locked state
  - Given the password dialog, when the user taps Cancel, then the dialog dismisses and the lock icon remains in unlocked state
  - Given the password dialog in create mode, when the user enters mismatched passwords and taps Save, then an error message is displayed
  - Given the password dialog, when the user submits with an empty password field, then an error message is displayed
- **Files:**
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/ChecklistFlowTest.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/LockFlowTest.kt`

### Sub-Task 3: Comprehensive Espresso Tests — Camera, Gallery, Reminder, Cross-Feature
- **Size:** M
- **Description:** Add comprehensive Espresso tests for camera, gallery, and reminder features, plus cross-feature interaction tests. Expands existing `ImageAttachmentFlowTest.kt` and `ReminderFlowTest.kt`, and creates a new cross-feature test file.
  - **Camera tests (FR-007, FR-010, FR-011, FR-012):**
    - Happy path: tap camera tool, verify camera intent fires (via espresso-intents stub), stub successful result, verify image list becomes visible
    - Edge case: tap camera tool, cancel camera (stub RESULT_CANCELED), verify no image added
    - Error scenario: verify camera tool is accessible even without WRITE_EXTERNAL_STORAGE (the bug fix)
  - **Gallery tests (FR-008, FR-010, FR-011, FR-012):**
    - Happy path: tap gallery tool, verify GetContent intent fires, stub successful result with a content URI, verify image list becomes visible
    - Edge case: tap gallery tool, cancel selection, verify no image added
    - Edge case: verify gallery launches without any permission prompt
  - **Reminder tests (FR-009, FR-010, FR-011, FR-012):**
    - Happy path: tap reminder tool, verify TimePickerDialog is displayed
    - Edge case: verify reminder tool is accessible from bottom sheet
  - **Cross-feature tests (FR-013):**
    - Add checklist + attach image + set lock on same note, verify all three features coexist
    - Add checklist items, lock note, verify checklist persists after lock
- **Dependencies:** Sub-task 1
- **Entry Criteria:** Sub-task 1 complete, bug fixes applied
- **Exit Criteria:**
  - `ImageAttachmentFlowTest.kt` has ≥5 new test methods covering camera and gallery happy paths, edge cases, error scenarios
  - `ReminderFlowTest.kt` has ≥2 new test methods
  - New `CrossFeatureFlowTest.kt` has ≥2 test methods
  - All new tests compile and pass on emulator
- **Success Criteria:**
  - Given a new note, when the user taps the camera tool, then a camera intent is fired (verified via espresso-intents `intended()`)
  - Given a stubbed successful camera result, when the camera returns, then the image list RecyclerView becomes visible
  - Given a stubbed canceled camera result, when the camera returns, then no image is added to the list
  - Given a new note, when the user taps the gallery tool, then a GetContent intent is fired without any permission dialog
  - Given a stubbed canceled gallery result, when the gallery returns, then no image is added
  - Given a new note, when the user taps the reminder tool, then a TimePickerDialog is displayed
  - Given a new note, when the user adds a checklist item, attaches an image (stubbed), and locks the note, then all three features are active simultaneously without conflict
- **Files:**
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/ImageAttachmentFlowTest.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/ReminderFlowTest.kt`
  - Create: `app/src/androidTest/java/com/nrs/nsnik/notes/CrossFeatureFlowTest.kt`

### Sub-Task 4: Full Build and Test Verification
- **Size:** S
- **Description:** Run the complete build and test pipeline to verify all fixes and new tests pass. Execute `assembleDebug` for both flavors, `testDemoDebugUnitTest` for unit tests, and `connectedDemoDebugAndroidTest` for all Espresso tests on the emulator. Document results. This sub-task produces no code changes — it is a verification-only gate.
- **Dependencies:** Sub-task 1, Sub-task 2, Sub-task 3
- **Entry Criteria:** All three prior sub-tasks complete, emulator running
- **Exit Criteria:**
  - `./gradlew assembleDebug` succeeds (both demo and full flavors)
  - `./gradlew testDemoDebugUnitTest` passes with 0 failures
  - `./gradlew connectedDemoDebugAndroidTest` passes with 0 failures
  - No existing tests removed (verified by comparing test file count before/after)
- **Success Criteria:**
  - Given the full project after all changes, when `assembleDebug` is executed, then the build succeeds for both demo and full flavors
  - Given the full project, when `testDemoDebugUnitTest` is executed, then all unit tests pass with 0 failures
  - Given the full project with emulator connected, when `connectedDemoDebugAndroidTest` is executed, then all Espresso tests pass with 0 failures
  - Given the test file listing before and after changes, when compared, then no previously existing test files have been removed
- **Files:**
  - Create: None
  - Modify: None (verification only)

## Implementation Order
1. Sub-task 1 — Fix Three Bugs + Verification Espresso Tests (no dependencies)
2. Sub-task 2 — Comprehensive Espresso Tests: Checklist and Lock (depends on 1)
3. Sub-task 3 — Comprehensive Espresso Tests: Camera, Gallery, Reminder, Cross-Feature (depends on 1; can run in parallel with Sub-task 2)
4. Sub-task 4 — Full Build and Test Verification (depends on 1, 2, 3)

## Integration Test Plan
After all sub-tasks are complete, the IntegrationTester should verify:

- **End-to-end checklist flow:** Create a note, add 5 checklist items via Enter key, type text in each, toggle checkboxes, remove one item, save the note, reopen it, and verify all items persist correctly with no duplicates.
- **End-to-end camera flow:** Create a note, tap camera tool (stubbed), verify image appears in the image list, save the note. Confirm no WRITE_EXTERNAL_STORAGE permission dialog appears.
- **End-to-end gallery flow:** Create a note, tap gallery tool (stubbed), verify image appears, save the note. Confirm no READ_EXTERNAL_STORAGE permission dialog appears.
- **End-to-end lock flow:** Create a note, tap lock icon, set password "test123" in both fields, tap Save, verify lock icon changes to locked state on first attempt. Save note, reopen, verify lock state persisted.
- **End-to-end reminder flow:** Create a note, tap reminder tool, verify TimePickerDialog appears.
- **Cross-feature interaction:** Create a note with checklist items + image attachment + lock, save, reopen, verify all three features coexist.
- **Error/edge cases:** Empty password submission shows error; mismatched passwords show error; cancel camera returns no image; cancel gallery returns no image; rapid checklist additions produce no duplicates.
- **Build verification:** `assembleDebug` succeeds for both flavors; `testDemoDebugUnitTest` 0 failures; `connectedDemoDebugAndroidTest` 0 failures; no existing tests removed.

## Requirements Traceability
| Requirement | Sub-Task(s) |
|-------------|-------------|
| FR-001 (Checklist DiffUtil fix) | Sub-task 1 |
| FR-002 (Camera permission fix) | Sub-task 1 |
| FR-003 (Lock FragmentResult fix) | Sub-task 1 |
| FR-004 (Bug fix verification tests) | Sub-task 1 |
| FR-005 (Checklist Espresso tests) | Sub-task 2 |
| FR-006 (Lock Espresso tests) | Sub-task 2 |
| FR-007 (Camera Espresso tests) | Sub-task 3 |
| FR-008 (Gallery Espresso tests) | Sub-task 3 |
| FR-009 (Reminder Espresso tests) | Sub-task 3 |
| FR-010 (Happy path coverage) | Sub-task 2, Sub-task 3 |
| FR-011 (Edge case coverage) | Sub-task 2, Sub-task 3 |
| FR-012 (Error scenario coverage) | Sub-task 2, Sub-task 3 |
| FR-013 (Cross-feature tests) | Sub-task 3 |
| FR-014 (connectedAndroidTest 0 failures) | Sub-task 4 |
| FR-015 (testDemoDebugUnitTest passes) | Sub-task 4 |
| FR-016 (assembleDebug succeeds) | Sub-task 4 |
| FR-017 (No tests removed) | Sub-task 4 |
| FR-018 (Both flavors build) | Sub-task 4 |
| FR-019 (Verification commands executed) | Sub-task 4 |
| NFR-001 (Build reliability) | Sub-task 4 |
| NFR-002 (Test reliability) | Sub-task 4 |
| NFR-003 (Test coverage breadth) | Sub-task 2, Sub-task 3 |
