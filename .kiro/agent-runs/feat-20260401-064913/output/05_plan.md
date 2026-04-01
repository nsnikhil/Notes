# Implementation Plan: Dependency Modernization & Build System Upgrade

## Overview

This plan modernizes the Notes Android app from AGP 3.2.1/Gradle 4.6/Kotlin 1.3.11 to AGP 8.7.x/Gradle 8.9/Kotlin 2.1.x, replacing abandoned libraries (Twitter Serial → kotlinx.serialization, RxJava 2 → 3, RxBinding 2 → 4), migrating Kotlin synthetics to view binding across 18 files, removing dead plugins (Hugo, firebase-perf, Fabric), upgrading all AndroidX/Firebase/third-party dependencies, and establishing JaCoCo coverage baselines. The project has 95 Kotlin source files, 24 layout XMLs, 2 product flavors (demo/full), and uses MVVM with Dagger 2, Room, and Navigation Component. Sub-tasks are ordered so each produces a building, testable baseline before the next begins.

## Risk Assessment

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| AGP 3→8 jump breaks build in unexpected ways | High | High | ST-001 focuses solely on build config; verify both flavors compile before proceeding |
| RxJava 2→3 package rename breaks runtime behavior | High | Medium | ST-006 is isolated; run all unit tests after migration |
| Twitter Serial removal breaks note file deserialization | High | Medium | ST-008 replaces with kotlinx.serialization using identical field order; test round-trip |
| View binding migration introduces null-pointer crashes | Medium | Medium | ST-004 migrates systematically by component type; verify each file compiles |
| Room 2.0→2.6 requires schema migration | Medium | Low | Room schema export dir already configured; verify schema JSON unchanged |
| EventBus 3.1→3.3 API compatibility | Low | Low | API is stable; minor version bump only |
| Okio 1→3 API changes in FileUtil | Medium | Medium | ST-009 is isolated to 1 file; `Okio.buffer(Okio.sink(...))` → `file.sink().buffer()` |

## Sub-Tasks

### Sub-Task 1: JaCoCo Baseline Coverage Measurement
- **Size:** S
- **Description:** Add JaCoCo plugin configuration to `app/build.gradle` and capture a baseline coverage report BEFORE any source changes. This establishes the pre-migration coverage number that ST-012 will compare against.
- **Dependencies:** None
- **Entry Criteria:** Project builds successfully with current configuration (`./gradlew assembleDebug`)
- **Exit Criteria:** `./gradlew testDemoDebugUnitTest jacocoTestReport` produces an HTML/XML report under `app/build/reports/jacoco/`; baseline percentage is recorded in a `coverage-baseline.txt` file
- **Success Criteria:**
  - Given the unmodified project, when `./gradlew testDemoDebugUnitTest jacocoTestReport` runs, then a JaCoCo XML report is generated at `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml`
  - Given the generated report, when the line/branch coverage percentage is extracted, then it is written to `coverage-baseline.txt` at the repo root
- **Files:**
  - Modify: `app/build.gradle` (add `apply plugin: 'jacoco'`, jacoco task config targeting debug unit test classes)
  - Create: `coverage-baseline.txt`

### Sub-Task 2: Build System Upgrade (Gradle, AGP, Kotlin, Plugins)
- **Size:** M
- **Description:** Upgrade the core build toolchain: Gradle 4.6→8.9, AGP 3.2.1→8.7.3, Kotlin 1.3.11→2.1.10. Remove dead buildscript plugins (Hugo, Fabric, firebase-perf, firebase-plugins). Remove `jcenter()` from all repository blocks. Remove dead repos (`maven.fabric.io`, `maven.google.com` duplicate). Replace `kotlin-android-extensions` with `kotlin-parcelize`. Enable view binding. Fix the nested `allprojects` block inside `android {}`. Fix `-Xmaxerrs` (missing value). Remove `enableCrashlytics` ext. Remove `androidExtensions { experimental = true }`. Update `google-services` plugin, `oss-licenses` plugin, and `navigation-safe-args` plugin to current versions. Update `gradle-wrapper.properties` distribution URL. Update `gradle.properties` (remove `MaxPermSize`, add `android.nonTransitiveRClass=true`). Update `compileSdk`/`targetSdk` to 34. Update `settings.gradle` with `pluginManagement` block.
- **Dependencies:** Sub-task 1
- **Entry Criteria:** JaCoCo baseline captured; current project state is unmodified source
- **Exit Criteria:** `./gradlew assembleDebug` succeeds for both demo and full flavors (compilation errors from removed synthetics/libraries are expected and acceptable — this sub-task only changes build config, not source)
- **Success Criteria:**
  - Given the updated build files, when `./gradlew assembleDebug` is run, then Gradle 8.9 is downloaded and AGP 8.7.3 is used
  - Given the updated `build.gradle`, when inspected, then no references to `jcenter()`, `maven.fabric.io`, Hugo plugin, `kotlin-android-extensions`, `firebase-perf` plugin, or `enableCrashlytics` exist
  - Given `gradle-wrapper.properties`, when read, then `distributionUrl` points to `gradle-8.9-all.zip`
  - Given `app/build.gradle`, when inspected, then `buildFeatures { viewBinding true }` is present and `compileSdk`/`targetSdk` are 34
