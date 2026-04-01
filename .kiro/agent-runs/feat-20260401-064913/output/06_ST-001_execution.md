# ST-001 Execution Report: JaCoCo Baseline Coverage Measurement

## Status: COMPLETED (with expected build failure documented)

## What Was Done

### 1. JaCoCo Plugin Added to `app/build.gradle`
- Added `apply plugin: 'jacoco'` immediately after `apply plugin: 'com.android.application'`
- Configured JaCoCo toolVersion `0.8.2` (compatible with Gradle 4.6)
- Created `jacocoTestReport` task of type `JacocoReport`:
  - Depends on `testDemoDebugUnitTest`
  - Produces both XML and HTML reports
  - Filters out generated classes (R, BuildConfig, Dagger, DataBinding, etc.)
  - Sources from `src/main/kotlin` and `src/main/java`
  - Class directories from both Java (`intermediates/classes/demoDebug`) and Kotlin (`tmp/kotlin-classes/demoDebug`) output dirs
  - Execution data from `jacoco/testDemoDebugUnitTest.exec`

### 2. Build Attempt Results
The build was attempted with Java 8 (Amazon Corretto 8) since Gradle 4.6 is incompatible with Java 17.

**Build failed during dependency resolution:**
```
Could not resolve all artifacts for configuration ':app:demoDebugRuntimeClasspath'.
Caused by: Could not find com.firebase:firebase-jobdispatcher:0.8.5.
```

### 3. Baseline Coverage
Recorded in `coverage-baseline.txt` at repo root:
```
baseline unavailable - pre-modernization build broken
```

## Build Failure Root Causes
1. **`com.firebase:firebase-jobdispatcher:0.8.5`** — This library was deprecated and removed from all Maven repositories. It's a transitive dependency pulled in by one of the Firebase/WorkManager libraries.
2. **Java version incompatibility** — Gradle 4.6 cannot run on Java 17 (`Could not determine java version from '17.0.18'`). Requires Java 8.
3. **Android SDK mismatch** — Project targets SDK 28 with build-tools 28.0.3, but only SDK 36.1 and build-tools 36.x are installed locally.
4. **`local.properties` SDK path** — Was pointing to `/Users/nikhilsoni/Library/Android/sdk` (original developer). Fixed to `/Users/eniksoni/Library/Android/sdk`.

## Files Modified
| File | Change |
|------|--------|
| `app/build.gradle` | Added JaCoCo plugin, toolVersion config, and `jacocoTestReport` task |
| `local.properties` | Fixed SDK path to current user's Android SDK location |

## Files Created
| File | Content |
|------|---------|
| `coverage-baseline.txt` | Documents baseline unavailability with root cause details |

## Exit Criteria Assessment
| Criterion | Status | Notes |
|-----------|--------|-------|
| `./gradlew testDemoDebugUnitTest jacocoTestReport` produces report | ❌ BLOCKED | Build broken due to unresolvable dependencies |
| Baseline percentage recorded in `coverage-baseline.txt` | ✅ DONE | Documented as unavailable with reasons |
| JaCoCo plugin added to app/build.gradle | ✅ DONE | `apply plugin: 'jacoco'` with toolVersion 0.8.2 |
| JaCoCo task configured for demoDebug | ✅ DONE | `jacocoTestReport` task targeting demoDebug classes |

## Notes for Subsequent Sub-Tasks
- The JaCoCo configuration is in place and will produce reports once the build is fixed (after repository modernization and dependency updates).
- The `jacocoTestReport` task references AGP 3.2.1 class output paths (`intermediates/classes/demoDebug`). If AGP is upgraded, these paths will need updating (newer AGP uses `intermediates/javac/demoDebug/classes`).
- Only 1 unit test file exists: `app/src/test/java/com/nrs/nsnik/notes/EncryptionUnitTest.kt`
