Fix the two remaining bugs in the Android Notes app. These bugs have survived multiple fix attempts. The agent must fix them definitively this time. A bug is ONLY considered fixed when an Espresso test running on the connected emulator proves the correct behavior. No exceptions.

The project is at the repo_path. An emulator/device is connected and available.

## Bug 1: Checklist Enter key creates duplicate items

Exact reproduction steps:
1. Open a new note
2. Tap the checklist tool → 1 item appears (correct)
3. Press Enter/Next on the keyboard → 2 NEW items are created (should be 1). Cursor is on item 3 (should be on item 2). Total items: 3 (should be 2)
4. Press Enter again → 2 MORE items created. Cursor on item 5 (should be on item 3). Total items: 5 (should be 3)

The pattern: every Enter press creates 2 items instead of 1. The extra item accumulates each time.

The agent must:
1. Read the CURRENT state of `CheckListAdapter.kt` and `NewNoteFragment.addCheckListItem()` — do not assume anything about previous fixes
2. Trace exactly what happens when the user presses Enter: `editorActions()` RxBinding → `IME_ACTION_NEXT` → `addClickListener()` → `addCheckListItem()`. Something in this chain fires twice or the list mutation + adapter notification creates a duplicate.
3. Fix the root cause so that pressing Enter creates exactly 1 new item
4. Write an Espresso test that does: add checklist item → press Enter → assert exactly 2 items in RecyclerView. Press Enter again → assert exactly 3 items. This test MUST pass on the emulator.

## Bug 2: Lock does not work on first attempt

Exact reproduction steps:
1. Open a new note (no password has been set yet)
2. Tap the lock icon in the menu
3. Password dialog appears → user enters and confirms a password
4. Dialog closes → but the note is NOT locked (lock icon still shows unlocked)
5. Tap the lock icon again → NOW it locks (icon changes to locked)

Expected behavior: after setting the password in step 3, the note should be locked immediately. The user should not need to tap lock a second time.

The agent must:
1. Read the CURRENT state of `NewNoteFragment.setLock()` and `PasswordUtil.checkLock()`
2. Understand the flow: `checkLock()` returns `false` when no password exists (shows dialog), and the lock toggle in `setLock()` only happens when `checkLock()` returns `true`. So on first tap: dialog shown, password set, but lock NOT toggled. On second tap: `checkLock()` returns `true` (password now exists), lock toggled.
3. Fix this so the lock state toggles after the password is successfully set in the dialog. This likely requires a callback from `PasswordDialogFragment` back to `NewNoteFragment` that triggers the lock toggle.
4. Write an Espresso test that does: open new note → tap lock → enter password in dialog → confirm → assert lock icon shows locked state. This test MUST pass on the emulator.

## Also: Clean up redundant Espresso tests

Previous runs created multiple Espresso test files that may have overlapping or redundant tests. The agent should:
1. Review all Espresso test files in `app/src/androidTest/`
2. Remove duplicate tests that test the same thing
3. Consolidate related tests into logical test classes
4. Ensure no test coverage is lost — only remove true duplicates

## Hard Requirements

- Bug 1 Espresso test: add checklist item → press Enter → assert exactly 2 items. Press Enter → assert exactly 3 items. MUST PASS.
- Bug 2 Espresso test: tap lock → set password → assert lock state toggled on first attempt. MUST PASS.
- `./gradlew connectedDemoDebugAndroidTest` MUST pass with 0 failures
- `./gradlew testDemoDebugUnitTest` MUST also pass
- `./gradlew assembleDebug` MUST succeed
- Do NOT remove test coverage — only remove duplicate tests, not unique test scenarios
- The agent MUST run `./gradlew connectedDemoDebugAndroidTest` to verify — do not claim success without running it and seeing 0 failures

## Constraints

- Fix app bugs in `app/src/main/`
- Espresso tests in `app/src/androidTest/`
- Unit tests in `app/src/test/`
- Work iteratively: fix → run Espresso tests → check → repeat until 0 failures
- Both product flavors must still build