- **Files:**
  - Modify: `build.gradle` (root — buildscript deps, repositories)
  - Modify: `app/build.gradle` (plugins, android block, compileSdk, targetSdk, buildFeatures, remove androidExtensions, fix allprojects/Xmaxerrs)
  - Modify: `gradle/wrapper/gradle-wrapper.properties` (distribution URL)
  - Modify: `gradle.properties` (remove MaxPermSize, add nonTransitiveRClass)
  - Modify: `settings.gradle` (add pluginManagement, remove jcenter)

### Sub-Task 3: Remove Dead Plugins, Crashlytics/Fabric, and Hugo from Source
- **Size:** S
- **Description:** Remove all Hugo `@DebugLog` annotations and `hugo.weaving.DebugLog` imports from source files (2 files: `NewNoteFragment.kt`, `PrefFragment.kt`). Remove Fabric/Crashlytics initialization from `MyApplication.kt` (`Fabric.with(...)`, Crashlytics import). Remove LeakCanary 1.x initialization (API changed completely in 2.x — it auto-installs). Remove BlockCanary initialization. Remove Stetho initialization. Remove Rollbar initialization. Clean `proguard-rules.pro` of ButterKnife, Dart/Henson rules (not used). Update proguard to keep kotlinx.serialization classes (prep for ST-008).
- **Dependencies:** Sub-task 2
- **Entry Criteria:** Build system upgrade complete; Gradle sync succeeds
- **Exit Criteria:** Zero references to `hugo.weaving`, `Fabric`, `Crashlytics`, `BlockCanary`, `Stetho`, `Rollbar`, or ButterKnife in source or proguard; `./gradlew assembleDebug` succeeds
- **Success Criteria:**
  - Given `NewNoteFragment.kt` and `PrefFragment.kt`, when searched for `@DebugLog` or `hugo.weaving`, then zero matches are found
  - Given `MyApplication.kt`, when searched for `Fabric`, `Crashlytics`, `BlockCanary`, `Stetho`, or `Rollbar`, then zero matches are found
  - Given `proguard-rules.pro`, when searched for `butterknife` or `f2prateek.dart`, then zero matches are found
  - Given the project, when `./gradlew assembleDebug` runs, then it succeeds
