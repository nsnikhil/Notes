Systematically verify and fix every feature in the Android Notes app. The app was recently modernized (Gradle, AGP, Kotlin, dependency upgrades, View Binding migration, RxJava 2→3, Twitter Serial→kotlinx.serialization). It builds and launches, but multiple features are broken at runtime. The agent must go through every feature, verify it works, fix what's broken, and write tests proving each feature works.

The project is at the repo_path. Espresso tests CANNOT be used — there is no emulator available. All tests must run as unit tests via Robolectric or pure JVM tests. Use `./gradlew testDemoDebugUnitTest` to run tests.

## Approach

For EVERY feature listed below, the agent must:
1. Read the source code that implements the feature
2. Trace the code path from UI action to data layer and back
3. Identify any bugs (crashes, wrong behavior, data corruption) introduced by the modernization
4. Fix the bugs
5. Write a test that exercises the feature's logic and proves it works
6. Run `./gradlew testDemoDebugUnitTest` to verify

Do NOT skip features. Do NOT assume a feature works without tracing the code. The modernization touched every file — any feature could be broken.

## Features to Verify and Fix

### Note Creation and Editing
- Create a new note with title and content → saved to Room DB and file system
- Edit an existing note → changes persisted to DB and file
- Note content serialization: NoteEntity is serialized to file via kotlinx.serialization and FileUtil → verify round-trip (serialize → write → read → deserialize → all fields match)
- Date handling: DateSerializer and DateConverter must correctly serialize/deserialize java.util.Date objects through both kotlinx.serialization and Room TypeConverters

### Checklist Feature
- Add a checklist item to a note → item appears in RecyclerView
- CheckListAdapter: verify that adding items does not create duplicate/extra items (ListAdapter + submitList behavior with mutable lists)
- Check/uncheck a checklist item → done state persists
- Remove a checklist item → item removed, list updated correctly
- CheckListObject serialization round-trip via kotlinx.serialization

### Image Attachment
- Attach an image to a note → image path stored in note's imageList
- Image list serialization round-trip
- Remove an image → imageList updated, adapter notified correctly

### Audio Recording
- Record audio → audio file path stored in note's audioList
- Audio list serialization round-trip
- Remove an audio recording → audioList updated correctly

### Reminders
- Set a reminder on a note → PendingIntent created with AlarmManager
- PendingIntent must use FLAG_IMMUTABLE (required on API 31+) — verify ReminderUtil uses correct flags
- Cancel a reminder → alarm cancelled
- NotificationReceiver handles the broadcast correctly

### Pin/Unpin Notes and Folders
- Pin a note → pinned field set to 1 in DB
- Unpin a note → pinned field set to 0
- Pin a folder → same behavior
- DAO queries that filter by pin status return correct results

### Lock/Unlock Notes and Folders
- Lock a note → requires password to be set first via PasswordDialogFragment
- PasswordUtil encryption/decryption round-trip with RSA keys
- Lock state persisted in DB
- Locked notes/folders filtered correctly by DAO queries

### Folder Operations
- Create a folder → saved to Room DB
- Delete a folder → folder deleted, notes inside deleted (cascade)
- Nested folders: create child folder with parentFolderName → DAO queries by parent return correct results
- Move a note to a different folder → folderName updated in DB
- Move a folder to a different parent → parentFolderName updated in DB

### Search
- Search notes by title → DAO query returns matching notes
- Search folders by name → DAO query returns matching folders

### Navigation
- Drawer navigation: Notes list, Settings, About → correct fragments displayed
- List → New Note → back to list
- Note editing: passing NoteEntity via navigation arguments (serialized as byte array) → verify deserialization works

### Color Picker
- Set a color on a note → color stored in DB, title text color changes
- ColorPickerEvent via EventBus → verify event delivery

### File Operations (FileUtil + Okio)
- Write note data to file → read it back → content matches
- Delete note resources (text file, images, audio) → files removed from disk
- Handle missing files gracefully (no crash on read of non-existent file)

### Dagger Dependency Injection
- Each Dagger component (Database, File, Folder, Glide, SharedPref, Network, Media) builds and provides non-null dependencies
- MyApplication.onCreate initializes all components without crash

### ViewModels
- NoteViewModel: insert, update, delete operations delegate to DAO correctly
- FolderViewModel: insert, update, delete operations delegate to DAO correctly
- LiveData emissions: when data changes in DB, ViewModel's LiveData emits updated values

## How to Test Without Espresso

Since there is no emulator, test features using:

- **Robolectric**: for anything that needs Android framework (Activities, Fragments, Context, SharedPreferences, AlarmManager, PendingIntent). Robolectric simulates the Android framework in the JVM.
- **Room in-memory database**: for all DAO and database tests. Use `Room.inMemoryDatabaseBuilder` with `allowMainThreadQueries()`.
- **Pure JVM tests**: for serialization, encryption, date conversion, DiffUtil, and any logic that doesn't need Android framework.
- **Mocks (Mockito)**: for isolating components — mock the DAO when testing ViewModel, mock FileUtil when testing note save logic.

## Hard Requirements

- Every feature listed above must have at least one test proving it works.
- If a feature is broken, fix it AND write a test. The test must pass.
- `./gradlew assembleDebug` MUST succeed.
- `./gradlew testDemoDebugUnitTest` MUST pass — all existing tests plus all new tests.
- Do NOT remove any existing tests.
- Do NOT change the app's intended functionality — only fix bugs from the modernization.
- Both product flavors (demo/full) must still build.

## Constraints

- NO Espresso tests — no emulator is available. All tests must run via `./gradlew testDemoDebugUnitTest`.
- Tests go in `app/src/test/` only.
- Use Robolectric for Android framework dependencies.
- Use Room in-memory database for DAO tests.
- The agent MUST run `./gradlew testDemoDebugUnitTest` to verify all tests pass.
- If fixing a bug requires changing source code, add an inline comment explaining what was broken and why.
- Do not refactor or restructure — only fix bugs and add tests.
