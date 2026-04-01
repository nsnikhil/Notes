# Code Review Report: Fix Critical Launch-Blocking Crashes

## Sub-Task
- ID: ST-001
- Name: Fix Critical Launch-Blocking Crashes
- Files reviewed: 3

## Critical Issues
None found

## Warnings

1. **DRY violation**: `MainActivity.kt`:103,108,129,130,141,146,153 — The expression `(supportFragmentManager.findFragmentById(R.id.mainNavHost) as NavHostFragment).navController` is repeated 7 times after the initial assignment to the `controller` field on line 75. All subsequent usages should reference `controller` instead of re-deriving the NavController each time. This reduces duplication and avoids redundant fragment lookups.

   Suggested fix — replace all inline occurrences with the existing field:
   ```kotlin
   // Before (lines 103, 108, 129, 130, 141, 146, 153):
   (supportFragmentManager.findFragmentById(R.id.mainNavHost) as NavHostFragment).navController.navigate(...)

   // After:
   controller.navigate(...)
   ```

2. **Broad exception catch**: `MyApplication.kt`:55 — `catch (e: Exception)` catches all exceptions including programming errors (e.g., `NullPointerException`, `IllegalStateException`). For a defensive Firebase init wrapper this is acceptable, but catching `IllegalStateException` specifically (the documented Firebase failure mode) would be more precise.

3. **Pre-existing: unsafe cast**: `MainActivity.kt`:146 — `intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri` will throw `NullPointerException` if the extra is missing and `ClassCastException` if it's not a `Uri`. This is a pre-existing issue not introduced by this sub-task, so it does not block the verdict.

## Linter Results
- No Kotlin linter (ktlint/detekt) configured in the project. Android Lint is configured but is a build-time check already validated by the Executor's successful `assembleDebug`.

## Verdict: PASS

## Feedback for Executor
No critical issues found. Code quality is acceptable.

The DRY warning (item 1) is strongly recommended to address — the `controller` field exists specifically to avoid repeated fragment lookups, and 7 of the 8 usages bypass it. This is a simple find-and-replace that would improve readability and consistency.
