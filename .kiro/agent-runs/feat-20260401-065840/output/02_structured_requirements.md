# Feature: Fix Runtime Crashes in Modernized Android Notes App

## Summary
Fix all runtime crashes that prevent the modernized Android Notes app from launching and running correctly. The app was recently modernized (build system upgrade, dependency updates, view binding migration) and builds successfully with passing unit tests, but crashes immediately on launch. This requirement covers tracing the full initialization chain, auditing all XML layouts and source files for runtime issues introduced by the modernization, fixing every crash found, and adding Robolectric-based launch verification tests — without changing existing functionality.

## Detailed Requirements

### Functional Requirements

REQ-001: The app must launch without crashing through the full initialization chain: `MyApplication.onCreate` → `SplashActivity.onCreate` → `MainActivity.onCreate` → Navigation graph inflation → Fragment creation → View binding inflation.

REQ-002: The agent must read and understand the entire project's current state after modernization before making any modifications.

REQ-003: Every runtime crash in the app initialization chain must be identified and fixed, including crashes that are revealed only after fixing a preceding crash.

REQ-004: All XML layout files must be audited and fixed for deprecated or broken patterns that compile but crash at runtime with modern library versions.

REQ-005: All source files must be audited and fixed for runtime issues introduced by the modernization, including but not limited to:
- Layout inflation failures from deprecated XML patterns
- Navigation component API changes affecting NavController acquisition
- Dagger component initialization failures from changed generated code
- Room database migration issues (schema version mismatch)
- Glide module registration issues
- RxJava 3 behavioral differences from RxJava 2 (null handling, error propagation)
- kotlinx.serialization runtime failures from data format changes
- View Binding null references from incorrect migration
- Navigation SafeArgs argument passing issues

REQ-006: Robolectric-based tests must be written that verify the app can launch without crashing, covering at minimum:
- `MainActivity` can be created and its `onCreate` completes without exception
- `SplashActivity` can be created and navigates to `MainActivity`
- Each Fragment referenced in the navigation graph can be instantiated
- The Dagger dependency graph initializes correctly (`MyApplication.onCreate` completes)

REQ-007: After all fixes, `./gradlew assembleDebug` must still succeed.

REQ-008: After all fixes, `./gradlew testDebugUnitTest` must still pass, including all existing tests and all new launch verification tests.

REQ-009: Both product flavors (demo and full) must still build successfully after all fixes.

REQ-010: No existing tests may be removed.

REQ-011: The app's existing functionality must not be changed — only runtime crashes may be fixed.

## Acceptance Criteria

AC-001: Given the modernized Notes app, when the full initialization chain is exercised via Robolectric tests (`MyApplication.onCreate` → `SplashActivity.onCreate` → `MainActivity.onCreate` → Navigation graph inflation → Fragment creation → View binding inflation), then every step completes without throwing an exception.

AC-002: Given a Robolectric test that creates `MainActivity`, when `onCreate` is invoked, then it completes without exception and the main screen view hierarchy is inflated.

AC-003: Given a Robolectric test that creates `SplashActivity`, when `onCreate` is invoked, then it completes without exception and navigation to `MainActivity` is initiated.

AC-004: Given a Robolectric test for each Fragment referenced in the navigation graph, when each Fragment is instantiated, then it is created without exception.

AC-005: Given a Robolectric test that triggers `MyApplication.onCreate`, when the Dagger dependency graph is initialized, then it completes without exception.

AC-006: Given the fixed codebase, when `./gradlew assembleDebug` is executed, then the build succeeds with exit code 0.

AC-007: Given the fixed codebase, when `./gradlew testDebugUnitTest` is executed, then all tests (existing and new) pass with exit code 0.

AC-008: Given the fixed codebase, when both product flavors (demo and full) are assembled, then both builds succeed.

AC-009: Given the fixed codebase, when the set of existing tests is compared to the pre-fix state, then no existing tests have been removed.

AC-010: Given the fixed codebase, when the app's functional behavior is compared to the pre-fix state, then all existing functionality (navigation, drawer, fragments, database, etc.) is preserved.

## Constraints

CON-001: This is a fix-only task. No refactoring, feature additions, or architecture changes are permitted.

CON-002: All existing functionality must be preserved — navigation, drawer, fragments, database, and all other features.

CON-003: Gradle commands (`assembleDebug`, `testDebugUnitTest`) must be executed to verify success; verification is not optional.

CON-004: If fixing one crash reveals another, the subsequent crash must also be fixed. The goal is a fully launchable app, not a single-crash fix.

CON-005: New tests must use Robolectric (already configured in the project) so they run as unit tests without requiring an emulator.

CON-006: No existing tests may be removed or disabled.

CON-007: The app's functionality must not be altered — only runtime crash fixes are in scope.

## Assumptions

1. The project builds successfully with `./gradlew assembleDebug` in its current state (pre-fix).
2. Existing unit tests pass with `./gradlew testDebugUnitTest` in the current state.
3. Robolectric is already configured and available in the project's test dependencies.
4. The runtime crashes are caused by the recent modernization (build system upgrade, dependency updates, view binding migration), not by pre-existing bugs.
5. The project has two product flavors: demo and full.
6. The project uses Dagger for dependency injection, Room for database, Glide for image loading, RxJava 3, Navigation component with SafeArgs, and View Binding — all recently upgraded.
7. The app's entry point follows the chain: `MyApplication.onCreate` → `SplashActivity.onCreate` → `MainActivity.onCreate`.

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

None explicitly stated. The requirements focus on correctness (crash-free launch) rather than performance, security, or scalability targets.

## Technical Notes

- The app was recently modernized with build system upgrades, dependency updates, and view binding migration.
- The full initialization chain to trace is: `MyApplication.onCreate` → `SplashActivity.onCreate` → `MainActivity.onCreate` → Navigation graph inflation → Fragment creation → View binding inflation.
- Common post-modernization runtime issues to check are enumerated in REQ-005.
- Robolectric is already configured in the project for emulator-free unit testing.
- Two product flavors exist: demo and full.
- The project resides at the repo path already known to the executing agent.

## Open Questions

1. What specific library versions were upgraded during modernization? Knowing the before/after versions would help pinpoint version-specific breaking changes.
2. What is the exact crash stack trace on launch? This would accelerate root cause identification.
3. Are there any ProGuard/R8 rules that might strip classes needed at runtime?
4. Does the Room database require a migration, or is a destructive migration (fallback) acceptable for development?
5. Are there any flavor-specific source files or layouts that might have different crash behaviors between demo and full?
6. Is there a specific minimum API level or target device configuration that must be supported?
7. What is the expected behavior of `SplashActivity` — does it perform async work (e.g., data loading) before navigating, or is it an immediate redirect?
