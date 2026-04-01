# Feature: Notes App Bug Fixes and Comprehensive Espresso Test Suite

## Summary
Two-workstream effort for an Android Notes application. Workstream 1 (critical) requires fixing three persistent bugs — duplicate checklist items on Enter key, broken camera functionality, and lock feature failing on first attempt — each verified by an Espresso test running on an emulator. Workstream 2 requires writing comprehensive Espresso tests for five features (checklist, lock, camera, gallery, reminder) covering happy paths, edge cases, error scenarios, and cross-feature interactions. All changes must pass the full build and test pipeline with zero failures.

## Detailed Requirements

### Workstream 1 — Bug Fixes (Critical)

1. The checklist feature MUST NOT create duplicate items when the user presses Enter
2. The camera feature MUST function correctly (capture and attach a photo to a note)
3. The lock feature MUST work on the first attempt (not require a second try)
4. Each bug fix MUST be verified by a dedicated Espresso test that runs on the emulator
5. These are regressions from previous fix attempts — prior fixes were insufficient and must be revisited

### Workstream 2 — Comprehensive Espresso Test Suite

6. Comprehensive Espresso tests MUST be written for the checklist feature
7. Comprehensive Espresso tests MUST be written for the lock feature
8. Comprehensive Espresso tests MUST be written for the camera feature
9. Comprehensive Espresso tests MUST be written for the gallery feature
10. Comprehensive Espresso tests MUST be written for the reminder feature
11. Tests MUST cover happy paths for each feature
12. Tests MUST cover edge cases for each feature
13. Tests MUST cover error scenarios for each feature
14. Tests MUST cover cross-feature interactions between the five features

### Build & Verification

15. The `connectedDemoDebugAndroidTest` task MUST complete with 0 failures
16. The `testDemoDebugUnitTest` task MUST pass
17. The `assembleDebug` task MUST succeed
18. Existing tests MUST NOT be removed
19. Both product flavors MUST build successfully
20. Verification commands MUST be executed (not just assumed to pass)

## Acceptance Criteria

1. Given a user editing a checklist in a note, When the user presses Enter, Then exactly one new checklist item is created (no duplicates)
2. Given a user opening the camera from a note, When the user captures a photo, Then the photo is successfully attached to the note
3. Given a user attempting to lock a note for the first time, When the user sets a lock, Then the note is locked successfully on the first attempt without requiring a retry
4. Given each bug fix (checklist, camera, lock), When the corresponding Espresso test runs on the emulator, Then the test passes and confirms the fix
5. Given the complete Espresso test suite for all five features, When `connectedDemoDebugAndroidTest` is executed, Then 0 test failures are reported
6. Given the full project, When `testDemoDebugUnitTest` is executed, Then all unit tests pass
7. Given the full project, When `assembleDebug` is executed, Then the build succeeds for both product flavors
8. Given the existing test suite, When new tests are added, Then no previously existing tests have been removed
9. Given the Espresso tests for each feature, When reviewed, Then each feature has tests covering happy paths, edge cases, error scenarios, and cross-feature interactions

## Constraints

- Bug fixes must not introduce new regressions — existing tests must continue to pass
- Tests must not be removed as part of any fix or refactor
- Both product flavors (demo and another) must build successfully
- Espresso tests must run on an emulator (not just local/unit test JVM)
- These bugs have had prior fix attempts that failed — solutions must address root causes, not symptoms

## Dependencies

- Android emulator must be available and running for Espresso test execution
- Camera hardware emulation must be available on the emulator
- Existing project build configuration (Gradle, product flavors: demo + at least one other)
- Existing checklist, lock, camera, gallery, and reminder feature implementations in the app

## Non-Functional Requirements

- Build reliability: `assembleDebug` must succeed deterministically (not flaky)
- Test reliability: `connectedDemoDebugAndroidTest` must achieve 0 failures — tests must not be flaky
- Test coverage: five features × four coverage dimensions (happy path, edge cases, error scenarios, cross-feature)

## Technical Notes

- Build system: Gradle with Android-specific tasks (`connectedDemoDebugAndroidTest`, `testDemoDebugUnitTest`, `assembleDebug`)
- Test framework: Espresso (Android UI testing)
- Product flavors: at least "demo" and one other (both must build)
- The three bugs are regressions — previous fix attempts exist in the codebase but were insufficient
- Verification requires actually running the commands, not just code review

## Open Questions

1. What is the second product flavor besides "demo"? (needed to verify both flavors build)
2. What is the expected camera behavior — photo capture only, or also video? What happens if the emulator camera is unavailable?
3. What does "lock" mean specifically — PIN lock, biometric lock, password lock? What UI flow is expected?
4. What constitutes "first attempt" for the lock bug — is there a specific sequence of actions that triggers the failure?
5. What are the exact duplicate-creation conditions for the checklist bug — does it happen on every Enter press, or only under specific timing/state conditions?
6. Are there specific cross-feature interaction scenarios that are high priority (e.g., locking a note with a checklist, adding a camera photo to a locked note)?
7. What is the minimum number of test cases expected per feature per coverage dimension?
8. Should Espresso tests use mock camera intents or actual emulator camera hardware?
9. Are there any accessibility or performance requirements for the bug fixes?
10. What Android API levels / emulator configurations must the tests support?
