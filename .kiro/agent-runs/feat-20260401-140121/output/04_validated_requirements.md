# Validated Requirements: Fix Broken Features in Android Notes App

## Validation Status
- Cross-reference check: FAIL — structured requirements hypothesized root causes that were already fixed in prior modernization runs (Activity Result API, FragmentResult API, FLAG_IMMUTABLE). Actual root causes differ and are now corrected below.
- Completeness check: FAIL — additional occurrences of the double-notification bug were missing; flavor-specific FileProvider path mismatches were not captured; gallery permission issue on API 33+ was underspecified. All gaps resolved in this document.

## Summary
Fix five broken features in the Android Notes app. Prior modernization runs already applied Activity Result API, FragmentResult API, and FLAG_IMMUTABLE. The actual remaining bugs are: (1) `submitList()` + `notifyItem*()` double-notification in the checklist adapter and five other methods, (2) LiveData observer timing in the FragmentResult-based lock flow, (3) `file_paths.xml` using `<external-path>` instead of `<external-files-path>` with hardcoded paths that don't match flavor applicationIds, (4) broken `READ_EXTERNAL_STORAGE` permission check on API 33+ for the gallery path that already uses `GetContent()`, and (5) `setInexactRepeating` batching/delay or alarm not firing for reminders. Each fix must include unit and Espresso tests, and all builds/test suites must pass with zero failures.

## Detailed Requirements

### Functional Requirements

- **FR-001** [MUST-HAVE]: Remove redundant `notifyItemInserted()` call after `submitList()` in `addCheckListItem()`. The `submitList()` call triggers DiffUtil which already handles the insertion; the subsequent `notifyItemInserted()` causes a duplicate item. Fix must result in exactly one new checklist item per Enter press.

- **FR-002** [MUST-HAVE]: Apply the same `submitList()` + `notifyItem*()` double-notification fix to all other affected methods: `addAudioToList()`, `addImageToList()`, and all three branches of `onItemRemoved()` (which call `notifyItemRemoved` after `submitList`). Remove the redundant `notifyItem*` call in each case.

- **FR-003** [MUST-HAVE]: Fix the note lock first-attempt failure. The FragmentResult API wiring is already in place (`setFragmentResult` in `PasswordDialogFragment.savePassword()`, listener in `NewNoteFragment.onViewCreated()`). The actual issue is LiveData observer timing — `setFragmentResult` is called inside a LiveData observer in `savePassword()`, and the fragment may be dismissed before the result listener fires. Fix the timing so the lock state toggles on the first attempt.

- **FR-004** [MUST-HAVE]: Fix `file_paths.xml` for the camera feature. Change `<external-path>` to `<external-files-path>` to match the code's use of `getExternalFilesDir(Environment.DIRECTORY_PICTURES)`. Use a generic path (e.g., `path="Pictures/"`) that works regardless of flavor applicationId, replacing the hardcoded `com.nrs.nsnik.notes` and `com.nrs.nsnik.notes.debug` paths that don't match the actual flavor applicationIds (`com.nrs.nsnik.notes.demo` and `com.nrs.nsnik.notes.full`).

- **FR-005** [MUST-HAVE]: Ensure the FileProvider authority string in code matches the manifest declaration across all product flavors. Verify that the authority used when creating the camera image URI matches `${applicationId}.fileprovider` as declared in the manifest.

- **FR-006** [MUST-HAVE]: Fix the gallery attachment tool. Remove or bypass the `READ_EXTERNAL_STORAGE` permission check for the gallery path. `ActivityResultContracts.GetContent()` uses the system picker which grants URI access automatically — no storage permission is needed. On API 33+, `READ_EXTERNAL_STORAGE` is deprecated and the check fails, preventing the gallery from opening.

- **FR-007** [MUST-HAVE]: Fix reminder notifications so they are delivered at the scheduled time. `FLAG_IMMUTABLE` is already applied. Investigate whether `setInexactRepeating` batching on API 31+ prevents timely delivery and whether the alarm actually fires. If the alarm fires but notification doesn't appear, investigate notification posting. If the alarm doesn't fire, consider switching to `setExactAndAllowWhileIdle()` (the `SCHEDULE_EXACT_ALARM` permission is already declared in the manifest).

### Testing Requirements

- **FR-008** [MUST-HAVE]: Write or update a unit test for the checklist double-add fix (FR-001) verifying that a single Enter press produces exactly one new item.

- **FR-009** [MUST-HAVE]: Write or update a unit test for the additional `notifyItem*` removals (FR-002).

- **FR-010** [MUST-HAVE]: Write or update a unit test for the lock first-attempt fix (FR-003) verifying the lock state toggles on first password creation.

- **FR-011** [MUST-HAVE]: Write or update a unit test for the camera FileProvider fix (FR-004/FR-005) verifying correct URI generation across flavors.

- **FR-012** [MUST-HAVE]: Write or update a unit test for the gallery permission fix (FR-006) verifying the gallery opens without a storage permission check.

- **FR-013** [MUST-HAVE]: Write or update a unit test for the reminder fix (FR-007) verifying alarm scheduling and notification delivery.

- **FR-014** [MUST-HAVE]: Write or update Espresso tests for each of the five user-facing features (checklist, lock, camera, gallery, reminder).

### Build Requirements

- **FR-015** [MUST-HAVE]: `connectedDemoDebugAndroidTest` must complete with 0 failures.
- **FR-016** [MUST-HAVE]: `testDemoDebugUnitTest` must complete with 0 failures.
- **FR-017** [MUST-HAVE]: `assembleDebug` must succeed.
- **FR-018** [MUST-HAVE]: Both product flavors (`demo` and `full`) must build successfully.
- **FR-019** [MUST-HAVE]: No existing tests may be removed or disabled.

