# Validated Requirements: Notes App Build Modernization & Dependency Cleanup

## Validation Status
- Cross-reference check: PASS (6 observations addressed)
- Completeness check: PARTIAL → PASS (14 gaps resolved via user answers and new requirements below)

## Summary
Modernize the Notes Android app's build system, dependencies, and source code to eliminate all deprecation warnings, remove dead code and abandoned libraries, upgrade to current stable dependency versions, and ensure the project builds cleanly on modern toolchains. This includes migrating from Kotlin synthetics to view binding, upgrading RxJava 2 to RxJava 3 (with full ecosystem cascade), replacing Twitter Serial with kotlinx.serialization, removing unused libraries (WorkManager, Hugo), cleaning up stale configuration, and verifying that all tests pass with coverage not decreasing.

## Detailed Requirements

### Build System

- **FR-001**: Upgrade Android Gradle Plugin to latest stable version compatible with the project's target/compile SDK. **Priority: MUST-HAVE**
- **FR-002**: Upgrade Gradle wrapper to the version required by the upgraded AGP. **Priority: MUST-HAVE**
- **FR-003**: Upgrade Kotlin plugin to latest stable 1.9.x or 2.0.x compatible with the upgraded AGP. **Priority: MUST-HAVE**
- **FR-004**: Enable view binding in build.gradle (`buildFeatures.viewBinding = true`). **Priority: MUST-HAVE**
- **FR-005**: Remove the `kotlin-android-extensions` plugin from all build.gradle files. **Priority: MUST-HAVE**
- **FR-006**: Add the `kotlin-parcelize` plugin to app/build.gradle for future use. No source migration needed — @Parcelize has zero usage in the codebase. **Priority: SHOULD-HAVE**
- **FR-007**: Remove the nested `allprojects` block inside `android {}` in app/build.gradle (build config bug). **Priority: MUST-HAVE**
- **FR-008**: Fix the incomplete `-Xmaxerrs` compiler argument — either add the required numeric value (e.g., `-Xmaxerrs=500`) or remove the argument entirely. **Priority: MUST-HAVE**
- **FR-009**: Remove the `hugo` plugin (`com.jakewharton.hugo`) from build.gradle. **Priority: MUST-HAVE**
- **FR-010**: Retain the `com.google.gms.google-services` plugin — it is required by Firebase and Google Ads dependencies. **Priority: MUST-HAVE**
- **FR-011**: Remove the `com.google.firebase.firebase-perf` plugin if Firebase Performance is not used, or retain if used. **Priority: SHOULD-HAVE**
- **FR-012**: Clean up stale proguard rules — remove rules referencing libraries no longer in the project (butterknife, dart, and any other absent libraries). **Priority: MUST-HAVE**
- **FR-013**: Remove the `enableCrashlytics` flag from `buildTypes` if it references the deprecated Fabric Crashlytics. Update to Firebase Crashlytics mapping file upload configuration if needed. **Priority: SHOULD-HAVE**

### Dead Repositories & Plugins

- **FR-014**: Remove the JCenter repository declaration from all build.gradle files. **Priority: MUST-HAVE**
- **FR-015**: Remove any other dead/deprecated repository URLs (e.g., old maven repos that no longer resolve). **Priority: MUST-HAVE**
- **FR-016**: Remove the Hugo plugin and all `@DebugLog` annotations from source code (found in `PrefFragment.kt` and `NewNoteFragment.kt`). **Priority: MUST-HAVE**

### Kotlin Synthetics Migration

- **FR-017**: Migrate all `kotlinx.android.synthetic` imports to view binding in Activities. **Priority: MUST-HAVE**
- **FR-018**: Migrate all `kotlinx.android.synthetic` imports to view binding in Fragments. **Priority: MUST-HAVE**
- **FR-019**: Migrate all `kotlinx.android.synthetic` imports to view binding in Adapters/ViewHolders. **Priority: MUST-HAVE**
- **FR-020**: Migrate all `kotlinx.android.synthetic` imports to view binding in custom Views. **Priority: MUST-HAVE**
- **FR-021**: Remove all `kotlinx.android.synthetic` imports after migration — zero remaining references. **Priority: MUST-HAVE**

### Dependency Updates

