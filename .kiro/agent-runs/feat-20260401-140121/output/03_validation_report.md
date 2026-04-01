# Validation Report

## Cross-Reference Result
- Status: FAIL
- The structured requirements capture all five broken features from the raw input (checklist double-add, lock first-attempt, camera, gallery, reminder) and the testing/build requirements.
- Discrepancies found:
  1. **FileProvider path mismatch not identified**: The raw input explicitly calls out FileProvider authority mismatch as a likely camera root cause. Source code review confirms `file_paths.xml` uses `<external-path>` pointing to `Android/data/com.nrs.nsnik.notes/files/Pictures` and `Android/data/com.nrs.nsnik.notes.debug/files/Pictures`, but the code calls `getExternalFilesDir(Environment.DIRECTORY_PICTURES)` which requires `<external-files-path>` (not `<external-path>`). The structured requirements mention FileProvider authority mismatch in Technical Notes but do not capture the specific `<external-path>` vs `<external-files-path>` tag mismatch as a concrete requirement to fix.
  2. **Checklist double-add root cause confirmed but fix incomplete**: Source code confirms `addCheckListItem()` calls both `submitList(mCheckList.toList())` AND `notifyItemInserted()`. The `submitList` call triggers DiffUtil which will insert the new item, and then `notifyItemInserted` fires a second insertion animation/callback. The structured requirements correctly identify this but frame it as "suspected" rather than confirmed. The same pattern exists in `addAudioToList()`, `addImageToList()`, and `onItemRemoved()` — these are not mentioned in the raw or structured requirements.
  3. **Lock feature — FragmentResult API already wired**: Source code shows the fix has already been partially applied: `NewNoteFragment` registers a `FragmentResultListener` for `PasswordDialogFragment.REQUEST_KEY`, and `PasswordDialogFragment.savePassword()` calls `setFragmentResult`. The structured requirements describe this as a bug to fix, but the code already contains the fix mechanism. The requirement should clarify whether this is a verification task or a new implementation.
  4. **Camera — Activity Result API already migrated**: Source code shows `cameraLauncher` and `galleryLauncher` are already registered via `registerForActivityResult`. The structured requirements mention migration to Activity Result API as potentially needed, but the code already uses it. The remaining bug is the `file_paths.xml` mismatch.
  5. **Reminder — FLAG_IMMUTABLE already applied**: Source code shows `PendingIntent.FLAG_IMMUTABLE` is already present in both `ReminderUtil.kt` and `NotificationReceiver.kt`. The notification channel is created in `buildNotification()` with a matching channel ID. The structured requirements list FLAG_IMMUTABLE as a suspected cause, but it's already fixed.
  6. **Raw input mentions `resolveActivity()` returning null on API 30+**: The manifest has no `<queries>` element. However, since the code now uses `ActivityResultContracts.TakePicture()` and `ActivityResultContracts.GetContent()` (which don't need `resolveActivity`), this is no longer an issue. The structured requirements should clarify this is already resolved.

## Completeness Result
- Status: FAIL
- Critical gaps found:

### Missing / Incorrect Root Cause Analysis (code-verified)

1. **Checklist (FR-1)**: The actual root cause is confirmed in source: `addCheckListItem()` at line calls `mCheckListAdapter.submitList(mCheckList.toList())` followed by `mCheckListAdapter.notifyItemInserted(mCheckList.size - 1)`. The `submitList` with a new list copy triggers DiffUtil to detect and animate the insertion; the subsequent `notifyItemInserted` causes a duplicate insertion notification. The fix is to remove the `notifyItemInserted` call. The same bug pattern exists in `addAudioToList()`, `addImageToList()`, and all three branches of `onItemRemoved()` (which call `notifyItemRemoved` after `submitList`). The structured requirements do not mention these additional occurrences.

2. **Lock (FR-2)**: The FragmentResult API callback is already wired in `NewNoteFragment.onViewCreated()`. The `PasswordDialogFragment.savePassword()` already calls `parentFragmentManager.setFragmentResult(REQUEST_KEY, bundleOf(RESULT_PASSWORD_CREATED to true))`. If this is still broken, the issue may be timing — the `setFragmentResult` is called inside a LiveData observer in `savePassword()`, and the fragment may be dismissed before the result listener fires. The structured requirements do not address this timing concern.

3. **Camera (FR-3)**: The Activity Result API migration is already done (`cameraLauncher` uses `ActivityResultContracts.TakePicture()`). The actual remaining bug is in `file_paths.xml`: it uses `<external-path>` but the code uses `getExternalFilesDir()` which requires `<external-files-path>`. Additionally, the paths hardcode `com.nrs.nsnik.notes` and `com.nrs.nsnik.notes.debug` but the product flavors produce applicationIds `com.nrs.nsnik.notes.demo` and `com.nrs.nsnik.notes.full` — neither matches the `file_paths.xml` entries. The FileProvider authority in the manifest is `com.nrs.nsnik.notes.fileprovider` but with flavor suffixes the actual package becomes `com.nrs.nsnik.notes.demo` — the authority string in code must match the manifest declaration. The structured requirements do not capture the flavor-specific applicationId mismatch.

4. **Gallery (FR-4)**: The Activity Result API migration is already done (`galleryLauncher` uses `ActivityResultContracts.GetContent()`). The gallery launcher does not use FileProvider, so the `file_paths.xml` issue does not affect it. The `READ_EXTERNAL_STORAGE` permission check in `listeners()` may be unnecessary for `GetContent()` (which uses the system picker and grants URI permission automatically). On API 33+ `READ_EXTERNAL_STORAGE` is deprecated in favor of `READ_MEDIA_IMAGES`. The structured requirements mention this but don't specify the concrete fix.

5. **Reminder (FR-5)**: `FLAG_IMMUTABLE` is already applied. The notification channel is created correctly in `NotificationReceiver.buildNotification()`. The channel ID uses `R.string.notificationChannelReminderId` which resolves to `"notificationChannelReminderId"` — this matches between the Builder constructor and `createNotificationChannel()`. If notifications still don't appear, the issue may be: (a) `SCHEDULE_EXACT_ALARM` permission is declared but `setInexactRepeating` is used (not exact), (b) on API 31+ `setInexactRepeating` may be batched/delayed significantly, (c) the `NotificationReceiver` has `android:exported="false"` which is correct for PendingIntent-triggered broadcasts. The structured requirements do not analyze whether the alarm actually fires.

### Scope Boundaries Missing
- No explicit "out of scope" section. The raw input says "no wholesale rewrites" but the structured requirements don't define what changes are in vs out of scope (e.g., are the `notifyItemRemoved` bugs in `onItemRemoved()` in scope?).

### Acceptance Criteria Gaps
- AC-1 (checklist): Does not specify what happens to the cursor position after the new item is created.
- AC-2 (lock): Does not specify behavior when the user cancels the password dialog on first attempt.
- AC-3 (camera): Does not specify behavior when the user cancels the camera without taking a photo.
- AC-3 (camera): Does not specify which product flavor(s) must work (the FileProvider path issue is flavor-dependent).
- AC-5 (reminder): Does not specify tolerance for notification delivery time (exact vs inexact alarm).
- AC-5 (reminder): Does not specify behavior when the selected time has already passed today (code rolls to next day — is this correct?).

### Non-Atomic Requirements
- FR-3 and FR-4 are described as separate requirements but share the same root cause class (Activity Result API, FileProvider). The FileProvider fix for camera is a single change that doesn't affect gallery. These should remain separate but the shared dependency should be noted.

### Vague Qualifiers
- NFR "Camera and gallery operations must not crash under any normal usage path" — "normal usage path" is not defined.
- NFR "Notification delivery must be reliable" — "reliable" is not quantified. `setInexactRepeating` is inherently inexact.

### Missing Observability
- No requirement for logging or analytics when features fail (e.g., camera FileProvider exception, notification delivery failure).

### Missing State Transitions
- Lock feature: No state diagram for the password lifecycle (no password → creating → created → verifying → locked/unlocked). What happens if password creation fails mid-way?
- Reminder: No state for alarm lifecycle (set → pending → fired → notification shown). What if the alarm fires but notification posting fails?

## Inferred Requirements
1. **FR-6 through FR-13 (testing/build requirements)** — These are directly from the raw input, not inferred.
2. **Technical Notes about `<queries>` manifest element** — Inferred as potentially needed, but source code shows Activity Result API is already used, making `<queries>` unnecessary.
3. **NFR about API 30+ support** — Directly from raw input context about `resolveActivity` issues.
4. **Open Question about front/rear camera** — Inferred; not in raw input.
5. **Open Question about multiple image selection** — Inferred; not in raw input.

## Clarifying Questions

### Scope
1. The `notifyItemInserted`/`notifyItemRemoved` double-notification bug exists in `addCheckListItem()`, `addAudioToList()`, `addImageToList()`, and all three branches of `onItemRemoved()`. The raw requirements only mention the checklist Enter key. Should the same fix (removing redundant `notifyItem*` calls after `submitList`) be applied to all six occurrences, or only the checklist?
2. The lock feature's FragmentResult API callback and the camera/gallery Activity Result API launchers appear to already be implemented in the current source code. Are these features still broken in manual testing despite these code changes, or were these fixes applied in a previous run and just need verification?
3. The `file_paths.xml` uses `<external-path>` with hardcoded paths for `com.nrs.nsnik.notes` and `com.nrs.nsnik.notes.debug`, but the product flavors produce applicationIds `com.nrs.nsnik.notes.demo` and `com.nrs.nsnik.notes.full`. Should the FileProvider paths be updated to use `<external-files-path>` with a generic path (e.g., `path="Pictures"`) that works for all flavors?

### Technical
4. The `file_paths.xml` uses `<external-path>` (maps to `Environment.getExternalStorageDirectory()`) but the code calls `getExternalFilesDir(Environment.DIRECTORY_PICTURES)` (maps to app-specific external files directory). These are different filesystem locations. The fix should change `<external-path>` to `<external-files-path>` with `path="Pictures/"`. Can you confirm this is the intended storage location for captured photos?
5. `ReminderUtil.setNotification()` uses `AlarmManager.setInexactRepeating()` which on API 31+ may be significantly delayed by battery optimization. Should this be changed to `setExactAndAllowWhileIdle()` for more reliable delivery, or is inexact acceptable? Note: `SCHEDULE_EXACT_ALARM` is already declared in the manifest.
6. On API 33+, `READ_EXTERNAL_STORAGE` is deprecated and `READ_MEDIA_IMAGES` should be used instead. The gallery launcher uses `ActivityResultContracts.GetContent()` which doesn't require any storage permission (the system picker grants URI access). Should the `READ_EXTERNAL_STORAGE` permission check be removed for the gallery path, or should it be updated to `READ_MEDIA_IMAGES` for API 33+?
7. The `WRITE_EXTERNAL_STORAGE` permission check for the camera path is a no-op on API 29+ (scoped storage). Since `minSdk` is 23 and `targetSdk` is 34, should this permission check be conditionally skipped on API 29+, or removed entirely since `getExternalFilesDir()` doesn't require it?

### Acceptance Criteria
8. AC-2 (lock): When the user taps lock for the first time and the password dialog appears, what should happen if the user cancels the dialog without creating a password? Should the lock state remain unchanged (unlocked)?
9. AC-5 (reminder): The code currently rolls the alarm to the next day if the selected time has already passed today. Is this the intended behavior, or should the user be warned that the time has passed?
10. AC-5 (reminder): What is the acceptable delivery tolerance for reminder notifications? `setInexactRepeating` can delay notifications by up to several minutes. Is this acceptable, or should exact alarms be used?
