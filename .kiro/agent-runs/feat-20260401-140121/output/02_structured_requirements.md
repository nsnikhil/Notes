# Feature: Fix Broken Features in Android Notes App

## Summary
Fix five remaining broken features in the Android Notes app: checklist item double-add on Enter, note lock not toggling on first attempt, camera tool not responding or crashing, gallery attachment tool not responding or crashing, and reminder notifications never appearing. Each fix must include unit tests and Espresso tests, and all builds and test suites must pass with zero failures.

## Detailed Requirements

### Functional Requirements

1. **Checklist Enter Key**: Pressing Enter in a checklist item MUST create exactly one new checklist item, not two. The root cause is suspected to be a double-add triggered by `submitList` combined with `notifyItemInserted`, or RxBinding `editorActions` firing twice.
2. **Note Lock Toggle**: Locking a note MUST work on the first attempt. Currently the password dialog appears but the lock state is not toggled after the user creates a password for the first time.
3. **Camera Tool**: Tapping the camera tool MUST open the device camera and allow the user to capture and attach a photo. Currently it does nothing or crashes due to FileProvider authority misconfiguration, `resolveActivity` returning null on API 30+, or Activity Result API issues.
4. **Gallery Attachment Tool**: Tapping the gallery tool MUST open the device gallery/photo picker and allow the user to select and attach an image. Currently it does nothing or crashes due to the same class of issues as the camera tool (FileProvider authority, `resolveActivity` null on API 30+, Activity Result API).
5. **Reminder Notification**: Setting a reminder on a note MUST result in a notification being delivered at the scheduled time. Currently the alarm is scheduled but the notification never appears, suspected to be caused by notification channel misconfiguration, PendingIntent flags, or NotificationReceiver issues.

### Testing Requirements

6. For each of the five fixes, a unit test MUST be written or updated to verify the fix.
7. For each of the five fixes, an Espresso (instrumented) test MUST be written or updated to verify the fix end-to-end.
8. Both test commands (`connectedDemoDebugAndroidTest` and `testDemoDebugUnitTest`) MUST be run after changes.

### Build Requirements

9. `connectedDemoDebugAndroidTest` MUST complete with 0 failures.
10. `testDemoDebugUnitTest` MUST complete with 0 failures.
11. `assembleDebug` MUST succeed.
12. Both product flavors MUST build successfully.
13. No existing tests may be removed.

## Acceptance Criteria

1. Given a checklist note is open, When the user presses Enter in a checklist item, Then exactly one new empty checklist item is created below the current item.
2. Given a note is unlocked and no password has been set, When the user taps the lock option and creates a password, Then the note is locked immediately on the first attempt without requiring a second interaction.
3. Given a note is open, When the user taps the camera tool, Then the device camera opens without crashing, and after capturing a photo, the image is attached to the note.
4. Given a note is open, When the user taps the gallery tool, Then the device gallery/photo picker opens without crashing, and after selecting an image, the image is attached to the note.
5. Given a note has a reminder set for a future time, When that time arrives, Then a notification is delivered to the user.
6. Given all fixes are applied, When `connectedDemoDebugAndroidTest` is run, Then 0 test failures are reported.
7. Given all fixes are applied, When `testDemoDebugUnitTest` is run, Then 0 test failures are reported.
8. Given all fixes are applied, When `assembleDebug` is run, Then the build succeeds.
9. Given all fixes are applied, When both product flavors are built, Then both builds succeed.
10. Given all fixes are applied, When the test suite is inspected, Then no pre-existing tests have been removed.

## Constraints

- No existing tests may be removed or disabled.
- Both product flavors must remain buildable.
- Fixes must target the existing codebase — no wholesale rewrites implied.
- Must support API 30+ (Android 11+) for camera and gallery intents given the `resolveActivity` and package visibility constraints.

## Dependencies

- Android device or emulator for running `connectedDemoDebugAndroidTest` (Espresso instrumented tests).
- FileProvider configuration in the app manifest (existing, needs to be correct).
- NotificationChannel registration (existing Android notification infrastructure).
- AlarmManager for reminder scheduling (existing).
- RxBinding library (existing, used for checklist editor actions).

## Non-Functional Requirements

- All five features must work reliably on API 30+ devices (Android 11 and above).
- Camera and gallery operations must not crash under any normal usage path.
- Notification delivery must be reliable — the notification must appear at the scheduled time (subject to OS-level battery optimization constraints outside app control).

## Technical Notes

- Checklist double-add: The raw requirements suggest the bug is in the interaction between `submitList` (DiffUtil/ListAdapter) and manual `notifyItemInserted`, or in RxBinding `editorActions` emitting duplicate events. Only one insertion mechanism should fire per Enter press.
- Lock first-attempt failure: The password dialog flow likely has an asynchronous callback that does not propagate the lock-state toggle when the password is first created (as opposed to when an existing password is verified).
- Camera/Gallery crashes: On API 30+ `resolveActivity` returns null due to package visibility restrictions. The app may need `<queries>` declarations in the manifest or should avoid the `resolveActivity` check entirely. FileProvider authority strings must match between manifest and code. Migration to the Activity Result API (`registerForActivityResult`) may be needed if the app still uses deprecated `startActivityForResult`.
- Reminder notification: Common causes include missing or misconfigured `NotificationChannel` (required on API 26+), `PendingIntent` missing `FLAG_IMMUTABLE` or `FLAG_MUTABLE` (required on API 31+), or `NotificationReceiver` not declared/exported correctly in the manifest.

## Open Questions

1. What is the minimum supported Android API level for this app? (API 30+ is implied by the camera/gallery issues, but the actual `minSdkVersion` is not stated.)
2. What are the two product flavors referenced by "both flavors build"? (Likely "demo" and another — the exact names are not fully specified beyond "demo".)
3. Is there a specific password storage mechanism (e.g., encrypted SharedPreferences, Room database) that the lock feature uses? This may affect how the first-attempt bug is diagnosed.
4. Should the camera feature support both front and rear cameras, or is rear-only sufficient?
5. Should the gallery picker support selecting multiple images, or only a single image?
6. Are there any OS-level battery optimization or Doze mode considerations that should be accounted for in the reminder notification fix, or is it acceptable to document those as known OS-level limitations?
7. What specific notification channel ID and importance level should be used for reminder notifications?
8. Is there a CI/CD environment where these tests run, or are they run locally only? This may affect emulator configuration for instrumented tests.
9. Are there any specific user personas or permission levels involved (e.g., free vs. premium users), or do all features apply uniformly to all users?