- **Files:**
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt` (remove `@DebugLog`, `hugo.weaving.DebugLog` import)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/PrefFragment.kt` (remove `@DebugLog`, `hugo.weaving.DebugLog` import)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/MyApplication.kt` (remove Fabric, Crashlytics, LeakCanary, BlockCanary, Stetho, Rollbar init and imports)
  - Modify: `app/build.gradle` (remove `crashlytics`, `leakcanary`, `blockcanary`, `stetho`, `rollbar`, `fabric` dependencies)
  - Modify: `app/proguard-rules.pro` (remove ButterKnife/Dart rules, add kotlinx.serialization keep rules)

### Sub-Task 4: Migrate Kotlin Synthetics to View Binding
- **Size:** L
- **Description:** Replace all `kotlinx.android.synthetic.*` imports with Android View Binding across 18 source files. For Activities: inflate binding in `onCreate`, replace `setContentView(R.layout.x)` with `setContentView(binding.root)`, replace all synthetic view references with `binding.viewId`. For Fragments: inflate binding in `onCreateView`, null out `_binding` in `onDestroyView`, replace synthetic references. For Adapters: inflate binding in `onCreateViewHolder`, store in ViewHolder. For DialogFragments: inflate binding in `onCreateView` or `onCreateDialog`. Remove the `androidExtensions` block if still present. Zero `kotlinx.android.synthetic` imports must remain.
- **Justification for L size:** This touches 18 files across 4 component types (Activity, Fragment, Adapter, DialogFragment) but is a single mechanical concern (synthetic→binding). Cannot be split further without creating file-ownership conflicts since fragments reference the same binding patterns.
- **Dependencies:** Sub-task 2
- **Entry Criteria:** View binding enabled in `app/build.gradle` (from ST-002); build system upgraded
- **Exit Criteria:** `grep -r "kotlinx.android.synthetic" app/src/` returns zero results; `./gradlew assembleDebug` succeeds for both flavors
- **Success Criteria:**
  - Given all Kotlin source files, when searched for `kotlinx.android.synthetic`, then zero matches are found
  - Given `MainActivity.kt`, when inspected, then it uses `ActivityMainBinding.inflate(layoutInflater)` and references views via `binding.mainToolbar`, `binding.mainDrawerLayout`, etc.
  - Given `ListFragment.kt`, when inspected, then it uses `FragmentListBinding.inflate()` in `onCreateView`, nulls `_binding` in `onDestroyView`, and references `binding.commonList`, `binding.fabAdd`, etc.
  - Given `NotesAdapter.kt`, when inspected, then ViewHolder uses `SingleNoteLayoutBinding` (or equivalent) inflated in `onCreateViewHolder`
  - Given the project, when `./gradlew assembleDebug` runs for both `demo` and `full` flavors, then it succeeds
- **Files:**
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/MainActivity.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/ListFragment.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/PrefFragment.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/AboutFragment.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/SearchFragment.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/IntroFragment.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/ImageListFragment.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/dialogFragments/MoveListDialogFragment.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/dialogFragments/PasswordDialogFragment.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/dialogFragments/ColorPickerDialogFragment.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/dialogFragments/CreateFolderDialog.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/adapters/NotesAdapter.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/adapters/ImageAdapter.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/adapters/IntroAdapter.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/adapters/FolderListAdapter.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/adapters/AudioListAdapter.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/adapters/CheckListAdapter.kt`
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/adapters/ColorPickerAdapter.kt`

### Sub-Task 5: Upgrade AndroidX, Room, Lifecycle, Navigation, Paging Dependencies
- **Size:** M
- **Description:** Upgrade all AndroidX libraries to current stable versions: `core-ktx` 1.0.1→1.15.0, `fragment-ktx` 1.0.0→1.8.5, `appcompat` 1.0.2→1.7.0, `recyclerview` 1.0.0→1.3.2, `annotation` 1.0.1→1.9.1, `webkit` 1.0.0→1.12.1, `material` 1.0.0→1.12.0, `constraintlayout` 1.1.3→2.2.0, `palette` 1.0.0→1.0.0 (keep), `cardview` 1.0.0→1.0.0 (keep), `preference` 1.0.0→1.2.1, `browser` 1.0.0→1.8.0. Upgrade Room 2.0.0→2.6.1 (update `room-runtime`, `room-compiler`, `room-rxjava2`→`room-rxjava3`, `room-guava`, `room-testing`). Upgrade Lifecycle 2.0.0→2.8.7 (`lifecycle-extensions` is deprecated — replace with specific artifacts: `lifecycle-viewmodel-ktx`, `lifecycle-livedata-ktx`, `lifecycle-runtime-ktx`, `lifecycle-common-java8`→`lifecycle-common`). Upgrade Navigation from `android.arch.navigation` 1.0.0-alpha→`androidx.navigation` 2.8.5. Upgrade Paging 2.0.0→3.3.5 (`paging-runtime`, `paging-rxjava2`→`paging-rxjava3`). Upgrade `jsr305` 3.0.2→3.0.2 (keep). Remove `legacy-preference-v14`. Remove `collection-ktx` (merged into core-ktx). Update `arch.core:core-testing`→`androidx.arch.core:core-testing:2.2.0`.
- **Dependencies:** Sub-task 4
- **Entry Criteria:** View binding migration complete; project compiles with no synthetic imports
- **Exit Criteria:** `./gradlew assembleDebug` succeeds; no `android.arch.*` imports remain in build files; all AndroidX deps are at target versions
- **Success Criteria:**
  - Given `app/build.gradle`, when inspected, then all AndroidX dependencies are at specified target versions
  - Given `app/build.gradle`, when searched for `android.arch.`, then zero matches are found
  - Given `app/build.gradle`, when searched for `lifecycle-extensions`, then zero matches are found (replaced with specific artifacts)
  - Given the project, when `./gradlew assembleDebug` runs, then it succeeds for both flavors
- **Files:**
  - Modify: `app/build.gradle` (all AndroidX dependency version bumps, Room/Lifecycle/Navigation/Paging artifact changes)
  - Modify: `build.gradle` (root — navigation safe-args plugin version)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/ListFragment.kt` (if `ViewModelProviders.of()` → `ViewModelProvider()`)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt` (if `ViewModelProviders.of()` → `ViewModelProvider()`)
  - Modify: Any files using deprecated `lifecycle-extensions` APIs

### Sub-Task 6: RxJava 2→3 Full Cascade Migration
- **Size:** L
- **Description:** Replace RxJava 2 with RxJava 3 across the entire project. In `app/build.gradle`: replace `io.reactivex.rxjava2:rxjava` with `io.reactivex.rxjava3:rxjava:3.1.10`, `rxandroid` 2→3.0.2, `rxkotlin` 2→3.0.1. Replace RxBinding 2 (`com.jakewharton.rxbinding2:*`) with RxBinding 4 (`com.jakewharton.rxbinding4:*:4.0.0`) — note artifact name changes: `rxbinding-support-v4-kotlin`→removed (use `rxbinding-core`), `rxbinding-appcompat-v7-kotlin`→`rxbinding-appcompat`, `rxbinding-design-kotlin`→`rxbinding-material`, `rxbinding-recyclerview-v7-kotlin`→`rxbinding-recyclerview`. Replace Retrofit `adapter-rxjava2` with `adapter-rxjava3`. In all 16 source files using RxJava: change `io.reactivex.` imports to `io.reactivex.rxjava3.`. In all 13 RxBinding files: change `com.jakewharton.rxbinding2.` to `com.jakewharton.rxbinding4.`. Update Room dependency from `room-rxjava2` to `room-rxjava3`. Update Paging from `paging-rxjava2` to `paging-rxjava3`. Update Lifecycle `lifecycle-reactivestreams` to `lifecycle-reactivestreams-ktx`.
- **Justification for L size:** RxJava package rename cascades through 16 source files + 5 Gradle dependency lines + RxBinding artifact renames. This is a single atomic concern (RxJava version) but with wide blast radius. Splitting would create partial-migration states that don't compile.
- **Dependencies:** Sub-task 5
- **Entry Criteria:** All AndroidX deps upgraded; project compiles
- **Exit Criteria:** Zero `io.reactivex.rxjava2` or `com.jakewharton.rxbinding2` references in source or build files; `./gradlew assembleDebug` succeeds
- **Success Criteria:**
  - Given all Kotlin source files, when searched for `io.reactivex.` (without `rxjava3`), then zero matches are found
  - Given all Kotlin source files, when searched for `com.jakewharton.rxbinding2`, then zero matches are found
  - Given `app/build.gradle`, when searched for `rxjava2`, then zero matches are found
  - Given `FileUtil.kt`, when inspected, then all RxJava imports use `io.reactivex.rxjava3.*` package
  - Given `ListFragment.kt`, when inspected, then RxBinding imports use `com.jakewharton.rxbinding4.*`
  - Given the project, when `./gradlew assembleDebug` runs, then it succeeds for both flavors
- **Files:**
  - Modify: `app/build.gradle` (RxJava/RxBinding/Retrofit-adapter/Room-rx/Paging-rx dependency changes)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/util/FileUtil.kt` (RxJava imports)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/util/DbUtil.kt` (RxJava imports)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/util/PasswordUtil.kt` (RxJava imports)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/ListFragment.kt` (RxBinding imports)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/PrefFragment.kt` (RxBinding imports)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt` (RxBinding imports)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/AboutFragment.kt` (RxBinding imports)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/IntroFragment.kt` (RxBinding imports)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/dialogFragments/PasswordDialogFragment.kt` (RxBinding imports)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/dialogFragments/CreateFolderDialog.kt` (RxBinding imports)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/adapters/NotesAdapter.kt` (RxBinding imports)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/adapters/ImageAdapter.kt` (RxBinding imports)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/adapters/FolderListAdapter.kt` (RxBinding imports)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/adapters/AudioListAdapter.kt` (RxBinding imports)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/adapters/CheckListAdapter.kt` (RxBinding imports)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/adapters/ColorPickerAdapter.kt` (RxBinding imports)

### Sub-Task 7: Upgrade EventBus, Timber, Glide, Dagger, and Remaining Third-Party Deps
- **Size:** M
- **Description:** Upgrade remaining third-party libraries: EventBus 3.1.1→3.3.1, Timber 4.7.1→5.0.1, Glide 4.7.1→4.16.0 (update both `glide` and `compiler`), Dagger 2.16→2.53.1 (update `dagger`, `dagger-compiler`, `dagger-android`, `dagger-android-support`, `dagger-android-processor`). Update test dependencies: JUnit 4.12→4.13.2, Mockito 2.23.0→5.14.2, Robolectric 3.8→4.14.1, Espresso 3.1.1→3.6.1, `androidx.test.ext:junit` 1.1.0→1.2.1, `androidx.test:rules` 1.1.1→1.6.1, `androidx.test:orchestrator` 1.1.1→1.5.1. Update Firebase: `firebase-core`→`firebase-analytics` (renamed), `firebase-ads`→remove (use `play-services-ads` only), `play-services-ads` 17.1.2→23.6.0. Update `play-services-oss-licenses` 15.0.1→17.1.0. Update OkHttp 3.10.0→4.12.0, Retrofit 2.4.0→2.11.0, Gson 2.8.5→2.11.0. Remove `org.jetbrains:annotations` (redundant with Kotlin). Remove Branch SDK (or update if needed). Remove `fileTree(include: ['*.jar'], dir: 'libs')`.
- **Dependencies:** Sub-task 6
- **Entry Criteria:** RxJava 3 migration complete; project compiles
- **Exit Criteria:** All third-party dependencies at target versions; `./gradlew assembleDebug` and `./gradlew testDemoDebugUnitTest` succeed
- **Success Criteria:**
  - Given `app/build.gradle`, when inspected, then Dagger is at 2.53.1, Glide at 4.16.0, Timber at 5.0.1, EventBus at 3.3.1
  - Given `app/build.gradle`, when searched for `firebase-core`, then zero matches (replaced with `firebase-analytics`)
  - Given `app/build.gradle`, when searched for `leakcanary`, then only `leakcanary-android:2.x` debug dependency exists (auto-installs, no code needed)
  - Given the project, when `./gradlew assembleDebug` runs, then it succeeds for both flavors
  - Given the project, when `./gradlew testDemoDebugUnitTest` runs, then existing tests pass
- **Files:**
  - Modify: `app/build.gradle` (all third-party dependency version bumps and artifact renames)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/MyApplication.kt` (remove LeakCanary manual init, update Firebase imports if needed)

