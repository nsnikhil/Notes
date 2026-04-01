# Code Review Report: Remove Dead Plugins, Crashlytics/Fabric, and Hugo from Source

## Sub-Task
- ID: ST-003
- Name: Remove Dead Plugins, Crashlytics/Fabric, and Hugo from Source
- Files reviewed: 5 (MyApplication.kt, NewNoteFragment.kt, PrefFragment.kt, app/build.gradle, proguard-rules.pro)

## Critical Issues

1. **Dangling `getRefWatcher` calls — compile error**: `MainActivity.kt:210` and `SplashActivity.kt:42` — Both files still call `MyApplication.getRefWatcher(this)?.watch(this)` in their `onDestroy()` methods, but `getRefWatcher()` and the `RefWatcher` field were removed from `MyApplication.kt`. This will cause an unresolved reference compile error. — Suggested fix: delete the entire `if (BuildConfig.DEBUG) MyApplication.getRefWatcher(this)?.watch(this)` line from both files. LeakCanary v2 auto-detects Activity/Fragment leaks; no manual `watch()` call is needed.

   ```kotlin
   // MainActivity.kt and SplashActivity.kt — remove this line from onDestroy():
   if (BuildConfig.DEBUG) MyApplication.getRefWatcher(this)?.watch(this)
   ```

2. **Leftover `@DebugLog` annotation**: `NewNoteFragment.kt:277` — The `@DebugLog` annotation on `setNote()` was not removed, even though the `import hugo.weaving.DebugLog` was deleted. Without the import this is an unresolved symbol and will cause a compile error. — Suggested fix: delete the `@DebugLog` line at line 277.

   ```kotlin
   // NewNoteFragment.kt — remove this line (277):
   @DebugLog
   ```

## Warnings

None found

## Linter Results

No linter configured (no ktlint or detekt config detected in project).

## Verdict: NEEDS_FIXES

## Feedback for Executor

Two critical issues must be fixed before testing:

1. **Remove dangling `getRefWatcher` calls** in `MainActivity.kt:210` and `SplashActivity.kt:42`. The companion function was removed from `MyApplication.kt` but these two callers were missed. Delete the entire `if (BuildConfig.DEBUG) MyApplication.getRefWatcher(this)?.watch(this)` line from both `onDestroy()` methods. LeakCanary v2 auto-installs and watches Activities/Fragments automatically — no manual instrumentation is needed.

2. **Remove leftover `@DebugLog` annotation** in `NewNoteFragment.kt:277`. The Hugo import was removed but the annotation on `setNote()` was left behind. Delete the `@DebugLog` line.

Both issues cause compile errors and must be fixed before proceeding.
