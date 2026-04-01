# ST-003 Execution Report: Feature Verification Tests

## Status: ✅ PASSED

## Files Created
- `app/src/test/java/com/nrs/nsnik/notes/FeatureVerificationTest.kt` — 14 tests covering reminders, pin/lock, search, color, and BUG-005

## Test Results

| # | Test Name | Category | Result |
|---|-----------|----------|--------|
| 1 | `reminderUtil_allPendingIntentCallsContainFlagImmutable` | Reminder structure | ✅ |
| 2 | `notificationReceiver_allPendingIntentCallsContainFlagImmutable` | Reminder structure | ✅ |
| 3 | `notePinQuery_returnsOnlyPinnedNotes` | Pin/Lock DAO | ✅ |
| 4 | `noteLockQuery_returnsOnlyLockedNotes` | Pin/Lock DAO | ✅ |
| 5 | `notePinnedAndLocked_combinedQuery` | Pin/Lock DAO (all 4 combos) | ✅ |
| 6 | `folderPinLockCombinedQueries` | Pin/Lock DAO (all 4 combos) | ✅ |
| 7 | `searchNote_emptyString_returnsAllNotes` | Search edge case | ✅ |
| 8 | `searchNote_specialCharacters_returnsEmptyWhenNoMatch` | Search edge case | ✅ |
| 9 | `searchNote_caseInsensitiveForAscii` | Search edge case | ✅ |
| 10 | `searchNote_noMatch_returnsEmptyList` | Search edge case | ✅ |
| 11 | `searchFolder_emptyString_returnsAllFolders` | Search edge case | ✅ |
| 12 | `noteColorFilter_returnsCorrectNotes` | Color filtering | ✅ |
| 13 | `folderColorFilter_returnsCorrectFolders` | Color filtering | ✅ |
| 14 | `bug005_deleteNoteDoesNotCancelReminder_documentedBehavior` | BUG-005 documentation | ✅ |

## Exit Criteria Verification

1. ✅ FeatureVerificationTest.kt created with 14 tests (>10 required)
2. ✅ `./gradlew testDemoDebugUnitTest` — BUILD SUCCESSFUL, 0 failures
3. ✅ `./gradlew assembleDebug` — BUILD SUCCESSFUL

## Key Findings

### BUG-005 Documentation
`DbUtil.deleteNote()` calls `noteDao.deleteNotes()` but never invokes `ReminderUtil.cancelReminder()`. Deleting a note with an active reminder leaves a dangling alarm. This is documented as a known behavioral gap in the test.

### Search Behavior
- The DAO query `LIKE :query|| '%'` performs prefix matching
- Empty string query matches all notes (becomes `LIKE '%'`)
- SQLite LIKE is case-insensitive for ASCII characters by default
- Special characters that don't match any prefix return empty list

### Pin/Lock Queries
All four combinations (pinned+locked, pinned+unlocked, unpinned+locked, unpinned+unlocked) are tested for both NoteDao and FolderDao, confirming the combined WHERE clauses work correctly.