### Sub-Task 8: Replace Twitter Serial with kotlinx.serialization
- **Size:** M
- **Description:** Remove `com.twitter.serial:serial:0.1.6` dependency and replace all serialization logic with `kotlinx.serialization`. Add `org.jetbrains.kotlin.plugin.serialization` plugin to `app/build.gradle` and `kotlinx-serialization-json` dependency. In `NoteEntity.kt`: remove the `SERIALIZER` companion object and `NoteEntityObjectSerializer` class, remove all `com.twitter.serial.*` imports, add `@Serializable` annotation (Room entity stays as-is; serialization is used only for Bundle passing via `ByteBufferSerial`). Replace `ByteBufferSerial().toByteArray(entity, SERIALIZER)` calls with `Json.encodeToString()` + `toByteArray()`. Replace `ByteBufferSerial().fromByteArray(bytes, SERIALIZER)` with `Json.decodeFromString()`. Same treatment for `FolderEntity.kt`, `CheckListObject.kt`. Update all consumer files: `ListFragment.kt` (2 usages), `NewNoteFragment.kt` (3 usages), `ImageListFragment.kt` (3 usages), `CreateFolderDialog.kt` (1 usage). Add custom `DateSerializer` for `java.util.Date` field in `NoteEntity`.
- **Dependencies:** Sub-task 6
- **Entry Criteria:** RxJava 3 migration complete; project compiles
- **Exit Criteria:** Zero `com.twitter.serial` references in source or build files; `./gradlew assembleDebug` succeeds
- **Success Criteria:**
  - Given all Kotlin source files, when searched for `com.twitter.serial` or `ByteBufferSerial`, then zero matches are found
  - Given `app/build.gradle`, when searched for `twitter`, then zero matches are found
  - Given `NoteEntity.kt`, when inspected, then it has `@Serializable` annotation and no `ObjectSerializer` inner class
  - Given `CheckListObject.kt`, when inspected, then it has `@Serializable` annotation and no `ObjectSerializer` inner class
  - Given `ListFragment.kt`, when the `openNoteEditor` method is inspected, then it uses `Json.encodeToString(NoteEntity.serializer(), noteEntity).toByteArray()` (or equivalent) instead of `ByteBufferSerial`
  - Given the project, when `./gradlew assembleDebug` runs, then it succeeds
