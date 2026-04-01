# Feature: Comprehensive Test Suite and Bug Fixes for Android Notes App

## Summary
Write a comprehensive test suite for an Android Notes app to verify all features work correctly after a modernization/dependency upgrade. The test suite must cover data access (Room DAOs), ViewModels, serialization, file utilities, general utilities, integration paths, and UI behavior via Robolectric. Any bugs discovered by the tests must be fixed in the production code so that all tests pass. Both build flavors must remain buildable.

## Detailed Requirements

### Functional Requirements

#### Room DAO Tests
1. NoteDao and FolderDao must be tested using an in-memory Room database
2. Tests must cover CRUD operations (create, read, update, delete) for both DAOs
3. Tests must cover filtered queries supported by each DAO
4. Tests must verify cascade delete behavior (e.g., deleting a folder cascades to its notes)
5. Tests must cover edge cases (empty results, duplicate inserts, null fields where applicable)

#### ViewModel Tests
6. NoteViewModel and FolderViewModel must be tested for correct LiveData emissions
7. Tests must verify that ViewModels invoke the correct DAO methods for each operation

#### Serialization Tests
8. kotlinx.serialization round-trip tests must be written for NoteEntity
9. kotlinx.serialization round-trip tests must be written for FolderEntity
10. kotlinx.serialization round-trip tests must be written for CheckListObject
11. kotlinx.serialization round-trip tests must be written for DateSerializer
12. Round-trip means: serialize an object to its serialized form, then deserialize back, and verify equality with the original

#### FileUtil Tests
13. FileUtil must be tested for file read and write operations using Okio
14. Tests must cover the missing-file scenario (file does not exist)
15. Tests must cover the empty-file scenario
16. Tests must cover the corrupted-file scenario (malformed content)

#### Utility Tests
17. PasswordUtil encryption and decryption must be tested (encrypt then decrypt yields original)
18. DateConverter must be tested for correct date-to-storage and storage-to-date conversions
19. DiffUtil classes must be tested for correct item comparison and change detection
20. SecurityChecks utility must be tested

#### Integration Tests
21. Database + ViewModel integration must be tested (ViewModel operating against a real in-memory Room DB)
22. FileUtil + Serialization integration must be tested (write serialized data, read it back, deserialize)
23. Dagger component initialization must be tested (dependency graph resolves without errors)

#### Robolectric Behavioral Tests
24. Activity launch must be tested (main activity starts without crashing)
25. Fragment creation must be tested (key fragments inflate and render)
26. Navigation flows must be tested (navigating between screens)
27. Note creation flow must be tested end-to-end via UI
28. Folder creation flow must be tested end-to-end via UI
29. Search functionality must be tested via UI

#### Bug Fixes
30. Any bugs discovered by the test suite must be fixed in the production source code
31. Bugs must be fixed rather than skipped or suppressed — tests must not be weakened to pass
32. Existing tests in the project must not be removed or disabled

## Acceptance Criteria

1. Given the complete test suite, When all tests are executed via Gradle, Then every test passes with zero failures
2. Given a Room DAO test for NoteDao, When a note is inserted and then queried, Then the returned note matches the inserted data
3. Given a Room DAO test for FolderDao, When a folder with child notes is deleted, Then all child notes are also deleted (cascade)
4. Given a ViewModel test, When an operation is triggered on NoteViewModel, Then the correct DAO method is called and LiveData emits the expected value
5. Given a serialization round-trip test, When a NoteEntity is serialized and deserialized, Then the result equals the original object
6. Given a FileUtil test with a missing file, When a read is attempted, Then the system handles the absence gracefully without crashing
7. Given a PasswordUtil test, When a plaintext string is encrypted and then decrypted, Then the result matches the original plaintext
8. Given an integration test for Database + ViewModel, When the ViewModel performs a write followed by a read against an in-memory DB, Then the data is consistent
9. Given a Robolectric test, When the main Activity is launched, Then it starts without throwing an exception
10. Given a bug is discovered by any test, When the bug is analyzed, Then the production code is fixed and the test passes without modification to the test expectation
11. Given both build flavors, When a Gradle build is executed for each, Then both flavors compile and build successfully
12. Given the test directory structure, When tests are inspected, Then all test files reside under `app/src/test/` with correct package naming and `<ClassName>Test.kt` naming convention

## Constraints
- All test files must be placed in `app/src/test/` (not `androidTest`)
- Test file naming must follow the `<ClassName>Test.kt` convention
- Package naming in test files must match the production source package structure
- Existing tests must not be removed or disabled
- Tests must not be skipped or weakened to achieve a passing build — bugs must be fixed instead
- Both build flavors of the app must compile and build successfully after all changes
- All tests must be deterministic (no flaky tests, no dependency on external state or timing)

## Dependencies
- Room testing library (for in-memory database creation)
- JUnit (test framework)
- Mockito (mocking framework for ViewModel/DAO isolation tests)
- Robolectric (Android framework simulation for Activity, Fragment, and navigation tests)
- AndroidX Test libraries (test runners, rules)
- kotlinx.serialization (already in production code, used in serialization tests)
- Okio (already in production code, used in FileUtil tests)
- Dagger (already in production code, tested for component initialization)
- Gradle build system (for running tests and verifying builds)

## Non-Functional Requirements
- All tests must be deterministic — no flaky behavior, no reliance on external services, network, or wall-clock time
- Tests must run via standard Gradle test tasks (no custom test runners or external tooling required)
- Test execution must complete in a reasonable time frame (exact threshold not specified — see Open Questions)

## Technical Notes
- The app has recently undergone dependency upgrades (modernization); some features may be broken as a result
- Room tests should use `Room.inMemoryDatabaseBuilder` for isolation and speed
- Robolectric is the designated framework for tests that need the Android framework (Activities, Fragments, Context) — instrumented tests on a device/emulator are not required
- kotlinx.serialization is used for data model serialization (not Gson, Moshi, etc.)
- Okio is used for file I/O in FileUtil (not java.io directly)
- Dagger is the dependency injection framework in use
- The app has two build flavors; both must remain buildable after changes
- The test-then-fix workflow is intentional: write tests first to discover what's broken, then fix the production code

## Open Questions
1. What are the two build flavor names? (Needed to verify both build successfully)
2. What is the expected behavior of SecurityChecks — what does it check and what are the expected outcomes?
3. Are there specific filtered queries on NoteDao and FolderDao that must be tested, or should the agent discover them from the DAO interfaces?
4. What is the expected cascade delete relationship — does deleting a Folder delete its Notes, or are there other cascade paths?
5. What specific navigation flows exist in the app (e.g., which screens connect to which)?
6. Is there a maximum acceptable test execution time?
7. What are the exact Gradle commands expected for running tests and verifying builds (e.g., `./gradlew test`, `./gradlew assembleDebug assembleRelease`)?
8. Are there any existing test files that the new tests must coexist with? If so, what are they?
9. What does "corrupted file" mean in the context of FileUtil tests — malformed JSON, binary garbage, truncated content, or all of the above?
10. For PasswordUtil, what encryption scheme is used? Are there known-answer test vectors, or is round-trip (encrypt-then-decrypt) the only verification method?
11. What user roles or personas interact with the app? The requirements do not mention distinct user types (e.g., admin vs. standard user). If the app has role-based features (e.g., password-protected notes), this should be clarified.
12. What does "search functionality" entail — full-text search, title-only search, folder-scoped search?
