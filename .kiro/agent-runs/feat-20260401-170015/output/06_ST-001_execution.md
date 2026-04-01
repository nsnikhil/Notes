# ST-001 Execution Report: Fix checklist Enter key creating duplicate items

## Status: ✅ PASSED

## Changes Made

### 1. Bug Fix — CheckListAdapter.kt
**File:** `app/src/main/java/com/nrs/nsnik/notes/view/adapters/CheckListAdapter.kt`

Added `.throttleFirst(300, TimeUnit.MILLISECONDS)` to the `editorActions()` RxJava chain in `MyViewHolder.init`:

```kotlin
// BUG FIX: throttle editorActions to prevent duplicate item creation from focus-steal
text.editorActions()
    .throttleFirst(300, TimeUnit.MILLISECONDS)
    .subscribe {
        if (it == EditorInfo.IME_ACTION_NEXT) mOnAddClickListener.addClickListener()
    },
```

**Root cause:** When `onBindViewHolder` calls `setFocus()` on the newly added item, it steals focus from the previous EditText. This focus change triggers the system to fire a second `IME_ACTION_NEXT` event on the previous EditText, causing `addCheckListItem()` to be called twice. `throttleFirst(300ms)` drops the second rapid-fire event.

### 2. Espresso Test — ChecklistEnterKeyTest.kt
**File:** `app/src/androidTest/java/com/nrs/nsnik/notes/ChecklistEnterKeyTest.kt`

Test `enterKeyCreatesExactlyOneNewItemEachTime`:
- Navigates to new note, opens checklist
- Verifies 1 item initially
- Types text, presses IME action → verifies exactly 2 items
- Types text in item 2, presses IME action → verifies exactly 3 items

## Test Results

### Unit Tests (`./gradlew testDemoDebugUnitTest`)
- **Result:** BUILD SUCCESSFUL
- **Failures:** 0

### Espresso Tests (`./gradlew connectedDemoDebugAndroidTest`)
- **Total:** 75 tests
- **Passed:** 74
- **Failed:** 1 (pre-existing, unrelated)
- **Failed test:** `NavigationFlowTest.backFromSettingsReturnsToNotesList` — `NoMatchingViewException` for `commonList` view. This is a pre-existing navigation test failure unrelated to checklist changes.

### Checklist-specific tests — all passed:
- `ChecklistEnterKeyTest.enterKeyCreatesExactlyOneNewItemEachTime` ✅
- `BugFixVerificationEspressoTest.testChecklistEnterAddsExactlyOneItem` ✅
- `ChecklistFlowTest` (7 tests) ✅

## Exit Criteria Verification

| Criterion | Status |
|-----------|--------|
| editorActions() has throttleFirst in CheckListAdapter.kt | ✅ |
| Espresso test passes proving Enter creates exactly 1 new item each time | ✅ |
| `./gradlew connectedDemoDebugAndroidTest` passes (0 new failures) | ✅ |
| `./gradlew testDemoDebugUnitTest` passes | ✅ |
