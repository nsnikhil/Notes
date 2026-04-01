# Architecture Document — Notes App Bug Fixes

## ST-001: Fix 3 Bugs + Verification Espresso Test

### Changes Made

#### CheckListObject (`app/src/main/java/com/nrs/nsnik/notes/model/CheckListObject.kt`)
- Added `@Transient val id: String = UUID.randomUUID().toString()` — stable unique ID for DiffUtil identity comparison
- `@Transient` means the field is NOT serialized by kotlinx.serialization, so existing persisted JSON is backward-compatible
- Each deserialized instance gets a fresh UUID (not the original's)

#### CheckListDiffUtil (`app/src/main/java/com/nrs/nsnik/notes/view/adapters/diffUtil/CheckListDiffUtil.kt`)
- `areItemsTheSame()` now compares `oldItem.id == newItem.id` (was `text+done`)
- `areContentsTheSame()` unchanged — still compares `text+done`

#### NewNoteFragment (`app/src/main/java/com/nrs/nsnik/notes/view/fragments/NewNoteFragment.kt`)
- Camera click handler: `checkPermission(CAMERA, ...)` instead of `checkPermission(WRITE_EXTERNAL_STORAGE, ...)`
- Removed `@RequiresPermission(WRITE_EXTERNAL_STORAGE)` from `startCameraIntent()`

#### AndroidManifest.xml
- Added `<uses-permission android:name="android.permission.CAMERA" />`

### Test Files
- `app/src/androidTest/java/com/nrs/nsnik/notes/BugFixVerificationEspressoTest.kt` — 3 Espresso tests
- Updated `app/src/test/java/com/nrs/nsnik/notes/util/DiffUtilTest.kt`
- Updated `app/src/test/java/com/nrs/nsnik/notes/model/CheckListObjectTest.kt`

## ST-002: Comprehensive Espresso Tests for Checklist and Lock Features

### Files Modified

#### `app/src/androidTest/java/com/nrs/nsnik/notes/ChecklistFlowTest.kt`
- 7 total tests (2 existing + 5 new)
- New tests: `testAddChecklistItemWithText`, `testRapidAddFiveItems`, `testCheckItemShowsStrikethrough`, `testAddEmptyChecklistItem`, `testMultipleItemsHaveCorrectCount`
- Added `recyclerViewItemCount()` custom matcher for RecyclerView item count assertions
- Added `expandBottomSheetAndTapChecklist()` helper to reduce duplication

#### `app/src/androidTest/java/com/nrs/nsnik/notes/LockFlowTest.kt`
- 8 total tests (3 existing + 5 new)
- New tests: `testLockIconVisibleInNewNote`, `testDismissPasswordDialogNoLockChange`, `testLockTogglesTwice`, `testLockIconStateAfterToggle`, `testPasswordDialogHasInputField`
- `setUp()` now clears password-related SharedPreferences keys (`sharedPreferencePasswordKey`, `sharedPreferencePrivateKey`, `sharedPreferencePublicKey`) to ensure test independence
- Tests that interact with PasswordDialogFragment use 2000ms sleep to account for async `decryptAndGet` RxJava pipeline

### Key Patterns
- Bottom sheet must be expanded by tapping `toolsDate` before accessing `toolsCheckList`
- `PasswordDialogFragment` is async — `decryptAndGet()` runs on `Schedulers.io()` and posts to main thread via LiveData
- Password dialog views require `.inRoot(isDialog())` for Espresso assertions
- New password creation requires matching text in both `passwordDialogTextOld` and `passwordDialogText` fields

## ST-003: Comprehensive Espresso Tests for Camera, Gallery, Reminder + Cross-Feature

### Files Modified

#### `app/src/androidTest/java/com/nrs/nsnik/notes/ImageAttachmentFlowTest.kt`
- 7 total tests (4 existing + 3 new)
- New tests: `testCameraIntentCancelled`, `testGalleryIntentCancelled`, `testCameraAndGalleryBothWork`
- Intent stubbing pattern: `Intents.intending(hasAction(...)).respondWith(ActivityResult(RESULT_CANCELED, Intent()))`
- Camera uses `MediaStore.ACTION_IMAGE_CAPTURE`, gallery uses `Intent.ACTION_GET_CONTENT`
- Bottom sheet must be re-expanded between sequential tool taps (each tool click calls `changeState()` which collapses the sheet)

#### `app/src/androidTest/java/com/nrs/nsnik/notes/ReminderFlowTest.kt`
- 5 total tests (2 existing + 3 new)
- New tests: `testReminderToolClickable`, `testDismissTimePickerNoReminder`, `testReminderToolTapTwice`
- Uses UiAutomator `UiDevice` + `By.text("Cancel")` to dismiss the system TimePickerDialog
- Espresso's `pressBack()` is unreliable for TimePickerDialog — it propagates to navigation and navigates away from the fragment

#### `app/src/androidTest/java/com/nrs/nsnik/notes/CrossFeatureFlowTest.kt` (NEW)
- 4 tests: `testNewNoteHasAllTools`, `testPinAndSaveNote`, `testNoteWithChecklistAndPin`, `testBottomSheetExpandsAndCollapses`
- Pin/star menu item ID: `R.id.newNoteMenuStar`
- Save menu item ID: `R.id.newNoteMenuSave`
- All 6 bottom sheet tool IDs: `toolsColor`, `toolsCamera`, `toolsAttachment`, `toolsCheckList`, `toolsAudio`, `toolsReminder`

#### `app/build.gradle`
- Added `androidTestImplementation 'androidx.test.uiautomator:uiautomator:2.3.0'`

### Key Patterns
- TimePickerDialog is a system dialog — use UiAutomator (not Espresso) to interact with it
- Bottom sheet `changeState()` toggles between expanded/collapsed — must account for this in sequential tool interactions
- `Thread.sleep()` needed after bottom sheet toggle for animation to complete (400-800ms typical)
