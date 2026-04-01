# ST-002 Execution Report: Fix BUG-004 — Folder Cascade Delete

## Status: ✅ COMPLETE

## Summary
Fixed BUG-004 where deleting a folder only deleted its direct notes but left nested sub-folders orphaned. Added recursive cascade deletion that depth-first traverses the folder tree, deleting all sub-folders and their notes before deleting the parent.

## Changes Made

### 1. FolderDao.kt — Added synchronous child query
Added `getChildFoldersByParentSync(parentFolder: String): List<FolderEntity>` — a synchronous Room query (returns `List` not `LiveData`) needed for recursive deletion on a background thread.

### 2. DbUtil.kt — Added cascade deletion methods
- `deleteFolderCascade(folderName: String)` — Public API. Wraps recursive deletion in `Completable.fromCallable` on IO scheduler, consistent with existing DbUtil patterns.
- `deleteFolderTreeSync(folderName: String)` — Internal recursive method. Depth-first: finds children via `getChildFoldersByParentSync`, recurses into each, deletes notes via `deleteNoteByFolderName`, deletes folder via `deleteFolderByName`.
- Inline comment added: `// BUG-004 fix: recursively delete nested sub-folders before deleting parent`

### 3. FolderCascadeDeleteTest.kt — 6 test scenarios
| # | Test | Verified |
|---|------|----------|
| 1 | Delete folder with notes → notes deleted | ✅ |
| 2 | Delete folder with sub-folder → sub-folder deleted | ✅ |
| 3 | Delete folder with 3-level deep nesting → all levels deleted | ✅ |
| 4 | Delete folder → notes in sub-folders also deleted | ✅ |
| 5 | Delete folder → other folders not affected | ✅ |
| 6 | Delete folder → multiple children at same level all deleted | ✅ |

## Exit Criteria Verification

| Criterion | Status |
|-----------|--------|
| DbUtil recursive folder deletion implemented | ✅ `deleteFolderCascade` + `deleteFolderTreeSync` |
| FolderCascadeDeleteTest.kt created with 5+ tests, all passing | ✅ 6 tests, 0 failures |
| `./gradlew testDemoDebugUnitTest` passes | ✅ 241 tests, 0 failures |
| `./gradlew assembleDebug` succeeds | ✅ BUILD SUCCESSFUL |

## Files Modified
- `app/src/main/java/com/nrs/nsnik/notes/data/FolderDao.kt`
- `app/src/main/java/com/nrs/nsnik/notes/util/DbUtil.kt`

## Files Created
- `app/src/test/java/com/nrs/nsnik/notes/FolderCascadeDeleteTest.kt`
