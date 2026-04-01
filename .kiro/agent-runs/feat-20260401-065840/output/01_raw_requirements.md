Fix runtime crashes in the modernized Android Notes app so it launches and runs correctly. The previous modernization (build system upgrade, dependency updates, view binding migration) made the app build and tests pass, but the app crashes on launch. This requirement fixes all runtime issues.

The project is at the repo_path. It is the same Notes app that was just modernized. The build works (`./gradlew assembleDebug` passes), unit tests pass (`./gradlew testDebugUnitTest` passes). But the app crashes immediately on launch.

## What To Do

1. Read the entire project to understand the current state after modernization. The agent MUST understand what was already changed before making further modifications.

2. Trace the full app initialization chain and identify every runtime crash: `MyApplication.onCreate` → `SplashActivity.onCreate` → `MainActivity.onCreate` → Navigation graph inflation → Fragment creation → View binding inflation. Fix each crash found.

3. Audit ALL XML layout files for deprecated or broken patterns that compile but crash at runtime with modern library versions.

4. Audit ALL source files for runtime issues introduced by the modernization. Common problems after a major dependency upgrade:
   - Layout inflation failures from deprecated XML patterns
   - Navigation component API changes that affect how NavController is obtained
   - Dagger component initialization failures (generated code may have changed)
   - Room database migration issues (schema version mismatch)
   - Glide module registration issues
   - RxJava 3 behavioral differences from RxJava 2 (e.g., null handling, error propagation)
   - kotlinx.serialization runtime failures (if Twitter Serial replacement changed data formats)
   - View Binding null references (if any binding was incorrectly migrated)
   - Navigation SafeArgs argument passing issues

5. Write Robolectric-based tests that verify the app can launch without crashing. At minimum:
   - Test that `MainActivity` can be created and its `onCreate` completes without exception
   - Test that `SplashActivity` can be created and navigates to `MainActivity`
   - Test that each Fragment referenced in the navigation graph can be instantiated
   - Test that the Dagger dependency graph initializes correctly (`MyApplication.onCreate` completes)

6. If fixing one crash reveals another, fix that too. Keep going until the app launches and the main screen renders. The goal is a fully launchable app, not just fixing one crash.

7. After all fixes, verify:
   - `./gradlew assembleDebug` still succeeds
   - `./gradlew testDebugUnitTest` still passes (all existing tests + new tests)
   - Both product flavors still build

## Hard Requirements

- The app MUST launch without crashing. Verify via Robolectric tests that simulate Activity/Fragment creation.
- `./gradlew assembleDebug` MUST still succeed.
- `./gradlew testDebugUnitTest` MUST still pass — all existing tests plus new launch tests.
- Do NOT remove any existing tests.
- Do NOT change the app's functionality — only fix runtime crashes.
- Both product flavors (demo/full) must still build.

## Constraints

- This is a fix-only task. Do not refactor, add features, or change architecture.
- Preserve all existing functionality — navigation, drawer, fragments, database, etc.
- The agent MUST run the Gradle commands to verify success.
- If fixing one crash reveals another, fix that too. The goal is a launchable app, not just fixing one crash.
- New tests should use Robolectric (already configured in the project) so they run as unit tests without an emulator.
