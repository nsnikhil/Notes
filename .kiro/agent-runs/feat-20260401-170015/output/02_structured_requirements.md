# Feature: Checklist Bug Fixes and Test Cleanup

## Summary
Address two distinct bugs in the Notes application — a checklist item duplication bug triggered by the Enter key and a lock feature that fails on first attempt — and clean up redundant Espresso test cases while preserving existing test coverage. All changes must pass the full build and test suite across both product flavors.

## Detailed Requirements

### Workstream 1: Fix Checklist Enter Duplication Bug
1. Pressing Enter in a checklist item MUST create exactly one new checklist item, not two
2. The root cause lies in the event chain: `editorActions()` RxBinding → `IME_ACTION_NEXT` → `addClickListener` → `addCheckListItem`
3. The duplicate creation event in this chain MUST be identified and eliminated

### Workstream 2: Fix Lock Toggle on First Attempt
4. The lock feature MUST work correctly on the very first attempt, even when no password has been set yet
5. When `checkLock()` returns false (no password exists), a `PasswordDialogFragment` is shown — but the lock toggle currently does not activate after the password is set
6. A callback mechanism from `PasswordDialogFragment` MUST be implemented so that the lock toggle is applied after the user successfully sets a password

### Workstream 3: Espresso Test Cleanup
7. Duplicate Espresso test cases MUST be identified and removed
8. Remaining tests MUST be consolidated where appropriate
9. Existing test coverage MUST be preserved — no tested behavior should lose coverage

## Acceptance Criteria

### Workstream 1: Checklist Enter Duplication
1. Given a note with a checklist, When the user presses Enter on a checklist item, Then exactly one new checklist item is created
2. Given a note with a checklist, When the user presses Enter multiple times in succession, Then each press creates exactly one new item (no doubling)

### Workstream 2: Lock Toggle
3. Given a note with no password previously set, When the user taps the lock toggle, Then a password dialog is shown and after the user sets a password the lock is successfully applied
4. Given a note with a password already set, When the user taps the lock toggle, Then the lock is toggled immediately without showing the password dialog

### Workstream 3: Test Cleanup
5. Given the Espresso test suite after cleanup, When `connectedDemoDebugAndroidTest` is run on an emulator, Then 0 test failures are reported
6. Given the cleaned-up test suite, When comparing tested behaviors before and after cleanup, Then all previously covered behaviors are still covered

### Build Verification
7. Given the full codebase after all changes, When `assembleDebug` is run, Then the build succeeds for both product flavors
8. Given the full codebase after all changes, When `testDemoDebugUnitTest` is run, Then all unit tests pass
9. Given the full codebase after all changes, When `connectedDemoDebugAndroidTest` is run on an emulator, Then all Espresso tests pass with 0 failures

## Constraints
- All specific Espresso tests must pass on an emulator
- `connectedDemoDebugAndroidTest` must report 0 failures
- `testDemoDebugUnitTest` must pass
- `assembleDebug` must succeed
- Both product flavors must build successfully
- Test cleanup must not reduce existing test coverage

## Dependencies
- `editorActions()` RxBinding library (existing dependency for checklist input handling)
- `PasswordDialogFragment` (existing component that needs callback enhancement)
- Android emulator (required for Espresso test verification)
- Both product flavors (Demo and another) must be buildable and testable

## Non-Functional Requirements
- None identified beyond the build and test pass requirements listed in Constraints

## Technical Notes
- Bug 1 involves the RxBinding `editorActions()` chain: the event flow is `editorActions()` → `IME_ACTION_NEXT` → `addClickListener` → `addCheckListItem`. The duplication likely stems from multiple subscriptions or event propagation in this chain
- Bug 2 involves `checkLock()` returning false when no password exists, which prevents the lock toggle. The fix requires a callback from `PasswordDialogFragment` back to the caller to complete the toggle after password creation
- The project uses two product flavors (one is "Demo"; the other is unspecified)
- Espresso is used for UI/integration testing; standard JUnit for unit tests

## Open Questions
1. What is the second product flavor besides "Demo"?
2. Are there specific Espresso test names or classes that must be preserved during cleanup, or is the requirement purely behavioral coverage preservation?
3. For Bug 1, is the duplication caused by multiple RxBinding subscriptions, event re-emission, or the `addClickListener` firing alongside the RxBinding stream? (Root cause to be confirmed during investigation)
4. For Bug 2, should the `PasswordDialogFragment` callback use a Fragment result listener, an interface callback, or another mechanism? (Implementation detail — not prescribed here)
5. Is there a minimum number of Espresso tests that should remain after cleanup, or is the only criterion that coverage is preserved?
6. Should the lock feature support biometric authentication in addition to password, or is password-only sufficient?
7. Are there any performance expectations for the checklist Enter key response time (e.g., new item appears within a specific latency)?
