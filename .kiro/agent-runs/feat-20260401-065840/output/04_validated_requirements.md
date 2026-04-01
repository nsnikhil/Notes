# Validated Requirements: Fix Runtime Crashes in Modernized Android Notes App

## Validation Status
- Cross-reference check: PASS
- Completeness check: PASS

## Summary
Fix all runtime crashes that prevent the modernized Android Notes app from launching and running correctly. The app was recently modernized (build system upgrade, dependency updates, view binding migration) and builds successfully with passing unit tests, but crashes immediately on launch. This requirement covers tracing the full initialization chain, auditing all XML layouts and source files for runtime issues introduced by the modernization, fixing every crash found, and adding Robolectric-based launch verification tests — without changing existing functionality.

## Detailed Requirements

### Functional Requirements

- FR-001 [MUST-HAVE]: The app must launch without crashing through the full initialization chain: `MyApplication.onCreate` → `SplashActivity.onCreate` → `MainActivity.onCreate` → Navigation graph inflation → Fragment creation → View binding inflation.

- FR-002 [MUST-HAVE]: The agent must read and understand the entire project's current state after modernization before making any modifications.

- FR-003 [MUST-HAVE]: Every runtime crash in the app initialization chain must be identified and fixed, including crashes that are revealed only after fixing a preceding crash.

- FR-004 [MUST-HAVE]: All XML layout files must be audited and fixed for deprecated or broken patterns that compile but crash at runtime with modern library versions.

- FR-005 [MUST-HAVE]: All source files must be audited and fixed for runtime issues introduced by the modernization, including but not limited to:
  - Layout inflation failures from deprecated XML patterns
  - Navigation component API changes affecting NavController acquisition
  - Dagger component initialization failures from changed generated code
  - Room database migration issues (schema version mismatch)
  - Glide module registration issues
  - RxJava 3 behavioral differences from RxJava 2 (null handling, error propagation)
  - kotlinx.serialization runtime failures from data format changes
  - View Binding null references from incorrect migration
  - Navigation SafeArgs argument passing issues

- FR-006 [MUST-HAVE]: Robolectric-based tests must be written that verify the app can launch without crashing, covering at minimum:
  - `MainActivity` can be created and its `onCreate` completes without exception
  - `SplashActivity` can be created and navigates to `MainActivity`
  - Each Fragment referenced in the navigation graph can be instantiated
  - The Dagger dependency graph initializes correctly (`MyApplication.onCreate` completes)

- FR-007 [MUST-HAVE]: After all fixes, `./gradlew assembleDebug` must still succeed.

- FR-008 [MUST-HAVE]: After all fixes, `./gradlew testDebugUnitTest` must still pass, including all existing tests and all new launch verification tests.

- FR-009 [SHOULD-HAVE]: Both product flavors (demo and full) must still build successfully after all fixes.

- FR-010 [MUST-HAVE]: No existing tests may be removed.

- FR-011 [MUST-HAVE]: The app's existing functionality must not be changed — only runtime crashes may be fixed.

## Acceptance Criteria

1. AC-001: Given the modernized Notes app, when the full initialization chain is exercised via Robolectric tests (`MyApplication.onCreate` → `SplashActivity.onCreate` → `MainActivity.onCreate` → Navigation graph inflation → Fragment creation → View binding inflation), then every step completes without throwing an exception.

2. AC-002: Given a Robolectric test that creates `MainActivity`, when `onCreate` is invoked, then it completes without exception and the main screen view hierarchy is inflated.

3. AC-003: Given a Robolectric test that creates `SplashActivity`, when `onCreate` is invoked, then it completes without exception and navigation to `MainActivity` is initiated.

4. AC-004: Given a Robolectric test for each Fragment referenced in the navigation graph, when each Fragment is instantiated, then it is created without exception.

5. AC-005: Given a Robolectric test that triggers `MyApplication.onCreate`, when the Dagger dependency graph is initialized, then it completes without exception.

6. AC-006: Given the fixed codebase, when `./gradlew assembleDebug` is executed, then the build succeeds with exit code 0.

7. AC-007: Given the fixed codebase, when `./gradlew testDebugUnitTest` is executed, then all tests (existing and new) pass with exit code 0.

8. AC-008: Given the fixed codebase, when both product flavors (demo and full) are assembled, then both builds succeed.

9. AC-009: Given the fixed codebase, when the set of existing tests is compared to the pre-fix state, then no existing tests have been removed.

10. AC-010: Given the fixed codebase, when the app's functional behavior is compared to the pre-fix state, then all existing functionality (navigation, drawer, fragments, database, etc.) is preserved.

## Constraints

- CON-001: This is a fix-only task. No refactoring, feature additions, or architecture changes are permitted.
- CON-002: All existing functionality must be preserved — navigation, drawer, fragments, database, and all other features.
- CON-003: Gradle commands (`assembleDebug`, `testDebugUnitTest`) must be executed to verify success; verification is not optional.
- CON-004: If fixing one crash reveals another, the subsequent crash must also be fixed. The goal is a fully launchable app, not a single-crash fix.
- CON-005: New tests must use Robolectric (already configured in the project) so they run as unit tests without requiring an emulator.
- CON-006: No existing tests may be removed or disabled.
- CON-007: The app's functionality must not be altered — only runtime crash fixes are in scope.

