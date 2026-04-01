# ST-001 Execution Report: Fix 3 Bugs + Verification Espresso Test

## Status: ✅ PASSED

## Bugs Fixed

### BUG 1 — Checklist duplicate on Enter
**Root cause:** `CheckListObject` had no stable unique ID. `CheckListDiffUtil.areItemsTheSame()` compared by `text+done`, so all empty items were identical to DiffUtil.

**Fix applied:**
- `CheckListObject.kt`: Added `@Transient val id: String = UUID.randomUUID().toString()` — each instance gets a unique ID. `@Transient` excludes it from kotlinx.serialization so existing persisted JSON is unaffected.
- `CheckListDiffUtil.kt`: Changed `areItemsTheSame()` to compare by `id` field. `areContentsTheSame()` still compares `text+done`.
- `DiffUtilTest.kt`: Updated tests — same instance returns true for `areItemsTheSame`, different instances return false (different UUIDs).
- `CheckListObjectTest.kt`: Added 3 tests: `id_isUniquePerInstance`, `id_isNotSerialized`, `id_isNewAfterDeserialization`.

### BUG 2 — Camera blocked by permission check
**Root cause:** Camera click handler wrapped `startCameraIntent()` inside `checkPermission(WRITE_EXTERNAL_STORAGE, ...)`. On API 29+, this permission is auto-denied and the callback never fires.

**Fix applied:**
- `NewNoteFragment.kt`: Replaced `Manifest.permission.WRITE_EXTERNAL_STORAGE` with `Manifest.permission.CAMERA` in the camera click handler.
- Removed stale `@RequiresPermission(WRITE_EXTERNAL_STORAGE)` annotation from `startCameraIntent()`.
- `AndroidManifest.xml`: Added `<uses-permission android:name="android.permission.CAMERA" />`.

### BUG 3 — Lock first-attempt
**Finding:** The FragmentResult API flow is correctly implemented. The Espresso test confirms lock toggles on first attempt — tap lock → password dialog appears → enter matching passwords → tap save → dialog dismisses and lock state changes.

## Files Modified
| File | Change |
|------|--------|
| `app/src/main/java/com/nrs/nsnik/notes/model/CheckListObject.kt` | Added `@Transient val id` field |
| `app/src/main/java/com/nrs/nsnik/notes/view/adapters/diffUtil/CheckListDiffUtil.kt` | `areItemsTheSame()` compares by `id` |
| `app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt` | Camera uses `CAMERA` permission instead of `WRITE_EXTERNAL_STORAGE` |
| `app/src/main/AndroidManifest.xml` | Added `CAMERA` permission |
| `app/src/test/java/com/nrs/nsnik/notes/util/DiffUtilTest.kt` | Updated checklist diff tests for id-based comparison |
| `app/src/test/java/com/nrs/nsnik/notes/model/CheckListObjectTest.kt` | Added id uniqueness and serialization tests |

## Files Created
| File | Description |
|------|-------------|
| `app/src/androidTest/java/com/nrs/nsnik/notes/BugFixVerificationEspressoTest.kt` | 3 Espresso tests verifying all 3 bug fixes |

## Test Results

### Unit Tests (`./gradlew testDemoDebugUnitTest`)
- **Result:** BUILD SUCCESSFUL
- **Failures:** 0

### Espresso Tests (`./gradlew connectedDemoDebugAndroidTest`)
- **Result:** BUILD SUCCESSFUL
- **Total tests:** 54 (including 3 new)
- **Failures:** 0
- **Skipped:** 0

## Exit Criteria Verification
1. ✅ CheckListObject has `id` field, CheckListDiffUtil compares by `id`
2. ✅ Camera click handler uses `CAMERA` permission, no `WRITE_EXTERNAL_STORAGE` check
3. ✅ `BugFixVerificationEspressoTest.kt` created with 3 tests
4. ✅ `./gradlew connectedDemoDebugAndroidTest` passes with 0 failures
5. ✅ `./gradlew testDemoDebugUnitTest` passes
