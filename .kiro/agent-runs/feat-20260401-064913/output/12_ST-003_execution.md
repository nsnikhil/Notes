# ST-003 Execution Report: Remove Dead Plugins, Crashlytics/Fabric, and Hugo from Source

## Status: COMPLETE

## Summary
Removed all dead library references (Hugo, Fabric/Crashlytics, LeakCanary 1.x, BlockCanary, Stetho, Rollbar) from source code and build files. Cleaned proguard rules of ButterKnife/Dart/Henson entries. Added kotlinx.serialization keep rules. Upgraded LeakCanary to v2 (auto-install).

## Changes Made

### Source Files Modified
1. **`NewNoteFragment.kt`** — Removed `import hugo.weaving.DebugLog` and `@DebugLog` annotation from `setNote()`.
2. **`PrefFragment.kt`** — Removed `import hugo.weaving.DebugLog` and `@DebugLog` annotation from `initializeItems()`.
3. **`MyApplication.kt`** — Removed all dead library init and imports:
   - `Fabric.with(this, Crashlytics())` + imports
   - `Stetho.initializeWithDefaults(this)` + import
   - `LeakCanary.install(this)`, `LeakCanary.isInAnalyzerProcess` guard, `RefWatcher` field, `getRefWatcher()` companion function + imports
   - `BlockCanary.install(this, AppBlockCanaryContext()).start()` + import
   - `Rollbar.init(...)` + import
   - Unused `Context` import

### Build Files Modified
4. **`app/build.gradle`** — Removed dependencies:
   - `releaseImplementation "com.squareup.leakcanary:leakcanary-android-no-op:1.6.2"`
   - `implementation "com.github.markzhai:blockcanary-android:1.5.0"`
   - `implementation 'com.rollbar:rollbar-java:1.2.0'`
   - `implementation 'com.rollbar:rollbar-android:1.2.0@aar'`
   - `implementation 'com.facebook.stetho:stetho:1.5.0'`
   - Upgraded LeakCanary: `debugImplementation "com.squareup.leakcanary:leakcanary-android:2.14"`

5. **`app/proguard-rules.pro`** — Removed ButterKnife and Dart/Henson rules. Added kotlinx.serialization keep rules.

### Files Deleted
6. **`AppBlockCanaryContext.kt`** — Dead code (extends removed `BlockCanaryContext`).

## Exit Criteria Verification
- ✅ Zero references to `hugo.weaving` in source
- ✅ Zero references to `Fabric` in source
- ✅ Zero references to `Crashlytics` in source
- ✅ Zero references to `BlockCanary` in source
- ✅ Zero references to `Stetho` in source
- ✅ Zero references to `Rollbar` in source
- ✅ Zero references to `ButterKnife` in proguard
- ✅ `./gradlew assembleDemoDebug` gets past plugin/dependency resolution (fails only on pre-existing `firebase-jobdispatcher:0.8.5` — expected, fix in ST-010)

## Build Output
```
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':app:dataBindingMergeDependencyArtifactsDemoDebug'.
> Could not resolve all files for configuration ':app:demoDebugCompileClasspath'.
   > Could not find com.firebase:firebase-jobdispatcher:0.8.5.
     Required by:
         project :app > android.arch.work:work-firebase:1.0.0-alpha11
```
This is the expected pre-existing failure (documented in ST-002, to be fixed in ST-010).
