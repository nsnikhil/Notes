# Architecture Document

## ST-001: Fix checklist Enter key creating duplicate items

### What was changed

**File modified:** `app/src/main/java/com/nrs/nsnik/notes/view/adapters/CheckListAdapter.kt`
- Added `.throttleFirst(300, TimeUnit.MILLISECONDS)` to the `text.editorActions()` RxJava chain in the `MyViewHolder.init` block
- This prevents duplicate `IME_ACTION_NEXT` events caused by focus-steal when `setFocus()` is called on the newly added item during `onBindViewHolder`

**File created:** `app/src/androidTest/java/com/nrs/nsnik/notes/ChecklistEnterKeyTest.kt`
- Espresso test that verifies pressing Enter creates exactly 1 new checklist item each time
- Tests: 1 item → Enter → 2 items → Enter → 3 items

### Key interfaces unchanged
- `OnAddClickListener.addClickListener()` — still called on `IME_ACTION_NEXT`, just throttled
- `CheckListAdapter` constructor signature unchanged
- No new dependencies added (`TimeUnit` was already imported)

## ST-002: Fix lock not working on first attempt

### What was changed

**File modified:** `app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt`
- Changed `setLock()` to pass `childFragmentManager` instead of `parentFragmentManager` to `PasswordUtil.checkLock()`, so the `PasswordDialogFragment` is shown as a child of `NewNoteFragment`
- Changed `setFragmentResultListener` registration in `onViewCreated()` to use `childFragmentManager` instead of `parentFragmentManager`
- Root cause: The dialog was shown on `parentFragmentManager` (the NavHostFragment's childFragmentManager), making the dialog a sibling of `NewNoteFragment`. The `setFragmentResult` / `setFragmentResultListener` communication between sibling fragments on the Navigation component's FragmentManager had a subtle delivery timing issue. Using `childFragmentManager` makes the dialog a child of `NewNoteFragment`, ensuring the result is delivered reliably within `NewNoteFragment`'s own scope.

**File modified:** `app/src/androidTest/java/com/nrs/nsnik/notes/BugFixVerificationEspressoTest.kt`
- Updated `testLockTogglesOnFirstAttempt` to verify the lock actually toggles by tapping lock three times after password creation (proving each tap toggles immediately without showing the dialog again)

### Key interfaces unchanged
- `PasswordDialogFragment.savePassword()` still uses `parentFragmentManager.setFragmentResult()` — this now correctly targets `NewNoteFragment.childFragmentManager` since the dialog is shown on it
- `PasswordDialogFragment.REQUEST_KEY` and `RESULT_PASSWORD_CREATED` constants unchanged
- `PasswordUtil.checkLock()` signature unchanged
- No new dependencies added

## ST-003: Clean up redundant Espresso tests

### What was changed

**Files deleted (empty shells — no @Test methods):**
- `app/src/androidTest/java/com/nrs/nsnik/notes/MainActivityScreenTest.kt`
- `app/src/androidTest/java/com/nrs/nsnik/notes/ListFragmentScreenTest.kt`
- `app/src/androidTest/java/com/nrs/nsnik/notes/NewNoteFragmentScreenTest.kt`

**File deleted (all tests were duplicates of AboutFlowTest):**
- `app/src/androidTest/java/com/nrs/nsnik/notes/AboutFragmentScreenTest.kt`

**File modified:** `app/src/androidTest/java/com/nrs/nsnik/notes/LockFlowTest.kt`
- Removed `testLockIconVisibleInNewNote` (duplicate of `lockIconIsDisplayedInNewNote`)
- Removed `testLockIconStateAfterToggle` (subset of `testLockTogglesTwice`)
- Cleaned up 3 unused imports (`pressBack`, `withClassName`, `endsWith`)

**File modified:** `app/src/androidTest/java/com/nrs/nsnik/notes/ReminderFlowTest.kt`
- Removed `tapReminderToolIsClickable` (duplicate of `testReminderToolClickable` which has explicit `isClickable()` assertion)

**File modified:** `app/src/androidTest/java/com/nrs/nsnik/notes/FolderFlowTest.kt`
- Removed `tapFabRevealsAddFolderButton` (identical to `NoteCrudFlowTest.tapFabRevealsAddFolderButton`)

**File modified:** `app/src/androidTest/java/com/nrs/nsnik/notes/ColorPickerFlowTest.kt`
- Removed `bottomSheetShowsAllTools` (duplicate of `CrossFeatureFlowTest.testNewNoteHasAllTools`)

### Test count
- Before: 77 @Test methods across 21 files (3 files had 0 tests)
- After: 70 @Test methods across 17 files
- Removed: 7 duplicate tests, 4 files deleted
- All 70 remaining tests pass with 0 failures

### Key interfaces unchanged
- No app source code was modified
- No test scenarios lost — every removed test was a true duplicate of a remaining test