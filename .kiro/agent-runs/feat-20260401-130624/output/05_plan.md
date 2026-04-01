# Implementation Plan: Fix All 41 Failing Espresso Tests

## Overview
Fix all 41 failing Espresso tests across 14 test files by addressing 2 root causes: (1) IntroFragment blocking ListFragment on fresh install due to `bundleIntroWatched` SharedPreferences defaulting to `false` — resolved by setting it to `true` in each test's `@Before` setup, and (2) AmbiguousViewMatcherException on `mainNavHost` in `AppLaunchFlowTest` — resolved by using a more specific Espresso matcher. All fixes are test-side only (`src/androidTest`); no app code changes. After fixes, the full suite is run on the connected emulator to verify 51/51 pass.

## Sub-Tasks

### Sub-Task 1: Add SharedPreferences intro bypass to all affected test files (Root Cause 1)
- **Size:** M
- **Description:** Add `PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("bundleIntroWatched", true).commit()` to the `@Before` method of every test class that launches `MainActivity` and expects `ListFragment` to be visible. This must execute before the `ActivityTestRule` launches the activity, so the `ActivityTestRule` must be changed to `ActivityTestRule(MainActivity::class.java, false, false)` (auto-launch disabled) and `activityRule.launchActivity(null)` called after setting the preference. Affected files (13 files with `@Test` methods that need the fix):
  - `AppLaunchFlowTest.kt` (5 tests)
  - `NavigationFlowTest.kt` (6 tests)
  - `NoteCrudFlowTest.kt` (6 tests)
  - `SearchFlowTest.kt` (3 tests)
  - `FolderFlowTest.kt` (5 tests)
  - `PinFlowTest.kt` (4 tests)
  - `ColorPickerFlowTest.kt` (4 tests)
  - `SettingsFlowTest.kt` (2 tests)
  - `AboutFlowTest.kt` (3 tests)
  - `ReminderFlowTest.kt` (2 tests)
  - `ChecklistFlowTest.kt` (2 tests)
  - `LockFlowTest.kt` (3 tests)
  - `AudioFlowTest.kt` (2 tests)
  - `ImageAttachmentFlowTest.kt` (4 tests)
  - `AboutFragmentScreenTest.kt` (2 tests)

  The 2 test files without `@Test` methods (`MainActivityScreenTest.kt`, `ListFragmentScreenTest.kt`) should also get the fix in their `@Before` for consistency, but they have no failing tests. `NewNoteFragmentScreenTest.kt` has no `@Before` or `@Test` — leave it as-is.

  The pattern for each file:
  1. Change `ActivityTestRule` constructor to disable auto-launch: `ActivityTestRule(MainActivity::class.java, false, false)`
  2. In `@Before`/`setUp()`, before launching the activity:
     ```kotlin
     val context = InstrumentationRegistry.getInstrumentation().targetContext
     PreferenceManager.getDefaultSharedPreferences(context)
         .edit().putBoolean("bundleIntroWatched", true).commit()
     activityRule.launchActivity(null)
     ```
  3. Then register the idling resource as before.
  4. Add necessary imports: `androidx.test.platform.app.InstrumentationRegistry`, `androidx.preference.PreferenceManager`.

- **Dependencies:** None
- **Entry Criteria:** Repository is at current state with 41 failing tests. Emulator connected.
- **Exit Criteria:** All 15 test files with `@Test` methods (13 *FlowTest + AboutFragmentScreenTest + MainActivityScreenTest/ListFragmentScreenTest for consistency) have the SharedPreferences bypass in `@Before`. Files compile without errors.
- **Success Criteria:**
  - Given any test class with the `@Before` fix, when the activity launches, then `ListFragment` is displayed instead of `IntroFragment`.
  - Given `NoteCrudFlowTest`, when `tapFabRevealsAddNoteButton` runs, then `fabAdd` is found and clickable (no longer blocked by IntroFragment).
  - Given `NavigationFlowTest`, when `drawerOpensAndCloses` runs, then `mainDrawerLayout` is found in closed state (ListFragment visible).
  - Given `SearchFlowTest`, when `searchIconIsDisplayed` runs, then `menuMainSearch` is visible in the toolbar.
  - Given `AboutFragmentScreenTest`, when `openLibrariesListTest` runs, then the drawer opens successfully from ListFragment.
