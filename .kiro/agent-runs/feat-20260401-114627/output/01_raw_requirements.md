Write Espresso UI tests for every user-facing flow in the Android Notes app. An emulator/device is available — use `./gradlew connectedDemoDebugAndroidTest` to run instrumented tests. If any test reveals a bug, fix the bug and add a corresponding unit test as well.

The project is at the repo_path. It is a multimedia note-taking app with notes, folders, checklists, images, audio, reminders, pin/lock, search, color picker, and navigation drawer.

## Approach

For every user flow below, the agent must:
1. Read the source code for the screens involved
2. Write an Espresso test that exercises the flow end-to-end on a real device/emulator
3. Run the test via `./gradlew connectedDemoDebugAndroidTest`
4. If the test fails due to a bug in the app (not a test bug), fix the app code and add a unit test for the fix
5. Re-run until the Espresso test passes

Espresso tests go in `app/src/androidTest/`. Unit tests go in `app/src/test/`.

## Known Broken Features

The following features have been manually tested and confirmed broken after the modernization. The agent MUST fix each of these AND write Espresso tests proving they work. These are not optional — they are confirmed bugs.

- **Checklist**: Adding checklist items creates duplicate/extra items. The underlying bug was not fixed by the previous test-writing pass. The agent must trace the full code path (CheckListAdapter, ListAdapter.submitList with mutable lists, NewNoteFragment.addCheckListItem) and fix the root cause.
- **Attachments**: Image and audio attachments do not work. The agent must trace the attachment flow (camera/gallery intent → onActivityResult → saveImage/addAudioToList → FileUtil → serialization) and fix whatever is broken.
- **Reminders**: Setting a reminder does not result in a notification being delivered. The agent must trace the full path (ReminderUtil.setReminder → AlarmManager → PendingIntent → NotificationReceiver) and fix it. Common issues: missing FLAG_IMMUTABLE on PendingIntent (required API 31+), missing notification channel (required API 26+), incorrect BroadcastReceiver registration.
- **Lock**: Locking a note does not work. The agent must trace the lock flow (NewNoteFragment.setLock → PasswordUtil.checkLock → PasswordDialogFragment → encryption/decryption) and fix it.
- **Navigation**: Navigation misbehaves in certain flows. The agent must verify all navigation paths (drawer items, list→note→back, search, deep links) and fix any incorrect behavior.

The agent must NOT just write tests around these bugs — it must FIX the bugs first, then write tests proving the fix works.

## User Flows to Test

### App Launch and Navigation
- App launches from splash screen to main activity without crash
- Drawer opens, shows Notes/Settings/About menu items
- Tap Notes → notes list displayed
- Tap Settings → settings fragment displayed
- Tap About → about fragment displayed
- Back navigation returns to previous screen

### Note CRUD
- Create a new note: tap FAB → enter title and content → save → note appears in list
- Open an existing note: tap note in list → note content displayed correctly
- Edit a note: open note → change title/content → save → changes persisted
- Delete a note: open note → delete menu → confirm → note removed from list

### Checklist
- Add a checklist item: open new note → tap checklist tool → item appears
- Add multiple checklist items → correct number of items displayed (no duplicates)
- Check an item → strikethrough applied, done state saved
- Uncheck an item → strikethrough removed
- Remove a checklist item → item removed, remaining items correct
- Save note with checklist → reopen → checklist items preserved with correct states

### Pin and Star
- Star a note: open note → tap star icon → star state toggled
- Save starred note → reopen → star state preserved
- Unstar a note → state toggled back

### Lock
- Lock a note: open note → tap lock icon → password dialog appears (if no password set)
- Set password → lock state toggled
- Save locked note → reopen → lock state preserved

### Folder Operations
- Create a folder from the list screen
- Navigate into a folder → shows notes/subfolders inside
- Create a note inside a folder → note's folderName set correctly
- Delete a folder → folder and its contents removed

### Search
- Tap search → search fragment displayed
- Type a query → matching notes/folders displayed
- Clear search → results cleared

### Color Picker
- Open note → tap color tool → color picker dialog appears
- Select a color → note title color changes
- Save note → reopen → color preserved

### Reminder
- Open note → tap reminder tool → time picker dialog appears
- Set a time → reminder state saved on the note

### Image Attachment
- Open note → tap camera/attachment tool → image picker/camera intent launched
- After attaching an image → image appears in the note's image list
- Save note → reopen → image list preserved
- Remove an image → image removed from list

### Audio Recording
- Open note → tap audio tool → recording starts (permission required)
- After recording → audio appears in the note's audio list
- Save note → reopen → audio list preserved

### Settings
- Navigate to settings → preferences displayed
- Change a preference → value persisted

### About
- Navigate to about → about content displayed without crash

### Edge Cases
- Create a note with empty title and content → appropriate validation message
- Rotate device during note editing → state preserved
- Navigate away during note editing → no crash
- Rapid tap on checklist add → no duplicate items beyond what's expected

## Hard Requirements

- `./gradlew connectedDemoDebugAndroidTest` MUST pass — all Espresso tests green
- `./gradlew testDemoDebugUnitTest` MUST also pass — all existing unit tests plus any new unit tests for bug fixes
- `./gradlew assembleDebug` MUST succeed
- If an Espresso test reveals a bug, fix the bug in the app source code AND add a unit test for the fix
- Do NOT remove any existing tests (unit or instrumented)
- Do NOT change the app's intended functionality — only fix bugs
- Both product flavors must still build
- Every user flow listed above must have at least one Espresso test

## Constraints

- Espresso tests go in `app/src/androidTest/java/com/nrs/nsnik/notes/`
- Unit tests go in `app/src/test/java/com/nrs/nsnik/notes/`
- Use the Espresso libraries already in the project (espresso-core, espresso-contrib, espresso-intents)
- Use Espresso IdlingResources if async operations need synchronization (the app already has SimpleIdlingResource)
- For intents that launch external apps (camera, gallery), use espresso-intents to stub the result
- The agent MUST run `./gradlew connectedDemoDebugAndroidTest` to verify Espresso tests pass — do not claim success without running them
- If a test requires permissions (camera, audio, storage), grant them programmatically using GrantPermissionRule
- Test class naming: `<FeatureName>FlowTest.kt`
- Each test must be independent — no test should depend on another test's state. Use @Before to set up clean state and @After to tear down.