## Acceptance Criteria

1. Given a checklist note is open, when the user presses Enter in a checklist item, then exactly one new empty checklist item is created below the current item (no duplicates).
2. Given `addAudioToList()`, `addImageToList()`, or `onItemRemoved()` is invoked, then no duplicate insertion/removal notification occurs — only `submitList()` drives the adapter update.
3. Given a note is unlocked and no password has been set, when the user taps the lock option and creates a password, then the note is locked immediately on the first attempt without requiring a second interaction.
4. Given a note is open on any product flavor, when the user taps the camera tool, then the device camera opens without crashing, and after capturing a photo, the image is attached to the note.
5. Given a note is open on a device running API 33+, when the user taps the gallery tool, then the gallery/photo picker opens without a storage permission prompt, and after selecting an image, the image is attached to the note.
6. Given a note has a reminder set for a future time, when that time arrives, then a notification is delivered to the user.
7. `connectedDemoDebugAndroidTest` reports 0 failures.
8. `testDemoDebugUnitTest` reports 0 failures.
9. `assembleDebug` succeeds.
10. Both product flavors build successfully.
11. No pre-existing tests have been removed or disabled.

## Constraints

- No existing tests may be removed or disabled.
- Both product flavors (`demo` and `full`) must remain buildable.
- Fixes must target the existing codebase — no wholesale rewrites.
- Must support API 30+ (Android 11+); `minSdk` is 23, `targetSdk` is 34.
- Prior modernization changes (Activity Result API, FragmentResult API, FLAG_IMMUTABLE) must not be reverted.
- `file_paths.xml` fix must work for all current and future flavor applicationIds (use generic paths, not hardcoded package names).

## Dependencies

- Android device or emulator for running `connectedDemoDebugAndroidTest`.
- FileProvider configuration in `file_paths.xml` and `AndroidManifest.xml`.
- `AlarmManager` for reminder scheduling (existing).
- `SCHEDULE_EXACT_ALARM` permission (already declared in manifest).
- RxBinding library (existing, used for checklist editor actions).
- DiffUtil / ListAdapter (existing, used by checklist adapter).

## Non-Functional Requirements

- **NFR-001** [MUST-HAVE]: All five features must work on API 23 through API 34+ devices.
- **NFR-002** [MUST-HAVE]: Camera and gallery operations must not crash on any supported API level when the user completes or cancels the operation.
- **NFR-003** [SHOULD-HAVE]: Reminder notifications should be delivered within a reasonable window of the scheduled time. `setInexactRepeating` may batch alarms; if exact delivery is required, `setExactAndAllowWhileIdle()` should be used. OS-level battery optimization (Doze mode) is outside app control and documented as a known limitation.
- **NFR-004** [SHOULD-HAVE]: No unnecessary permission prompts should be shown to the user (e.g., no `READ_EXTERNAL_STORAGE` prompt for gallery on API 33+).

## Technical Notes

- **Checklist (FR-001, FR-002)**: Remove all `notifyItemInserted()` / `notifyItemRemoved()` calls that follow `submitList()`. `submitList()` with a new list copy triggers DiffUtil which handles all adapter notifications. The redundant manual notifications cause double-add/double-remove. Affected methods: `addCheckListItem()`, `addAudioToList()`, `addImageToList()`, and three branches in `onItemRemoved()`.
- **Lock (FR-003)**: The FragmentResult API is already wired. The issue is that `setFragmentResult` is called inside a LiveData observer in `savePassword()`. Ensure the result is set before or independently of fragment dismissal so the listener in `NewNoteFragment` receives it.
- **Camera (FR-004, FR-005)**: `cameraLauncher` already uses `ActivityResultContracts.TakePicture()` — no Activity Result API migration needed. The fix is purely in `file_paths.xml`: change `<external-path>` to `<external-files-path>` with `path="Pictures/"`. Remove hardcoded flavor-specific paths. Ensure the FileProvider authority `${applicationId}.fileprovider` is used consistently in code and manifest.
- **Gallery (FR-006)**: `galleryLauncher` already uses `ActivityResultContracts.GetContent()` — no migration needed. `GetContent()` uses the system picker which grants URI permission automatically. Remove the `READ_EXTERNAL_STORAGE` permission check gate; it's unnecessary and broken on API 33+ where the permission is deprecated.
- **Reminder (FR-007)**: `FLAG_IMMUTABLE` is already applied. Notification channel is created correctly. Investigate whether `setInexactRepeating` actually fires the alarm on time. If not, switch to `setExactAndAllowWhileIdle()` since `SCHEDULE_EXACT_ALARM` is already declared. Verify `NotificationReceiver` receives the broadcast and posts the notification.

## Scope Exclusions

- Activity Result API migration (already done in prior run).
- FragmentResult API wiring (already done in prior run).
- `FLAG_IMMUTABLE` application to PendingIntents (already done in prior run).
- `<queries>` manifest element for `resolveActivity()` (not needed since Activity Result API is used).
- Social login, two-factor authentication, or any authentication features.
- Multi-image selection from gallery.
- Front/rear camera selection UI.
- Logging or analytics instrumentation for feature failures.
- `WRITE_EXTERNAL_STORAGE` permission changes (no-op on API 29+ with scoped storage; `getExternalFilesDir()` doesn't require it).

## Deferred Items

- Acceptable delivery tolerance for reminder notifications (exact vs. inexact alarm) — to be determined during implementation based on whether `setInexactRepeating` is the actual cause of missed notifications.
- Behavior when user cancels the password dialog on first lock attempt — assumed to leave note unlocked (no state change).
- Behavior when reminder time has already passed today — existing code rolls to next day; assumed correct.
- Cursor position after new checklist item creation — not specified; preserve existing behavior.
