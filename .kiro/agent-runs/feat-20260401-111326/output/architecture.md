# Architecture Document

## ST-001: Bug Fixes (BUG-001, BUG-002, BUG-003)

### Files Modified
- `app/src/main/java/com/nrs/nsnik/notes/util/ReminderUtil.kt` — Added `PendingIntent.FLAG_IMMUTABLE` to both `getBroadcast()` calls
- `app/src/main/java/com/nrs/nsnik/notes/util/receiver/NotificationReceiver.kt` — Added `PendingIntent.FLAG_IMMUTABLE` to `getActivity()` call
- `app/src/main/java/com/nrs/nsnik/notes/model/CheckListObject.kt` — Removed `java.io.Serializable`, kept `@Serializable`

### Files Created
- `app/src/test/java/com/nrs/nsnik/notes/BugFixVerificationTest.kt` — 3 file-based JUnit 4 tests

### Design Decisions
- FLAG_IMMUTABLE is OR'd with existing FLAG_UPDATE_CURRENT to preserve update semantics while satisfying API 31+ requirement
- Test follows existing RuntimeCrashFixTest pattern: file-based assertions using projectRoot resolution

## ST-003: Feature Verification Tests (Reminders, Pin/Lock, Color, Search)

### Files Created
- `app/src/test/java/com/nrs/nsnik/notes/FeatureVerificationTest.kt` — 14 Robolectric + Room in-memory DB tests

### Test Coverage
- Reminder FLAG_IMMUTABLE verification (file-based assertions on ReminderUtil.kt and NotificationReceiver.kt)
- Pin/Lock combined DAO queries for notes (all 4 combinations via folder-scoped queries)
- Pin/Lock combined DAO queries for folders (all 4 combinations via parent-scoped queries)
- Search edge cases: empty string, special characters, case insensitivity, no-match
- Color filtering for notes and folders
- BUG-005 documentation: DbUtil.deleteNote() does not cancel reminders (known gap)

### Design Decisions
- Uses same Robolectric + Room in-memory DB pattern as existing NoteDaoTest/FolderDaoTest
- BUG-005 is a documentation-only test — asserts the absence of cancelReminder in deleteNote to track the known gap

## ST-004: App Structure Verification Tests

### Files Created
- `app/src/test/java/com/nrs/nsnik/notes/AppStructureVerificationTest.kt` — 8 file-based JUnit 4 tests

### Test Coverage
- Dagger component existence (all 7 files)
- Dagger @Component annotation verification (all 7 files)
- NetworkComponent interface declaration (not abstract class)
- NoteViewModel key methods (insert, delete, update, search)
- FolderViewModel key methods (insert, delete, update, search)
- Navigation graph fragment-to-source-file mapping (7 fragments)
- MyApplication Dagger component wiring (6 core components)
- ContextModule initialization location (inside onCreate, not field)

### Design Decisions
- Follows RuntimeCrashFixTest pattern: file-based assertions using projectRoot resolution
- MyApplication only references 6 of 7 components; MediaComponent is used directly in fragments — test verifies the 6 that are actually wired in MyApplication
- ViewModels are in `viewmodel/` package, not `view/`
