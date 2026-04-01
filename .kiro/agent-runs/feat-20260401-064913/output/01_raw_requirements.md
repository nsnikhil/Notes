Modernize and fix a legacy Android Notes app so it builds and all tests pass. The project is at the repo_path. It was last actively developed in 2018-2019 (7 years ago). The build currently fails due to outdated Gradle, deprecated plugins, removed repositories, and obsolete dependencies. The app is written in Kotlin with XML layouts.

This is a large modernization effort. The end goal is simple: `./gradlew assembleDebug` succeeds AND `./gradlew testDebugUnitTest` passes. Nothing else matters until these two commands work.

## Current Project State

Language: Kotlin (with some Java interop via kapt)
Build system: Gradle 4.6 with Android Gradle Plugin 3.2.1
Min SDK: 23, Target SDK: 28, Compile SDK: 28
Kotlin version: 1.3.11
Architecture: MVVM with Dagger 2 DI, Room database, RxJava 2, Navigation Component
Product flavors: demo and full — both must be preserved and build

## Known Broken Things (discovered from reading build files)

The agent MUST read all build files and source files to discover the full picture. These are starting points, not an exhaustive list:

### Dead Repositories & Plugins
- `jcenter()` repository is shut down — dependencies must resolve from `mavenCentral()` and `google()`
- `maven.fabric.io` is shut down — Fabric/Crashlytics was sunset
- `io.fabric` plugin — Fabric is dead
- `com.jakewharton.hugo:hugo-plugin` — uses transform API removed in modern AGP, must be removed
- `com.google.firebase:firebase-plugins` (firebase-perf plugin) — ancient
- `com.google.gms:oss-licenses` — old artifact ID, has been replaced

### Deprecated/Removed Kotlin Features
- `kotlin-android-extensions` plugin is removed in modern Kotlin — synthetic view bindings (`kotlinx.android.synthetic.*`) used throughout ALL Fragment and Activity files need migration to View Binding or `findViewById`
- `@Parcelize` from `kotlin-android-extensions` — needs migration to `kotlin-parcelize` plugin
- `androidExtensions { experimental = true }` block — removed, must be deleted

### Outdated Dependencies (agent must determine correct modern versions)
- Gradle, AGP, Kotlin — all need major version upgrades, agent must resolve a compatible version chain
- `android.arch.navigation:*` — migrated to `androidx.navigation:*`
- `android.arch.work:*` — migrated to `androidx.work:*`
- `compileSdkVersion` and `targetSdkVersion` — need upgrade to modern API level
- `buildToolsVersion` — can be removed (modern AGP auto-selects)
- All AndroidX libraries, Room, Lifecycle, Paging — need updates to modern stable versions
- Dagger, Glide, RxJava/RxKotlin, Retrofit, OkHttp, Gson — need updates to modern stable versions
- LeakCanary — v2 has completely different API (no-op artifact removed)
- Robolectric — needs major version upgrade for AndroidX compatibility
- Test dependencies (JUnit, Espresso, Mockito, AndroidX Test) — need updates

### Potentially Dead/Abandoned Libraries (agent must investigate)
- `com.crashlytics.sdk.android:crashlytics` — Fabric is dead
- `com.github.markzhai:blockcanary-android` — may be abandoned
- `com.facebook.stetho:stetho` — deprecated
- `io.branch.sdk.android:library` — very old version
- `com.rollbar:rollbar-android` — old version
- `com.twitter.serial:serial` — may be abandoned
- `com.jakewharton.rxbinding2:rxbinding-*` — RxBinding 2 is for old support library
- `org.greenrobot:eventbus` — listed twice in dependencies

For each of these, the agent must determine: is it still available? Is there a modern replacement? Is it actually used in source code? Then decide: update, replace, or remove. If removed, clean up ALL references in source files.

### Source Code Concerns
- `kotlin-android-extensions` synthetic imports used throughout all Fragment and Activity files — every view reference needs migration
- Dagger 2 generated code may need adjustments for newer annotation processing
- Room annotations — verify schema compatibility after library update
- Navigation SafeArgs generated code — verify it regenerates correctly with updated plugin
- Glide `@GlideModule` annotation processing — verify with updated Glide

### Test Files
- `app/src/test/` — unit test(s), must pass after modernization
- `app/src/androidTest/` — Espresso UI tests, must at minimum compile (running requires an emulator which may not be available)
- Robolectric — needs major version upgrade, test code may need updates for new API

## What To Do

### Phase 1: Make it build

1. Read the entire project — all build files, all source files, all test files. Understand the dependency graph before changing anything.
2. Upgrade Gradle wrapper, AGP, and Kotlin to a compatible modern version chain.
3. Remove dead repositories and dead plugins.
4. Update all dependencies to modern versions. For abandoned libraries, find replacements or remove them and clean up all source references.
5. Replace `kotlin-android-extensions` synthetic view bindings with View Binding across ALL files.
6. Migrate `@Parcelize` to `kotlin-parcelize` plugin.
7. Update `compileSdkVersion` and `targetSdkVersion` to a modern API level.
8. Fix any compilation errors from API changes in updated libraries.
9. `./gradlew assembleDebug` must succeed.

### Phase 2: Make tests pass

10. Upgrade all test dependencies to modern versions compatible with the updated main dependencies.
11. Fix test compilation errors from API changes.
12. Update test code if needed for new library APIs.
13. `./gradlew testDebugUnitTest` must pass.
14. Instrumented tests (`androidTest`) must at minimum compile.

### Phase 3: Verify nothing is broken

15. Both product flavors (demo and full) must build: `./gradlew assembleDemoDebug` and `./gradlew assembleFullDebug`
16. Room database schema must be preserved — do NOT change entity definitions or DAO queries unless absolutely necessary for compilation.
17. Navigation graph must still work — SafeArgs generated classes must compile.
18. Dagger dependency injection graph must still compile.

## Hard Requirements

- `./gradlew assembleDebug` MUST succeed (exit code 0)
- `./gradlew testDebugUnitTest` MUST pass (all tests green)
- Do NOT remove tests to make the suite pass. If a test needs updating due to API changes, update it. If a test must be removed, replace it with an equivalent test covering the same functionality.
- Test coverage must not decrease — if you remove a test, add a replacement.
- Both product flavors (demo/full) must build.
- Room database schema compatibility must be preserved.
- The app's functionality must not change — this is a build modernization, not a feature change.

## Constraints

- Android/Kotlin project, Gradle build system
- The agent MUST run `./gradlew assembleDebug` and `./gradlew testDebugUnitTest` to verify success — do not claim success without running the commands
- Do NOT change the app's package name (`com.nrs.nsnik.notes`)
- Do NOT change the app's functionality or UI
- Do NOT remove features to make the build simpler
- Preserve the MVVM architecture, Dagger DI, Room database, and Navigation Component
- If a library is removed, ensure no source file references it (clean up all imports and usages)
- Every dependency change must be verified by a successful build — do not blindly update versions
- The `google-services.json` and `fabric.properties` files should be preserved (even if Fabric is removed, don't delete config files)
- `gradle.properties` values (API keys, build flags) must continue to work
