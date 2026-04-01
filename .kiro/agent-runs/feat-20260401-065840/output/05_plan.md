# Implementation Plan: Fix Runtime Crashes in Modernized Notes App

## Overview
The modernized Notes app builds and passes all 105 unit tests, but crashes on launch due to 8 post-modernization runtime issues. This plan fixes all crashes in 4 sub-tasks: (1) fix the 3 critical launch-blocking crashes (FragmentContainerView, Firebase init, NavController lookup), (2) fix the remaining 5 runtime issues (Dagger early init, NetworkComponent abstract class, Room fallback, dual Serializable, deprecated setTargetFragment), (3) write Robolectric launch verification tests, and (4) perform comprehensive build verification across both flavors. Each sub-task is independently verifiable and sliced vertically through the crash categories.

## Sub-Tasks

### Sub-Task 1: Fix Critical Launch-Blocking Crashes (CRASH-01, CRASH-03, CRASH-08)
- **Size:** S
- **Description:** Fix the three crashes that prevent the app from reaching `MainActivity.onCreate()`. Replace the `<fragment>` tag in `activity_main.xml` with `<androidx.fragment.app.FragmentContainerView>` (required by Navigation 2.8.5). Wrap `FirebaseApp.initializeApp(this)` in a try-catch in `MyApplication.onCreate()` so a missing or invalid `google-services.json` at runtime does not crash the app. Replace all `findNavController(R.id.mainNavHost)` calls in `MainActivity.kt` with `supportFragmentManager.findFragmentById(R.id.mainNavHost)!!.findNavController()` (required after FragmentContainerView migration because `Activity.findNavController()` does not work with FragmentContainerView).
- **Dependencies:** None
- **Entry Criteria:** Build succeeds (`./gradlew assembleDebug` passes). All 105 existing tests pass.
- **Exit Criteria:**
  - `activity_main.xml` contains `<androidx.fragment.app.FragmentContainerView>` and no `<fragment>` tag for the NavHostFragment.
  - `MyApplication.kt` wraps `FirebaseApp.initializeApp(this)` in a try-catch that logs the error via Timber instead of crashing.
  - `MainActivity.kt` uses `supportFragmentManager.findFragmentById(R.id.mainNavHost)!!.findNavController()` for all NavController lookups (the `controller` field initialization and all `findNavController(R.id.mainNavHost)` calls in `handleIntent()` and `setNavigationItemSelectedListener`).
  - `./gradlew assembleDebug` still succeeds.
  - All 105 existing tests still pass.
- **Success Criteria:**
  - Given `activity_main.xml`, When inspected, Then it contains `<androidx.fragment.app.FragmentContainerView android:id="@+id/mainNavHost" android:name="androidx.navigation.fragment.NavHostFragment"` and does not contain `<fragment android:id="@+id/mainNavHost"`.
  - Given `MyApplication.kt`, When `FirebaseApp.initializeApp()` throws an exception at runtime, Then the exception is caught and logged, and `onCreate()` continues to `moduleSetter()`.
  - Given `MainActivity.kt`, When searched for `findNavController(R.id.mainNavHost)`, Then zero occurrences are found — all replaced with the `supportFragmentManager` pattern.
  - Given the project, When `./gradlew testDemoDebugUnitTest` is run, Then all 105 tests pass.
