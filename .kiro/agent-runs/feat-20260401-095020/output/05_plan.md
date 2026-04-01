# Implementation Plan: Comprehensive Tests and Bug Fixes for Android Notes App

## Overview
This plan adds comprehensive unit and integration tests across all layers of the Notes Android app, while fixing a critical serialization bug in FileUtil. The work is organized into 5 sub-tasks progressing from foundation (bug fix + DAO tests) through data/utility layer, business logic, UI behavior, and final verification. The critical bug — FileUtil uses Java `ObjectOutputStream`/`ObjectInputStream` but `NoteEntity` no longer implements `java.io.Serializable` — must be fixed first since file-based note persistence depends on it. All tests use JUnit 4, Mockito 5.14.2, Robolectric 4.14.1, Room testing, and AndroidX Test already present in the project. Every sub-task is verified by running `./gradlew testDemoDebugUnitTest`.

## Sub-Tasks

### Sub-Task 1: Fix Critical FileUtil Serialization Bug + Room DAO Tests
- **Size:** M
- **Description:** Fix the critical runtime bug where `FileUtil.serialize()` uses `ObjectOutputStream.writeObject()` and `FileUtil.deSerialize()` uses `ObjectInputStream.readObject()` — both require `java.io.Serializable` — but `NoteEntity` no longer implements it. Replace Java serialization with kotlinx.serialization `Json.encodeToString`/`Json.decodeFromString` using Okio for file I/O. Then add Room DAO tests for both `NoteDao` and `FolderDao` using an in-memory Room database, covering insert, query, update, delete, filtering by pin/lock/color/folder, and ordering operations.
- **Dependencies:** None
- **Entry Criteria:** Repository builds successfully with `./gradlew assembleDemoDebug`
- **Exit Criteria:**
  - `FileUtil.kt` no longer uses `ObjectOutputStream` or `ObjectInputStream`
  - `FileUtil.kt` uses `kotlinx.serialization.json.Json` for serialize/deserialize
  - `NoteEntity` remains free of `java.io.Serializable` (existing test `crash06_noteEntityNoJavaSerializable` still passes)
  - `NoteDaoTest.kt` exists and all tests pass
  - `FolderDaoTest.kt` exists and all tests pass
  - `./gradlew testDemoDebugUnitTest` passes
- **Success Criteria:**
  - Given a NoteEntity with title, content, imageList, audioList, checkList, and dateModified, When FileUtil.serialize() is called, Then it produces a valid JSON byte string that can be deserialized back to an equivalent NoteEntity
  - Given an empty Room database, When a NoteEntity is inserted via NoteDao, Then querying by its uid returns the same entity
  - Given multiple notes in different folders, When getNoteByFolderName is called, Then only notes in that folder are returned
  - Given notes with different pin/lock states, When getNotesByFolderNamePinnedNotLocked is called, Then only pinned-unlocked notes are returned
  - Given a note exists, When deleteNotes is called, Then the note is no longer in the database
  - Given a note exists, When changeNotePinStatus is called, Then the note's pinned field is updated
  - Given a note exists, When changeNoteFolder is called, Then the note's folderName is updated
  - Given notes with a color, When getNoteByColor is called, Then only matching notes are returned
  - Given a note with title "Shopping", When getNoteByQuery("Shop") is called, Then the note is returned (prefix search)
  - Given an empty Room database, When a FolderEntity is inserted via FolderDao, Then querying by uid returns the same entity
  - Given folders with different parents, When getFolderByParent is called, Then only folders with that parent are returned
  - Given folders with pin/lock combinations, When getFolderByParentPinNoLock is called, Then only pinned-unlocked folders under that parent are returned
  - Given a folder exists, When deleteFolderByName is called, Then the folder is removed
  - Given a folder exists, When changeFolderPinStatus is called, Then the folder's pinned field is updated
  - Given a folder exists, When changeFolderParent is called, Then the folder's parentFolderName is updated
  - Given folders with a color, When getFolderByColor is called, Then only matching folders are returned
  - Given a folder named "Work", When getFolderByQuery("Wo") is called, Then the folder is returned
