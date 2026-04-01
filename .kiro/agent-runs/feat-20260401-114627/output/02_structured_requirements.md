# Feature: Android Notes App — Espresso UI Tests & Bug Fixes

## Summary
Write comprehensive Espresso UI tests covering every user-facing flow in the Android Notes app, and fix five confirmed broken features: checklist duplicate items, broken image/audio attachments, undelivered reminder notifications, non-functional note locking, and misbehaving navigation. All tests must pass on an emulator via `./gradlew connectedDemoDebugAndroidTest`.

## Detailed Requirements

### Bug Fixes

1. Fix checklist feature so that adding a new checklist item does not create duplicate entries (root cause: ListAdapter combined with mutable list mutation)
2. Fix image and audio attachment flows so that selecting or recording media correctly attaches to a note (root cause: broken onActivityResult handling)
3. Fix reminder notifications so that scheduled reminders are actually delivered to the user (root cause: missing notification channel creation and incorrect PendingIntent construction)
4. Fix note locking so that users can password-protect individual notes and be prompted to unlock them (root cause: issues in PasswordUtil and/or PasswordDialogFragment)
5. Fix navigation so that it behaves correctly across all user flows (specific misbehavior scenarios not fully detailed — see Open Questions)

### UI Tests

6. Write an Espresso UI test for the App Launch and Navigation flow
7. Write an Espresso UI test for Note CRUD (create, read, update, delete)
8. Write an Espresso UI test for the Checklist flow
9. Write an Espresso UI test for Pin/Star functionality
10. Write an Espresso UI test for the Lock (password-protect) flow
11. Write an Espresso UI test for Folder Operations
12. Write an Espresso UI test for Search
13. Write an Espresso UI test for the Color Picker
14. Write an Espresso UI test for Reminders
15. Write an Espresso UI test for Image Attachment
16. Write an Espresso UI test for Audio Recording
17. Write an Espresso UI test for Settings
18. Write an Espresso UI test for the About screen
19. Write an Espresso UI test for Edge Cases
20. All five bug fixes must have corresponding test coverage in the relevant flow tests

## Acceptance Criteria

1. Given a user opens a note with a checklist, When they tap the add-item button, Then exactly one new item is appended and no duplicates appear
2. Given a user opens a note and taps the image attachment option, When they select an image from the gallery, Then the image is attached to the note and visible in the note editor
3. Given a user opens a note and taps the audio attachment option, When they record or select audio, Then the audio clip is attached to the note and playable from the note editor
4. Given a user sets a reminder on a note for a future time, When that time arrives, Then a notification is delivered to the device
5. Given a user chooses to lock a note with a password, When they set the password and close the note, Then reopening the note prompts for the password before displaying content
6. Given a user navigates between screens (e.g., note list → note editor → back), When they use back navigation or drawer navigation, Then the app navigates to the expected screen without incorrect state or crashes
7. Given the full Espresso test suite, When `./gradlew connectedDemoDebugAndroidTest` is executed on the emulator, Then all tests pass
8. Given each test class, When any single test method runs, Then it is independent — it sets up its own state in `@Before` and cleans up in `@After`
9. Given the test source tree, When inspecting `app/src/androidTest/`, Then every test file follows the naming convention `<FeatureName>FlowTest.kt`
10. Given the 15 identified user flows, When the test suite is reviewed, Then each flow has at least one corresponding test class with meaningful assertions

## Constraints

- Espresso UI tests must reside in `app/src/androidTest/`
- Unit tests (if any) must reside in `app/src/test/`
- Tests must use the existing Espresso libraries already in the project dependencies
- Tests requiring external intent interception must use `espresso-intents`
- Tests requiring runtime permissions must use `GrantPermissionRule`
- Each test must be independent with proper `@Before` setup and `@After` teardown
- Test file naming convention: `<FeatureName>FlowTest.kt`
- Verification command: `./gradlew connectedDemoDebugAndroidTest`
- Bug fixes must not introduce regressions in unrelated features

## Dependencies

- Android emulator must be running and accessible for connected tests
- Existing Espresso and espresso-intents libraries in the project's Gradle dependencies
- Existing app source code (ListAdapter, onActivityResult, notification APIs, PasswordUtil, PasswordDialogFragment, navigation components)
- Android notification channel APIs (API 26+)
- PendingIntent APIs with correct mutability flags (API 31+)

## Non-Functional Requirements

- All 15 flow tests plus bug-fix validations must complete within a single `connectedDemoDebugAndroidTest` run without flakiness
- Tests must be deterministic — no dependence on external network, timing, or device state beyond what `@Before` establishes

## Technical Notes

- Checklist bug is traced to ListAdapter receiving a mutated mutable list rather than a new list instance — fix likely involves submitting a copy to `ListAdapter.submitList()`
- Attachment bug is traced to a broken `onActivityResult` flow — may need migration to Activity Result API or correction of request codes and data extraction
- Reminder bug involves two issues: a missing `NotificationChannel` creation (required on API 26+) and an incorrectly constructed `PendingIntent` (likely missing `FLAG_IMMUTABLE` or `FLAG_UPDATE_CURRENT`)
- Lock bug is traced to `PasswordUtil` and/or `PasswordDialogFragment` — specifics of the failure not detailed
- Navigation bug specifics ("certain flows") are not detailed
- Build variant for tests: `demoDebug`
- Test runner command: `./gradlew connectedDemoDebugAndroidTest`

## Open Questions

1. What specific navigation flows misbehave? (Bug #5 says "misbehaves in certain flows" but does not enumerate them — which screens or transitions are affected?)
2. What is the expected behavior for the Lock feature when a user forgets their password? Is there a recovery mechanism?
3. Should the image attachment support camera capture in addition to gallery selection, or only gallery?
4. What are the edge cases referred to in flow #15 ("Edge Cases")? Are there specific scenarios to cover, or is this left to the tester's judgment?
5. Are there minimum Android API level constraints for the bug fixes (e.g., notification channels require API 26, PendingIntent immutability flags require API 31)?
6. Should audio recording tests use a real microphone or mock the audio source?
7. What does "Pin/Star" mean precisely — are these two separate features (pin to top + star/favorite) or one feature?
8. What folder operations need testing — create, rename, delete, move notes between folders, or a subset?
9. What settings are available and which need test coverage?
10. Is there a specific password complexity requirement for the Lock feature?
11. No user personas or roles are mentioned — is this a single-user app with no authentication, or are there multiple user types? If single-user, this is resolved; if multi-user, permission levels need clarification.
