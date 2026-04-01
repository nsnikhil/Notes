# ST-002 Execution Report: Fix camera FileProvider path + gallery permission check

## Status: ✅ COMPLETE

## Changes Made

### 1. `app/src/main/res/xml/file_paths.xml`
- Replaced two `<external-path>` entries (hardcoded for `.notes` and `.notes.debug`) with a single `<external-files-path name="my_images" path="Pictures" />`
- `<external-files-path>` maps to `context.getExternalFilesDir()`, matching `createImageFile()` and working on API 29+
- Single entry works for all flavors since `getExternalFilesDir()` is already scoped per-package

### 2. `app/src/main/AndroidManifest.xml`
- Changed FileProvider authority from `com.nrs.nsnik.notes.fileprovider` to `${applicationId}.fileprovider`
- Gradle replaces `${applicationId}` with the flavor-specific ID at build time

### 3. `app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt`
- **Camera fix**: `startCameraIntent()` now uses `"${BuildConfig.APPLICATION_ID}.fileprovider"` instead of hardcoded string
- **Gallery fix**: Removed `checkPermission(READ_EXTERNAL_STORAGE, ...)` wrapper; `galleryLauncher.launch("image/*")` called directly
- Removed unused `startGalleryIntent()` method and `READ_EXTERNAL_STORAGE_PERMISSION` constant
- Added `import com.nrs.nsnik.notes.BuildConfig`

### 4. `app/src/test/java/com/nrs/nsnik/notes/CameraAndGalleryFixTest.kt` (new)
- 5 tests verifying:
  - `file_paths.xml` uses `<external-files-path>`, not `<external-path>`
  - `file_paths.xml` does not hardcode any applicationId variant
  - Manifest uses `${applicationId}.fileprovider`
  - Gallery handler has no permission check, calls launcher directly
  - Camera intent uses `BuildConfig.APPLICATION_ID`

## Verification

| Check | Result |
|-------|--------|
| `./gradlew assembleDemoDebug` | ✅ BUILD SUCCESSFUL |
| `./gradlew testDemoDebugUnitTest` (full suite) | ✅ BUILD SUCCESSFUL |
| `CameraAndGalleryFixTest` (5 tests) | ✅ All passed |

## Exit Criteria Verification

1. ✅ `file_paths.xml` uses `<external-files-path>` (not `<external-path>`)
2. ✅ Gallery launch has no permission check wrapper
3. ✅ Build succeeds, all unit tests pass