- **FR-022**: Update AndroidX libraries (appcompat, core-ktx, recyclerview, constraintlayout, etc.) to latest stable versions. **Priority: MUST-HAVE**
- **FR-023**: Update Room to latest stable 2.6.x and update Room compiler annotation processor to KSP if applicable. **Priority: MUST-HAVE**
- **FR-024**: Upgrade RxJava 2 to RxJava 3 (`io.reactivex.rxjava2` → `io.reactivex.rxjava3`). Update all imports across the entire codebase (13+ files). **Priority: MUST-HAVE**
- **FR-025**: Upgrade RxAndroid to 3.x (compatible with RxJava 3). **Priority: MUST-HAVE**
- **FR-026**: Upgrade RxKotlin to 3.x (compatible with RxJava 3). **Priority: MUST-HAVE**
- **FR-027**: Upgrade RxBinding to 4.x (AndroidX + RxJava 3 compatible). Update all 54+ RxBinding occurrences across the codebase. **Priority: MUST-HAVE**
- **FR-028**: Update Room RxJava adapter to `room-rxjava3`. **Priority: MUST-HAVE**
- **FR-029**: Update Retrofit RxJava call adapter to `adapter-rxjava3`. **Priority: MUST-HAVE**
- **FR-030**: Update Firebase Core (`firebase-core`) to modern Firebase BoM or `firebase-analytics`. **Priority: MUST-HAVE**
- **FR-031**: Update Google Ads SDK (`play-services-ads`) to latest stable version. **Priority: MUST-HAVE**
- **FR-032**: Update EventBus to latest stable version. Remove the duplicate EventBus dependency entry in build.gradle. **Priority: MUST-HAVE**
- **FR-033**: Update Timber to latest stable version. **Priority: SHOULD-HAVE**
- **FR-034**: Update Glide to latest stable version. **Priority: SHOULD-HAVE**
- **FR-035**: Update Dagger/Hilt or other DI framework to latest stable version (if applicable). **Priority: SHOULD-HAVE**
- **FR-036**: Update test orchestrator and test runner dependencies to latest stable versions. **Priority: SHOULD-HAVE**
- **FR-037**: Evaluate and update or remove the `jsr305` dependency (used for `@Nullable`/`@Nonnull` annotations — may be replaceable with Kotlin nullability). **Priority: COULD-HAVE**

### Abandoned Library Replacement

- **FR-038**: Remove WorkManager dependency entirely — zero usage in source code, no feature lost. **Priority: MUST-HAVE**
- **FR-039**: Replace Twitter Serial with kotlinx.serialization across all 9 affected files (58 occurrences). Affected entities: `NoteEntity`, `FolderEntity`, `CheckListObject`, and data transfer between fragments. **Priority: MUST-HAVE**
- **FR-040**: Add kotlinx.serialization plugin and runtime dependency to build.gradle. **Priority: MUST-HAVE**
- **FR-041**: Migrate Okio API calls in `FileUtil.kt` to be compatible with the version of Okio bundled with updated OkHttp/Retrofit (Okio 3.x `okio.Path` / `okio.FileSystem` APIs if needed). **Priority: MUST-HAVE**

### OSS Licenses

- **FR-042**: Complete migration to the Google OSS Licenses Gradle plugin (`com.google.android.gms.oss-licenses-plugin`) if partially migrated, or add it if missing. Ensure the OSS licenses activity is properly registered and accessible. **Priority: SHOULD-HAVE**

### Source Code Fixes

- **FR-043**: Remove all `@DebugLog` annotations from `PrefFragment.kt` and `NewNoteFragment.kt` (Hugo cleanup). **Priority: MUST-HAVE** *(covered by FR-016, listed here for source-code traceability)*
- **FR-044**: Update all RxJava imports from `io.reactivex` to `io.reactivex.rxjava3` across the entire codebase. **Priority: MUST-HAVE** *(covered by FR-024, listed here for source-code traceability)*
- **FR-045**: Replace all Twitter Serial annotations and serializer usage with kotlinx.serialization equivalents (`@Serializable`, `Json.encodeToString`, `Json.decodeFromString`). **Priority: MUST-HAVE** *(covered by FR-039, listed here for source-code traceability)*

### Build Verification

