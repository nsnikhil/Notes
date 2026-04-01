# Code Review Report: JaCoCo Baseline Coverage Measurement

## Sub-Task
- ID: ST-001
- Name: JaCoCo Baseline Coverage Measurement
- Files reviewed: 3 (`app/build.gradle`, `coverage-baseline.txt`, `local.properties`)

## Critical Issues
None found

## Warnings

1. **Incorrect primary source directory**: `app/build.gradle:53` — `mainSrc` is set to `"${project.projectDir}/src/main/kotlin"` but this directory does not exist. All 90 Kotlin source files reside in `src/main/java`. The `sourceDirectories` line does include `src/main/java` as a second entry, so JaCoCo will still find sources, but the primary variable name and ordering is misleading. Consider swapping the order or removing the non-existent path:
   ```groovy
   sourceDirectories = files(["${project.projectDir}/src/main/java"])
   ```
   Note: The build.gradle `sourceSets` block adds `main.java.srcDirs += 'src/main/kotlin'`, so if that directory is created later, JaCoCo would pick it up from the second entry. Low risk since both are listed.

2. **Class directory path will need updating for AGP upgrade**: `app/build.gradle:45-49` — The path `${buildDir}/intermediates/classes/demoDebug` is correct for AGP 3.2.1 but will break after the AGP upgrade planned in ST-002 (newer AGP uses `intermediates/javac/demoDebug/classes`). This is already documented in the execution report and project context, so flagging as a reminder only.

3. **Unintended `.idea/gradle.xml` modification**: `.idea/gradle.xml` was modified (removed `distributionType` and `resolveModulePerSourceSet`, added `testRunner`). This was not listed in the execution report's "Files Modified" table. The change appears to be a side effect of Gradle sync rather than intentional. Since `.idea/` is in `.gitignore`, this won't be committed, but it should be noted for completeness.

4. **`local.properties` is tracked in git**: The `.gitignore` has `local.properties` commented out (`# local.properties`), meaning this file with the machine-specific SDK path is version-controlled. The executor's change to fix the SDK path is correct for the local environment, but committing it would break other developers' setups. This is a pre-existing project issue, not introduced by ST-001, but worth noting. The executor should avoid staging this file.

5. **Missing blank line before `apply plugin: 'io.fabric'`**: `app/build.gradle:57` — The closing `}` of the `jacocoTestReport` task is immediately followed by `apply plugin: 'io.fabric'` with no blank line separator. Minor readability issue.

## Linter Results
- No linter configured (no ktlint, detekt, checkstyle, or spotless found in the project)

## Verdict: PASS

## Feedback for Executor
No critical issues found. Code quality is acceptable.

The JaCoCo configuration is well-structured:
- Correct `toolVersion` (0.8.2) for Gradle 4.6 compatibility
- Proper class file exclusion filters for generated code (R, BuildConfig, Dagger, DataBinding)
- Both Java and Kotlin class output directories covered
- Correct execution data path for `testDemoDebugUnitTest`
- Good task documentation with `group` and `description`

The `coverage-baseline.txt` is thorough — it documents the root cause, date, and all relevant version numbers, which will be valuable context for ST-002.

Minor suggestions for future cleanup:
- Remove the non-existent `src/main/kotlin` from `sourceDirectories` (or make it the secondary entry)
- Add a blank line between the `jacocoTestReport` task closing brace and the next `apply plugin` line
- Avoid staging `local.properties` in commits
