# ST-007 Execution Report: Upgrade Third-Party Dependencies

## Status: ✅ PASSED

## Changes Made

### app/build.gradle — Dependency Upgrades
| Dependency | Old Version | New Version |
|---|---|---|
| Timber | 4.7.1 | 5.0.1 |
| EventBus | 3.1.1 | 3.3.1 |
| Glide (glide + compiler) | 4.7.1 | 4.16.0 |
| Dagger (all 5 artifacts) | 2.16 | 2.53.1 |
| JUnit | 4.12 | 4.13.2 |
| Mockito | 2.23.0 | 5.14.2 |
| Robolectric | 3.8 | 4.14.1 |
| Espresso (all 5 artifacts + idling) | 3.1.1 | 3.6.1 |
| androidx.test.ext:junit | 1.1.0 | 1.2.1 |
| androidx.test:rules | 1.1.1 | 1.6.1 |
| androidx.test:orchestrator | 1.1.1 | 1.5.1 |
| firebase-core | 16.0.6 | Replaced with firebase-analytics:22.4.0 |
| play-services-ads | 17.1.2 | 23.6.0 |
| play-services-oss-licenses | 15.0.1 | 17.1.0 |
| OkHttp (logging-interceptor) | 3.10.0 | 4.12.0 |
| Retrofit (retrofit + converter-gson) | 2.4.0 | 2.11.0 |
| Gson | 2.8.5 | 2.11.0 |

### app/build.gradle — Removals
- `implementation fileTree(include: ['*.jar'], dir: 'libs')` — no libs/ directory exists
- `implementation "org.jetbrains:annotations:16.0.1"` — redundant with Kotlin
- `implementation 'io.branch.sdk.android:library:3.0.3'` — removed (see source cleanup below)
- `implementation 'com.google.firebase:firebase-ads:17.1.2'` — not used in source code
- `implementation 'com.squareup.okio:okio:1.15.0'` — bundled with OkHttp 4.x
- `resValue` entries for `BRANCH_IO_API_KEY` and `BRANCH_IO_TEST_API_KEY`

### Source File Changes

**MyApplication.kt:**
- Removed `import io.branch.referral.Branch`
- Removed `Branch.getAutoInstance(this)` call
- Fixed `createStackElementTag` return type `String` → `String?` (Timber 5.0 API change)

**MainActivity.kt:**
- Removed `import io.branch.referral.Branch`, `import org.json.JSONException`, `import timber.log.Timber`
- Removed unused `import com.nrs.nsnik.notes.BuildConfig`, `import com.nrs.nsnik.notes.MyApplication`, `import com.google.android.material.navigation.NavigationView`
- Removed `onStart()` method (Branch.initSession)
- Removed `onResume()` method (Branch deep link handling)

**ImageAdapter.kt:**
- Fixed `onLoadFailed` parameter `model: Any` → `model: Any?` (Glide 4.16.0 API change)

**AndroidManifest.xml:**
- Removed Branch SDK `<meta-data>` entries (BranchKey, BranchKey.test, TestMode)
- Removed Branch `InstallListener` `<receiver>`
- Removed Branch deep link `<intent-filter>` (nsnotes.app.link) from SplashActivity

**gradle.properties:**
- Removed `BRANCH_IO_API_KEY`, `BRANCH_IO_TEST_API_KEY`, `IO_FABRICS_API_KEY`

## Build Verification

- `./gradlew assembleDebug` — **BUILD SUCCESSFUL** ✅
- `./gradlew testDemoDebugUnitTest --tests EncryptionUnitTest` — **PASSED** ✅
- 70/74 tests pass; 4 pre-existing failures from earlier sub-tasks:
  - `BuildSystemUpgradeTest.compileSdkIs34` — broken by ST-005 (compileSdk bumped to 35)
  - `ViewBindingMigrationTest` (3 tests) — pre-existing FileNotFoundException from ST-004 test path issue
- No duplicate dependency entries ✅
- Zero Branch SDK references in source/manifest/build files ✅
