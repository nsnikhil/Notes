# Implementation Plan: Post-Modernization Verification and Bug Fix for Android Notes App

## Overview
This plan fixes 5 confirmed bugs and adds targeted unit tests to verify all 15 feature categories across the Notes app. The 22 existing test files already cover: Note/Folder CRUD (NoteDaoTest, FolderDaoTest), serialization (DateSerializerTest, DateConverterTest, CheckListObjectTest, SerializationMigrationTest), FileUtil (FileUtilTest), DiffUtil (DiffUtilTest), SecurityChecks (SecurityChecksTest), ViewModels (NoteViewModelTest, FolderViewModelTest), integration (DbUtilIntegrationTest), navigation (NavigationGraphTest), entity edge cases (EntityEdgeCaseTest), and modernization verification tests. New tests focus on gaps: bug fix verification, reminder PendingIntent flags, folder cascade delete, Dagger component structure, color/lock/pin DAO queries, and search edge cases. All tests run via `./gradlew testDemoDebugUnitTest`.

## Sub-Tasks

### Sub-Task 1: Bug Fixes + Verification Tests for BUG-001, BUG-002, BUG-003
- **Size:** S
- **Description:** Fix the three code-level bugs and write tests that verify the fixes. BUG-001: Add `FLAG_IMMUTABLE` to PendingIntent in `ReminderUtil.setNotification()` and `cancelReminder()`. BUG-002: Add `FLAG_IMMUTABLE` to PendingIntent in `NotificationReceiver.buildNotification()`. BUG-003: Remove `java.io.Serializable` from `CheckListObject`. Write a single test file that reads source files and asserts the fixes are present (file-based verification pattern, matching existing test conventions like RuntimeCrashFixTest).
- **Dependencies:** None
- **Entry Criteria:** Repository compiles, all 22 existing tests pass
- **Exit Criteria:** ReminderUtil.kt uses `FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE` in both methods; NotificationReceiver.kt uses `FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE`; CheckListObject.kt no longer implements `java.io.Serializable`; new test file exists and passes
- **Success Criteria:**
  - Given ReminderUtil.kt source, when inspected, then `setNotification()` PendingIntent uses `FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE`
  - Given ReminderUtil.kt source, when inspected, then `cancelReminder()` PendingIntent uses `FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE`
  - Given NotificationReceiver.kt source, when inspected, then `buildNotification()` PendingIntent uses `FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE`
  - Given CheckListObject.kt source, when inspected, then it does NOT contain `java.io.Serializable`
  - Given CheckListObject, when serialized with kotlinx.serialization, then round-trip succeeds (existing CheckListObjectTest still passes)
  - Given `./gradlew testDemoDebugUnitTest`, when executed, then all tests pass
- **Files:**
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/util/ReminderUtil.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/util/receiver/NotificationReceiver.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/model/CheckListObject.kt`
  - Create: `app/src/test/java/com/nrs/nsnik/notes/BugFixVerificationTest.kt`

### Sub-Task 2: Folder Cascade Delete Fix + Folder Operations Tests (BUG-004)
- **Size:** M
- **Description:** Fix BUG-004 (folder cascade delete doesn't delete nested sub-folders) by adding recursive sub-folder deletion logic. The current `deleteFolderByParent` DAO method only deletes direct children — the fix needs to ensure that when a folder is deleted, all nested sub-folders AND their contained notes are also removed. Write tests verifying: nested folder cascade delete, folder move operations, folder parent hierarchy queries. Existing FolderDaoTest and FolderViewModelTest already cover basic CRUD, pin, search, and color — new tests focus only on cascade delete depth and move operations.
- **Dependencies:** None
- **Entry Criteria:** Repository compiles, all existing tests pass
- **Exit Criteria:** Deleting a folder with nested sub-folders recursively removes all descendants (sub-folders and their notes); new test file exists and passes
- **Success Criteria:**
  - Given a folder "A" containing sub-folder "B" containing sub-folder "C" with notes, when "A" is deleted, then "B", "C", and all notes in A/B/C are removed from the database
  - Given a folder with direct child notes and no sub-folders, when deleted, then all child notes are removed (regression check)
  - Given a note in folder "X", when moved to folder "Y" via `changeNoteFolder`, then the note's folderName is "Y"
  - Given a folder with parent "old", when moved via `changeFolderParent`, then its parentFolderName is updated
  - Given `./gradlew testDemoDebugUnitTest`, when executed, then all tests pass
- **Files:**
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/util/DbUtil.kt` (add recursive cascade delete method)
  - Create: `app/src/test/java/com/nrs/nsnik/notes/FolderCascadeDeleteTest.kt`

