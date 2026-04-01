# Validated Requirements: Android Notes App — Espresso UI Tests & Bug Fixes

## Validation Status
- Cross-reference check: PASS
- Completeness check: PARTIAL (all bugs confirmed with root causes; implementation details deferred to implementing agent's judgment)

## Summary
Write comprehensive Espresso UI tests covering every user-facing flow in the Android Notes app, and fix five confirmed broken features: checklist duplicate items, broken image/audio attachments, undelivered reminder notifications, non-functional note locking, and misbehaving navigation. All root causes have been confirmed via source code inspection. All tests must pass on an emulator via `./gradlew connectedDemoDebugAndroidTest`.

## Detailed Requirements

### Bug Fixes

- FR-001: Fix checklist feature so that adding a new checklist item does not create duplicate entries. **Priority: MUST-HAVE**
  - Root cause: `NewNoteFragment.addCheckListItem()` mutates `mCheckList` in-place and passes the same list reference to `mCheckListAdapter.submitList()`. `ListAdapter.submitList()` compares by reference and no-ops when old == new. `CheckListDiffUtil.areItemsTheSame()` and `areContentsTheSame()` both unconditionally return `false`, compounding the issue.
  - Fix guidance: Submit a defensive copy via `submitList(mCheckList.toList())`. Apply the same fix in `onItemRemoved()` and `setNote()`. Fix `CheckListDiffUtil` to use meaningful identity comparison.

- FR-002: Fix the same `submitList()` mutable-list-reference bug in `ImageAdapter` and `AudioListAdapter`. **Priority: MUST-HAVE**
  - Root cause: `mImageAdapter.submitList(mImagesLocations)` and `mAudioListAdapter.submitList(mAudioLocations)` suffer the identical same-reference no-op as the checklist adapter.
  - Fix guidance: Submit defensive copies: `submitList(mImagesLocations.toList())` and `submitList(mAudioLocations.toList())`.

- FR-003: Fix image and audio attachment flows so that selecting or recording media correctly attaches to a note. **Priority: MUST-HAVE**
  - Root cause: `NewNoteFragment` uses deprecated `startActivityForResult()` / `onActivityResult()`. With `FragmentContainerView` + Navigation 2.8.5, the fragment's `onActivityResult` is not reliably dispatched. The result is never delivered.
  - Fix guidance: Migrate to Activity Result API (`registerForActivityResult` with `ActivityResultContracts.PickVisualMedia()` or `GetContent` for images, `ActivityResultContracts.TakePicture()` for camera). Verify audio recording separately — it uses a custom dialog, not an intent, and may work independently.

- FR-004: Fix reminder notifications so that scheduled reminders are actually delivered. **Priority: MUST-HAVE**
  - Prior fixes already applied: `FLAG_IMMUTABLE` on `PendingIntent` ✓, `NotificationChannel` creation ✓.
  - Remaining issue 1: `ReminderUtil.setReminder()` does not check if the selected time is in the past. If a user picks 9:00 AM when it's 10:00 AM, the alarm fires immediately or is ignored.
  - Remaining issue 2: On API 31+ (Android 12), `SCHEDULE_EXACT_ALARM` or `USE_EXACT_ALARM` permission is needed for reliable alarm delivery with `RTC_WAKEUP`. Check `AndroidManifest.xml` and add if missing.
  - Remaining issue 3: `NotificationCompat.Builder` constructor uses a different channel ID string resource than the one set via `setChannelId()`. Use the same `channelId` variable in the constructor for consistency.
  - Fix guidance: Add past-time validation (roll to next day or reject). Add `SCHEDULE_EXACT_ALARM` permission to manifest. Align channel ID in builder constructor.

- FR-005: Fix note locking so that users can password-protect individual notes and be prompted to unlock them. **Priority: MUST-HAVE**
  - Root cause: `PasswordUtil.checkLock()` returns `false` when no password exists (opens creation dialog) and `true` when a password exists. In `NewNoteFragment.setLock()`, the lock toggle only executes when `checkLock()` returns `true` — so on first use, the lock state is never toggled. After the user creates a password in `PasswordDialogFragment`, no `PasswordEvent` is posted via EventBus (it's only posted in the "enter existing password" flow). The calling fragment never learns the password was created. Same issue in `ListFragment.setLock()`.
  - Fix guidance: After `savePassword()` succeeds and the dialog dismisses, post a `PasswordEvent` (or use FragmentResult API) so the calling fragment can complete the lock toggle. Ensure both `NewNoteFragment.setLock()` and `ListFragment.setLock()` handle the async password-creation flow.

- FR-006: Fix navigation so that it behaves correctly across all user flows. **Priority: MUST-HAVE**
  - Root cause 1: Drawer navigation in `MainActivity.setNavigationItemSelectedListener` navigates to Settings/About via `navController.navigate()` without `popUpTo` or `launchSingleTop`. Repeatedly tapping Settings stacks multiple fragments.
  - Root cause 2: `NewNoteFragment.onOptionsItemSelected()` uses `activity?.findNavController(R.id.mainNavHost)` — the Activity extension which is problematic with `FragmentContainerView`. May cause `IllegalStateException`.
  - Root cause 3: `onFullScreenEvent` uses `binding.newNoteImageList.findNavController()` — fragile if view hierarchy changes.
  - Fix guidance: Add `popUpTo` and `launchSingleTop = true` to drawer navigation actions. Migrate `activity?.findNavController()` calls to `supportFragmentManager.findFragmentById()` pattern (already used in `MainActivity`). Consider stabilizing the `onFullScreenEvent` navigation.

### UI Tests

- FR-007: Write an Espresso UI test for the App Launch and Navigation flow. **Priority: MUST-HAVE**
- FR-008: Write an Espresso UI test for Note CRUD (create, read, update, delete). **Priority: MUST-HAVE**
- FR-009: Write an Espresso UI test for the Checklist flow. **Priority: MUST-HAVE**
- FR-010: Write an Espresso UI test for Pin/Star functionality. **Priority: SHOULD-HAVE**
- FR-011: Write an Espresso UI test for the Lock (password-protect) flow. **Priority: MUST-HAVE**
- FR-012: Write an Espresso UI test for Folder Operations. **Priority: SHOULD-HAVE**
- FR-013: Write an Espresso UI test for Search. **Priority: SHOULD-HAVE**
- FR-014: Write an Espresso UI test for the Color Picker. **Priority: SHOULD-HAVE**
- FR-015: Write an Espresso UI test for Reminders. **Priority: MUST-HAVE**
- FR-016: Write an Espresso UI test for Image Attachment. **Priority: MUST-HAVE**
- FR-017: Write an Espresso UI test for Audio Recording. **Priority: MUST-HAVE**
- FR-018: Write an Espresso UI test for Settings. **Priority: SHOULD-HAVE**
- FR-019: Write an Espresso UI test for the About screen. **Priority: COULD-HAVE**
- FR-020: Write an Espresso UI test for Edge Cases. **Priority: COULD-HAVE**
- FR-021: All five bug fixes must have corresponding test coverage in the relevant flow tests. **Priority: MUST-HAVE**

## Acceptance Criteria

1. AC-001: Given a user opens a note with a checklist, when they tap the add-item button, then exactly one new item is appended and no duplicates appear.
2. AC-002: Given a user opens a note and taps the image attachment option, when they select an image from the gallery, then the image is attached to the note and visible in the note editor.
3. AC-003: Given a user opens a note and taps the audio attachment option, when they record or select audio, then the audio clip is attached to the note and playable from the note editor.
4. AC-004: Given a user sets a reminder on a note for a future time, when that time arrives, then a notification is delivered to the device.
5. AC-005: Given a user chooses to lock a note with a password, when they set the password and close the note, then reopening the note prompts for the password before displaying content. This must work on the first attempt (not require tapping lock twice).
6. AC-006: Given a user navigates between screens (note list → note editor → back, drawer → settings → back, drawer → about → back), when they use back navigation or drawer navigation, then the app navigates to the expected screen without stacking duplicate fragments, incorrect state, or crashes.
7. AC-007: Given the full Espresso test suite, when `./gradlew connectedDemoDebugAndroidTest` is executed on the emulator, then all tests pass.
8. AC-008: Given each test class, when any single test method runs, then it is independent — it sets up its own state in `@Before` and cleans up in `@After`.
9. AC-009: Given the test source tree, when inspecting `app/src/androidTest/`, then every test file follows the naming convention `<FeatureName>FlowTest.kt`.
10. AC-010: Given the 15 identified user flows, when the test suite is reviewed, then each flow has at least one corresponding test class with meaningful assertions.
11. AC-011: Given the `submitList()` fix is applied to the checklist adapter, then the same fix is also applied to the image and audio adapters — no mutable-list-reference bugs remain.
12. AC-012: Given a user picks a reminder time earlier than the current time on the same day, then the app either rolls the reminder to the next day or rejects the selection with a user-visible message.

## Constraints

- Espresso UI tests must reside in `app/src/androidTest/`
- Unit tests (if any) must reside in `app/src/test/`
- Tests must use the existing Espresso libraries already in the project dependencies
- Tests requiring external intent interception must use `espresso-intents`
- Tests requiring runtime permissions must use `GrantPermissionRule`
- Each test must be independent with proper `@Before` setup and `@After` teardown
- Test file naming convention: `<FeatureName>FlowTest.kt`
- Verification command: `./gradlew connectedDemoDebugAndroidTest`
- Build variant for tests: `demoDebug`
- Bug fixes must not introduce regressions in unrelated features
- Activity Result API migration (FR-003) must use `registerForActivityResult` — do not attempt to fix the deprecated `onActivityResult` path

## Dependencies

- Android emulator must be running and accessible for connected tests
- Existing Espresso and espresso-intents libraries in the project's Gradle dependencies
- Existing app source code: `ListAdapter`, `NewNoteFragment`, `ListFragment`, `PasswordUtil`, `PasswordDialogFragment`, `ReminderUtil`, `NotificationReceiver`, `MainActivity`, navigation components
- Android notification channel APIs (API 26+)
- PendingIntent APIs with correct mutability flags (API 31+)
- `SCHEDULE_EXACT_ALARM` permission (API 31+) for reliable alarm delivery
- Activity Result API (`androidx.activity:1.2.0+` / `androidx.fragment:1.3.0+`)

## Non-Functional Requirements

- NFR-001: All flow tests plus bug-fix validations must complete within a single `connectedDemoDebugAndroidTest` run without flakiness. **Priority: MUST-HAVE**
- NFR-002: Tests must be deterministic — no dependence on external network, timing, or device state beyond what `@Before` establishes. **Priority: MUST-HAVE**

## Technical Notes

### Bug #1 — Checklist (FR-001, FR-002)
- `NewNoteFragment.addCheckListItem()` (~line 355): mutates `mCheckList` in-place, passes same reference to `submitList()`.
- `CheckListDiffUtil`: `areItemsTheSame()` and `areContentsTheSame()` both return `false` unconditionally — defeats ListAdapter diffing.
- Same pattern in image/audio adapters with `mImagesLocations` and `mAudioLocations`.
- Fix: `submitList(list.toList())` everywhere. Fix DiffUtil to use meaningful comparison.

### Bug #2 — Attachments (FR-003)
- `NewNoteFragment` lines 410, 430, 455–462: deprecated `startActivityForResult()` / `onActivityResult()`.
- `FragmentContainerView` + Navigation 2.8.5 breaks `onActivityResult` dispatch to fragments.
- Migrate to `registerForActivityResult(ActivityResultContracts.PickVisualMedia())` for gallery, `ActivityResultContracts.TakePicture()` for camera.
- Audio recording uses a custom dialog (not an intent) — verify independently.

### Bug #3 — Reminders (FR-004)
- `FLAG_IMMUTABLE` and `NotificationChannel` already fixed in prior work.
- `ReminderUtil.setReminder()`: no past-time check on `calendar.timeInMillis`.
- API 31+: `SCHEDULE_EXACT_ALARM` permission needed for `RTC_WAKEUP` alarms.
- `NotificationCompat.Builder` constructor channel ID vs `setChannelId()` mismatch — use same variable.

### Bug #4 — Lock (FR-005)
- `PasswordUtil.checkLock()`: returns `false` (no password) → opens creation dialog → lock toggle skipped.
- `PasswordDialogFragment.savePassword()`: does NOT post `PasswordEvent` via EventBus after creation.
- `PasswordEvent` only posted in `isPasswordCorrect()` (existing password flow).
- Result: user must tap lock twice. Fix: post event after `savePassword()`, or use FragmentResult API.
- Affects both `NewNoteFragment.setLock()` (~line 243) and `ListFragment.setLock()` (~line 310).

### Bug #5 — Navigation (FR-006)
- Drawer navigation: `navController.navigate(R.id.navItemSettings)` without `popUpTo` or `launchSingleTop` → fragment stacking.
- `NewNoteFragment.onOptionsItemSelected()`: `activity?.findNavController(R.id.mainNavHost)` — Activity extension problematic with `FragmentContainerView`.
- `onFullScreenEvent` (~line 510): `binding.newNoteImageList.findNavController()` — fragile RecyclerView-based navigation.
- Fix: add `popUpTo`/`launchSingleTop` to drawer actions, migrate to `supportFragmentManager.findFragmentById()` pattern.

## Scope Exclusions

- Social login or multi-user authentication (this is a single-user app)
- Fixing `verifyAndSave()` logic bug (empty notes can be saved) — not one of the 5 confirmed bugs
- Adding password recovery/reset mechanism for the Lock feature
- Camera capture support for image attachments (unless already implemented)
- Network-dependent features or tests

## Deferred Items

- Edge case test scenarios (FR-020): specific scenarios left to implementing agent's judgment
- Pin vs Star distinction: whether these are two separate features or one — implementing agent to determine from source code
- Folder operations scope (create, rename, delete, move): implementing agent to determine from source code
- Settings coverage scope: implementing agent to determine from source code
- Password complexity requirements: implementing agent to determine from source code
- Audio test strategy (real microphone vs mock): implementing agent to determine based on emulator capabilities
