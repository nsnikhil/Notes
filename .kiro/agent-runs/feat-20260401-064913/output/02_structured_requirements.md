# Feature: Legacy Android Notes App Build Modernization

## Summary
Modernize a legacy Android Notes app (last actively developed 2018–2019) so that it builds and all tests pass on a current toolchain. The project uses Kotlin, XML layouts, MVVM architecture with Dagger 2, Room, RxJava 2, and the Navigation Component. The build is currently broken due to outdated Gradle/AGP versions, dead repositories (jcenter, Fabric), removed Kotlin plugins (kotlin-android-extensions), and obsolete or abandoned dependencies. The work is strictly a build modernization — no functionality, UI, or architectural changes. Success is defined by two commands: `./gradlew assembleDebug` exits 0 and `./gradlew testDebugUnitTest` passes all tests green.

## User Personas
- **Developer / Build Engineer**: The primary actor performing the modernization. Must be able to build, test, and verify the project after changes.
- **End User**: Not directly involved in this effort. The app's functionality and UI must remain unchanged from the end user's perspective.

> No RBAC or permission-level concerns apply — this is a build infrastructure task, not a feature change.

## Detailed Requirements

### Phase 1: Make It Build

#### Build System & Toolchain
- REQ-001: Upgrade the Gradle wrapper from 4.6 to a modern version compatible with the target AGP and Kotlin versions.
- REQ-002: Upgrade the Android Gradle Plugin from 3.2.1 to a modern stable version.
- REQ-003: Upgrade Kotlin from 1.3.11 to a modern stable version. The Gradle, AGP, and Kotlin versions MUST form a compatible version chain.
- REQ-004: Upgrade `compileSdkVersion` and `targetSdkVersion` from 28 to a modern API level (minSdkVersion 23 is preserved).
- REQ-005: Remove the explicit `buildToolsVersion` declaration (modern AGP auto-selects).

#### Dead Repositories & Plugins
- REQ-006: Replace `jcenter()` repository references with `mavenCentral()` and `google()`.
- REQ-007: Remove the `io.fabric` plugin and all Fabric/Crashlytics plugin configuration.
- REQ-008: Remove the `com.jakewharton.hugo:hugo-plugin` plugin (uses transform API removed in modern AGP).
- REQ-009: Remove or replace the `com.google.firebase:firebase-plugins` (firebase-perf) plugin with its modern equivalent, or remove if unused.
- REQ-010: Replace the `com.google.gms:oss-licenses` plugin with its modern artifact ID.

#### Kotlin Plugin Migration
- REQ-011: Remove the `kotlin-android-extensions` plugin from all build files.
- REQ-012: Migrate all synthetic view binding imports (`kotlinx.android.synthetic.*`) across ALL Fragment and Activity files to View Binding (or `findViewById`).
- REQ-013: Migrate `@Parcelize` usage from `kotlin-android-extensions` to the `kotlin-parcelize` plugin.
- REQ-014: Remove the `androidExtensions { experimental = true }` block from build files.

#### Dependency Updates
- REQ-015: Migrate `android.arch.navigation:*` dependencies to `androidx.navigation:*`.
- REQ-016: Migrate `android.arch.work:*` dependencies to `androidx.work:*`.
- REQ-017: Update all AndroidX libraries (Room, Lifecycle, Paging) to modern stable versions.
- REQ-018: Update Dagger 2 to a modern stable version and verify the DI graph compiles.
- REQ-019: Update Glide to a modern stable version and verify `@GlideModule` annotation processing works.
- REQ-020: Update RxJava 2, RxKotlin, and Retrofit/OkHttp/Gson to modern stable versions.
- REQ-021: Update LeakCanary to v2 (which has a completely different API — the no-op artifact is removed).

#### Abandoned / Dead Library Triage
- REQ-022: For each potentially dead or abandoned library (`com.crashlytics.sdk.android:crashlytics`, `com.github.markzhai:blockcanary-android`, `com.facebook.stetho:stetho`, `io.branch.sdk.android:library`, `com.rollbar:rollbar-android`, `com.twitter.serial:serial`, `com.jakewharton.rxbinding2:rxbinding-*`, `org.greenrobot:eventbus`), determine: (a) is it still available, (b) is there a modern replacement, (c) is it actually used in source code. Then decide: update, replace, or remove.
- REQ-023: If a library is removed, clean up ALL references in source files (imports, usages, configuration).
- REQ-024: Remove the duplicate `org.greenrobot:eventbus` dependency entry.