- **FR-046**: Project must compile without errors after all changes. **Priority: MUST-HAVE**
- **FR-047**: Project must produce a signed APK/AAB successfully. **Priority: MUST-HAVE**
- **FR-048**: Zero Kotlin synthetics references remaining in the codebase. **Priority: MUST-HAVE**
- **FR-049**: Zero references to removed libraries (Hugo, WorkManager, Twitter Serial, RxJava 2) remaining in source or build files. **Priority: MUST-HAVE**

### Test Updates

- **FR-050**: Update test dependencies to be compatible with all upgraded libraries (RxJava 3 test helpers, Room testing, etc.). **Priority: MUST-HAVE**
- **FR-051**: Fix any tests broken by the view binding migration. **Priority: MUST-HAVE**
- **FR-052**: Fix any tests broken by the RxJava 3 migration. **Priority: MUST-HAVE**
- **FR-053**: Fix any tests broken by the kotlinx.serialization migration. **Priority: MUST-HAVE**
- **FR-054**: Add JaCoCo plugin for code coverage measurement. **Priority: MUST-HAVE**
- **FR-055**: Measure baseline line coverage before modernization changes. **Priority: MUST-HAVE**
- **FR-056**: Measure line coverage after modernization — must be ≥ baseline. **Priority: MUST-HAVE**

### Final Verification

- **FR-057**: All existing tests pass. **Priority: MUST-HAVE**
- **FR-058**: No new lint errors introduced (lint baseline may be used for pre-existing issues). **Priority: SHOULD-HAVE**
- **FR-059**: App launches and core flows (create note, edit note, delete note, folder operations) work correctly on an emulator or device. **Priority: MUST-HAVE**

## Acceptance Criteria

1. **AC-001**: `./gradlew assembleDebug` completes with zero errors.
2. **AC-002**: `./gradlew assembleRelease` completes with zero errors and produces a valid APK/AAB.
3. **AC-003**: `grep -r "kotlinx.android.synthetic" app/src/` returns zero results.
4. **AC-004**: `grep -r "io.reactivex.rxjava2\|io\.reactivex\.[^r]" app/src/` returns zero results (no RxJava 2 imports remain).
5. **AC-005**: `grep -r "com.twitter.serial" app/src/` returns zero results.
6. **AC-006**: `grep -r "WorkManager\|androidx.work" app/` returns zero results (source and build files).
7. **AC-007**: `grep -r "hugo\|DebugLog" app/src/` returns zero results.
8. **AC-008**: `grep -r "jcenter" build.gradle*` and `settings.gradle*` returns zero results.
9. **AC-009**: `./gradlew test` passes with zero failures.
10. **AC-010**: `./gradlew connectedAndroidTest` passes with zero failures (if instrumented tests exist).
11. **AC-011**: JaCoCo coverage report is generated and post-modernization line coverage ≥ pre-modernization baseline.
12. **AC-012**: No duplicate dependency entries in any build.gradle file.
13. **AC-013**: Proguard/R8 rules reference only libraries that are actually present in the dependency tree.
14. **AC-014**: App installs and launches on an API 28+ emulator; user can create, view, edit, and delete a note.

## Constraints

- **CON-001**: Must maintain backward compatibility with the app's current `minSdkVersion`.
- **CON-002**: Must not change any user-facing behavior or UI.
- **CON-003**: Must not remove any feature that is currently functional.
- **CON-004**: All dependency versions must be stable releases (no alpha/beta/RC) unless no stable version exists for a required API level.
- **CON-005**: Room database schema must not change — preserve both schema export directories.
- **CON-006**: Kotlin synthetics migration must be a direct 1:1 replacement with view binding (no architectural changes like migrating to Compose).
- **CON-007**: RxJava 3 migration must preserve existing reactive chains — no rewriting to coroutines/Flow in this scope.
- **CON-008**: kotlinx.serialization migration must produce byte-compatible or logically equivalent serialized output for existing persisted data (Room entities, fragment arguments).
- **CON-009**: Firebase and Google Ads integration must remain functional after dependency updates.
- **CON-010**: EventBus must be retained and updated — it is actively used (8 files, 29 occurrences).
- **CON-011**: The `google-services` plugin must be retained.
- **CON-012**: No new third-party libraries may be added beyond what is required for direct replacements (kotlinx.serialization, JaCoCo).
- **CON-013**: Changes should be structured in logical, reviewable commits (build system → dependency updates → source migrations → verification).

## Dependencies

