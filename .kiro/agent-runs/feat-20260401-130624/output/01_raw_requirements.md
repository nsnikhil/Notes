Fix all failing Espresso tests in the Android Notes app. An emulator/device is connected and available. The ONLY definition of done is: `./gradlew connectedDemoDebugAndroidTest` passes with 0 failures. Do NOT claim success without running this command and seeing 0 failures.

The project is at the repo_path. There are 51 Espresso tests in `app/src/androidTest/`. Currently 41 are failing, 10 are passing.

## Current Test Results (from the latest run)

Passing (10 tests):
- AboutFlowTest: 3/3 (100%)
- SettingsFlowTest: 2/2 (100%)
- NavigationFlowTest: 4/6 (66% — 2 failing)
- AppLaunchFlowTest: 1/5 (20% — 4 failing)

Failing (41 tests across 12 test classes):
- NoteCrudFlowTest: 0/6
- FolderFlowTest: 0/5
- PinFlowTest: 0/4
- ColorPickerFlowTest: 0/4
- ImageAttachmentFlowTest: 0/4
- LockFlowTest: 0/3
- SearchFlowTest: 0/3
- ChecklistFlowTest: 0/2
- AudioFlowTest: 0/2
- ReminderFlowTest: 0/2
- NavigationFlowTest: 2 failing
- AppLaunchFlowTest: 4 failing

## What To Do

1. Read ALL Espresso test files in `app/src/androidTest/` to understand what each test expects.

2. Run `./gradlew connectedDemoDebugAndroidTest` to see the actual failure messages and stack traces. The test report at `app/build/reports/androidTests/connected/debug/flavors/demo/` contains the error details for each failing test.

3. For each failing test, determine the root cause:
   - Is it an app bug? (the feature is broken — fix the app code)
   - Is it a test bug? (the test has wrong assumptions about the UI — fix the test)
   - Is it a missing view or changed ID? (the View Binding migration changed how views are accessed — fix the layout or test)

4. Fix the root cause. If it's an app bug, fix the app AND verify the fix doesn't break other tests. If it's a test bug, fix the test to match the actual app behavior.

5. After each batch of fixes, run `./gradlew connectedDemoDebugAndroidTest` again to check progress. Work iteratively — fix, run, check, repeat.

6. Continue until ALL 51 tests pass with 0 failures.

## Approach

Start with the tests that are closest to passing:
- NavigationFlowTest (4/6 passing — only 2 failures, likely small fixes)
- AppLaunchFlowTest (1/5 passing — 4 failures, likely related to the same root cause)

Then move to the 0% test classes. Many of them probably share a common root cause — if NoteCrudFlowTest fails because the FAB or navigation to NewNoteFragment is broken, then Checklist, Pin, Lock, ColorPicker, Image, Audio, and Reminder tests all fail for the same reason (they all need to navigate to NewNoteFragment first).

Fix the foundational issues first (app launch, navigation, FAB, note creation), then the feature-specific issues will likely resolve or become easier to diagnose.

## Hard Requirements

- `./gradlew connectedDemoDebugAndroidTest` MUST pass with 0 failures and 51 tests passing
- `./gradlew testDemoDebugUnitTest` MUST also still pass — do not break unit tests while fixing Espresso tests
- `./gradlew assembleDebug` MUST still succeed
- Do NOT delete or skip any test. Fix the app or fix the test — do not remove tests.
- Do NOT change the app's intended functionality
- Both product flavors must still build

## Constraints

- An emulator/device is connected — `adb devices` will show it
- The agent MUST run `./gradlew connectedDemoDebugAndroidTest` to verify — do not claim success without running it
- Read the test report HTML files for detailed error messages and stack traces
- Fix app bugs in `app/src/main/`, fix test bugs in `app/src/androidTest/`
- After fixing app code, always re-run both unit tests and Espresso tests to ensure no regressions
- Work iteratively: fix → run → check → repeat. Do not make 20 changes and then run once.