- **Files:**
  - Modify: `app/build.gradle` (add serialization plugin, add `kotlinx-serialization-json` dep, remove `twitter-serial` dep)
  - Modify: `build.gradle` (root — add `kotlin-serialization` classpath if using legacy plugin application)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/data/NoteEntity.kt` (replace Twitter Serial with @Serializable)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/data/FolderEntity.kt` (replace Twitter Serial with @Serializable)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/model/CheckListObject.kt` (replace Twitter Serial with @Serializable)
  - Create: `app/src/main/java/com/nrs/nsnik/notes/util/DateSerializer.kt` (custom kotlinx.serialization serializer for java.util.Date)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/ListFragment.kt` (replace ByteBufferSerial usage)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt` (replace ByteBufferSerial usage)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/ImageListFragment.kt` (replace ByteBufferSerial usage)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/view/fragments/dialogFragments/CreateFolderDialog.kt` (replace ByteBufferSerial usage)

### Sub-Task 9: Okio 1→3 API Migration
- **Size:** S
- **Description:** Upgrade `com.squareup.okio:okio` from 1.15.0 to 3.9.1 in `app/build.gradle`. Migrate `FileUtil.kt` from Okio 1 API to Okio 3 API: `Okio.buffer(Okio.sink(file))` → `file.sink().buffer()`, `Okio.buffer(Okio.source(file))` → `file.source().buffer()`, `okio.Buffer` → `okio.Buffer` (same), `okio.ByteString` → `okio.ByteString` (same). Import paths change from `okio.Okio` (removed in Okio 3) to extension functions on `File`/`Path`.
- **Dependencies:** Sub-task 6
- **Entry Criteria:** RxJava 3 migration complete; project compiles
- **Exit Criteria:** `FileUtil.kt` uses Okio 3 API; `./gradlew assembleDebug` succeeds
- **Success Criteria:**
  - Given `FileUtil.kt`, when searched for `Okio.buffer` or `Okio.sink` or `Okio.source`, then zero matches are found
  - Given `FileUtil.kt`, when inspected, then it uses `file.sink().buffer()` and `file.source().buffer()` extension functions
  - Given `app/build.gradle`, when the okio dependency is inspected, then it is `com.squareup.okio:okio:3.9.1`
  - Given the project, when `./gradlew assembleDebug` runs, then it succeeds
- **Files:**
  - Modify: `app/build.gradle` (okio version bump)
  - Modify: `app/src/main/java/com/nrs/nsnik/notes/util/FileUtil.kt` (Okio API migration)

### Sub-Task 10: Remove WorkManager and Update OSS Licenses
- **Size:** S
- **Description:** Remove WorkManager dependencies (`android.arch.work:work-runtime-ktx`, `android.arch.work:work-firebase`) from `app/build.gradle` — they are not used in any source file (confirmed by grep). Update `com.google.android.gms:play-services-oss-licenses` to 17.1.0 and the `com.google.gms.oss.licenses.plugin` to current version. Remove the `exclude group: 'com.google.guava'` workaround that was only needed for WorkManager.
- **Dependencies:** Sub-task 7
- **Entry Criteria:** Third-party deps upgraded; project compiles
- **Exit Criteria:** Zero WorkManager references in build files; OSS licenses plugin at current version; `./gradlew assembleDebug` succeeds
- **Success Criteria:**
  - Given `app/build.gradle`, when searched for `work-runtime` or `work-firebase`, then zero matches are found
  - Given `app/build.gradle`, when the oss-licenses dependency is inspected, then it is at 17.1.0
  - Given the project, when `./gradlew assembleDebug` runs, then it succeeds
- **Files:**
  - Modify: `app/build.gradle` (remove WorkManager deps, update OSS licenses)
  - Modify: `build.gradle` (root — update oss-licenses plugin version if needed)

### Sub-Task 11: Fix Tests for Updated Dependencies
- **Size:** M
- **Description:** Update test source files to work with the new dependency versions. `EncryptionUnitTest.kt` should still pass as-is (pure JDK crypto, no Android deps). Update any androidTest files that reference deprecated APIs from Espresso, AndroidX Test, or Navigation testing. If Robolectric 4.x requires `@RunWith(RobolectricTestRunner::class)` or `testOptions.unitTests.includeAndroidResources = true`, add it. Ensure `testInstrumentationRunner` is still `androidx.test.runner.AndroidJUnitRunner`. Verify `./gradlew testDemoDebugUnitTest` passes. Verify `./gradlew testFullDebugUnitTest` passes.
- **Dependencies:** Sub-task 7, Sub-task 8, Sub-task 9, Sub-task 10
- **Entry Criteria:** All dependency upgrades and library replacements complete; project compiles
- **Exit Criteria:** `./gradlew testDemoDebugUnitTest` and `./gradlew testFullDebugUnitTest` pass with zero failures
- **Success Criteria:**
  - Given the project, when `./gradlew testDemoDebugUnitTest` runs, then all tests pass
  - Given the project, when `./gradlew testFullDebugUnitTest` runs, then all tests pass
  - Given `EncryptionUnitTest.kt`, when run individually, then it passes
  - Given `app/build.gradle`, when the test runner is inspected, then it is `androidx.test.runner.AndroidJUnitRunner`
- **Files:**
  - Modify: `app/build.gradle` (add `testOptions.unitTests.includeAndroidResources = true` if needed)
  - Modify: `app/src/test/java/com/nrs/nsnik/notes/EncryptionUnitTest.kt` (only if needed for Robolectric 4.x compat)
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/MainActivityScreenTest.kt` (update deprecated test APIs if needed)
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/ListFragmentScreenTest.kt` (update deprecated test APIs if needed)
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/NewNoteFragmentScreenTest.kt` (update deprecated test APIs if needed)
  - Modify: `app/src/androidTest/java/com/nrs/nsnik/notes/AboutFragmentScreenTest.kt` (update deprecated test APIs if needed)