- **Files:**
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/util/FileUtil.kt`
  - Create: `app/src/test/java/com/nrs/nsnik/notes/data/NoteDaoTest.kt`
  - Create: `app/src/test/java/com/nrs/nsnik/notes/data/FolderDaoTest.kt`

### Sub-Task 2: Serialization, FileUtil, and Utility Tests
- **Size:** M
- **Description:** Add tests for the serialization layer (DateSerializer, DateConverter, CheckListObject kotlinx.serialization round-trips), FileUtil file operations (saveNote/getLiveNote round-trip, deleteNote, deleteNoteResources), DiffUtil classes (NotesDiffUtil, FolderDiffUtil, CheckListDiffUtil, StringDiffUtil), and SecurityChecks utility. FileUtil tests use a real temp directory. DiffUtil tests are pure JUnit. SecurityChecks tests use Mockito to mock Context and PackageManager.
- **Dependencies:** Sub-task 1
- **Entry Criteria:** Sub-task 1 complete — FileUtil bug is fixed and uses kotlinx.serialization; DAO tests pass
- **Exit Criteria:**
  - `DateSerializerTest.kt` exists and passes
  - `DateConverterTest.kt` exists and passes
  - `CheckListObjectTest.kt` exists and passes
  - `FileUtilTest.kt` exists and passes
  - `DiffUtilTest.kt` exists and passes
  - `SecurityChecksTest.kt` exists and passes
  - `./gradlew testDemoDebugUnitTest` passes
- **Success Criteria:**
  - Given a Date object, When DateSerializer.serialize() is called, Then it encodes the date as a Long epoch millis
  - Given a Long epoch millis, When DateSerializer.deserialize() is called, Then it returns the correct Date
  - Given a null Date, When DateConverter.dateToLong() is called, Then it returns null
  - Given a null Long, When DateConverter.longToDate() is called, Then it returns null
  - Given a valid Long, When DateConverter.longToDate() is called, Then it returns the correct Date
  - Given a CheckListObject with text="Buy milk" and done=true, When serialized to JSON and deserialized back, Then the fields match
  - Given a NoteEntity with all fields populated, When FileUtil.saveNote() is called and then getLiveNote() reads it back, Then the deserialized entity matches the original (title, noteContent, imageList, audioList, checkList, dateModified)
  - Given a saved note file, When FileUtil.deleteNote() is called, Then the file no longer exists on disk
  - Given two NoteEntity objects with the same uid, When NotesDiffUtil.areItemsTheSame() is called, Then it returns true
  - Given two NoteEntity objects with different uids, When NotesDiffUtil.areItemsTheSame() is called, Then it returns false
  - Given two identical FolderEntity objects, When FolderDiffUtil.areContentsTheSame() is called, Then it returns true
  - Given any two CheckListObjects, When CheckListDiffUtil.areItemsTheSame() is called, Then it returns false (current implementation always returns false)
  - Given two identical strings, When StringDiffUtil.areItemsTheSame() is called, Then it returns true
  - Given a mocked Context with a known package signature, When SecurityChecks.checkAppSignature() is called, Then it returns the expected boolean based on signature match
  - Given a mocked Context where installer is "com.android.vending", When SecurityChecks.verifyInstaller() is called, Then it returns true
  - Given a mocked Context with FLAG_DEBUGGABLE set, When SecurityChecks.checkDebuggable() is called, Then it returns true
- **Files:**
  - Create: `app/src/test/java/com/nrs/nsnik/notes/util/DateSerializerTest.kt`
  - Create: `app/src/test/java/com/nrs/nsnik/notes/util/DateConverterTest.kt`
  - Create: `app/src/test/java/com/nrs/nsnik/notes/util/CheckListObjectTest.kt`
  - Create: `app/src/test/java/com/nrs/nsnik/notes/util/FileUtilTest.kt`
  - Create: `app/src/test/java/com/nrs/nsnik/notes/util/DiffUtilTest.kt`
  - Create: `app/src/test/java/com/nrs/nsnik/notes/util/SecurityChecksTest.kt`

### Sub-Task 3: ViewModel and DbUtil Integration Tests
- **Size:** M
- **Description:** Add tests for NoteViewModel and FolderViewModel (verifying delegation to DbUtil), and DbUtil integration tests that verify the coordination between Room DAO operations and FileUtil calls. ViewModel tests use Mockito to mock the Application/MyApplication and DbUtil. DbUtil tests use a real in-memory Room database with a mocked FileUtil to verify that insertNote triggers both DAO insert and FileUtil.saveNote, updateNote triggers both DAO update and FileUtil.saveNote, and deleteNoteResources cleans up files. Uses `InstantTaskExecutorRule` for LiveData and RxJava test schedulers via `RxJavaPlugins`/`RxAndroidPlugins` trampoline overrides.
- **Dependencies:** Sub-task 1, Sub-task 2
- **Entry Criteria:** Sub-tasks 1 and 2 complete — DAO tests pass, FileUtil is fixed and tested, serialization tests pass
- **Exit Criteria:**
  - `NoteViewModelTest.kt` exists and passes
  - `FolderViewModelTest.kt` exists and passes
  - `DbUtilIntegrationTest.kt` exists and passes
  - `./gradlew testDemoDebugUnitTest` passes
- **Success Criteria:**
  - Given a NoteViewModel with a mocked DbUtil, When insertNote() is called, Then DbUtil.insertNote() is invoked with the same entity
  - Given a NoteViewModel, When getNoteByFolderName("Work") is called, Then it returns the LiveData from DbUtil.getNoteByFolderName("Work")
  - Given a NoteViewModel, When changeNotePinStatus(1, 1) is called, Then DbUtil.changeNotePinStatus(1, 1) is invoked
  - Given a NoteViewModel, When searchNote("test") is called, Then DbUtil.searchNote("test") is invoked
  - Given a FolderViewModel with a mocked DbUtil, When insertFolder() is called, Then DbUtil.insertFolder() is invoked
  - Given a FolderViewModel, When getFolderByParent("root") is called, Then it returns the LiveData from DbUtil.getFolderByParent("root")
  - Given a FolderViewModel, When changeFolderLockStatus(1, 1) is called, Then DbUtil.changeFolderLockStatus(1, 1) is invoked
  - Given a FolderViewModel, When deleteFolderByParent("old") is called, Then DbUtil.deleteFolderByParent("old") is invoked
  - Given a DbUtil with a real in-memory Room DB and mocked FileUtil, When insertNote is called with a NoteEntity that has a fileName, Then the note is persisted in Room AND FileUtil.saveNote is called
  - Given a DbUtil, When deleteNote is called, Then the note is removed from Room
  - Given a DbUtil, When updateNote is called, Then the note is updated in Room AND FileUtil.saveNote is called with the updated entity
  - Given a DbUtil, When changeNotePinStatus is called, Then the note's pin status is updated in Room
  - Given a DbUtil, When insertFolder and then deleteFolder are called, Then the folder is removed from Room
- **Files:**
  - Create: `app/src/test/java/com/nrs/nsnik/notes/viewmodel/NoteViewModelTest.kt`
  - Create: `app/src/test/java/com/nrs/nsnik/notes/viewmodel/FolderViewModelTest.kt`
  - Create: `app/src/test/java/com/nrs/nsnik/notes/util/DbUtilIntegrationTest.kt`

### Sub-Task 4: Robolectric Behavioral and Navigation Tests
- **Size:** M
- **Description:** Add Robolectric-based tests that verify Android-specific behavior: navigation graph structure (all destinations and actions exist), entity Parcelable/Bundle serialization if applicable, and Activity lifecycle behavior. Uses `@RunWith(RobolectricTestRunner::class)` with `@Config(sdk = [34])`. Tests verify the nav_graph.xml has correct destinations (ListFragment, NewNoteFragment, SearchFragment, ImageListFragment, AboutFragment, PrefFragment, IntroFragment) and actions (listToAddNewNote, listToSearch, noteToImageList, introToNoteList). Also tests NoteEntity and FolderEntity field defaults, edge cases (null fields, empty strings, boundary values for pin/lock), and CheckListObject java.io.Serializable round-trip (CheckListObject still implements it).
- **Dependencies:** Sub-task 1
- **Entry Criteria:** Sub-task 1 complete — entities and bug fix are stable
- **Exit Criteria:**
  - `NavigationGraphTest.kt` exists and passes
  - `EntityEdgeCaseTest.kt` exists and passes
  - `./gradlew testDemoDebugUnitTest` passes
- **Success Criteria:**
  - Given the nav_graph.xml, When parsed by Robolectric, Then it contains 7 fragment destinations (navItemNotes, newNoteFragment, navItemAbout, navItemSettings, imageListFragment, introFragment, searchFragment)
  - Given the nav_graph.xml, When the listToAddNewNote action is inspected, Then its destination is newNoteFragment
  - Given the nav_graph.xml, When the listToSearch action is inspected, Then its destination is searchFragment
  - Given the nav_graph.xml, When the noteToImageList action is inspected, Then its destination is imageListFragment
  - Given the nav_graph.xml, When the introToNoteList action is inspected, Then its destination is navItemNotes
  - Given a default NoteEntity, When created with no-arg constructor, Then uid=0, title=null, fileName=null, folderName=null, pinned=0, locked=0, color=null, dateModified=null
  - Given a default FolderEntity, When created with no-arg constructor, Then uid=0, folderName=null, parentFolderName=null, pinned=0, locked=0, color=null
  - Given a NoteEntity with all nullable fields set to null, When used in Room operations, Then no crash occurs
  - Given a CheckListObject, When serialized via Java ObjectOutputStream and deserialized via ObjectInputStream, Then the text and done fields match (CheckListObject still implements java.io.Serializable)
  - Given a NoteEntity with pinned=0 and locked=0, When pinned is set to 1, Then only pinned changes and locked remains 0
- **Files:**
  - Create: `app/src/test/java/com/nrs/nsnik/notes/NavigationGraphTest.kt`
  - Create: `app/src/test/java/com/nrs/nsnik/notes/EntityEdgeCaseTest.kt`

### Sub-Task 5: Final Verification and Both-Flavor Build Validation
- **Size:** S
- **Description:** Run the full test suite across both product flavors (demo and full) to confirm all tests pass, no existing tests were removed, and both flavors build cleanly. Verify the FileUtil bug fix doesn't break any existing tests (especially `RuntimeCrashFixTest.crash06_noteEntityNoJavaSerializable` and `SerializationMigrationTest`). Confirm total test count increased and all 9 pre-existing test files are still present.
- **Dependencies:** Sub-task 1, Sub-task 2, Sub-task 3, Sub-task 4
- **Entry Criteria:** All sub-tasks 1–4 complete and their individual test runs pass
- **Exit Criteria:**
  - `./gradlew testDemoDebugUnitTest` passes with zero failures
  - `./gradlew testFullDebugUnitTest` passes with zero failures
  - `./gradlew assembleDemoDebug assembleFullDebug` succeeds
  - All 9 pre-existing test files remain unmodified
  - New test files total at least 10 (NoteDaoTest, FolderDaoTest, DateSerializerTest, DateConverterTest, CheckListObjectTest, FileUtilTest, DiffUtilTest, SecurityChecksTest, NoteViewModelTest, FolderViewModelTest, DbUtilIntegrationTest, NavigationGraphTest, EntityEdgeCaseTest)
- **Success Criteria:**
  - Given the complete test suite, When `./gradlew testDemoDebugUnitTest` is run, Then all tests pass with 0 failures
  - Given the complete test suite, When `./gradlew testFullDebugUnitTest` is run, Then all tests pass with 0 failures
  - Given both flavors, When `./gradlew assembleDemoDebug assembleFullDebug` is run, Then both APKs build successfully
  - Given the test directory, When listing all test files, Then all 9 original test files exist unchanged and at least 13 new test files exist
  - Given the existing `RuntimeCrashFixTest`, When it runs, Then `crash06_noteEntityNoJavaSerializable` still passes (NoteEntity does not implement java.io.Serializable)
  - Given the existing `SerializationMigrationTest`, When it runs, Then all assertions about kotlinx.serialization still pass
- **Files:**
  - Modify: None (verification only — no file changes)

## Implementation Order
1. Sub-task 1 — Fix critical FileUtil serialization bug + Room DAO tests (no dependencies)
2. Sub-task 4 — Robolectric behavioral and navigation tests (depends on 1; can run in parallel with 2)
3. Sub-task 2 — Serialization, FileUtil, and utility tests (depends on 1)
4. Sub-task 3 — ViewModel and DbUtil integration tests (depends on 1, 2)
5. Sub-task 5 — Final verification and both-flavor build validation (depends on 1, 2, 3, 4)

Note: Sub-tasks 2 and 4 can execute in parallel after Sub-task 1 completes, as they have non-overlapping file ownership.

## Integration Test Plan
After all sub-tasks are complete, the IntegrationTester should verify:

- **End-to-end note lifecycle:** Insert a NoteEntity via NoteDao into an in-memory Room DB, verify it's queryable by folder name, update its pin status, move it to a different folder, then delete it — confirming it's gone from all queries.
- **End-to-end folder lifecycle:** Insert a FolderEntity, query by parent, update lock status, change parent folder, delete by name — confirming cascading behavior with notes in that folder (deleteNoteByFolderName).
- **FileUtil round-trip after bug fix:** Create a NoteEntity with all @Ignore fields populated (noteContent, imageList, audioList, checkList), serialize via the fixed FileUtil, deserialize, and assert all fields match — proving the kotlinx.serialization replacement works end-to-end.
- **ViewModel → DbUtil → DAO chain:** Using a NoteViewModel with a real DbUtil backed by an in-memory Room DB, call insertNote, then verify getNoteByFolderName returns the note — proving the full delegation chain works.
- **Cross-flavor consistency:** Both `testDemoDebugUnitTest` and `testFullDebugUnitTest` produce identical pass/fail results.
- **Regression safety:** All 9 pre-existing test files (EncryptionUnitTest, SerializationMigrationTest, BuildSystemUpgradeTest, ViewBindingMigrationTest, ThirdPartyDepsUpgradeTest, RxJavaMigrationTest, DependencyUpgradeTest, DeadPluginRemovalTest, RuntimeCrashFixTest) continue to pass without modification.
- **Edge cases:**
  - NoteEntity with all null optional fields survives Room insert/query cycle
  - FolderEntity with empty string folderName is handled correctly
  - DateConverter handles null input without crashing
  - FileUtil.deleteNote on a non-existent file does not throw
  - CheckListDiffUtil always returns false for areItemsTheSame (verifying current intentional behavior)

## Requirements Traceability
| Requirement | Sub-Task(s) |
|---|---|
| Fix FileUtil serialization bug (NoteEntity not java.io.Serializable) | Sub-task 1 |
| NoteDao CRUD tests (insert, query, update, delete) | Sub-task 1 |
| NoteDao filtering tests (by folder, pin, lock, color, query) | Sub-task 1 |
| NoteDao status change tests (pin, lock, folder move) | Sub-task 1 |
| FolderDao CRUD tests (insert, query, update, delete) | Sub-task 1 |
| FolderDao filtering tests (by parent, pin, lock, color, query) | Sub-task 1 |
| FolderDao status change tests (pin, lock, parent change) | Sub-task 1 |
| DateSerializer round-trip tests | Sub-task 2 |
| DateConverter null-safety and conversion tests | Sub-task 2 |
| CheckListObject kotlinx.serialization round-trip | Sub-task 2 |
| FileUtil saveNote/getLiveNote round-trip test | Sub-task 2 |
| FileUtil deleteNote test | Sub-task 2 |
| DiffUtil tests (NotesDiffUtil, FolderDiffUtil, CheckListDiffUtil, StringDiffUtil) | Sub-task 2 |
| SecurityChecks tests (signature, installer, debuggable) | Sub-task 2 |
| NoteViewModel delegation tests | Sub-task 3 |
| FolderViewModel delegation tests | Sub-task 3 |
| DbUtil integration tests (DAO + FileUtil coordination) | Sub-task 3 |
| Navigation graph structure verification | Sub-task 4 |
| Entity default values and edge case tests | Sub-task 4 |
| CheckListObject java.io.Serializable round-trip | Sub-task 4 |
| Both-flavor build validation (demo + full) | Sub-task 5 |
| No existing tests removed verification | Sub-task 5 |
| Full regression suite passes | Sub-task 5 |
