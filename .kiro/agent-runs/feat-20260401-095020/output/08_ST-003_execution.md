# ST-003 Execution Report: ViewModel + Integration Tests

## Status: ✅ PASSED

## Files Created (3 test files, 33 new tests)

### 1. `app/src/test/java/com/nrs/nsnik/notes/view/NoteViewModelTest.kt` — 12 tests
Tests NoteViewModel's delegation to DbUtil using an in-memory Room database with RxJava trampoline schedulers:
- `insertNote_isRetrievableByFolder` — insert via DbUtil, query by folder
- `updateNote_changesTitle` — update title, verify via getNoteById
- `deleteNote_removesFromDb` — delete note, verify list empty
- `deleteNoteByFolderName_removesAllInFolder` — bulk delete by folder
- `getNoteById_returnsCorrectNote` — single note retrieval
- `getNoteByFolderNameNoPinNoLock_filtersCorrectly` — pin/lock filter combination
- `searchNote_findsByTitlePrefix` — LIKE query prefix search
- `getNotesByPin_returnsOnlyPinned` — pin filter
- `changeNotePinStatus_updatesPinField` — pin status update
- `changeNoteFolder_updatesFolder` — folder reassignment
- `getNotesByColor_filtersCorrectly` — color filter
- `changeNoteLockStatus_updatesLockField` — lock status update

### 2. `app/src/test/java/com/nrs/nsnik/notes/view/FolderViewModelTest.kt` — 12 tests
Tests FolderViewModel's delegation to DbUtil using same pattern:
- `insertFolder_isRetrievableByParent` — insert via DbUtil, query by parent
- `updateFolder_changesName` — update folder name
- `deleteFolder_removesFromDb` — delete folder
- `deleteFolderByName_removesCorrectFolder` — delete by name
- `deleteFolderByParent_removesChildren` — bulk delete children
- `getFolders_returnsAll` — list all folders
- `getFolderByName_returnsCorrectFolder` — single folder by name
- `searchFolder_findsByNamePrefix` — LIKE query prefix search
- `getFolderByParentNoPinNoLock_filtersCorrectly` — pin/lock filter
- `changeFolderPinStatus_updatesPinField` — pin status update
- `changeFolderLockStatus_updatesLockField` — lock status update
- `getFolderByColor_filtersCorrectly` — color filter

### 3. `app/src/test/java/com/nrs/nsnik/notes/util/DbUtilIntegrationTest.kt` — 9 tests
Integration tests verifying DbUtil coordinates DAO + FileUtil:
- `insertNote_withFileName_savesFileToFileSystem` — verifies file written to disk
- `insertNote_withoutFileName_doesNotCrash` — null fileName handled gracefully
- `updateNote_withFileName_updatesFileOnDisk` — file updated on disk after update
- `deleteNote_removesFromDatabase` — DAO delete works through DbUtil
- `insertFolder_andRetrieveByParent` — folder insert + query
- `deleteNoteByFolderName_removesAllNotesInFolder` — bulk note delete by folder
- `noteAndFolderOperations_areIndependent` — note and folder tables independent
- `changeNotePinAndLock_updatesCorrectly` — combined pin+lock update
- `changeFolderParent_updatesParentField` — parent folder reassignment

## Production Bug Fixed

### DbUtil.kt — NPE on null fileName (lines 64 and 131)
- `insertNote()` and `updateNote()` used `it.fileName!!` which throws NPE when fileName is null
- Fixed by replacing with `val name = it.fileName ?: return@forEach` — skips file save when no fileName
- Also improved error logging: `e.printStackTrace()` → `Timber.e(e, "Failed to save note file: %s", name)`

## Test Infrastructure
- Robolectric `@Config(sdk = [34])` for Android context
- Room in-memory database with `allowMainThreadQueries()`
- `InstantTaskExecutorRule` for synchronous LiveData
- RxJava trampoline schedulers (`RxJavaPlugins.setIoSchedulerHandler`, `RxAndroidPlugins.setInitMainThreadSchedulerHandler`)
- DbUtil constructed directly (bypassing Dagger/MyApplication) with real Room DB + FileUtil pointing to temp directory

## Build Verification
- `./gradlew testDemoDebugUnitTest` — **BUILD SUCCESSFUL**, 204 tests, 0 failures ✓
- `./gradlew assembleDebug` — **BUILD SUCCESSFUL** ✓