### Sub-Task 12: Final Verification and Post-Migration Coverage Check
- **Size:** S
- **Description:** Run comprehensive verification: (1) `./gradlew assembleDemoDebug` succeeds, (2) `./gradlew assembleFullDebug` succeeds, (3) `./gradlew assembleDemoRelease` succeeds, (4) `./gradlew assembleFullRelease` succeeds, (5) `./gradlew testDemoDebugUnitTest` passes, (6) `./gradlew testFullDebugUnitTest` passes, (7) `./gradlew lint` produces no new errors, (8) `./gradlew testDemoDebugUnitTest jacocoTestReport` generates coverage report, (9) post-migration coverage ≥ baseline from `coverage-baseline.txt`, (10) grep verification: zero `kotlinx.android.synthetic`, zero `com.twitter.serial`, zero `io.reactivex.rxjava2` (without rxjava3), zero `hugo.weaving`, zero `com.jakewharton.rxbinding2`, zero `jcenter()` references. Record final coverage in `coverage-baseline.txt` (append post-migration line).
- **Dependencies:** Sub-task 11
- **Entry Criteria:** All sub-tasks 1–11 complete; all tests pass
- **Exit Criteria:** All verification checks pass; coverage report generated; no regressions
- **Success Criteria:**
  - Given the project, when `./gradlew assembleDemoDebug assembleFullDebug` runs, then both succeed producing APKs
  - Given the project, when `./gradlew assembleDemoRelease assembleFullRelease` runs, then both succeed
  - Given the project, when `./gradlew testDemoDebugUnitTest testFullDebugUnitTest` runs, then all tests pass
  - Given the project, when `./gradlew lint` runs, then no new lint errors are introduced (warnings acceptable)
  - Given the project source, when searched for removed library imports (`kotlinx.android.synthetic`, `com.twitter.serial`, `io.reactivex.rxjava2`, `hugo.weaving`, `com.jakewharton.rxbinding2`), then zero matches are found in any file
  - Given the JaCoCo report, when post-migration coverage is compared to `coverage-baseline.txt`, then coverage is ≥ baseline
