# Validated Requirements: Checklist Bug Fixes and Test Cleanup

## Validation Status
- Cross-reference check: PASS
- Completeness check: PASS (after root cause corrections incorporated)

## Summary
Fix two bugs in the Notes application and clean up the Espresso test suite. Bug 1: pressing Enter in a checklist item creates two items instead of one — caused by `onBindViewHolder.setFocus()` stealing focus and triggering a second `IME_ACTION_NEXT` through the unguarded `editorActions()` subscription. Bug 2: the lock toggle fails on first attempt when no password exists — the FragmentResult callback is already implemented but has a timing/lifecycle issue in the existing flow. Additionally, remove 3 empty test shell classes and consolidate 8+ duplicate test pairs while preserving behavioral coverage.

## Detailed Requirements

### Workstream 1: Fix Checklist Enter Duplication Bug

- FR-001: Pressing Enter in a checklist item MUST create exactly one new checklist item, not two. **MUST-HAVE**
- FR-002: The root cause is `onBindViewHolder.setFocus()` stealing focus from the previous EditText, which triggers a second `IME_ACTION_NEXT` event. The `editorActions()` RxBinding subscription has no debounce or re-entrancy guard, so the second event fires `addCheckListItem()` again. **MUST-HAVE**
- FR-003: Fix MUST add a debounce/`throttleFirst` to the `editorActions()` subscription, OR add a boolean re-entrancy guard to prevent duplicate `addCheckListItem` calls. Either approach is acceptable. **MUST-HAVE**
- FR-004: After pressing Enter, cursor/focus SHOULD be on the newly created checklist item. **SHOULD-HAVE**

### Workstream 2: Fix Lock Toggle on First Attempt

- FR-005: The lock feature MUST work correctly on the very first attempt, including when no password has been set yet. **MUST-HAVE**
- FR-006: The FragmentResult callback (`REQUEST_KEY` / `RESULT_PASSWORD_CREATED`) already exists in `PasswordDialogFragment` and `NewNoteFragment`. The fix MUST NOT re-implement this callback but instead debug and fix the existing callback flow. **MUST-HAVE**
- FR-007: The fix MUST address the timing/lifecycle issue in the existing callback — possible causes include: (a) the `FragmentResultListener` not being registered before the dialog is shown, (b) the result key not matching between sender and receiver, (c) `setFragmentResult` firing after `dismiss()` destroys the dialog view, or (d) `parentFragmentManager` vs `childFragmentManager` mismatch. **MUST-HAVE**

### Workstream 3: Espresso Test Cleanup

- FR-008: The following 3 empty shell classes (no `@Test` methods) MUST be deleted: `NewNoteFragmentScreenTest.kt`, `MainActivityScreenTest.kt`, `ListFragmentScreenTest.kt`. **MUST-HAVE**
- FR-009: The following 8+ duplicate/overlapping test pairs MUST be consolidated (remove the redundant one, keep the more comprehensive version): **MUST-HAVE**
  1. `AboutFragmentScreenTest.openLibrariesListTest` ↔ `AboutFlowTest.aboutShowsLibrariesButton`
  2. `AboutFragmentScreenTest.openLicenseTest` ↔ `AboutFlowTest.aboutShowsLicenseButton`
  3. `BugFixVerificationEspressoTest.testCameraToolLaunchesIntent` ↔ `ImageAttachmentFlowTest.tapCameraToolWithStubbedIntent`
  4. `NoteCrudFlowTest.tapFabRevealsAddFolderButton` ↔ `FolderFlowTest.tapFabRevealsAddFolderButton`
  5. `ColorPickerFlowTest.bottomSheetShowsAllTools` ↔ `CrossFeatureFlowTest.testNewNoteHasAllTools`
  6. `LockFlowTest.lockIconIsDisplayedInNewNote` ↔ `LockFlowTest.testLockIconVisibleInNewNote`
  7. `LockFlowTest.testLockIconStateAfterToggle` ↔ `LockFlowTest.testLockTogglesTwice`
  8. `ReminderFlowTest.tapReminderToolIsClickable` ↔ `ReminderFlowTest.testReminderToolClickable`
