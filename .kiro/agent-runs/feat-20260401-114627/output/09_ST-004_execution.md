# ST-004 Execution Report: Fix Lock and Navigation Bugs

## Status: ✅ PASSED

## Bugs Fixed

### BUG #4 — Lock toggle requires two taps (password creation doesn't complete lock)

**Root Cause**: `PasswordDialogFragment.savePassword()` saved the password and dismissed the dialog, but never communicated back to `NewNoteFragment`. Since `PasswordUtil.checkLock()` returned `false` when no password existed, `setLock()` in `NewNoteFragment` never toggled `mIsLocked`.

**Fix**:
1. Added `companion object` with `REQUEST_KEY` and `RESULT_PASSWORD_CREATED` constants to `PasswordDialogFragment`
2. In `savePassword()`, added `parentFragmentManager.setFragmentResult(REQUEST_KEY, bundleOf(RESULT_PASSWORD_CREATED to true))` before `dismiss()`
3. In `NewNoteFragment.onViewCreated()`, registered a `FragmentResultListener` for `PasswordDialogFragment.REQUEST_KEY` that completes the lock toggle when the result arrives

### BUG #5 — Drawer navigation stacks fragments (back button traverses all visited items)

**Root Cause**: `MainActivity`'s drawer `setNavigationItemSelectedListener` called `controller.navigate(R.id.navItemSettings)` and `controller.navigate(R.id.navItemAbout)` without any `NavOptions`, causing each navigation to push onto the back stack.

**Fix**:
1. Added `NavOptions` import
2. Built `NavOptions` with `.setPopUpTo(R.id.navItemNotes, false)` and `.setLaunchSingleTop(true)`
3. Applied these options to Settings and About navigation calls
4. For Notes item, used `controller.popBackStack(R.id.navItemNotes, false)` to return to start destination

## Files Modified
- `app/src/main/java/com/nrs/nsnik/notes/view/fragments/dialogFragments/PasswordDialogFragment.kt`
- `app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt`
- `app/src/main/java/com/nrs/nsnik/notes/view/MainActivity.kt`

## Files Created
- `app/src/test/java/com/nrs/nsnik/notes/LockAndNavigationBugFixTest.kt`

## Verification
- `./gradlew assembleDebug` — ✅ BUILD SUCCESSFUL
- `./gradlew testDemoDebugUnitTest` — ✅ BUILD SUCCESSFUL (all tests pass)
- `LockAndNavigationBugFixTest` — ✅ 3/3 tests pass