#### Source Code Fixes
- REQ-025: Fix any compilation errors arising from API changes in updated libraries.
- REQ-026: Verify Room annotation processing and schema compatibility after library updates.
- REQ-027: Verify Navigation SafeArgs generated code regenerates correctly with the updated plugin.
- REQ-028: Verify Dagger 2 generated code compiles with newer annotation processing (kapt).

#### Build Verification
- REQ-029: `./gradlew assembleDebug` MUST succeed (exit code 0).

### Phase 2: Make Tests Pass

- REQ-030: Upgrade all test dependencies (JUnit, Espresso, Mockito, AndroidX Test, Robolectric) to modern versions compatible with the updated main dependencies.
- REQ-031: Update Robolectric to a major version that supports AndroidX and the updated compileSdk.
- REQ-032: Fix test compilation errors caused by API changes in updated test libraries.
- REQ-033: Update test code where needed for new library APIs (e.g., Robolectric runner changes).
- REQ-034: `./gradlew testDebugUnitTest` MUST pass (all tests green).
- REQ-035: Instrumented tests in `app/src/androidTest/` MUST at minimum compile.

### Phase 3: Verify Nothing Is Broken

- REQ-036: Both product flavors MUST build: `./gradlew assembleDemoDebug` and `./gradlew assembleFullDebug`.
- REQ-037: Room database schema MUST be preserved — do NOT change entity definitions or DAO queries unless absolutely necessary for compilation.
- REQ-038: Navigation graph MUST still work — SafeArgs generated classes MUST compile.
- REQ-039: Dagger dependency injection graph MUST still compile.

## Acceptance Criteria

- AC-001: Given the modernized project, When `./gradlew assembleDebug` is executed, Then the build succeeds with exit code 0.
- AC-002: Given the modernized project, When `./gradlew testDebugUnitTest` is executed, Then all unit tests pass (all green, zero failures).
- AC-003: Given the modernized project, When `./gradlew assembleDemoDebug` is executed, Then the demo flavor builds successfully.
- AC-004: Given the modernized project, When `./gradlew assembleFullDebug` is executed, Then the full flavor builds successfully.
- AC-005: Given the modernized project, When instrumented tests in `app/src/androidTest/` are compiled, Then they compile without errors.
- AC-006: Given any library was removed during modernization, When the full source tree is searched for references to that library, Then zero import statements or usages of the removed library remain.
- AC-007: Given the modernized project, When the Room database entities and DAOs are compared to the original, Then the schema is functionally identical (no entity or DAO query changes unless strictly required for compilation).
- AC-008: Given the modernized project, When the app's package name is inspected, Then it remains `com.nrs.nsnik.notes`.
- AC-009: Given the modernized project, When the app's functionality and UI are reviewed, Then no features have been removed, added, or altered.
- AC-010: Given a test was removed or modified during modernization, When the test suite is reviewed, Then an equivalent replacement test covering the same functionality exists and test coverage has not decreased.
- AC-011: Given the modernized project, When `google-services.json` and `fabric.properties` files are checked, Then they are still present in the project (even if Fabric plugin is removed).
- AC-012: Given the modernized project, When `gradle.properties` values (API keys, build flags) are checked, Then all original values are preserved and functional.
- AC-013: Given the modernized project, When all synthetic view binding imports (`kotlinx.android.synthetic.*`) are searched for, Then zero occurrences remain — all have been migrated to View Binding or `findViewById`.

## Constraints

- CON-001: This is an Android/Kotlin project using the Gradle build system. All changes must stay within this ecosystem.
- CON-002: The app's package name (`com.nrs.nsnik.notes`) MUST NOT change.
- CON-003: The app's functionality and UI MUST NOT change — this is a build modernization only.
- CON-004: Features MUST NOT be removed to simplify the build.
- CON-005: The MVVM architecture, Dagger DI, Room database, and Navigation Component MUST be preserved.
- CON-006: If a library is removed, ALL source file references (imports, usages, configuration) MUST be cleaned up.
- CON-007: Every dependency change MUST be verified by a successful build — do not blindly update versions.
- CON-008: `google-services.json` and `fabric.properties` files MUST be preserved (even if Fabric is removed).
- CON-009: `gradle.properties` values (API keys, build flags) MUST continue to work.
- CON-010: Tests MUST NOT be removed to make the suite pass. If a test needs updating due to API changes, update it. If a test must be removed, replace it with an equivalent test.
- CON-011: Test coverage MUST NOT decrease.
- CON-012: The agent MUST run `./gradlew assembleDebug` and `./gradlew testDebugUnitTest` to verify success — do not claim success without executing the commands.
- CON-013: Room database schema compatibility MUST be preserved — do NOT change entity definitions or DAO queries unless absolutely necessary for compilation.

