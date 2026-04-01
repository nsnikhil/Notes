# Implementation Plan: Checklist Bug Fixes and Test Cleanup

## Overview
Fix two bugs in the Notes app and clean up the Espresso test suite. Bug 1: `editorActions()` in `CheckListAdapter.MyViewHolder.init` has no throttle, so when `onBindViewHolder.setFocus()` steals focus from the previous EditText, the system fires a second `IME_ACTION_NEXT`, calling `addCheckListItem()` twice. Fix by adding `throttleFirst(300, MILLISECONDS)` to the `editorActions()` subscription. Bug 2: `PasswordUtil.checkLock()` returns `false` when no password exists (showing the dialog), but `setLock()` only calls `setMenuState()` when `checkLock()` returns `true` — so on first attempt with no password, the lock never toggles even after the FragmentResult callback fires. The callback itself is correctly wired. The fix is to ensure `setLock()` does not short-circuit when `checkLock()` returns `false`; the FragmentResult listener already handles the deferred toggle. Bug 3: Delete 3 empty shell test classes and consolidate 8 duplicate test pairs by removing the less comprehensive version from each pair.

## Sub-Tasks

### Sub-Task 1: Fix Checklist Enter Duplication Bug + Verification Test
- **Size:** S
- **Description:** Add `throttleFirst(300, TimeUnit.MILLISECONDS)` to the `text.editorActions()` subscription in `CheckListAdapter.MyViewHolder.init`. This prevents the second `IME_ACTION_NEXT` (caused by `setFocus()` stealing focus) from firing `addClickListener()` a second time. Also update the existing `BugFixVerificationEspressoTest.testChecklistEnterAddsExactlyOneItem` to add a rapid-fire Enter scenario that presses IME action twice within 300ms and asserts exactly 2 items (not 3).
- **Dependencies:** None
- **Entry Criteria:** Repository compiles with `assembleDebug`
- **Exit Criteria:**
  - `CheckListAdapter.kt` line `text.editorActions().subscribe {` is changed to `text.editorActions().throttleFirst(300, TimeUnit.MILLISECONDS).subscribe {`
  - `BugFixVerificationEspressoTest.kt` has an updated or additional test verifying rapid Enter does not double-add
- **Success Criteria:**
  - Given a note with a checklist item containing text, when the user presses Enter (IME_ACTION_NEXT), then exactly one new checklist item is created (RecyclerView child count increases by 1)
  - Given a note with a checklist item, when IME_ACTION_NEXT fires twice within 300ms (simulating the setFocus re-entrancy), then only one new item is added (not two)
  - Given the existing checklist tests in `ChecklistFlowTest`, when `connectedDemoDebugAndroidTest` runs, then all existing checklist tests still pass (add, remove, check, rapid-add-5)