- **Files:**
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/AppLaunchFlowTest.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/NavigationFlowTest.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/NoteCrudFlowTest.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/SearchFlowTest.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/FolderFlowTest.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/PinFlowTest.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/ColorPickerFlowTest.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/SettingsFlowTest.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/AboutFlowTest.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/ReminderFlowTest.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/ChecklistFlowTest.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/LockFlowTest.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/AudioFlowTest.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/ImageAttachmentFlowTest.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/AboutFragmentScreenTest.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/MainActivityScreenTest.kt`
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/ListFragmentScreenTest.kt`

### Sub-Task 2: Fix AmbiguousViewMatcherException in AppLaunchFlowTest (Root Cause 2)
- **Size:** S
- **Description:** Fix `AppLaunchFlowTest.mainActivityDisplaysNavHost()` which throws `AmbiguousViewMatcherException` because `FragmentContainerView` with `NavHostFragment` creates a child view that also has `R.id.mainNavHost`. Replace `onView(withId(R.id.mainNavHost)).check(matches(isDisplayed()))` with a matcher that uniquely identifies the `FragmentContainerView` — use `onView(allOf(withId(R.id.mainNavHost), isAssignableFrom(FragmentContainerView::class.java))).check(matches(isDisplayed()))`. Add import for `androidx.fragment.app.FragmentContainerView` and `org.hamcrest.Matchers.allOf` and `androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom`.
- **Dependencies:** Sub-task 1
- **Entry Criteria:** Sub-task 1 complete. `AppLaunchFlowTest` already has the SharedPreferences bypass from Sub-task 1.
- **Exit Criteria:** `AppLaunchFlowTest.mainActivityDisplaysNavHost()` uses a specific matcher that targets only the `FragmentContainerView` with `mainNavHost` ID. File compiles.
- **Success Criteria:**
  - Given `AppLaunchFlowTest.mainActivityDisplaysNavHost()`, when the test runs, then no `AmbiguousViewMatcherException` is thrown and the assertion passes.
  - Given the updated matcher, when there are multiple views with `R.id.mainNavHost`, then only the `FragmentContainerView` instance is matched.
