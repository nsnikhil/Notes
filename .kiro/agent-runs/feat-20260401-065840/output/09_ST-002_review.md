# Code Review Report: Fix Remaining Runtime Issues (CRASH-02, CRASH-04, CRASH-05, CRASH-06, CRASH-07)

## Sub-Task
- ID: ST-002
- Name: Fix Remaining Runtime Issues
- Files reviewed: 6

## Critical Issues
None found

## Warnings
1. Magic string `"is_change_password"` — `PrefFragment.kt:109` and `PasswordDialogFragment.kt:72` — The bundle key `"is_change_password"` is a raw string literal duplicated across two files. If either side changes the key, the communication silently breaks (the boolean defaults to `false`). Consider extracting to a companion object constant in `PasswordDialogFragment` (e.g., `const val ARG_IS_CHANGE_PASSWORD = "is_change_password"`) and referencing it from both files.

2. Deprecated `fallbackToDestructiveMigration()` — `DatabaseModule.kt:55` — The no-arg `fallbackToDestructiveMigration()` is deprecated in Room 2.6+. The replacement is `fallbackToDestructiveMigration(dropAllTables = true)`. This is acceptable for now since the project likely targets an older Room version, but should be updated when Room is upgraded.

3. `lateinit` fields `itemType`/`eventType` uninitialized when `arguments` is null — `PasswordDialogFragment.kt:68-76` — When `PasswordDialogFragment` is shown via `PasswordUtil.showPasswordDialog()` (no arguments), the `if (arguments != null)` block is skipped, leaving `itemType` and `eventType` as uninitialized `lateinit`. Accessing them later would crash. This is a pre-existing issue not introduced by this sub-task, so it is not blocking, but worth noting for a future fix.

4. `activity!!` non-null assertions — `PasswordDialogFragment.kt:79,148` — Using `activity!!` can throw `KotlinNullPointerException` if the fragment is detached. This is pre-existing code not modified by this sub-task, so not blocking.

## Linter Results
- No dedicated Kotlin linter (ktlint/detekt) configured. Android Lint runs as part of `assembleDebug`, which passed successfully.

## Verdict: PASS

## Feedback for Executor
No critical issues found. Code quality is acceptable.

All six changes are correctly implemented and verified:
- ✅ `ContextModule` initialization moved from field initializer to `onCreate()` (CRASH-02)
- ✅ `NetworkComponent` changed from `abstract class` to `interface` (CRASH-04)
- ✅ `fallbackToDestructiveMigration()` added to Room builder (CRASH-05)
- ✅ `java.io.Serializable` removed from `NoteEntity` (CRASH-06)
- ✅ `setTargetFragment`/`targetRequestCode` fully eliminated — grep confirms zero matches in production source (CRASH-07)
- ✅ Bundle-based argument passing between `PrefFragment` and `PasswordDialogFragment` is functionally correct — the `isChangePassword` flag correctly routes to `changePassword()` flow, avoiding uninitialized `itemType`/`eventType` fields
- ✅ Build and tests pass