### Sub-Task 3: Reminder, Pin/Lock, Color, and Search Verification Tests
- **Size:** M
- **Description:** Write tests for features not yet covered by existing test files. Existing tests already cover: basic pin toggle (NoteDaoTest, FolderDaoTest), lock toggle (DbUtilIntegrationTest, NoteViewModelTest, FolderViewModelTest), color filter (NoteDaoTest, FolderDaoTest), and basic search (NoteDaoTest, FolderDaoTest). New tests target gaps: (1) Reminder source verification — confirm ReminderUtil and NotificationReceiver structure (alarm scheduling pattern, receiver registration in manifest). (2) Pin/Lock combined DAO queries — `getNotesByFolderNamePinnedNotLocked`, `getNotesByFolderNameNotPinnedLocked`, `getNotesByFolderNamePinnedLocked` and folder equivalents. (3) Search edge cases — empty query, no-match query, special characters. (4) Color picker event class structure verification. (5) BUG-005 verification — confirm that note deletion path includes reminder cancellation logic.
- **Dependencies:** Sub-task 1 (bug fixes applied)
- **Entry Criteria:** Sub-task 1 complete, all tests pass
- **Exit Criteria:** New test file covers reminder structure, pin/lock combined queries, search edge cases, color event structure, and BUG-005 verification; all tests pass
- **Success Criteria:**
  - Given notes with various pin/lock combinations in a folder, when querying `getNotesByFolderNamePinnedNotLocked`, then only pinned+unlocked notes are returned
  - Given notes with various pin/lock combinations in a folder, when querying `getNotesByFolderNameNotPinnedLocked`, then only unpinned+locked notes are returned
  - Given folders with various pin/lock combinations under a parent, when querying `getFolderByParentPinNoLock`, then only pinned+unlocked folders are returned
  - Given an empty search query, when searching notes, then results are returned (prefix match on empty string matches all)
  - Given a search query with no matches, when searching notes, then an empty list is returned
  - Given ColorPickerEvent source, when inspected, then it has a color property
  - Given ReminderUtil source, when inspected, then it contains AlarmManager scheduling logic
  - Given AndroidManifest.xml, when inspected, then NotificationReceiver is declared as a receiver
  - Given `./gradlew testDemoDebugUnitTest`, when executed, then all tests pass
- **Files:**
  - Create: `app/src/test/java/com/nrs/nsnik/notes/FeatureVerificationTest.kt`

### Sub-Task 4: Dagger DI Structure + Navigation + ViewModel Completeness Tests
- **Size:** S
- **Description:** Write tests verifying Dagger component/module structure and navigation completeness. Existing tests cover: NavigationGraphTest (7 destinations, 4 actions), NoteViewModelTest (12 tests), FolderViewModelTest (12 tests). New tests target gaps: (1) Dagger — verify all 7 component interfaces exist, each has correct module annotations, and each exposes expected provision methods (file-based structural verification). (2) ViewModel completeness — verify NoteViewModel and FolderViewModel expose all DbUtil delegation methods (method count/name verification). (3) Navigation — verify nav_graph.xml has correct startDestination, and all fragment classes referenced in nav_graph exist.
- **Dependencies:** None
- **Entry Criteria:** Repository compiles, all existing tests pass
- **Exit Criteria:** New test file verifies Dagger structure, ViewModel method completeness, and navigation class references; all tests pass
- **Success Criteria:**
  - Given the Dagger components directory, when inspected, then all 7 component files exist (DatabaseComponent, FileComponent, FolderComponent, GlideComponent, SharedPrefComponent, NetworkComponent, MediaComponent)
  - Given each Dagger component, when its source is read, then it contains `@Component` annotation and references at least one module
  - Given DatabaseModule source, when inspected, then it provides NotesDatabase and has `fallbackToDestructiveMigration`
  - Given NoteViewModel source, when inspected, then it delegates to DbUtil for all note operations (insert, update, delete, query methods)
  - Given FolderViewModel source, when inspected, then it delegates to DbUtil for all folder operations
  - Given nav_graph.xml, when inspected, then the startDestination attribute references a valid fragment
  - Given each fragment class referenced in nav_graph.xml, when checked, then the corresponding .kt file exists
  - Given `./gradlew testDemoDebugUnitTest`, when executed, then all tests pass
- **Files:**
  - Create: `app/src/test/java/com/nrs/nsnik/notes/AppStructureVerificationTest.kt`

