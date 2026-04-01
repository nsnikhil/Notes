# Validated Requirements: Notes App Bug Fixes and Comprehensive Espresso Test Suite

## Validation Status
- Cross-reference check: PASS
- Completeness check: PASS (root causes now identified and incorporated as technical guidance)

## Summary
Two-workstream effort for an Android Notes application. Workstream 1 fixes three confirmed bugs — checklist duplicate items caused by missing stable IDs in DiffUtil, camera broken by unnecessary WRITE_EXTERNAL_STORAGE permission check on API 29+, and lock feature requiring runtime verification of FragmentResult delivery. Each fix is verified by a dedicated Espresso test. Workstream 2 adds comprehensive Espresso tests for five features (checklist, lock, camera, gallery, reminder) covering happy paths, edge cases, error scenarios, and cross-feature interactions. All changes must pass the full build and test pipeline with zero failures.

## Detailed Requirements

### Workstream 1 — Bug Fixes

- **FR-001** [MUST-HAVE] Fix checklist duplicate items on Enter key. Root cause: `CheckListObject` has no stable unique ID — `CheckListDiffUtil.areItemsTheSame()` compares by `text` and `done`, so all empty items are identical, producing incorrect diffs. Fix: add a `val id: String = UUID.randomUUID().toString()` field to `CheckListObject` and compare by `id` only in `CheckListDiffUtil`.
- **FR-002** [MUST-HAVE] Fix camera functionality on API 29+. Root cause: `startCameraIntent()` is wrapped in `checkPermission(WRITE_EXTERNAL_STORAGE)` which is auto-denied on API 29+ (scoped storage). `getExternalFilesDir()` does not require this permission. Fix: remove the `WRITE_EXTERNAL_STORAGE` permission check around `startCameraIntent()` — call it directly (same pattern already used for gallery launcher).
- **FR-003** [MUST-HAVE] Fix lock feature first-attempt failure. Root cause: `FragmentResult` API flow appears logically correct in code. Needs runtime Espresso verification to confirm `FragmentResultListener` fires when `PasswordDialogFragment` dismisses. May need timing fix if result delivery is delayed.
- **FR-004** [MUST-HAVE] Each bug fix must be verified by a dedicated Espresso test that runs on the emulator.

### Workstream 2 — Comprehensive Espresso Test Suite

- **FR-005** [MUST-HAVE] Comprehensive Espresso tests for the checklist feature.
- **FR-006** [MUST-HAVE] Comprehensive Espresso tests for the lock feature.
- **FR-007** [MUST-HAVE] Comprehensive Espresso tests for the camera feature.
- **FR-008** [MUST-HAVE] Comprehensive Espresso tests for the gallery feature.
- **FR-009** [MUST-HAVE] Comprehensive Espresso tests for the reminder feature.
- **FR-010** [MUST-HAVE] Tests must cover happy paths for each feature.
- **FR-011** [MUST-HAVE] Tests must cover edge cases for each feature.
- **FR-012** [MUST-HAVE] Tests must cover error scenarios for each feature.
- **FR-013** [SHOULD-HAVE] Tests must cover cross-feature interactions between the five features.

### Build & Verification

- **FR-014** [MUST-HAVE] The `connectedDemoDebugAndroidTest` task must complete with 0 failures.
- **FR-015** [MUST-HAVE] The `testDemoDebugUnitTest` task must pass.
- **FR-016** [MUST-HAVE] The `assembleDebug` task must succeed.
- **FR-017** [MUST-HAVE] Existing tests must not be removed.
- **FR-018** [MUST-HAVE] Both product flavors (demo and full) must build successfully.
- **FR-019** [MUST-HAVE] Verification commands must be executed (not just assumed to pass).

## Acceptance Criteria

