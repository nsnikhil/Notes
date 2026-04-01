# ST-002 Execution Report: Fix lock not working on first attempt

## Status: ✅ PASSED

## Root Cause Analysis

The `PasswordDialogFragment` was shown on `parentFragmentManager` (the NavHostFragment's `childFragmentManager`), making the dialog a **sibling** of `NewNoteFragment` in the fragment hierarchy. The `setFragmentResultListener` was also registered on `parentFragmentManager`. While the keys and FragmentManager instances matched, the FragmentResult delivery between sibling fragments within the Navigation component's FragmentManager had a subtle timing issue — the result was stored but not reliably delivered to the listener.

**Fix:** Changed both `setLock()` and the `setFragmentResultListener` registration to use `childFragmentManager` instead of `parentFragmentManager`. This makes the `PasswordDialogFragment` a **child** of `NewNoteFragment`, ensuring the FragmentResult communication happens within `NewNoteFragment`'s own scope. The dialog's `parentFragmentManager` (used in `setFragmentResult()`) now correctly points to `NewNoteFragment.childFragmentManager`, matching the listener registration.

## Changes Made

### 1. Bug Fix — NewNoteFragment.kt
**File:** `app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt`

Two changes:

**a) `setLock()` — use `childFragmentManager` for dialog:**
```kotlin
private fun setLock(item: MenuItem) {
    if (PasswordUtil.checkLock(..., childFragmentManager, "password"))
        mIsLocked = setMenuState(...)
}
```

**b) `onViewCreated()` — register listener on `childFragmentManager`:**
```kotlin
childFragmentManager.setFragmentResultListener(
    PasswordDialogFragment.REQUEST_KEY, viewLifecycleOwner
) { _, bundle -> ... }
```

### 2. Test Update — BugFixVerificationEspressoTest.kt
**File:** `app/src/androidTest/java/com/nrs/nsnik/notes/BugFixVerificationEspressoTest.kt`

Updated `testLockTogglesOnFirstAttempt` to verify the lock actually toggles by tapping lock three times after password creation, proving each tap toggles immediately without showing the dialog again.

## Test Results

### Unit Tests (`./gradlew testDemoDebugUnitTest`)
- **Result:** BUILD SUCCESSFUL
- **Failures:** 0

### Espresso Tests (`./gradlew connectedDemoDebugAndroidTest`)
- **Total:** 75 tests
- **Passed:** 75
- **Failed:** 0

### Lock-specific tests — all passed:
- `BugFixVerificationEspressoTest.testLockTogglesOnFirstAttempt` ✅
- `LockFlowTest` (8 tests) ✅

## Exit Criteria Verification

| Criterion | Status |
|-----------|--------|
| Lock toggles on first tap after setting password | ✅ |
| Espresso test passes proving first-attempt lock works | ✅ |
| `./gradlew connectedDemoDebugAndroidTest` passes | ✅ (75/75) |
| `./gradlew testDemoDebugUnitTest` passes | ✅ |