## Dependencies

- Google's Maven repository (`google()`) — required for AndroidX, AGP, and Google services artifacts.
- Maven Central (`mavenCentral()`) — required for third-party libraries previously hosted on jcenter.
- Android SDK with a modern API level installed on the build machine.
- Kotlin compiler compatible with the chosen AGP version.
- `google-services.json` — required for Google/Firebase services plugin (must remain in project).
- Existing frontend login form / UI — the app already has XML layouts that must be preserved.
- Existing `app/src/test/` unit tests and `app/src/androidTest/` instrumented tests — must be updated, not removed.

## Non-Functional Requirements

- NFR-001: Build verification — both `./gradlew assembleDebug` and `./gradlew testDebugUnitTest` must complete successfully. (Quantified: exit code 0, zero test failures.)
- NFR-002: Schema preservation — Room database schema must remain functionally identical after modernization. (Quantified: zero entity or DAO query changes unless strictly required for compilation.)
- NFR-003: Test coverage — must not decrease from pre-modernization levels. (Quantified: number of test cases covering equivalent functionality must be >= original count.)

> Note: No explicit performance, scalability, or security NFRs were stated in the raw requirements. This is expected — the scope is build modernization, not feature development.

## Technical Notes

- The project uses kapt (Kotlin Annotation Processing Tool) for Dagger 2, Room, and Glide code generation. Kapt compatibility must be verified with the chosen Kotlin version.
- LeakCanary v2 has a fundamentally different integration API compared to v1 — the no-op debug/release split artifact pattern is removed; v2 uses a single dependency that auto-initializes.
- `kotlin-android-extensions` synthetic bindings are used throughout ALL Fragment and Activity files. Migration to View Binding requires enabling `viewBinding` in the build file and updating every file that uses synthetic imports.
- `@Parcelize` annotation moves from `kotlinx.android.parcel` (kotlin-android-extensions) to the standalone `kotlin-parcelize` plugin with the same package.
- `com.jakewharton.rxbinding2:rxbinding-*` is tied to the old Android Support Library. If RxBinding is needed, it must be upgraded to RxBinding 4.x (AndroidX + RxJava 3) or RxBinding 3.x (AndroidX + RxJava 2), depending on whether RxJava is also upgraded.
- The project has two product flavors: `demo` and `full`. Both must be preserved and both must build.
- The project was last actively developed in 2018–2019 (approximately 7 years ago).

## Open Questions

1. What specific modern versions of Gradle, AGP, and Kotlin should be targeted? The raw requirements say "modern" but do not specify exact versions. The agent must determine a compatible version chain.
2. What specific modern API level should `compileSdkVersion` and `targetSdkVersion` be upgraded to? (e.g., 34, 35?)
3. For each abandoned library (BlockCanary, Stetho, Branch SDK, Rollbar, Twitter Serial, RxBinding 2, EventBus), is it actually used in source code? The agent must investigate before deciding to update, replace, or remove.
4. Should RxJava be upgraded from 2 to 3? This would cascade into RxKotlin, RxBinding, and RxAndroid upgrades and potentially require source code changes. Alternatively, staying on RxJava 2 with updated patch versions may be simpler.
5. Is the `com.google.gms:google-services` plugin still needed? If Firebase services are removed (Fabric, firebase-perf), does the app still use any Google/Firebase services that require it?
6. What is the actual content and usage of `google-services.json`? If no Firebase services remain after cleanup, the plugin may be removable (though the file must be preserved per constraints).
7. Are there any proguard/R8 rules that reference removed libraries and need cleanup?
8. The raw requirements mention "some Java interop via kapt" — are there any `.java` source files in the project, or is kapt the only Java touchpoint?
9. The duplicate `org.greenrobot:eventbus` dependency — is EventBus actually used in source code, or can it be removed entirely?
10. What is the minimum acceptable Robolectric version for compatibility with the target compileSdk and AndroidX?
