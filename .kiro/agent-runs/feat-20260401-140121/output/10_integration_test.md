# Integration Test Report

## Build
- Status: PASS

## Existing Tests
- Tests run: 51 (connected/instrumented)
- Tests passed: 51
- Tests failed: 0
- Status: PASS

## Unit Tests
- Status: PASS (all unit tests pass, no regressions)

## Root Cause Analysis

The 2 failing Espresso tests (`tapLockIconShowsPasswordDialog`, `passwordDialogHasCreateAndCancelButtons`) failed with `NoMatchingViewException` — the dialog's views were not found in the view hierarchy.

**Cause:** `PasswordDialogFragment` is shown via `DialogFragment.show()` which commits a fragment transaction asynchronously. The dialog renders in its own window (separate from the activity's DecorView). The ST-003 fix (changing `.observe(this,...)` to `.observe(viewLifecycleOwner,...)`) subtly changed the timing of when the dialog's LiveData observer fires relative to the view lifecycle, but the fundamental issue is that the tests were searching for dialog views in the default (activity) root window instead of the dialog's root window.

**Fix applied:** Added `.inRoot(isDialog())` to all Espresso view assertions that target views inside the `PasswordDialogFragment`. This tells Espresso to look in the dialog's window root rather than the activity's default root, which also properly synchronizes with the dialog's appearance.

**File changed:** `app/src/androidTest/java/com/nrs/nsnik/notes/LockFlowTest.kt`
- Added import: `androidx.test.espresso.matcher.RootMatchers.isDialog`
- `tapLockIconShowsPasswordDialog`: Added `.inRoot(isDialog())` before `.check(matches(isDisplayed()))` on `passwordDialogText`
- `passwordDialogHasCreateAndCancelButtons`: Added `.inRoot(isDialog())` before `.check(matches(isDisplayed()))` on both `passwordDialogCreate` and `passwordDialogCancel`

No implementation code was modified — this was purely a test fix.

## Integration Test Results
- Tests run: 51
- Tests passed: 51
- Tests failed: 0
- Tests skipped: 0
- Failed tests: (none)
- Skipped tests: (none)
- Status: PASS

## Requirements Coverage
| Requirement | Covered By | Status |
|-------------|-----------|--------|
| ST-003: Fix viewLifecycleOwner in PasswordDialogFragment | LockFlowTest: tapLockIconShowsPasswordDialog, passwordDialogHasCreateAndCancelButtons | COVERED |
| Lock icon visible in new note | LockFlowTest: lockIconIsDisplayedInNewNote | COVERED |
| Password dialog appears on lock tap | LockFlowTest: tapLockIconShowsPasswordDialog | COVERED |
| Password dialog has create/cancel buttons | LockFlowTest: passwordDialogHasCreateAndCancelButtons | COVERED |
| All existing tests pass (no regressions) | Full connectedDemoDebugAndroidTest suite: 51/51 pass | COVERED |
| Unit tests pass (no regressions) | testDemoDebugUnitTest: PASS | COVERED |