- **Files:**
  - Modify: `coverage-baseline.txt` (append post-migration coverage line)

## Implementation Order

1. **Sub-task 1** — JaCoCo Baseline (no dependencies)
2. **Sub-task 2** — Build System Upgrade (depends on 1)
3. **Sub-task 3** — Remove Dead Plugins & Hugo from Source (depends on 2)
4. **Sub-task 4** — Migrate Kotlin Synthetics to View Binding (depends on 2)
5. **Sub-task 5** — Upgrade AndroidX/Room/Lifecycle/Navigation/Paging (depends on 4)
6. **Sub-task 6** — RxJava 2→3 Full Cascade Migration (depends on 5)
7. **Sub-task 7** — Upgrade EventBus/Timber/Glide/Dagger/Third-Party (depends on 6)
8. **Sub-task 8** — Replace Twitter Serial with kotlinx.serialization (depends on 6)
9. **Sub-task 9** — Okio 1→3 API Migration (depends on 6)
10. **Sub-task 10** — Remove WorkManager & Update OSS Licenses (depends on 7)
11. **Sub-task 11** — Fix Tests for Updated Dependencies (depends on 7, 8, 9, 10)
12. **Sub-task 12** — Final Verification & Post-Migration Coverage (depends on 11)

**Parallelizable groups:**
- ST-3 and ST-4 can run in parallel (both depend only on ST-2, non-overlapping files)
- ST-8 and ST-9 can run in parallel (both depend on ST-6, non-overlapping files except `app/build.gradle` — coordinate dependency section edits)

## Integration Test Plan

After all sub-tasks are complete, the IntegrationTester agent should verify:

### Build Verification
- `./gradlew assembleDemoDebug` produces a valid APK at `app/build/outputs/apk/demo/debug/`
- `./gradlew assembleFullDebug` produces a valid APK at `app/build/outputs/apk/full/debug/`
- `./gradlew assembleDemoRelease` produces a valid APK (may be unsigned)
- `./gradlew assembleFullRelease` produces a valid APK (may be unsigned)
- APK file sizes are within reasonable range of original (±30%)

### Test Verification
- `./gradlew testDemoDebugUnitTest` — all unit tests pass
- `./gradlew testFullDebugUnitTest` — all unit tests pass
- JaCoCo coverage report generates successfully and coverage ≥ baseline

### Dead Code Verification (grep checks)
- `grep -r "kotlinx.android.synthetic" app/src/` → 0 matches
- `grep -r "com.twitter.serial" app/src/` → 0 matches
- `grep -r "ByteBufferSerial" app/src/` → 0 matches
- `grep -r "io.reactivex.rxjava2" app/src/` → 0 matches (note: `io.reactivex.rxjava3` is expected)
- `grep -r "com.jakewharton.rxbinding2" app/src/` → 0 matches
- `grep -r "hugo.weaving" app/src/` → 0 matches
- `grep -r "@DebugLog" app/src/` → 0 matches
- `grep -r "jcenter" build.gradle settings.gradle` → 0 matches
- `grep -r "maven.fabric.io" build.gradle` → 0 matches
- `grep -r "com.crashlytics" app/src/ app/build.gradle` → 0 matches
- `grep -r "io.fabric" app/build.gradle build.gradle` → 0 matches
- `grep -r "firebase-perf" app/build.gradle build.gradle` → 0 matches
- `grep -r "work-runtime\|work-firebase" app/build.gradle` → 0 matches
- `grep -r "android.arch." app/build.gradle` → 0 matches

### Lint Verification
- `./gradlew lint` produces no new errors (baseline comparison)

