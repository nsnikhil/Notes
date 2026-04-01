# Validated Requirements: Comprehensive Test Suite and Bug Fixes for Android Notes App

## Validation Status
- Cross-reference check: PASS
- Completeness check: PARTIAL — all raw requirements are covered; source code analysis revealed gaps and a critical bug that the implementing agent must address (see Technical Notes)

## Summary
Write a comprehensive test suite for an Android Notes app to verify all features work correctly after a modernization/dependency upgrade. The test suite must cover Room DAOs, ViewModels, serialization, file utilities, general utilities, integration paths, and UI behavior via Robolectric. Any bugs discovered by the tests must be fixed in the production code so that all tests pass. Both build flavors (`demo` and `full`) must remain buildable. A critical known bug exists: `FileUtil` uses Java `ObjectOutputStream`/`ObjectInputStream` but `NoteEntity` no longer implements `java.io.Serializable`, causing `NotSerializableException` at runtime — this must be fixed.

## Detailed Requirements

### Room DAO Tests
- FR-001: [MUST-HAVE] NoteDao and FolderDao must be tested using an in-memory Room database (`Room.inMemoryDatabaseBuilder`)
- FR-002: [MUST-HAVE] Tests must cover CRUD operations (create, read, update, delete) for both DAOs
- FR-003: [MUST-HAVE] Tests must cover all filtered queries discoverable from `NoteDao.kt` and `FolderDao.kt` interfaces (e.g., `getNoteByQuery` prefix match on title, `getFolderByQuery` prefix match on folderName)
- FR-004: [MUST-HAVE] Tests must verify cascade delete behavior. NOTE: There is NO Room-level `@ForeignKey` cascade. Cascade is manual — `DbUtil.deleteFolderByName()` calls `FolderDao.deleteFolderByName()` and separately `NoteDao.deleteNoteByFolderName()`. Test this at the DbUtil/ViewModel layer, not at the DAO level alone
- FR-005: [SHOULD-HAVE] Tests must cover edge cases (empty results, duplicate inserts, null fields where applicable)

### ViewModel Tests
- FR-006: [MUST-HAVE] NoteViewModel and FolderViewModel must be tested for correct LiveData emissions
- FR-007: [MUST-HAVE] Tests must verify that ViewModels invoke the correct DAO methods for each operation

### Serialization Tests
- FR-008: [MUST-HAVE] kotlinx.serialization round-trip tests must be written for NoteEntity
- FR-009: [MUST-HAVE] kotlinx.serialization round-trip tests must be written for FolderEntity
- FR-010: [MUST-HAVE] kotlinx.serialization round-trip tests must be written for CheckListObject
- FR-011: [MUST-HAVE] DateSerializer must be tested with: epoch 0, negative epoch (pre-1970), null date (via nullable `NoteEntity.dateModified`), and current date. NOTE: DateSerializer serializes `Date` as `Long` (epoch millis) — there are no "formats" involved
- FR-012: [MUST-HAVE] Round-trip means: serialize → deserialize → verify equality with original

### FileUtil Tests
- FR-013: [MUST-HAVE] FileUtil must be tested for file read and write operations. NOTE: FileUtil internally uses Java `ObjectOutputStream`/`ObjectInputStream`, NOT kotlinx.serialization + Okio. Tests must reflect the actual implementation
- FR-014: [MUST-HAVE] Tests must cover the missing-file scenario (file does not exist)
- FR-015: [SHOULD-HAVE] Tests must cover the empty-file scenario
- FR-016: [SHOULD-HAVE] Tests must cover the corrupted-file scenario (non-serialized bytes that cannot be deserialized by `ObjectInputStream`)

### Utility Tests
- FR-017: [MUST-HAVE] PasswordUtil encryption and decryption must be tested (encrypt then decrypt yields original). NOTE: Requires Android `Context` and `SharedPreferences` — use Robolectric. RSA key pair is generated on first use; round-trip is the only verification method
- FR-018: [MUST-HAVE] DateConverter must be tested for correct date-to-storage and storage-to-date conversions
- FR-019: [MUST-HAVE] DiffUtil classes must be tested. NOTE: `NoteEntity` is `open class` (not `data class`), so `NotesDiffUtil.areContentsTheSame()` uses reference equality (`==`) — it will return `false` for distinct objects with identical fields. `CheckListDiffUtil` always returns `false` for both `areItemsTheSame` and `areContentsTheSame` unconditionally. Test as-is — these may be intentional or latent bugs
- FR-020: [SHOULD-HAVE] SecurityChecks utility must be tested. NOTE: Under Robolectric, `checkDebuggable` is straightforward; `checkAppSignature`/`verifyInstaller` require shadow configuration; `checkEmulator` uses reflection on `android.os.SystemProperties`. Implement what is feasible