- FR-010: Existing behavioral test coverage MUST be preserved — no tested behavior should lose coverage after cleanup. **MUST-HAVE**

## Acceptance Criteria

1. AC-001 (FR-001, FR-003): Given a note with a checklist, when the user presses Enter on a checklist item, then exactly one new checklist item is created
2. AC-002 (FR-001, FR-003): Given a note with a checklist, when the user presses Enter multiple times in rapid succession, then each press creates exactly one new item (no doubling)
3. AC-003 (FR-004): Given a note with a checklist, when the user presses Enter on a checklist item, then the cursor/focus is on the newly created item
4. AC-004 (FR-005, FR-006, FR-007): Given a note with no password previously set, when the user taps the lock toggle, then a password dialog is shown and after the user sets a password the lock is successfully applied on that same first attempt
5. AC-005 (FR-005): Given a note with a password already set, when the user taps the lock toggle, then the lock is toggled immediately without showing the password dialog
6. AC-006 (FR-008, FR-009, FR-010): Given the Espresso test suite after cleanup, when `connectedDemoDebugAndroidTest` is run on an emulator, then 0 test failures are reported
7. AC-007 (FR-010): Given the cleaned-up test suite, when comparing tested behaviors before and after cleanup, then all previously covered behaviors are still covered
8. AC-008: Given the full codebase after all changes, when `assembleDebug` is run, then the build succeeds for both product flavors
9. AC-009: Given the full codebase after all changes, when `testDemoDebugUnitTest` is run, then all unit tests pass

## Constraints
- `connectedDemoDebugAndroidTest` must report 0 failures
- `testDemoDebugUnitTest` must pass
- `assembleDebug` must succeed for both product flavors
- Test cleanup must not reduce existing behavioral test coverage
- Bug 1 fix must not break existing checklist functionality (add, delete, reorder, check/uncheck)
- Bug 2 fix must not alter the existing `PasswordDialogFragment` API contract — only fix the callback timing/lifecycle

## Dependencies
- `editorActions()` RxBinding library (existing dependency for checklist input handling)
- `PasswordDialogFragment` (existing component — callback already implemented, needs lifecycle fix)
- Android emulator (required for Espresso test verification)
- Both product flavors (Demo and the other) must be buildable and testable

## Non-Functional Requirements
- NFR-001: The checklist Enter key response must feel instantaneous to the user — any debounce/throttle window should be short enough (e.g., ≤300ms) to not introduce perceptible input lag. **SHOULD-HAVE**

## Technical Notes
- Bug 1: The event flow is `editorActions()` → `IME_ACTION_NEXT` → `addClickListener()` → `addCheckListItem()`. Previous fixes (UUID-based identity, DiffUtil, removed redundant `notifyItemInserted`) are already in place. The remaining issue is that `onBindViewHolder.setFocus()` on the newly added item steals focus from the previous EditText, causing the system to fire a second `IME_ACTION_NEXT`. Fix with `throttleFirst` on the `editorActions()` Observable or a boolean guard in `addCheckListItem()`.
- Bug 2: `PasswordDialogFragment` already calls `parentFragmentManager.setFragmentResult(REQUEST_KEY, ...)` in `savePassword()`. `NewNoteFragment.onViewCreated()` already registers a `FragmentResultListener` for `REQUEST_KEY`. Debug the existing flow — check listener registration timing, result key matching, and `parentFragmentManager` identity between dialog and host fragment.
- The project uses two product flavors; build commands target Demo flavor for testing.

## Scope Exclusions
- Social login or biometric authentication for the lock feature
- New Espresso test creation (only cleanup/consolidation of existing tests)
- Refactoring checklist architecture beyond the specific Enter-key bug fix
- Changes to the second product flavor's specific configuration

## Deferred Items
- Second product flavor name not specified — build verification for the non-Demo flavor deferred until flavor name is confirmed
- Whether Bug 1 fix should also suppress default `IME_ACTION_NEXT` focus-transfer behavior (beyond debounce/guard) — left to implementer judgment
- Specific lock-state verification method in Espresso tests (drawable/content-description check vs. `isDisplayed` check) — left to implementer judgment
