Write comprehensive Espresso UI tests for the recently fixed features in the Android Notes app. The previous run fixed bugs in checklist, lock, camera, attachment, and reminder. This requirement adds thorough Espresso tests covering happy paths, edge cases, and error scenarios for each of these features. The agent must also discover and test additional edge cases beyond what is listed here.

The project is at the repo_path. An emulator/device is connected and available.

## CRITICAL: These Bugs Are Still Not Fixed

The following bugs have survived multiple fix attempts and are STILL broken. The agent MUST fix these first before writing any other tests. A bug is NOT considered fixed until an Espresso test running on the connected emulator proves it works.

### 1. Checklist: pressing Enter STILL creates duplicate items
This bug has not been fixed despite previous attempts. The agent must read the CURRENT state of `CheckListAdapter.kt` and `NewNoteFragment.kt`, understand exactly what happens when Enter is pressed, and fix the root cause. Do not assume previous fixes worked — they didn't.

### 2. Camera STILL does not work
Tapping the camera tool still does nothing or crashes. Previous fix attempts did not resolve this. The agent must read the CURRENT state of the camera code path and fix it.

### 3. Lock STILL does not work on first attempt
Tapping lock the first time shows the password dialog but does not toggle the lock state after the password is set. It only works from the second tap onwards. Previous fix attempts did not resolve this. The agent must fix the flow so lock toggles on the FIRST tap after setting the password.

For each of these 3 bugs: fix the app code → write an Espresso test that exercises the exact broken scenario → run `./gradlew connectedDemoDebugAndroidTest` → verify the test passes. If the Espresso test fails, the bug is NOT fixed. Keep iterating until it passes.

## Approach

For each feature below:
1. Read the source code to understand the full behavior including edge cases
2. Write Espresso tests covering the happy path, edge cases, and error scenarios
3. Run `./gradlew connectedDemoDebugAndroidTest` to verify all tests pass
4. If any test reveals a bug that was missed by the previous fix, fix the bug and add a unit test for the fix

The agent MUST think about edge cases independently — the lists below are starting points, not exhaustive. The agent should ask: "What happens if the user does something unexpected? What happens with empty input? What happens if they do the same action twice? What about rapid taps? What about rotation?"

## Features to Test

### Checklist
Happy path:
- Tap checklist tool → exactly 1 item added
- Type text → press Enter → exactly 1 new item added, cursor on new item
- Check an item → strikethrough applied, done state saved
- Uncheck an item → strikethrough removed
- Remove an item → item removed, count decreases

Edge cases the agent must discover and test:
- Add item with empty text → what happens?
- Rapidly tap checklist tool 5 times → exactly 5 items, no duplicates
- Add 20 items → RecyclerView scrolls, all items accessible
- Check all items → all show strikethrough
- Remove all items → checklist area hidden
- Save note with checklist → reopen → all items preserved with correct text and done states
- Press Enter on the last item when it's empty → should it add another or not?
- Rotate device with checklist open → items preserved

### Lock
Happy path:
- Tap lock icon → password dialog appears (first time)
- Set password → lock state toggled immediately (no second tap needed)
- Tap lock again → lock state toggles back

Edge cases:
- Tap lock, dismiss password dialog without setting password → lock state unchanged
- Set password, lock note, save, reopen → lock state preserved
- Set password, lock note, unlock note, save → unlocked state preserved
- Tap lock rapidly → state toggles correctly, no crash
- Rotate device while password dialog is showing → dialog survives or re-shows

### Camera
Happy path:
- Tap camera tool → camera intent launched (stub with espresso-intents)
- Stubbed camera result → image appears in image list

Edge cases:
- Camera intent cancelled by user → no image added, no crash
- Take multiple photos → all appear in image list in order
- Remove a camera image → image removed from list
- Save note with camera images → reopen → images preserved
- Camera permission denied → appropriate handling (no crash)

### Attachment/Gallery
Happy path:
- Tap attachment tool → gallery intent launched (stub with espresso-intents)
- Stubbed gallery result → image appears in image list

Edge cases:
- Gallery intent cancelled → no image added, no crash
- Attach multiple images sequentially → all appear in list
- Remove an attached image → removed from list
- Save note with attached images → reopen → images preserved
- Storage permission denied → appropriate handling

### Reminder
Happy path:
- Tap reminder tool → time picker dialog appears
- Select a time → dialog closes, reminder state set on note

Edge cases:
- Dismiss time picker without selecting → no reminder set, no crash
- Set reminder, save note → reopen → reminder state preserved
- Set reminder then cancel it → reminder cleared
- Tap reminder tool twice → second tap shows picker again (no crash or double-set)

### Cross-Feature Edge Cases
The agent must also test interactions between features:
- Create note with checklist + images + reminder → save → reopen → all data preserved
- Lock a note that has checklist and images → save → verify lock state and content preserved
- Pin a note with reminder → save → verify both states preserved
- Delete a note that has images and audio → verify files cleaned up (no orphaned files)

## Hard Requirements

- `./gradlew connectedDemoDebugAndroidTest` MUST pass with 0 failures
- `./gradlew testDemoDebugUnitTest` MUST also pass
- `./gradlew assembleDebug` MUST succeed
- Do NOT remove any existing tests
- If a test reveals a new bug, fix the bug AND add a unit test
- Both product flavors must still build
- The agent MUST run both test commands to verify

## Constraints

- Espresso tests go in `app/src/androidTest/`
- Use espresso-intents to stub camera and gallery intents
- Use GrantPermissionRule for permissions
- Each test must be independent — clean state via @Before/@After
- For intent stubbing, use `Intents.intending(hasAction(...)).respondWith(...)` pattern
- The agent MUST discover additional edge cases beyond what is listed — the lists above are not exhaustive
- The agent MUST run `./gradlew connectedDemoDebugAndroidTest` to verify — do not claim success without running it