### Integration Tests
- FR-021: [MUST-HAVE] Database + ViewModel integration must be tested (ViewModel operating against a real in-memory Room DB)
- FR-022: [MUST-HAVE] FileUtil + Serialization integration must be tested (write serialized data, read it back, deserialize). NOTE: This test WILL expose the `NotSerializableException` bug — `NoteEntity` no longer implements `java.io.Serializable`. The bug must be fixed first (see FR-030)
- FR-023: [SHOULD-HAVE] Dagger component initialization must be tested (dependency graph resolves without errors)

### Robolectric Behavioral Tests
- FR-024: [MUST-HAVE] Activity launch must be tested (main activity starts without crashing)
- FR-025: [SHOULD-HAVE] Fragment creation must be tested (key fragments inflate and render)
- FR-026: [SHOULD-HAVE] Navigation flows must be tested. Known flows: List→NewNote→ImageList, List→Search, Intro→NoteList, drawer items (About, Settings)
- FR-027: [SHOULD-HAVE] Note creation flow must be tested end-to-end via UI
- FR-028: [SHOULD-HAVE] Folder creation flow must be tested end-to-end via UI
- FR-029: [COULD-HAVE] Search functionality must be tested via UI. Search is prefix match on note title (`NoteDao.getNoteByQuery`) and folder name (`FolderDao.getFolderByQuery`)

### Bug Fixes
- FR-030: [MUST-HAVE] **CRITICAL**: `NoteEntity` must be restored to implement `java.io.Serializable` (or `FileUtil` must be refactored) so that `FileUtil.saveNote()`/`FileUtil.getLiveNote()` no longer throw `NotSerializableException`. This was broken when `Serializable` was removed in a previous fix run (ST-002)
- FR-031: [MUST-HAVE] Any additional bugs discovered by the test suite must be fixed in the production source code
- FR-032: [MUST-HAVE] Bugs must be fixed rather than skipped or suppressed — tests must not be weakened to pass
- FR-033: [MUST-HAVE] Existing tests in the project must not be removed or disabled. Existing tests are in `app/src/test/java/com/nrs/nsnik/notes/` — 8 test files (migration verification tests + EncryptionUnitTest)

## Acceptance Criteria
1. Given the complete test suite, when all tests are executed via `./gradlew testDemoDebugUnitTest` and `./gradlew testFullDebugUnitTest`, then every test passes with zero failures
2. Given a Room DAO test for NoteDao, when a note is inserted and then queried, then the returned note matches the inserted data
3. Given a Room DAO test for FolderDao, when a folder with child notes is deleted via DbUtil/ViewModel (manual cascade), then all child notes are also deleted
4. Given a ViewModel test, when an operation is triggered on NoteViewModel, then the correct DAO method is called and LiveData emits the expected value
5. Given a serialization round-trip test, when a NoteEntity is serialized and deserialized via kotlinx.serialization, then the result equals the original object
6. Given a FileUtil test with a missing file, when a read is attempted, then the system handles the absence gracefully without crashing
7. Given a PasswordUtil test, when a plaintext string is encrypted and then decrypted, then the result matches the original plaintext
8. Given an integration test for Database + ViewModel, when the ViewModel performs a write followed by a read against an in-memory DB, then the data is consistent
9. Given a Robolectric test, when the main Activity is launched, then it starts without throwing an exception
10. Given the FileUtil serialization bug (FR-030), when `FileUtil.saveNote()` is called with a NoteEntity, then it completes without `NotSerializableException`
11. Given both build flavors (`demo` and `full`), when `./gradlew assembleDebug` is executed, then both flavors compile and build successfully
12. Given the test directory structure, when tests are inspected, then all test files reside under `app/src/test/` with correct package naming and `<ClassName>Test.kt` naming convention