- **Files:**
  - Modify: `app/src/main/res/layout/activity_main.xml`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/MyApplication.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/MainActivity.kt`

### Sub-Task 2: Fix Remaining Runtime Issues (CRASH-02, CRASH-04, CRASH-05, CRASH-06, CRASH-07)
- **Size:** M
- **Description:** Fix the 5 remaining runtime issues that cause crashes after the app launches. (CRASH-02) Move `ContextModule` instantiation from field initializer to inside `onCreate()` so it runs after `Application.attachBaseContext()`. (CRASH-04) Change `NetworkComponent` from `abstract class` to `interface` to match Dagger 2.53.1 requirements (all other components are already interfaces). (CRASH-05) Add `fallbackToDestructiveMigration()` to the Room database builder in `DatabaseModule.kt` so version mismatches don't crash. (CRASH-06) Remove `java.io.Serializable` from `NoteEntity` since `@Serializable` (kotlinx.serialization) is the active serialization mechanism and the dual interface causes ambiguity. (CRASH-07) Replace deprecated `setTargetFragment`/`targetRequestCode` in `PrefFragment.kt` and `PasswordDialogFragment.kt` with the `FragmentResult` API.
- **Dependencies:** Sub-task 1
- **Entry Criteria:** Sub-task 1 is complete. Build succeeds. All existing tests pass.
- **Exit Criteria:**
  - `MyApplication.kt` initializes `ContextModule` inside `onCreate()`, not as a field initializer.
  - `NetworkComponent.kt` is declared as `interface`, not `abstract class`.
  - `DatabaseModule.kt` calls `.fallbackToDestructiveMigration()` on the Room database builder.
  - `NoteEntity.kt` does not implement `java.io.Serializable` (only has `@Serializable` annotation).
  - `PrefFragment.kt` does not call `setTargetFragment()`. Uses `setFragmentResultListener`/`setFragmentResult` instead.
  - `PasswordDialogFragment.kt` does not reference `targetRequestCode`. Uses `setFragmentResult` to communicate back.
  - `./gradlew assembleDebug` still succeeds.
  - All 105 existing tests still pass.
- **Success Criteria:**
  - Given `MyApplication.kt`, When inspected, Then `ContextModule(this)` appears only inside `onCreate()` (or a method called from `onCreate()`), not as a property initializer.
  - Given `NetworkComponent.kt`, When inspected, Then it is declared as `interface NetworkComponent`, not `abstract class NetworkComponent`.
  - Given `DatabaseModule.kt`, When inspected, Then the `Room.databaseBuilder(...)` chain includes `.fallbackToDestructiveMigration()` before `.build()`.
  - Given `NoteEntity.kt`, When inspected, Then the class declaration does not include `: java.io.Serializable` or `: Serializable`.
  - Given the codebase, When searched for `setTargetFragment`, Then zero occurrences are found in production source.
  - Given the codebase, When searched for `targetRequestCode`, Then zero occurrences are found in production source.
  - Given the project, When `./gradlew testDemoDebugUnitTest` is run, Then all 105 tests pass.
- **Files:**
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/MyApplication.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/dagger/components/NetworkComponent.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/dagger/modules/DatabaseModule.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/data/NoteEntity.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/PrefFragment.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/dialogFragments/PasswordDialogFragment.kt`

### Sub-Task 3: Write Robolectric Launch Verification Tests
- **Size:** S
- **Description:** Write Robolectric-based unit tests that verify the app initialization chain does not crash. Tests should cover: (1) `MyApplication.onCreate()` completes without exception, (2) `MainActivity` can be created via Robolectric, (3) the XML layout `activity_main.xml` uses `FragmentContainerView` (not `<fragment>`), (4) `NetworkComponent` is an interface, (5) `NoteEntity` does not implement `java.io.Serializable`, (6) no `setTargetFragment` calls exist in production source, (7) Room database builder uses fallback strategy, and (8) `ContextModule` is not instantiated before `onCreate()`. These tests follow the existing project test pattern (file-based assertion tests in `app/src/test/`).
- **Dependencies:** Sub-task 2
- **Entry Criteria:** Sub-tasks 1 and 2 are complete. All crash fixes are applied. Build succeeds.
- **Exit Criteria:**
  - A new test file `app/src/test/java/com/nrs/nsnik/notes/RuntimeCrashFixTest.kt` exists.
  - The test file contains at least 8 test methods covering all 8 crash points.
  - `./gradlew testDemoDebugUnitTest` passes with all new tests green.
  - `./gradlew testFullDebugUnitTest` passes with all new tests green.
- **Success Criteria:**
  - Given the test file, When `./gradlew testDemoDebugUnitTest --tests "com.nrs.nsnik.notes.RuntimeCrashFixTest"` is run, Then all tests pass.
  - Given the test file, When `./gradlew testFullDebugUnitTest --tests "com.nrs.nsnik.notes.RuntimeCrashFixTest"` is run, Then all tests pass.
  - Given the test for CRASH-01, When `activity_main.xml` is read, Then it contains `FragmentContainerView` and does not contain `<fragment` with `android:name="androidx.navigation.fragment.NavHostFragment"`.
  - Given the test for CRASH-04, When `NetworkComponent.kt` is read, Then it contains `interface NetworkComponent` and does not contain `abstract class NetworkComponent`.
  - Given the test for CRASH-05, When `DatabaseModule.kt` is read, Then it contains `fallbackToDestructiveMigration`.
  - Given the test for CRASH-06, When `NoteEntity.kt` is read, Then it does not contain `java.io.Serializable`.
  - Given the test for CRASH-07, When all production `.kt` files are scanned, Then none contain `setTargetFragment`.
  - Given the test for CRASH-08, When `MainActivity.kt` is read, Then it does not contain `findNavController(R.id.mainNavHost)`.
- **Files:**
  - Create: `app/src/test/java/com/nrs/nsnik/notes/RuntimeCrashFixTest.kt`