1. Given a user editing a checklist, when the user presses Enter, then exactly one new checklist item is created (no duplicates) — verified by DiffUtil correctly distinguishing items by unique ID.
2. Given a user opening the camera from a note on API 29+, when the user captures a photo, then the photo is successfully attached to the note without any permission denial.
3. Given a user attempting to lock a note for the first time, when the user sets a password in the dialog, then the note is locked successfully on the first attempt without requiring a retry.
4. Given each bug fix (checklist, camera, lock), when the corresponding Espresso test runs on the emulator, then the test passes and confirms the fix.
5. Given the complete Espresso test suite for all five features, when `connectedDemoDebugAndroidTest` is executed, then 0 test failures are reported.
6. Given the full project, when `testDemoDebugUnitTest` is executed, then all unit tests pass.
7. Given the full project, when `assembleDebug` is executed, then the build succeeds for both product flavors (demo and full).
8. Given the existing test suite, when new tests are added, then no previously existing tests have been removed.
9. Given the Espresso tests for each feature, when reviewed, then each feature has tests covering happy paths, edge cases, and error scenarios.

## Constraints

- Bug fixes must not introduce new regressions — existing tests must continue to pass
- Tests must not be removed as part of any fix or refactor
- Both product flavors (demo and full) must build successfully
- Espresso tests must run on an emulator (not just local/unit test JVM)
- Bug fixes must address the identified root causes, not symptoms
- Adding UUID `id` field to `CheckListObject` changes serialized JSON format — kotlinx.serialization will use the default value for the missing field on old data, which is acceptable (no explicit migration required)

## Dependencies

- Android emulator must be available and running for Espresso test execution
- Camera hardware emulation must be available on the emulator (or espresso-intents used to stub camera intents)
- Existing project build configuration (Gradle, product flavors: demo and full)
- Existing checklist, lock, camera, gallery, and reminder feature implementations in the app

## Non-Functional Requirements

- **NFR-001** [MUST-HAVE] Build reliability: `assembleDebug` must succeed deterministically (not flaky).
- **NFR-002** [MUST-HAVE] Test reliability: `connectedDemoDebugAndroidTest` must achieve 0 failures — tests must not be flaky.
- **NFR-003** [SHOULD-HAVE] Test coverage: five features × four coverage dimensions (happy path, edge cases, error scenarios, cross-feature).

## Technical Notes

- **Checklist fix:** Add `val id: String = UUID.randomUUID().toString()` to `CheckListObject`. Update `CheckListDiffUtil.areItemsTheSame()` to compare by `id` only. Old serialized data without `id` field will get auto-generated IDs on deserialization (kotlinx.serialization default value behavior).
- **Camera fix:** Remove the `checkPermission(WRITE_EXTERNAL_STORAGE, ...)` wrapper around `startCameraIntent()`. Call `startCameraIntent()` directly. `getExternalFilesDir()` does not require `WRITE_EXTERNAL_STORAGE`. This matches the pattern already used for the gallery launcher in the current code.
- **Lock fix:** The `FragmentResult` API flow appears correct in code. Verify at runtime via Espresso that `FragmentResultListener` fires when `PasswordDialogFragment` calls `parentFragmentManager.setFragmentResult()` and dismisses. If it does not fire, investigate FragmentManager mismatch (activity FM vs child FM) or timing issues.
- Build system: Gradle with Android-specific tasks (`connectedDemoDebugAndroidTest`, `testDemoDebugUnitTest`, `assembleDebug`)
- Test framework: Espresso (Android UI testing), espresso-intents for stubbing camera/gallery intents
- Product flavors: "demo" and "full" (`applicationIdSuffix ".full"`)
- Lock mechanism: password-based (RSA-encrypted, stored in SharedPreferences)

## Scope Exclusions

- Social login or biometric lock enhancements
- UI/UX redesign of any feature
- Performance optimization beyond bug fix scope
- Migration tooling for old checklist data format (auto-generated IDs on load is sufficient)
- Logging, analytics, or crash reporting additions

## Deferred Items

- None — no clarifying questions were asked; all root causes are identified and actionable.