## Dependencies

- Dagger (dependency injection — generated code must be compatible with current library version)
- Room (database — schema version must be compatible)
- Glide (image loading — module registration must be correct)
- RxJava 3 (reactive streams — behavioral differences from RxJava 2 must be accounted for)
- AndroidX Navigation component with SafeArgs (navigation graph and argument passing)
- View Binding (layout inflation)
- kotlinx.serialization (data serialization — format compatibility)
- Robolectric (test framework for launch verification tests)
- Gradle build system (assembleDebug, testDebugUnitTest tasks)

## Non-Functional Requirements

- NFR-001 [SHOULD-HAVE]: The app should launch without user-perceptible delay caused by the fixes (fixes must not introduce performance regressions).

## Technical Notes

### Initialization Chain
The full initialization chain to trace is: `MyApplication.onCreate` → `SplashActivity.onCreate` → `MainActivity.onCreate` → Navigation graph inflation → Fragment creation → View binding inflation.

### Known Crash Points from Source Code Analysis
The validation phase identified 8 specific crash points from scanning the actual source code. These are high-confidence starting points for investigation:

1. **CRASH-01: Deprecated `<fragment>` tag in `activity_main.xml` (line 45)** — Uses the deprecated `<fragment>` XML tag for `NavHostFragment`. With Navigation 2.8.5, this is a known crash source. Fix: replace `<fragment>` with `<androidx.fragment.app.FragmentContainerView>`. This is the most likely cause of the immediate launch crash.

2. **CRASH-02: `ContextModule` initialized at field declaration time in `MyApplication.kt` (line 50)** — `private var contextModule: ContextModule = ContextModule(this)` passes `this` before `Application.onCreate()` runs. Fragile pattern that can cause issues with newer Android framework versions. Verify this doesn't crash with compileSdk 35 / targetSdk 34.

3. **CRASH-03: Missing `google-services.json`** — `FirebaseApp.initializeApp(this)` is called in `MyApplication.onCreate()`, and `apply plugin: 'com.google.gms.google-services'` is in `build.gradle`, but no `google-services.json` file exists. This will crash at runtime when Firebase tries to initialize.

4. **CRASH-04: `NetworkComponent` is `abstract class` instead of `interface`** — All other Dagger components are interfaces, but `NetworkComponent` is an abstract class. Dagger 2.53.1 may handle this differently than older versions. Verify this doesn't cause a Dagger code generation issue.

5. **CRASH-05: `NotesDatabase` version 1 with no migration strategy** — Room 2.6.1 is stricter about schema validation. If the schema generated by Room 2.6.1 differs from the version 1 schema created by Room 2.0.0 (e.g., due to `@Serializable` annotation on entities or different nullability handling), Room will throw `IllegalStateException`. No `fallbackToDestructiveMigration()` is configured.

6. **CRASH-06: `NoteEntity` has both `@Serializable` (kotlinx) and `java.io.Serializable`** — The `@Serializable` annotation may cause Room's annotation processor to generate different schema metadata, potentially triggering a schema mismatch even though the database version hasn't changed.

7. **CRASH-07: Deprecated `setTargetFragment` in `PrefFragment.kt` (line 109)** — `dialog.setTargetFragment(this, 1954)` is deprecated and may throw `IllegalStateException` with Fragment 1.8.5 if the fragment is not in the same FragmentManager. Won't crash on launch but will crash when navigating to Settings and tapping "Change Password".

8. **CRASH-08: `findNavController(R.id.mainNavHost)` in `MainActivity`** — With `<fragment>` tag, `findNavController` may fail because the fragment hasn't been added to the FragmentManager yet during `onCreate`. After replacing `<fragment>` with `FragmentContainerView` (CRASH-01 fix), the NavController acquisition must also change to use `supportFragmentManager.findFragmentById(R.id.mainNavHost)` since `FragmentContainerView` doesn't support `findNavController` by view ID the same way.

### Additional Investigation Areas
These areas should be checked by the implementing agent through direct code inspection:
- Specific library version changes (before/after modernization)
- ProGuard/R8 rules that might strip classes needed at runtime
- Room migration strategy (destructive fallback may be acceptable for development)
- Flavor-specific source files or layouts with different crash behaviors between demo and full
- Minimum API level and target device configuration
- SplashActivity behavior (async work vs. immediate redirect)

### Project Context
- Two product flavors exist: demo and full
- Robolectric is already configured in the project for emulator-free unit testing
- The app uses Dagger, Room, Glide, RxJava 3, Navigation with SafeArgs, View Binding, and kotlinx.serialization — all recently upgraded

## Scope Exclusions

- No refactoring beyond what is strictly necessary to fix crashes
- No feature additions
- No architecture changes
- No performance optimization (unless a fix inherently improves performance)
- No UI changes (unless a layout fix is required to prevent a crash)
- No dependency version changes (unless required to resolve a crash)

## Deferred Items

- None — no clarifying questions were asked and no items were deferred by the user