- **Files:**
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/AppLaunchFlowTest.kt`

### Sub-Task 3: Run full test suite on emulator and fix any remaining failures
- **Size:** S
- **Description:** Execute `./gradlew connectedDemoDebugAndroidTest` on the connected emulator. Analyze results. If any tests still fail, diagnose and fix. Common residual issues could include: timing issues (add `Thread.sleep` or idling resource waits), view hierarchy mismatches, or edge cases not covered by the two root causes. Iterate until all 51 tests pass. Also run unit tests via `./gradlew testDemoDebugUnitTest` to confirm no regressions.
- **Dependencies:** Sub-task 1, Sub-task 2
- **Entry Criteria:** Sub-tasks 1 and 2 complete. All test files compile. Emulator connected.
- **Exit Criteria:** `./gradlew connectedDemoDebugAndroidTest` reports 51 tests run, 0 failures. `./gradlew testDemoDebugUnitTest` passes.
- **Success Criteria:**
  - Given the full test suite, when `./gradlew connectedDemoDebugAndroidTest` is executed, then the output shows `Tests run: 51, Failures: 0`.
  - Given the unit test suite, when `./gradlew testDemoDebugUnitTest` is executed, then all unit tests pass.
  - Given any test that was previously passing (10 tests: AboutFlowTest 3, SettingsFlowTest 2, NavigationFlowTest 4, AppLaunchFlowTest 1), when the suite runs, then these tests still pass (no regressions).
- **Files:**
  - Modify: (any test files that need additional fixes based on test run results — TBD)

### Sub-Task 4: Final verification — full build and both flavors
- **Size:** S
- **Description:** Run final verification: (1) `./gradlew connectedDemoDebugAndroidTest` one more time to confirm 51/51 pass, (2) `./gradlew testDemoDebugUnitTest` for unit tests, (3) `./gradlew assembleDemoDebug assembleFullDebug` to confirm both flavors build. Report final results.
- **Dependencies:** Sub-task 3
- **Entry Criteria:** Sub-task 3 complete with all tests passing.
- **Exit Criteria:** All 51 instrumented tests pass. All unit tests pass. Both `demo` and `full` flavors build successfully.
- **Success Criteria:**
  - Given the connected emulator, when `./gradlew connectedDemoDebugAndroidTest` runs, then 51 tests pass with 0 failures.
  - Given both flavors, when `./gradlew assembleDemoDebug assembleFullDebug` runs, then both APKs are produced without errors.
  - Given the unit tests, when `./gradlew testDemoDebugUnitTest` runs, then all pass.
  - Given the test file count, when listing `src/androidTest` files, then all 14 original test files exist (none deleted).
- **Files:**
  - No new file changes expected (verification only)

## Implementation Order
1. Sub-task 1 — Add SharedPreferences intro bypass to all affected test files (no dependencies)
2. Sub-task 2 — Fix AmbiguousViewMatcherException in AppLaunchFlowTest (depends on 1)
3. Sub-task 3 — Run full test suite on emulator and fix remaining failures (depends on 1, 2)
4. Sub-task 4 — Final verification with both flavors (depends on 3)

## Integration Test Plan
After all sub-tasks are complete, the IntegrationTester should verify:
- Run `./gradlew connectedDemoDebugAndroidTest` and confirm 51 tests pass, 0 failures.
- Run `./gradlew testDemoDebugUnitTest` and confirm all unit tests pass.
- Run `./gradlew assembleDemoDebug assembleFullDebug` and confirm both flavors build.
- Verify no test files were deleted: `ls app/src/androidTest/java/com/nrs/nsnik/notes/` should show all 14 original files.
- Verify no app source files were modified: `git diff --name-only app/src/main/` should show no changes.
- Spot-check that the 10 previously passing tests still pass (AboutFlowTest ×3, SettingsFlowTest ×2, NavigationFlowTest ×4, AppLaunchFlowTest ×1).
- Verify `AppLaunchFlowTest.mainActivityDisplaysNavHost()` passes without AmbiguousViewMatcherException.
- Verify a representative test from each fixed suite passes: `NoteCrudFlowTest.tapFabRevealsAddNoteButton`, `FolderFlowTest.tapFabRevealsAddFolderButton`, `SearchFlowTest.searchIconIsDisplayed`.

## Requirements Traceability
| Requirement | Sub-Task(s) |
|-------------|-------------|
| FR-001 (All 41 failing tests pass) | 1, 2, 3 |
| FR-002 (10 passing tests still pass) | 1, 3, 4 |
| FR-003 (51 tests pass via connectedDemoDebugAndroidTest) | 3, 4 |
| FR-004 (Unit tests still pass) | 3, 4 |
| FR-005 (Both flavors build) | 4 |
| FR-006 (No tests deleted or skipped) | 1, 2 |
| FR-007 (App functionality unchanged) | 1, 2 (test-only changes) |
| FR-008 (SharedPreferences bundleIntroWatched=true in @Before) | 1 |
| FR-009 (Missing views now present after fix) | 1, 3 |
| FR-010 (AppLaunchFlowTest ambiguous matcher fix) | 2 |
| FR-011 (NoteCrudFlowTest 6 tests) | 1 |
| FR-012 (FolderFlowTest 5 tests) | 1 |
| FR-013 (PinFlowTest 4 tests) | 1 |
| FR-014 (ColorPickerFlowTest 4 tests) | 1 |
| FR-015 (ImageAttachmentFlowTest 4 tests) | 1 |
| FR-016 (LockFlowTest 3 tests) | 1 |
| FR-017 (SearchFlowTest 3 tests) | 1 |
| FR-018 (ChecklistFlowTest 2 tests) | 1 |
| FR-019 (AudioFlowTest 2 tests) | 1 |
| FR-020 (ReminderFlowTest 2 tests) | 1 |
| FR-021 (NavigationFlowTest 2 failing tests) | 1 |
| FR-022 (AppLaunchFlowTest 4 failing tests) | 1, 2 |
| FR-023 (Apply RC1 first, then RC2) | 1 → 2 → 3 |
| NFR-001 (No flaky failures) | 3, 4 |
| NFR-002 (Both flavors build) | 4 |
| NFR-003 (Minimal fixes, preserve ActivityTestRule) | 1, 2 |
