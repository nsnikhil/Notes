# Architecture Document

## ST-004: Robolectric Behavioral Tests

### Files Created
- `app/src/test/java/com/nrs/nsnik/notes/NavigationGraphTest.kt` — JUnit 4 file-based XML test for nav_graph.xml (13 tests)
- `app/src/test/java/com/nrs/nsnik/notes/EntityEdgeCaseTest.kt` — Robolectric + Room in-memory DB edge case tests for NoteEntity and FolderEntity (16 tests)

### Design Decisions
- NavigationGraphTest uses pure Java XML parsing (`DocumentBuilderFactory`) to avoid Android framework dependency, making it a fast JVM-only test
- EntityEdgeCaseTest follows the same Robolectric + Room in-memory pattern established by existing `NoteDaoTest` and `FolderDaoTest`
- Tests use the `LiveData.getValueBlocking()` helper pattern consistent with existing test files
- Navigation graph IDs match actual XML: `navItemNotes` (not `listFragment`), `navItemAbout` (not `aboutFragment`), `navItemSettings` (not `prefFragment`)

### Test Configuration
- Test task: `./gradlew testDemoDebugUnitTest`
- Robolectric SDK: 34
- Room testing dependency: `androidx.room:room-testing:2.6.1`
- Architecture components testing: `androidx.arch.core:core-testing:2.2.0`