### Edge Cases
- Verify `proguard-rules.pro` has no rules for removed libraries (ButterKnife, Dart)
- Verify `proguard-rules.pro` has keep rules for kotlinx.serialization
- Verify `gradle.properties` does not contain `MaxPermSize`
- Verify `gradle-wrapper.properties` points to Gradle 8.9
- Verify `settings.gradle` has `pluginManagement` block
- Verify no duplicate repository declarations in root `build.gradle`

## Requirements Traceability

| Requirement | Sub-Task(s) | Description |
|-------------|-------------|-------------|
| FR-001 | ST-002 | Upgrade AGP to 8.7.x |
| FR-002 | ST-002 | Upgrade Gradle wrapper to 8.9 |
| FR-003 | ST-002 | Upgrade Kotlin to 2.1.x |
| FR-004 | ST-002 | Enable `buildFeatures { viewBinding true }` |
| FR-005 | ST-002 | Remove `kotlin-android-extensions` plugin |
| FR-006 | ST-002 | Add `kotlin-parcelize` plugin |
| FR-007 | ST-002 | Fix nested `allprojects` block inside `android {}` |
| FR-008 | ST-002 | Fix `-Xmaxerrs` missing value |
| FR-009 | ST-002 | Remove Hugo plugin from buildscript |
| FR-010 | ST-002 | Retain `google-services` plugin (update version) |
| FR-011 | ST-002 | Remove `firebase-perf` plugin |
| FR-012 | ST-003 | Clean proguard of dead rules |
| FR-013 | ST-002 | Remove `enableCrashlytics` ext property |
| FR-014 | ST-002 | Remove `jcenter()` from all repository blocks |
| FR-015 | ST-002 | Remove dead repos (maven.fabric.io, duplicate maven.google.com) |
| FR-016 | ST-003 | Remove Hugo plugin + `@DebugLog` annotations from source |
| FR-017 | ST-004 | Migrate Activity synthetic imports to view binding |
| FR-018 | ST-004 | Migrate Fragment synthetic imports to view binding |
| FR-019 | ST-004 | Migrate Adapter synthetic imports to view binding |
| FR-020 | ST-004 | Migrate custom View synthetic imports to view binding |
| FR-021 | ST-004 | Zero remaining `kotlinx.android.synthetic` references |
| FR-022 | ST-005 | Upgrade AndroidX core-ktx, fragment-ktx, appcompat |
| FR-023 | ST-005 | Upgrade Room to 2.6.x |
| FR-024 | ST-006 | Upgrade RxJava 2→3 |
| FR-025 | ST-006 | Upgrade RxAndroid 2→3 |
| FR-026 | ST-006 | Upgrade RxKotlin 2→3 |
| FR-027 | ST-006 | Upgrade RxBinding 2→4 |
| FR-028 | ST-006 | Update Retrofit RxJava adapter 2→3 |
| FR-029 | ST-006 | Update Room RxJava2 artifact → RxJava3 |
| FR-030 | ST-006 | Update Paging RxJava2 artifact → RxJava3 |
| FR-031 | ST-006 | Update Lifecycle reactivestreams |
| FR-032 | ST-007 | Upgrade Firebase/Ads dependencies |
| FR-033 | ST-007 | Upgrade EventBus 3.1→3.3 |
| FR-034 | ST-007 | Upgrade Timber 4.7→5.0 |
| FR-035 | ST-007 | Upgrade Glide 4.7→4.16 |
| FR-036 | ST-007 | Upgrade Dagger 2.16→2.53 |
| FR-037 | ST-007 | Upgrade test orchestrator and test deps |
| FR-038 | ST-010 | Remove WorkManager dependencies |
| FR-039 | ST-008 | Replace Twitter Serial with kotlinx.serialization |
| FR-040 | ST-009 | Migrate Okio 1→3 API |
| FR-041 | ST-008 | Remove all Twitter Serial serializer classes |
| FR-042 | ST-010 | Update OSS Licenses plugin |
| FR-043 | ST-003 | Remove Hugo `@DebugLog` from source files |
| FR-044 | ST-006 | Update RxJava import paths in all source files |
| FR-045 | ST-008 | Replace Twitter Serial usage in all consumer files |
| FR-046 | ST-012 | Verify both flavors compile |
| FR-047 | ST-012 | Verify APKs are produced |
| FR-048 | ST-012 | Verify zero synthetic references |
| FR-049 | ST-012 | Verify zero removed library references |
| FR-050 | ST-011 | Update test dependencies for compatibility |
| FR-051 | ST-011 | Fix tests for view binding changes |
| FR-052 | ST-011 | Fix tests for RxJava 3 changes |
| FR-053 | ST-011 | Fix tests for kotlinx.serialization changes |
| FR-054 | ST-001 | Set up JaCoCo plugin and task configuration |
| FR-055 | ST-001 | Capture baseline coverage measurement |
| FR-056 | ST-012 | Capture post-migration coverage and compare |
| FR-057 | ST-012 | All tests pass (unit + both flavors) |
| FR-058 | ST-012 | No new lint errors |
| FR-059 | ST-012 | App launches and core flows work |