## Constraints
- All test files must be placed in `app/src/test/` (not `androidTest`)
- Test file naming must follow the `<ClassName>Test.kt` convention
- Package naming in test files must match the production source package structure
- Existing tests (8 files in `app/src/test/java/com/nrs/nsnik/notes/`) must not be removed or disabled
- Tests must not be skipped or weakened to achieve a passing build — bugs must be fixed instead
- Both build flavors (`demo` and `full`) must compile and build successfully after all changes
- All tests must be deterministic (no flaky tests, no dependency on external state or timing)
- Build verification commands: `./gradlew testDemoDebugUnitTest`, `./gradlew testFullDebugUnitTest`, `./gradlew assembleDebug`

## Dependencies
- Room testing library (for in-memory database creation)
- JUnit (test framework)
- Mockito (mocking framework for ViewModel/DAO isolation tests)
- Robolectric (Android framework simulation for Activity, Fragment, navigation, PasswordUtil, SecurityChecks tests)
- AndroidX Test libraries (test runners, rules)
- kotlinx.serialization (already in production code, used in serialization tests)
- Okio (already in production code — note: FileUtil does NOT use Okio, it uses Java ObjectOutputStream)
- Dagger (already in production code, tested for component initialization)
- Gradle build system (for running tests and verifying builds)

## Non-Functional Requirements
- NFR-001: [MUST-HAVE] All tests must be deterministic — no flaky behavior, no reliance on external services, network, or wall-clock time
- NFR-002: [MUST-HAVE] Tests must run via standard Gradle test tasks (no custom test runners or external tooling required)
- NFR-003: [SHOULD-HAVE] Test execution should complete in a reasonable time frame — use in-memory databases and avoid unnecessary delays

## Technical Notes
- **CRITICAL BUG — FileUtil Serialization**: `FileUtil.serialize()` uses `ObjectOutputStream` and `FileUtil.deSerialize()` uses `ObjectInputStream` (Java serialization). `NoteEntity` had `java.io.Serializable` removed in post-migration fix ST-002. This means `FileUtil.saveNote()` and `FileUtil.getLiveNote()` will throw `NotSerializableException`. The implementing agent MUST fix this — either restore `Serializable` on `NoteEntity` or refactor `FileUtil` to use kotlinx.serialization
- **NoteEntity is `open class`, not `data class`**: `==` defaults to reference equality. `NotesDiffUtil.areContentsTheSame()` will always return `false` for distinct objects with identical fields. Be aware when writing DiffUtil tests
- **CheckListDiffUtil always returns false**: Both `areItemsTheSame` and `areContentsTheSame` return `false` unconditionally. Test as-is (forces full rebind on every update)
- **No Room-level cascade**: Cascade delete is manual via `DbUtil`/ViewModel calling both `FolderDao.deleteFolderByName()` and `NoteDao.deleteNoteByFolderName()`. Do not test cascade at the DAO level alone
- **PasswordUtil requires Robolectric**: Needs `Context` and `SharedPreferences`. RSA key pair generated on first use, stored in SharedPreferences
- **SecurityChecks under Robolectric**: `checkDebuggable` is straightforward; other methods require shadow configuration or reflection
- **DateSerializer**: Serializes `Date` as `Long` (epoch millis) — no format strings involved
- **Search**: Prefix match on title (`NoteDao.getNoteByQuery`) and folder name (`FolderDao.getFolderByQuery`) using `LIKE :query|| '%'`
- **Build flavors**: `demo` and `full`
- **Navigation graph**: List→NewNote→ImageList, List→Search, Intro→NoteList, drawer items (About, Settings)
- Room tests should use `Room.inMemoryDatabaseBuilder` for isolation and speed
- The app has no role-based access — all users are equal. Password protection is per-note/per-folder, not per-user

## Scope Exclusions
- Social login or user authentication system
- Instrumented tests on a device/emulator (Robolectric only)
- Custom test runners or external tooling
- Two-factor authentication or role-based access
- Network-dependent tests
- Performance benchmarking (no explicit time threshold required)

## Deferred Items
- None — all open questions from the structured requirements were resolved via source code analysis by the validator