### Sub-Task 4: Comprehensive Build Verification
- **Size:** S
- **Description:** Run full build and test verification across both product flavors (demo and full) to confirm all crash fixes are applied, no regressions are introduced, and the project is in a clean state. Verify `assembleDemoDebug`, `assembleFullDebug`, `testDemoDebugUnitTest`, and `testFullDebugUnitTest` all succeed. Run grep checks to confirm zero occurrences of all removed/replaced patterns in production source.
- **Dependencies:** Sub-task 3
- **Entry Criteria:** Sub-tasks 1, 2, and 3 are complete. All crash fixes applied and tests written.
- **Exit Criteria:**
  - `./gradlew assembleDemoDebug` — BUILD SUCCESSFUL
  - `./gradlew assembleFullDebug` — BUILD SUCCESSFUL
  - `./gradlew testDemoDebugUnitTest` — all tests pass (105 existing + new RuntimeCrashFixTest tests)
  - `./gradlew testFullDebugUnitTest` — all tests pass
  - Grep verification: zero hits in `app/src/main/` for: `<fragment android:id="@+id/mainNavHost"`, `findNavController(R.id.mainNavHost)`, `abstract class NetworkComponent`, `java.io.Serializable` (in NoteEntity), `setTargetFragment`, `targetRequestCode`
- **Success Criteria:**
  - Given the project, When `./gradlew assembleDemoDebug assembleFullDebug` is run, Then both builds succeed.
  - Given the project, When `./gradlew testDemoDebugUnitTest testFullDebugUnitTest` is run, Then all tests pass in both flavors.
  - Given production source (`app/src/main/`), When grepped for `<fragment android:id="@+id/mainNavHost"`, Then 0 results are returned.
  - Given production source, When grepped for `findNavController(R.id.mainNavHost)`, Then 0 results are returned.
  - Given production source, When grepped for `abstract class NetworkComponent`, Then 0 results are returned.
  - Given production source, When grepped for `setTargetFragment`, Then 0 results are returned.
  - Given production source, When grepped for `targetRequestCode`, Then 0 results are returned.

- **Files:**
  - Modify: None (verification only)

## Implementation Order
1. Sub-task 1 — Fix Critical Launch-Blocking Crashes (no dependencies)
2. Sub-task 2 — Fix Remaining Runtime Issues (depends on 1)
3. Sub-task 3 — Write Robolectric Launch Verification Tests (depends on 2)
4. Sub-task 4 — Comprehensive Build Verification (depends on 3)

## Integration Test Plan
After all sub-tasks are complete, the IntegrationTester should verify:
- **End-to-end launch chain:** `./gradlew assembleDemoDebug` and `./gradlew assembleFullDebug` both succeed, confirming no compile-time regressions.
- **Full test suite:** `./gradlew testDemoDebugUnitTest` and `./gradlew testFullDebugUnitTest` both pass with zero failures, including all new RuntimeCrashFixTest tests.
- **CRASH-01 verification:** `activity_main.xml` contains `FragmentContainerView` and no `<fragment>` tag for NavHostFragment.
- **CRASH-02 verification:** `MyApplication.kt` does not initialize `ContextModule` as a field initializer — it is created inside `onCreate()`.
- **CRASH-03 verification:** `MyApplication.kt` wraps `FirebaseApp.initializeApp()` in try-catch.
- **CRASH-04 verification:** `NetworkComponent.kt` is an `interface`, not `abstract class`.
- **CRASH-05 verification:** `DatabaseModule.kt` includes `fallbackToDestructiveMigration()` in the Room builder chain.
- **CRASH-06 verification:** `NoteEntity.kt` does not implement `java.io.Serializable`.
- **CRASH-07 verification:** Zero occurrences of `setTargetFragment` or `targetRequestCode` in production source.
- **CRASH-08 verification:** Zero occurrences of `findNavController(R.id.mainNavHost)` in `MainActivity.kt` — all replaced with `supportFragmentManager.findFragmentById().findNavController()`.
- **Edge case — Firebase missing:** If `google-services.json` is removed or corrupted, the app should still launch (Firebase init failure is caught and logged).
- **Edge case — Room schema change:** If the database schema changes in a future version, the app should not crash on upgrade (destructive migration fallback is in place).

## Requirements Traceability
| Requirement | Sub-Task(s) |
|-------------|-------------|
| FR-001: Replace `<fragment>` with `<FragmentContainerView>` in activity_main.xml | Sub-task 1 |
| FR-002: Fix `ContextModule(this)` early initialization in MyApplication | Sub-task 2 |
| FR-003: Guard against missing/invalid `google-services.json` Firebase crash | Sub-task 1 |
| FR-004: Change `NetworkComponent` from abstract class to interface | Sub-task 2 |
| FR-005: Add Room database fallback migration strategy | Sub-task 2 |
| FR-006: Remove dual `@Serializable` + `java.io.Serializable` from NoteEntity | Sub-task 2 |
| FR-007: Replace deprecated `setTargetFragment` with FragmentResult API | Sub-task 2 |
| FR-008: Fix `findNavController(R.id.mainNavHost)` after FragmentContainerView migration | Sub-task 1 |
| FR-009: Write Robolectric launch verification tests for all crash fixes | Sub-task 3 |
| FR-010: Verify demo flavor builds and tests pass | Sub-task 4 |
| FR-011: Verify full flavor builds and tests pass | Sub-task 4 |
| NFR-001: Build and existing tests must continue to pass after each sub-task | Sub-tasks 1, 2, 3, 4 |