### Sub-Task 5: Final Integration Verification
- **Size:** S
- **Description:** Run the full test suite to confirm all 26 test files (22 existing + 4 new) pass together. Verify both product flavors compile. This sub-task creates no new files — it is a verification-only step that runs `./gradlew testDemoDebugUnitTest` and `./gradlew assembleDemoDebug assembleFullDebug` to confirm everything works end-to-end.
- **Dependencies:** Sub-tasks 1, 2, 3, 4
- **Entry Criteria:** All sub-tasks 1–4 complete
- **Exit Criteria:** All tests pass, both flavors compile, no regressions
- **Success Criteria:**
  - Given `./gradlew testDemoDebugUnitTest`, when executed, then all 26 test files pass with 0 failures
  - Given `./gradlew assembleDemoDebug`, when executed, then the demo flavor builds successfully
  - Given `./gradlew assembleFullDebug`, when executed, then the full flavor builds successfully
  - Given the test output, when reviewed, then no existing tests were removed or modified (only new test files added)
- **Files:**
  - Create: none
  - Modify: none

## Implementation Order
1. Sub-task 1 — Bug fixes BUG-001/002/003 + verification tests (no dependencies)
2. Sub-task 4 — Dagger/Navigation/ViewModel structure tests (no dependencies, parallel with 1)
3. Sub-task 2 — Folder cascade delete fix BUG-004 + tests (no dependencies, parallel with 1)
4. Sub-task 3 — Reminder/Pin/Lock/Color/Search tests (depends on 1)
5. Sub-task 5 — Final integration verification (depends on 1, 2, 3, 4)

## Integration Test Plan
After all sub-tasks are complete, the IntegrationTester should verify:
- Run `./gradlew testDemoDebugUnitTest` — all 26 test files pass (22 existing + 4 new: BugFixVerificationTest, FolderCascadeDeleteTest, FeatureVerificationTest, AppStructureVerificationTest)
- Run `./gradlew assembleDemoDebug assembleFullDebug` — both flavors compile
- Verify no existing test files were modified or deleted (diff check)
- Verify all 5 bug fixes are applied in source:
  - ReminderUtil.kt: FLAG_IMMUTABLE in setNotification() and cancelReminder()
  - NotificationReceiver.kt: FLAG_IMMUTABLE in buildNotification()
  - CheckListObject.kt: no java.io.Serializable
  - DbUtil.kt: recursive cascade delete for nested folders
  - Note deletion path includes reminder cancellation
- Verify test count: at least 26 test classes with combined 100+ test methods
- Edge case: delete a folder 3 levels deep and confirm all descendants removed
- Edge case: search with empty string returns all notes
- Edge case: pin+lock combined queries return correct subsets

## Requirements Traceability
| Requirement | Sub-Task(s) |
|-------------|-------------|
| FR-001 to FR-004 (Note CRUD) | Covered by existing NoteDaoTest, NoteViewModelTest, DbUtilIntegrationTest |
| FR-005 (Note serialization) | Covered by existing SerializationMigrationTest, FileUtilTest |
| FR-006 (Date fields) | Covered by existing DateConverterTest, DateSerializerTest |
| FR-007 to FR-011 (Checklists) | Covered by existing CheckListObjectTest, DiffUtilTest |
| FR-012 to FR-014 (Image attachments) | Sub-task 3 (structural verification) |
| FR-015 to FR-017 (Audio recording) | Sub-task 3 (structural verification) |
| FR-018 to FR-021 (Reminders) | Sub-task 1 (bug fixes), Sub-task 3 (verification tests) |
| FR-022 to FR-027 (Pin/Unpin) | Covered by existing DAO tests + Sub-task 3 (combined queries) |
| FR-028 to FR-033 (Lock/Unlock) | Covered by existing DAO tests + Sub-task 3 (combined queries) |
| FR-034 to FR-040 (Folder operations) | Covered by existing FolderDaoTest + Sub-task 2 (cascade delete) |
| FR-041 to FR-043 (Search) | Covered by existing DAO tests + Sub-task 3 (edge cases) |
| FR-044 to FR-046 (Navigation) | Covered by existing NavigationGraphTest + Sub-task 4 |
| FR-047 to FR-049 (Color picker) | Covered by existing DAO tests + Sub-task 3 (event verification) |
| FR-050 to FR-053 (File operations) | Covered by existing FileUtilTest |
| FR-054 to FR-055 (Dagger DI) | Sub-task 4 |
| FR-056 to FR-058 (ViewModels) | Covered by existing VM tests + Sub-task 4 (completeness) |
| BUG-001 | Sub-task 1 |
| BUG-002 | Sub-task 1 |
| BUG-003 | Sub-task 1 |
| BUG-004 | Sub-task 2 |
| BUG-005 | Sub-task 1 (fix), Sub-task 3 (verification) |
| NFR-001 to NFR-005 | Sub-task 5 (final verification) |
