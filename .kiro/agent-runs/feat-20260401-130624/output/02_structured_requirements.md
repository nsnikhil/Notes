# Feature: Fix All Failing Espresso Tests in Android Notes App

## Summary
Fix all 41 failing Espresso UI tests in the Android Notes app so that the full suite of 51 tests passes with zero failures. The fix must preserve existing app functionality, keep unit tests passing, and ensure both build flavors compile successfully. Root causes may be app bugs, test bugs, or view ID mismatches — each must be diagnosed and fixed at the source rather than by removing or weakening tests.

## Detailed Requirements

### Functional Requirements
1. All 41 currently failing Espresso tests must be fixed to pass
2. All 10 currently passing tests must continue to pass
3. The full test suite (51 tests) must pass when run via `./gradlew connectedDemoDebugAndroidTest`
4. Existing unit tests must continue to pass after changes
5. The app must build successfully for both flavors (demo and full/production)
6. No tests may be deleted to achieve a passing suite
7. Existing app functionality must not be changed — behavioral parity must be maintained
8. For each failing test, the root cause must be diagnosed before applying a fix:
   - App bug in `src/main` → fix the app code
   - Test bug in `src/androidTest` → fix the test code
   - View ID mismatch → fix whichever side is incorrect
9. Fixes must be applied iteratively — run the test suite after each batch of changes to verify progress

### Failing Test Suites to Fix
10. NoteCrudFlowTest — 6 of 6 failing (note create/read/update/delete operations)
11. FolderFlowTest — 5 of 5 failing (folder management)
12. PinFlowTest — 4 of 4 failing (note pinning)
13. ColorPickerFlowTest — 4 of 4 failing (note color selection)
14. ImageAttachmentFlowTest — 4 of 4 failing (image attachment to notes)
15. LockFlowTest — 3 of 3 failing (note locking)
16. SearchFlowTest — 3 of 3 failing (note search)
17. ChecklistFlowTest — 2 of 2 failing (checklist functionality)
18. AudioFlowTest — 2 of 2 failing (audio attachment/recording)
19. ReminderFlowTest — 2 of 2 failing (note reminders)
20. NavigationFlowTest — 2 of 6 failing (partial navigation failures)
21. AppLaunchFlowTest — 4 of 5 failing (app launch/startup flows)

## Acceptance Criteria
1. Given the connected emulator and the full test suite, when `./gradlew connectedDemoDebugAndroidTest` is executed, then all 51 tests pass with 0 failures
2. Given the existing unit test suite, when unit tests are run, then all unit tests pass without regressions
3. Given both build flavors (demo and the other flavor), when a build is triggered for each, then both compile and build successfully
4. Given the 10 currently passing tests (AboutFlowTest 3/3, SettingsFlowTest 2/2, NavigationFlowTest 4/6, AppLaunchFlowTest 1/5), when the full suite is run after fixes, then these tests still pass
5. Given any failing test, when the root cause is diagnosed, then the fix is applied to the correct source location (`src/main` for app bugs, `src/androidTest` for test bugs)
6. Given the original test file count, when fixes are complete, then no test files or test methods have been deleted
7. Given the app's current user-facing behavior, when fixes are applied, then no functional behavior of the app is altered from the user's perspective

## Constraints
- An emulator must be connected and available for test execution
- Fixes to app code must be made in `src/main`
- Fixes to test code must be made in `src/androidTest`
- Tests must not be deleted or skipped to achieve passing status
- App functionality must remain unchanged — only bug fixes and test corrections are permitted
- Work must proceed iteratively: fix → run → verify → repeat
- Both build flavors must remain buildable throughout the process

## Dependencies
- Android emulator (connected and running)
- Gradle build system with `connectedDemoDebugAndroidTest` task
- Espresso testing framework (already configured in the project)
- Android Studio project structure with demo flavor configured

## Non-Functional Requirements
- Build must complete successfully for both flavors (build reliability)
- All 51 tests must pass in a single test run — no flaky or intermittent failures acceptable
- Iterative verification required after each set of changes (process constraint)

## Technical Notes
- Test execution command: `./gradlew connectedDemoDebugAndroidTest`
- Project location: `/Users/eniksoni/AndroidStudioProjects/Notes/`
- App source code is in `src/main`
- Test source code is in `src/androidTest`
- The project uses Android build flavors (at minimum a "demo" flavor)
- Root cause categories to investigate: app bugs, test bugs, view ID mismatches
- All test files follow the naming pattern `*FlowTest` suggesting UI flow-based test organization
- 10 passing tests across 4 suites serve as a regression baseline

## Open Questions
1. What is the second build flavor besides "demo"? (The requirement says "both flavors build" but only "demo" is named explicitly)
2. Are there any known flaky tests in the suite that may require retry logic or idle resource handling?
3. Is there a specific order of priority among the failing test suites, or should they be addressed in any order?
4. Are the 41 failures caused by a small number of shared root causes (e.g., a common view ID change) or are they independent issues?
5. What Espresso idle resource configurations exist — could async timing issues be a common failure cause?
6. Are there any pending app migrations or schema changes that might affect test data setup?
7. What Android API level is the emulator running, and could API-level differences contribute to failures?
8. Is there a CI/CD pipeline where these tests run, or is local emulator execution the only verification method?
