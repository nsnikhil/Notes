# Code Review Report: Build System Upgrade (Gradle, AGP, Kotlin, Plugins)

## Sub-Task
- ID: ST-002
- Name: Build System Upgrade (Gradle, AGP, Kotlin, Plugins)
- Files reviewed: 5 (`build.gradle`, `app/build.gradle`, `settings.gradle`, `gradle.properties`, `gradle/wrapper/gradle-wrapper.properties`)

## Critical Issues
None found

## Warnings

1. **Dead property `IO_FABRICS_API_KEY`**: `gradle.properties:33` — The Fabric API key `IO_FABRICS_API_KEY` is still defined in `gradle.properties` but is no longer referenced anywhere in the build files (Fabric plugin was removed). This is dead code that should be cleaned up. — Suggested fix: Remove the line `IO_FABRICS_API_KEY="e3979606d7bddaddef52cc923bf7cc5cc18c38dd"`.

2. **Deprecated `buildDir` usage**: `app/build.gradle:32,35,43` and `build.gradle:17` — `Project.buildDir` was deprecated in Gradle 8.x in favor of `project.layout.buildDirectory`. Currently produces deprecation warnings but still works. — Suggested fix: Replace `${buildDir}` with `${layout.buildDirectory.get().asFile}` in JaCoCo paths, and `rootProject.buildDir` with `rootProject.layout.buildDirectory` in the clean task. Low priority since this is a warning, not an error.

3. **Deprecated `flavorDimensions` syntax**: `app/build.gradle:92` — `flavorDimensions "version"` uses the deprecated method-call syntax. AGP 8.x prefers the property-assignment syntax `flavorDimensions = ["version"]`. Still works but produces a deprecation warning. — Suggested fix: Change to `flavorDimensions = ["version"]`.

4. **Stale comment in debug buildType**: `app/build.gradle:83` — `// No Crashlytics-specific config needed` is a leftover comment referencing the removed Crashlytics integration. Minor clutter. — Suggested fix: Remove the comment or leave the `debug {}` block empty/remove it entirely.

## Linter Results
- No standalone linter configured (Android Lint is integrated into the Gradle build but requires a successful compilation to run, which is blocked by the known `work-firebase` dependency issue).

## Review Checklist

| # | Check | Result |
|---|-------|--------|
| 1 | Dead plugins removed (Hugo, Fabric, firebase-perf, firebase-plugins) | ✅ No references found |
| 2 | Dead repos removed (jcenter, maven.fabric.io) | ✅ No references found |
| 3 | AGP 8.7.3 + Gradle 8.9 compatibility | ✅ AGP 8.7 requires minimum Gradle 8.9 (confirmed via official docs) |
| 4 | Kotlin 2.1.10 set correctly | ✅ `ext.kotlin_version = '2.1.10'` in root `build.gradle` |
| 5 | View binding enabled | ✅ `buildFeatures { viewBinding true; buildConfig true }` |
| 6 | compileSdk/targetSdk = 34 | ✅ Both set to 34 |
| 7 | JaCoCo preserved and updated | ✅ Version 0.8.12, AGP 8.x class paths (`intermediates/javac/...`, `tmp/kotlin-classes/...`), new report API (`xml.required`, `html.required`) |
| 8 | No unintended source file changes | ✅ Only build files + IDE files modified |
| 9 | Product flavors (demo/full) preserved | ✅ Both flavors with correct dimension, suffixes intact |
| 10 | google-services plugin at bottom of app/build.gradle | ✅ Line 268: `apply plugin: 'com.google.gms.google-services'` |
| 11 | kotlin-android-extensions → kotlin-parcelize | ✅ Replaced correctly |
| 12 | namespace added | ✅ `namespace "com.nrs.nsnik.notes"` |
| 13 | Java/Kotlin target 17 | ✅ `sourceCompatibility/targetCompatibility VERSION_17`, `jvmTarget = '17'` |
| 14 | lintOptions → lint, packagingOptions → packaging | ✅ Updated to non-deprecated APIs |
| 15 | -Xmaxerrs fixed | ✅ Now has value "500" as separate arg |
| 16 | Nested allprojects block removed | ✅ No `allprojects` block found |
| 17 | enableCrashlytics / androidExtensions removed | ✅ No references found |
| 18 | MaxPermSize removed from gradle.properties | ✅ Removed |
| 19 | nonTransitiveRClass added | ✅ `android.nonTransitiveRClass=true` |
| 20 | settings.gradle pluginManagement added | ✅ With google(), mavenCentral(), gradlePluginPortal() |
| 21 | Build config syntactically correct | ✅ `./gradlew tasks` succeeds |

## Verdict: PASS

## Feedback for Executor
No critical issues found. Code quality is acceptable. The build system upgrade is comprehensive and correctly executed — all version numbers are compatible, dead plugins/repos are fully removed, and the build configuration passes Gradle sync.

The 4 warnings above are minor cleanup items (dead property, deprecation warnings, stale comment) that do not affect correctness or block testing. They can be addressed in a follow-up cleanup pass.