- **Files:**
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/adapters/CheckListAdapter.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/BugFixVerificationEspressoTest.kt`

### Sub-Task 2: Fix Lock Toggle on First Attempt + Verification Test
- **Size:** S
- **Description:** The bug is in `NewNoteFragment.setLock()`. Currently: `if (PasswordUtil.checkLock(...)) mIsLocked = setMenuState(...)`. When no password exists, `checkLock()` returns `false` (and shows the dialog), so `setMenuState()` is never called. The FragmentResult listener in `onViewCreated` correctly calls `setMenuState()` when the password is created, but `setLock()` stores the `MenuItem` reference in the local `item` parameter — the listener uses `mLockMenu` which may be null if the menu hasn't been created yet, or may reference a stale item. The fix: remove the `if` guard so `setLock()` doesn't try to toggle immediately when no password exists — let the FragmentResult callback handle it exclusively. Specifically, change `setLock()` to just call `checkLock()` without conditioning `setMenuState()` on its return value. When `checkLock()` returns `true` (password exists), call `setMenuState()` immediately. When it returns `false`, do nothing — the FragmentResult listener will handle it. Also update `BugFixVerificationEspressoTest.testLockTogglesOnFirstAttempt` to add a `Thread.sleep` before the dialog interaction to wait for the async `decryptAndGet` LiveData, and verify the lock icon state changes after password creation.
- **Dependencies:** None
- **Entry Criteria:** Repository compiles with `assembleDebug`
- **Exit Criteria:**
  - `NewNoteFragment.setLock()` no longer wraps `setMenuState()` inside the `if (checkLock())` — the immediate toggle only happens when `checkLock()` returns `true`
  - `BugFixVerificationEspressoTest.testLockTogglesOnFirstAttempt` waits for async password decryption before interacting with dialog
- **Success Criteria:**
  - Given a note with no password previously set, when the user taps the lock icon, then the password dialog appears, and after setting matching passwords and tapping Save, the lock state toggles to locked on that same first attempt
  - Given a note with a password already set, when the user taps the lock icon, then the lock toggles immediately without showing the password dialog
  - Given the existing lock tests in `LockFlowTest`, when `connectedDemoDebugAndroidTest` runs, then all existing lock tests still pass
- **Files:**
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/BugFixVerificationEspressoTest.kt`

