Write comprehensive tests for the Android Notes app to verify all features work correctly after modernization. The app builds and launches, but some features may not work as expected due to the recent dependency upgrades (RxJava 2→3, Twitter Serial→kotlinx.serialization, View Binding migration, Dagger updates, Room updates, etc.). The agent must discover what's broken by writing tests, then fix any bugs the tests reveal.

The project is at the repo_path. It is a multimedia note-taking Android app with Kotlin, MVVM, Dagger 2, Room, RxJava 3, Navigation Component, and kotlinx.serialization.

## App Features (derived from source code)

The agent MUST read the entire codebase to understand all features before writing any tests. These are the core features:

### Notes
- Create a note with title, content, color, images, audio recordings, and checklists
- Edit an existing note
- Delete notes (single and bulk)
- Pin/unpin notes
- Lock/unlock notes (password protected)
- Move notes between folders
- Search notes by title
- Notes are stored in Room database (NoteEntity, NoteDao)
- Note content (text, images, audio, checklists) is serialized to/from files on disk using kotlinx.serialization

### Folders
- Create folders
- Delete folders (cascades to notes inside)
- Nested folders (parent-child relationship via parentFolderName)
- Pin/unpin folders
- Lock/unlock folders
- Move folders between parent folders
- Search folders by name
- Folders stored in Room database (FolderEntity, FolderDao)

### Navigation
- Splash screen → Main activity
- Drawer navigation: Notes list, Settings, About
- Navigation between list → new note → image list
- Search fragment
- Intro fragment (first launch)
- Deep linking support

### Data Layer
- Room database with NoteEntity and FolderEntity
- DAOs with LiveData queries (filtered by folder, pin status, lock status, color, search query)
- ViewModels (NoteViewModel, FolderViewModel) exposing LiveData to UI
- File-based storage for note content (FileUtil reads/writes serialized data)
- Date serialization (DateSerializer, DateConverter)

### Utilities
- Password encryption/decryption (PasswordUtil)
- File management (FileUtil — create, read, write, delete files using Okio)
- Image handling (ImageUtil)
- Audio recording (AudioUtil)
- Reminders (ReminderUtil + NotificationReceiver)
- Network connectivity checking (NetworkUtil)
- Security checks (SecurityChecks — root detection, debugger detection)
- Swipe-to-delete (RvItemTouchHelper)

### DI (Dagger 2)
- DatabaseComponent, FileComponent, FolderComponent, GlideComponent, SharedPrefComponent, NetworkComponent, MediaComponent
- Each provides dependencies via modules

## What To Do

### 1. Read and understand the entire codebase
Map every feature, every class, every data flow. Understand what each component does before writing tests.

### 2. Write unit tests for the data layer
- Room DAO tests: test every query in NoteDao and FolderDao using an in-memory Room database
  - Insert, query, update, delete operations
  - Filtered queries (by folder, pin status, lock status, color, search)
  - Cascade behavior (deleting a folder deletes its notes)
  - Edge cases: empty results, duplicate inserts, null fields
- ViewModel tests: test NoteViewModel and FolderViewModel
  - Verify LiveData emissions on data changes
  - Verify correct DAO methods are called
- Serialization tests: test that NoteEntity and FolderEntity serialize/deserialize correctly with kotlinx.serialization
  - Round-trip: serialize → deserialize → verify all fields match
  - Test with null fields, empty lists, special characters
  - Test DateSerializer with various date formats
- FileUtil tests: test file read/write operations
  - Write serialized data → read it back → verify content matches
  - Test with missing files, empty files, corrupted data

### 3. Write unit tests for utilities
- PasswordUtil: test encryption and decryption round-trip
- SecurityChecks: test root detection and debugger detection logic
- DateConverter: test Room TypeConverter for Date objects
- DiffUtil classes: test NotesDiffUtil, FolderDiffUtil, CheckListDiffUtil, StringDiffUtil

### 4. Write integration tests for component interactions
- Database + ViewModel: insert data via DAO, verify ViewModel's LiveData updates
- FileUtil + Serialization: write a NoteEntity to file, read it back, verify all fields
- Dagger component initialization: verify each Dagger component builds and provides non-null dependencies

### 5. Write behavioral tests using Robolectric
- Activity launch tests: SplashActivity → MainActivity transition
- Fragment creation tests: each fragment in the navigation graph can be instantiated
- Navigation tests: verify navigation actions work (list → new note, list → search, drawer items)
- Note creation flow: create a note → verify it appears in the list
- Folder creation flow: create a folder → verify it appears in the list
- Search flow: add notes → search by title → verify correct results

### 6. Fix any bugs discovered by the tests
If a test reveals that a feature is broken (e.g., serialization fails, DAO query returns wrong results, navigation crashes), fix the bug. The test should pass after the fix. Document what was broken and why in inline comments.

### 7. Verify everything
- `./gradlew assembleDebug` must succeed
- `./gradlew testDemoDebugUnitTest` must pass — all existing tests plus all new tests
- `./gradlew testFullDebugUnitTest` must pass
- Both product flavors must build

## Hard Requirements

- ALL new tests must pass. Do not write tests that are expected to fail.
- If a test reveals a bug, fix the bug — do not skip the test or weaken the assertion.
- Do NOT remove any existing tests.
- Do NOT change the app's intended functionality — only fix bugs that prevent features from working as designed.
- `./gradlew assembleDebug` and `./gradlew testDemoDebugUnitTest` MUST succeed. Run these commands to verify.
- Tests must use Robolectric for anything that needs Android framework (no emulator required).
- Room DAO tests must use in-memory database (`Room.inMemoryDatabaseBuilder`).
- All tests must be deterministic — no flaky tests, no timing dependencies.

## Constraints

- Android/Kotlin project, Gradle build system
- Tests go in `app/src/test/` (unit tests with Robolectric) — NOT `app/src/androidTest/` (those need an emulator)
- Use the test frameworks already in the project: JUnit, Mockito, Robolectric, AndroidX Test, Room testing
- Do not add new test framework dependencies unless the existing ones cannot cover a scenario
- Every test file must be in the correct package matching the class it tests
- Test class naming: `<ClassName>Test.kt`
- The agent MUST run the Gradle test commands to verify all tests pass — do not claim success without running them