- **DEP-001**: Android Gradle Plugin ↔ Gradle wrapper version compatibility matrix.
- **DEP-002**: Kotlin plugin version ↔ AGP version compatibility.
- **DEP-003**: RxJava 3 ↔ RxAndroid 3.x, RxKotlin 3.x, RxBinding 4.x, Room RxJava3 adapter, Retrofit RxJava3 adapter (all must be upgraded together).
- **DEP-004**: kotlinx.serialization runtime ↔ Kotlin compiler plugin version.
- **DEP-005**: Firebase BoM / firebase-analytics ↔ google-services plugin version.
- **DEP-006**: Google Ads SDK ↔ Google Play Services version compatibility.
- **DEP-007**: Okio version bundled with OkHttp/Retrofit ↔ direct Okio usage in FileUtil.kt.
- **DEP-008**: JaCoCo plugin version ↔ AGP/Gradle version compatibility.

## Non-Functional Requirements

- **NFR-001**: Build time must not increase by more than 20% compared to the current build (measured via `./gradlew assembleDebug --profile`).
- **NFR-002**: APK size must not increase by more than 10% compared to the current APK.
- **NFR-003**: Test coverage (line coverage measured by JaCoCo) must not decrease after modernization. Baseline must be measured before changes begin.

## Technical Notes

- RxJava 3 migration cascade: RxJava 3 requires simultaneous updates to RxAndroid (3.x), RxKotlin (3.x), RxBinding (4.x), Room RxJava3 adapter (`androidx.room:room-rxjava3`), and Retrofit RxJava3 adapter (`com.squareup.retrofit2:adapter-rxjava3`). All `io.reactivex` imports become `io.reactivex.rxjava3`. Key API changes: `Flowable`/`Observable`/`Single`/`Maybe`/`Completable` packages move; `Function` → `io.reactivex.rxjava3.functions.Function`; null values are strictly prohibited in streams.
- Twitter Serial → kotlinx.serialization: Replace `@ThriftStruct`/`@ThriftField` (or Serial equivalents) with `@Serializable`. Replace `Serial.serialize()`/`Serial.deserialize()` with `Json.encodeToString()`/`Json.decodeFromString()`. Affected files: NoteEntity, FolderEntity, CheckListObject, and fragment argument serialization (9 files, 58 occurrences). Must verify Room TypeConverters still work if serialization is used for complex column types.
- Okio: Updated OkHttp/Retrofit may bundle Okio 3.x. If `FileUtil.kt` uses Okio 1.x APIs (`okio.Okio.buffer()`, `okio.Okio.source()`), migrate to Okio 3.x equivalents (`source.buffer()`, `path.source()`).
- Hugo `@DebugLog` removal: Simply delete the annotations from `PrefFragment.kt` and `NewNoteFragment.kt`. No replacement needed — Timber logging can be added manually if debug logging is desired.
- `@Parcelize`: The `kotlin-parcelize` plugin can be added for future use, but no source migration is needed since `@Parcelize` has zero usage in the current codebase.
- `-Xmaxerrs` fix: The Kotlin compiler argument `-Xmaxerrs` requires a value (e.g., `-Xmaxerrs=500`). Currently it is specified without a value, which is a bug. Either add the value or remove the argument.
- Nested `allprojects` block: There is an `allprojects {}` block nested inside the `android {}` block in `app/build.gradle`. This is a configuration error — it should be at the project root level or removed from the app module.
- JaCoCo setup: Add `jacoco` plugin to app/build.gradle, configure `jacocoTestReport` task for debug variant, set `testCoverageEnabled = true` for debug build type.

## Scope Exclusions

- **EX-001**: Migration from RxJava to Kotlin Coroutines/Flow — out of scope; RxJava 3 is the target.
- **EX-002**: Migration from View system to Jetpack Compose — out of scope; view binding is the target.
- **EX-003**: Migration from Dagger to Hilt (if applicable) — out of scope unless already partially done.
- **EX-004**: Migration from EventBus to another event system — out of scope; EventBus is retained and updated.
- **EX-005**: Adding new features or changing app behavior — out of scope; this is a modernization/cleanup effort only.
- **EX-006**: `@Parcelize` source migration — no-op, zero usage found. Plugin added for future use only.
- **EX-007**: Social login, analytics rework, or ad mediation changes — out of scope.

## Deferred Items

- None — all clarifying questions were answered by the user.