### Sub-Task 3: Clean Up Redundant Espresso Tests
- **Size:** M
- **Description:** Delete 3 empty shell classes and consolidate 8 duplicate test pairs by removing the less comprehensive version. Detailed actions:

  **Delete (empty shells — no `@Test` methods):**
  1. `NewNoteFragmentScreenTest.kt` — empty, no tests
  2. `MainActivityScreenTest.kt` — only setup/teardown, no tests
  3. `ListFragmentScreenTest.kt` — only setup/teardown, no tests

  **Consolidate duplicates (remove the redundant one, keep the more comprehensive):**
  1. `AboutFragmentScreenTest.openLibrariesListTest` ↔ `AboutFlowTest.aboutShowsLibrariesButton` — **Remove** `AboutFragmentScreenTest.openLibrariesListTest` (AboutFlowTest uses NavigationViewActions for proper navigation; AboutFragmentScreenTest uses manual drawer open without navigating). **Keep** `AboutFlowTest.aboutShowsLibrariesButton`. Since removing `openLibrariesListTest` leaves `AboutFragmentScreenTest` with only `openLicenseTest`, and `AboutFlowTest.aboutShowsLicenseButton` covers the same behavior, **delete the entire `AboutFragmentScreenTest.kt` file**.
  2. `AboutFragmentScreenTest.openLicenseTest` ↔ `AboutFlowTest.aboutShowsLicenseButton` — covered by deleting `AboutFragmentScreenTest.kt` above.
  3. `BugFixVerificationEspressoTest.testCameraToolLaunchesIntent` ↔ `ImageAttachmentFlowTest.tapCameraToolWithStubbedIntent` — **Keep** `BugFixVerificationEspressoTest.testCameraToolLaunchesIntent` (it asserts `Intents.intended()`). **Remove** `ImageAttachmentFlowTest.tapCameraToolWithStubbedIntent` (it only taps, no assertion). Remove just the method, keep the file (it has 6 other unique tests).
  4. `NoteCrudFlowTest.tapFabRevealsAddFolderButton` ↔ `FolderFlowTest.tapFabRevealsAddFolderButton` — **Keep** `FolderFlowTest.tapFabRevealsAddFolderButton` (it's in the folder-focused test class). **Remove** `NoteCrudFlowTest.tapFabRevealsAddFolderButton` (remove just the method; file has 5 other unique tests).
  5. `ColorPickerFlowTest.bottomSheetShowsAllTools` ↔ `CrossFeatureFlowTest.testNewNoteHasAllTools` — **Keep** `ColorPickerFlowTest.bottomSheetShowsAllTools` (it's in the tool-focused test class and checks all 7 tools). **Remove** `CrossFeatureFlowTest.testNewNoteHasAllTools` (remove just the method; file has 3 other unique tests).
  6. `LockFlowTest.lockIconIsDisplayedInNewNote` ↔ `LockFlowTest.testLockIconVisibleInNewNote` — **Keep** `LockFlowTest.lockIconIsDisplayedInNewNote` (simpler, same assertion). **Remove** `LockFlowTest.testLockIconVisibleInNewNote` (adds unnecessary replaceText before the same check).
  7. `LockFlowTest.testLockIconStateAfterToggle` ↔ `LockFlowTest.testLockTogglesTwice` — **Keep** `LockFlowTest.testLockTogglesTwice` (it tests both lock and unlock — more comprehensive). **Remove** `LockFlowTest.testLockIconStateAfterToggle` (only tests lock, subset of the other).
  8. `ReminderFlowTest.tapReminderToolIsClickable` ↔ `ReminderFlowTest.testReminderToolClickable` — **Keep** `ReminderFlowTest.testReminderToolClickable` (checks both `isDisplayed()` and `isClickable()`). **Remove** `ReminderFlowTest.tapReminderToolIsClickable` (only performs click, no assertion).

- **Dependencies:** Sub-task 1, Sub-task 2 (bug fix tests in `BugFixVerificationEspressoTest.kt` must be finalized before cleanup touches that file)
- **Entry Criteria:** Sub-tasks 1 and 2 are complete; all bug fix tests pass
- **Exit Criteria:**
  - 3 empty shell files deleted
  - 4 files modified (methods removed from `ImageAttachmentFlowTest`, `NoteCrudFlowTest`, `CrossFeatureFlowTest`, `LockFlowTest`, `ReminderFlowTest`)
  - 1 file deleted (`AboutFragmentScreenTest.kt`)
  - Total: 4 files deleted, 5 files modified
- **Success Criteria:**
  - Given the cleaned-up test suite, when `connectedDemoDebugAndroidTest` runs, then 0 test failures are reported
  - Given the cleaned-up test suite, when comparing tested behaviors before and after, then every previously tested behavior (about navigation, camera intent, folder FAB, all-tools visibility, lock icon display, lock toggle, reminder clickability) is still covered by at least one remaining test
  - Given the project after cleanup, when `assembleDebug` runs, then the build succeeds
- **Files:**
  - Delete: `app/src/androidTest/java/com/nrs/nsnik/notes/NewNoteFragmentScreenTest.kt`
  - Delete: `app/src/androidTest/java/com/nrs/nsnik/notes/MainActivityScreenTest.kt`
  - Delete: `app/src/androidTest/java/com/nrs/nsnik/notes/ListFragmentScreenTest.kt`
  - Delete: `app/src/androidTest/java/com/nrs/nsnik/notes/AboutFragmentScreenTest.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/ImageAttachmentFlowTest.kt` (remove `tapCameraToolWithStubbedIntent`)
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/NoteCrudFlowTest.kt` (remove `tapFabRevealsAddFolderButton`)
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/CrossFeatureFlowTest.kt` (remove `testNewNoteHasAllTools`)
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/LockFlowTest.kt` (remove `testLockIconVisibleInNewNote` and `testLockIconStateAfterToggle`)
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/ReminderFlowTest.kt` (remove `tapReminderToolIsClickable`)

### Sub-Task 4: Full Test Suite Verification on Emulator
- **Size:** S
- **Description:** Run the complete test suite on the connected emulator to verify all changes work together. Run `assembleDebug` for both flavors, `testDemoDebugUnitTest` for unit tests, and `connectedDemoDebugAndroidTest` for Espresso tests. Document the test count and confirm 0 failures.
- **Dependencies:** Sub-task 1, Sub-task 2, Sub-task 3
- **Entry Criteria:** All 3 previous sub-tasks are complete
- **Exit Criteria:**
  - `assembleDebug` succeeds for both product flavors
  - `testDemoDebugUnitTest` passes with 0 failures
  - `connectedDemoDebugAndroidTest` passes with 0 failures
  - Final Espresso test count is documented (expected: ~66 tests, down from 74 after removing ~8 duplicate/empty tests)
- **Success Criteria:**
  - Given the full codebase after all changes, when `./gradlew assembleDebug` runs, then it succeeds with exit code 0
  - Given the full codebase, when `./gradlew testDemoDebugUnitTest` runs, then all ~31 unit test files pass with 0 failures
  - Given the full codebase, when `./gradlew connectedDemoDebugAndroidTest` runs on the Pixel 9a API 35 emulator, then 0 failures are reported
  - Given the test results, when the Espresso test count is compared to the pre-change count of 74, then the reduction matches the number of removed duplicate/empty tests (expected ~8 fewer)
- **Files:**
  - Create: None
  - Modify: None (verification only)

## Implementation Order
1. Sub-task 1 — Fix checklist Enter duplication (no dependencies)
2. Sub-task 2 — Fix lock toggle on first attempt (no dependencies, parallel-safe with Sub-task 1)
3. Sub-task 3 — Clean up redundant Espresso tests (depends on 1, 2)
4. Sub-task 4 — Full test suite verification on emulator (depends on 1, 2, 3)

Note: Sub-tasks 1 and 2 can be executed in parallel since they modify different source files. Sub-task 2 modifies `BugFixVerificationEspressoTest.kt` (lock test) and Sub-task 1 also modifies `BugFixVerificationEspressoTest.kt` (checklist test) — but they touch different test methods, so parallel execution is safe if each sub-task only modifies its own test method.

## Integration Test Plan
After all sub-tasks are complete, the IntegrationTester should verify:

- **End-to-end checklist scenario:** Create a new note → add a checklist item → type text → press Enter → verify exactly 1 new item added → press Enter again → verify exactly 1 more item added → save note → reopen → verify all 3 items present
- **End-to-end lock scenario (no password):** Create a new note → tap lock icon → password dialog appears → enter matching passwords → tap Save → verify lock icon shows locked state → tap lock again → verify it toggles to unlocked immediately (no dialog)
- **End-to-end lock scenario (existing password):** With password already set from previous test → create new note → tap lock → verify immediate toggle (no dialog) → tap again → verify toggle back
- **Rapid Enter stress test:** Create a note → add checklist → press Enter 5 times rapidly → verify exactly 6 items (1 original + 5 new)
- **Test count verification:** Run full `connectedDemoDebugAndroidTest` → verify 0 failures → verify test count is ~66 (74 minus ~8 removed)
- **Build verification:** Run `assembleDebug` for both flavors → verify success
- **Unit test verification:** Run `testDemoDebugUnitTest` → verify 0 failures

## Requirements Traceability
| Requirement | Sub-Task(s) |
|-------------|-------------|
| FR-001: Enter creates exactly one checklist item | Sub-task 1 |
| FR-002: Root cause is setFocus() stealing focus → second IME_ACTION_NEXT | Sub-task 1 |
| FR-003: Add throttleFirst/debounce to editorActions() | Sub-task 1 |
| FR-004: Cursor/focus on newly created item after Enter | Sub-task 1 (existing `setFocus()` already handles this) |
| FR-005: Lock works on first attempt including no-password case | Sub-task 2 |
| FR-006: Fix existing callback, don't re-implement | Sub-task 2 |
| FR-007: Fix timing/lifecycle issue in callback flow | Sub-task 2 |
| FR-008: Delete 3 empty shell classes | Sub-task 3 |
| FR-009: Consolidate 8 duplicate test pairs | Sub-task 3 |
| FR-010: Preserve behavioral test coverage | Sub-task 3, Sub-task 4 |
| NFR-001: Debounce ≤300ms for imperceptible lag | Sub-task 1 |
| AC-001 through AC-009 | Sub-task 4 (final verification) |
