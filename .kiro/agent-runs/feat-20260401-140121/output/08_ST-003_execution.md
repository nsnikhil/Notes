# ST-003 Execution Report: Fix lock not working on first attempt

## Status: ✅ PASSED

## Summary
Fixed the root cause of the "lock not working on first attempt" bug in `PasswordDialogFragment.kt`. Two LiveData `.observe()` calls were using `this` (the Fragment instance) as the lifecycle owner instead of `viewLifecycleOwner`. In a DialogFragment, the Fragment lifecycle and the view lifecycle are different — using `this` causes the observer to not fire correctly when the view is ready, especially on the first attempt.

## Changes Made

### Files Modified
| File | Change |
|------|--------|
| `app/src/main/java/com/nrs/nsnik/notes/view/fragments/dialogFragments/PasswordDialogFragment.kt` | Changed 2x `.observe(this, Observer {` → `.observe(viewLifecycleOwner, Observer {` with inline BUG FIX comments |

### Files Created
| File | Purpose |
|------|---------|
| `app/src/test/java/com/nrs/nsnik/notes/PasswordDialogViewLifecycleOwnerTest.kt` | 5 unit tests verifying the fix |

## Verification

### Entry Criteria
- [x] `PasswordDialogFragment.kt` exists with `.observe(this, ...)` calls — confirmed 2 occurrences at lines 94 and 175
- [x] FragmentResult API already wired in NewNoteFragment (`setFragmentResultListener` for `REQUEST_KEY`) — confirmed in `onViewCreated()`

### Exit Criteria
- [x] Zero `.observe(this, ...)` calls remain in PasswordDialogFragment
- [x] Two `.observe(viewLifecycleOwner, ...)` calls present
- [x] `./gradlew assembleDebug` — BUILD SUCCESSFUL
- [x] `./gradlew testDemoDebugUnitTest` — BUILD SUCCESSFUL (280 tests, 0 failures)

## Test Details
| Test | Result |
|------|--------|
| `sourceFileExists` | ✅ |
| `noLiveDataObserversUseThisAsLifecycleOwner` | ✅ |
| `liveDataObserversUseViewLifecycleOwner` | ✅ |
| `fragmentResultRequestKeyExists` | ✅ |
| `savePasswordSetsFragmentResultBeforeDismiss` | ✅ |

## Root Cause Explanation
In Android Fragments, `this` as a lifecycle owner tracks the Fragment's full lifecycle (`onCreate` → `onDestroy`), while `viewLifecycleOwner` tracks the view's lifecycle (`onCreateView` → `onDestroyView`). For DialogFragments, the view can be created/destroyed multiple times within a single Fragment lifecycle. Using `this` means the LiveData observer may be registered against a stale lifecycle state, causing it to not deliver values when the view is actually ready — manifesting as "lock doesn't work on first attempt."
