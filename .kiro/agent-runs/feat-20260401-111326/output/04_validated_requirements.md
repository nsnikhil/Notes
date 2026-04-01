# Validated Requirements: Post-Modernization Verification and Fix for Android Notes App

## Validation Status
- Cross-reference check: PASS
- Completeness check: PASS

## Summary
Systematically verify and fix every feature in the Android Notes app following a modernization effort. The scope covers 15 feature categories — from core CRUD operations through checklists, media attachments, reminders, folder management, navigation, DI, and ViewModels — ensuring each works correctly and is covered by unit tests that run under Robolectric with an in-memory Room database. Five confirmed bugs must be fixed: missing FLAG_IMMUTABLE on PendingIntents in ReminderUtil and NotificationReceiver, residual java.io.Serializable on CheckListObject, incomplete folder cascade delete for nested sub-folders, and reminders not cancelled on note deletion.

## Detailed Requirements

### Note Creation/Editing
- FR-001: Users can create a new note with title and body [MUST-HAVE]
- FR-002: Users can read/view an existing note [MUST-HAVE]
- FR-003: Users can update a note's title and body [MUST-HAVE]
- FR-004: Users can delete a note [MUST-HAVE]
- FR-005: Note entities serialize and deserialize correctly via nav args using kotlinx.serialization with JSON encoding to ByteArray [MUST-HAVE]
- FR-006: Date fields (created, modified) are stored and retrieved accurately [MUST-HAVE]

### Checklist Feature
- FR-007: Users can add checklist items to a note [MUST-HAVE]
- FR-008: Users can check (mark complete) a checklist item [MUST-HAVE]
- FR-009: Users can uncheck (mark incomplete) a checklist item [MUST-HAVE]
- FR-010: Users can remove a checklist item [MUST-HAVE]
- FR-011: CheckListAdapter correctly processes list updates via `submitList` [MUST-HAVE]

### Image Attachment
- FR-012: Users can attach an image to a note [MUST-HAVE]
- FR-013: Users can remove an attached image from a note [MUST-HAVE]
- FR-014: Image attachment references serialize and deserialize correctly for storage [MUST-HAVE]

### Audio Recording
- FR-015: Users can record and attach audio to a note [MUST-HAVE]
- FR-016: Users can remove an attached audio recording from a note [MUST-HAVE]
- FR-017: Audio attachment references serialize and deserialize correctly for storage [MUST-HAVE]

### Reminders
- FR-018: Users can set a reminder on a note [MUST-HAVE]
- FR-019: Reminders schedule alarms via AlarmManager with PendingIntent using `FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE` [MUST-HAVE]
- FR-020: NotificationReceiver fires and displays a notification when a reminder triggers, using `FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE` on its content intent PendingIntent [MUST-HAVE]
- FR-021: When a note is deleted, any scheduled reminder alarm for that note is cancelled via AlarmManager [MUST-HAVE]

### Pin/Unpin Notes and Folders
- FR-022: Users can pin a note so it appears at the top of the list [MUST-HAVE]
- FR-023: Users can unpin a previously pinned note [MUST-HAVE]
- FR-024: Users can pin a folder [MUST-HAVE]
- FR-025: Users can unpin a previously pinned folder [MUST-HAVE]
- FR-026: Pin state is persisted as a database field toggle [MUST-HAVE]
- FR-027: DAO queries return pinned items filtered/sorted appropriately [MUST-HAVE]

### Lock/Unlock Notes and Folders
- FR-028: Users can lock a note with a password [MUST-HAVE]
- FR-029: Users can unlock a previously locked note by entering the correct password [MUST-HAVE]
- FR-030: Users can lock a folder with a password [MUST-HAVE]
- FR-031: Users can unlock a previously locked folder by entering the correct password [MUST-HAVE]
- FR-032: Password is encrypted using RSA (1024-bit, default padding) via PasswordUtil [MUST-HAVE]
- FR-033: PasswordDialogFragment presents the lock/unlock UI flow [MUST-HAVE]

### Folder Operations
- FR-034: Users can create a folder [MUST-HAVE]
- FR-035: Users can read/view a folder's contents [MUST-HAVE]
- FR-036: Users can update a folder's name [MUST-HAVE]
- FR-037: Users can delete a folder, which cascade-deletes all contained notes AND nested sub-folders recursively [MUST-HAVE]
- FR-038: Nested folders are supported (folders within folders) [MUST-HAVE]
- FR-039: Users can move notes into a folder [MUST-HAVE]
- FR-040: Users can move folders into another folder [MUST-HAVE]

### Search
- FR-041: Users can search notes by title (prefix match via `LIKE :query || '%'`, case-insensitive) [MUST-HAVE]
- FR-042: Users can search folders by name (prefix match via `LIKE :query || '%'`, case-insensitive) [MUST-HAVE]
- FR-043: DAO queries support partial-match search [MUST-HAVE]

### Navigation
- FR-044: Navigation drawer displays the correct menu items and navigates to destinations [MUST-HAVE]
- FR-045: List-to-new-note-to-back navigation flow works without crashes or data loss [MUST-HAVE]
- FR-046: NoteEntity is correctly serialized and passed via navigation arguments using kotlinx.serialization [MUST-HAVE]

### Color Picker
- FR-047: Users can select a color for a note [SHOULD-HAVE]
- FR-048: Selected color is persisted in the database [SHOULD-HAVE]
- FR-049: Color selection is communicated via EventBus event delivery [SHOULD-HAVE]

### File Operations
- FR-050: FileUtil writes files to storage using Okio [MUST-HAVE]
- FR-051: FileUtil reads files from storage using Okio [MUST-HAVE]
- FR-052: FileUtil deletes files from storage [MUST-HAVE]
- FR-053: Missing or inaccessible files are handled gracefully without crashes [MUST-HAVE]

### Dagger Dependency Injection
- FR-054: All 7 Dagger components (DatabaseComponent, FileComponent, FolderComponent, GlideComponent, SharedPrefComponent, NetworkComponent, MediaComponent) build successfully [MUST-HAVE]
- FR-055: Each component provides non-null dependencies to its consumers [MUST-HAVE]

### ViewModels
- FR-056: NoteViewModel correctly delegates operations and exposes LiveData [MUST-HAVE]
- FR-057: FolderViewModel correctly delegates operations and exposes LiveData [MUST-HAVE]
- FR-058: LiveData emissions reflect underlying data changes (observed on main thread, DB operations on Schedulers.io() via RxJava) [MUST-HAVE]

### Bug Fixes (Confirmed)
- BUG-001: Fix missing FLAG_IMMUTABLE on PendingIntent in ReminderUtil — `setNotification()` and `cancelReminder()` must use `PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE` [MUST-HAVE]
- BUG-002: Fix missing FLAG_IMMUTABLE on PendingIntent in NotificationReceiver — `buildNotification()` content intent must use `PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE` [MUST-HAVE]
- BUG-003: Remove `java.io.Serializable` from CheckListObject to align with kotlinx.serialization migration (NoteEntity already had this removed) [SHOULD-HAVE]
- BUG-004: Fix folder cascade delete — `ListFragment.deleteFolder()` must also call `dbUtil.deleteFolderByParent()` (or equivalent recursive deletion) to delete nested sub-folders, not just contained notes [MUST-HAVE]
- BUG-005: Cancel scheduled reminder alarms on note deletion — `DbUtil.deleteNote()` or the calling code must cancel any AlarmManager alarm for the deleted note before removing it [MUST-HAVE]

### General
- FR-059: Any additional feature found broken during verification is fixed [MUST-HAVE]

## Acceptance Criteria
1. Given a user on the notes list screen, when they create a new note with title and body and save it, then the note appears in the list and persists across app restarts
2. Given an existing note, when the user edits its title or body and saves, then the updated content is persisted and displayed
3. Given an existing note, when the user deletes it, then it is removed from the list and the database
4. Given a note with a checklist, when the user adds an item, then it appears unchecked in the checklist
5. Given a checklist item, when the user checks it, then it is marked complete; when unchecked, it reverts to incomplete
6. Given a checklist item, when the user removes it, then it no longer appears in the checklist
7. Given a note, when the user attaches an image, then the image reference is stored and the image displays on the note
8. Given a note with an attached image, when the user removes it, then the reference is deleted and the image no longer displays
9. Given a note, when the user records audio, then the audio reference is stored and playback is available
10. Given a note with attached audio, when the user removes it, then the reference is deleted
11. Given a note, when the user sets a reminder time, then an alarm is scheduled via AlarmManager with `FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE` on the PendingIntent
12. Given a scheduled reminder, when the alarm fires, then NotificationReceiver displays a notification using a PendingIntent with `FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE`
13. Given a note or folder, when the user pins it, then the pin DB field is toggled to true and the item sorts to the top
14. Given a pinned note or folder, when the user unpins it, then the pin DB field is toggled to false and normal sort order resumes
15. Given a note or folder, when the user locks it with a password, then the password is RSA-encrypted via PasswordUtil and stored
16. Given a locked note or folder, when the user enters the correct password in PasswordDialogFragment, then access is granted
17. Given a locked note or folder, when the user enters an incorrect password, then access is denied
18. Given a folder with nested sub-folders and notes, when the user deletes it, then all contained notes AND all nested sub-folders (recursively) are cascade-deleted
19. Given a note or folder, when the user moves it into a folder, then the parent reference updates and the item appears in the target folder
20. Given a search query, when the user types a partial title or folder name, then matching results are returned by the DAO via prefix match
21. Given the navigation drawer, when the user selects a destination, then the app navigates to the correct screen
22. Given the new-note screen, when the user presses back, then they return to the list without data loss or crash
23. Given a NoteEntity, when it is passed via navigation arguments, then it serializes and deserializes without data loss via kotlinx.serialization
24. Given the color picker, when the user selects a color, then it is saved to the DB and delivered via EventBus
25. Given a file path, when FileUtil writes then reads the file, then the content round-trips correctly via Okio
26. Given a missing file path, when FileUtil attempts to read or delete, then it handles the error gracefully without crashing
27. Given the Dagger dependency graph, when all 7 components are built, then every provided dependency is non-null
28. Given NoteViewModel and FolderViewModel, when underlying data changes, then LiveData emissions reflect those changes
29. Given the test suite, when `./gradlew testDemoDebugUnitTest` is executed, then all tests pass
30. Given both product flavors (demo and full), when the project is built, then both flavors compile successfully
31. Given a note with a scheduled reminder, when the note is deleted, then the scheduled alarm is cancelled via AlarmManager before the note is removed from the database
32. Given CheckListObject, when inspected, then it does NOT implement `java.io.Serializable` (only uses kotlinx.serialization `@Serializable`)

## Constraints
- No Espresso or instrumented tests — all tests must reside in `app/src/test/`
- Robolectric must be used for any tests requiring the Android framework
- Room must use an in-memory database for testing
- Tests must pass via `./gradlew testDemoDebugUnitTest`
- Existing tests must not be removed
- Both product flavors (demo and full) must build successfully
- Bug fixes must not introduce regressions in other features
- PendingIntent must always include FLAG_IMMUTABLE (Android 12+ / API 31+ compliance)

## Dependencies
- Android Room persistence library (in-memory DB for tests)
- Robolectric (Android framework simulation in unit tests)
- Dagger (dependency injection — 7 components: DatabaseComponent, FileComponent, FolderComponent, GlideComponent, SharedPrefComponent, NetworkComponent, MediaComponent)
- AlarmManager / PendingIntent (system alarm scheduling)
- EventBus (color picker event delivery)
- Okio (file read/write/delete operations)
- Android Navigation Component (nav args serialization via kotlinx.serialization)
- RSA encryption APIs (PasswordUtil — 1024-bit key, default padding)
- kotlinx.serialization (NoteEntity and related model serialization)
- RxJava (DB operations on Schedulers.io())

## Non-Functional Requirements
- NFR-001: All unit tests must execute and pass within the `testDemoDebugUnitTest` Gradle task [MUST-HAVE]
- NFR-002: Both product flavors (demo and full) must compile without errors [MUST-HAVE]
- NFR-003: File operations must handle missing files gracefully (no unhandled exceptions) [MUST-HAVE]
- NFR-004: PendingIntent must use FLAG_IMMUTABLE for Android 12+ (API 31+) compliance [MUST-HAVE]
- NFR-005: LiveData observed on main thread; DB operations on Schedulers.io() via RxJava [MUST-HAVE]

## Technical Notes
- CheckListAdapter uses `submitList` for list diffing — tests should verify correct diff behavior
- NoteEntity uses `@Serializable` (kotlinx.serialization) with JSON encoding to `ByteArray` for nav args
- PasswordUtil uses RSA encryption (1024-bit, default padding via `Cipher.getInstance("RSA")`) for lock/unlock passwords
- PasswordDialogFragment handles the UI flow for entering lock/unlock passwords
- Dagger has 7 components (DatabaseComponent, FileComponent, FolderComponent, GlideComponent, SharedPrefComponent, NetworkComponent, MediaComponent) that must all build and provide non-null dependencies
- FileUtil uses Okio for file I/O
- EventBus 3.x is used for color picker event delivery; events silently dropped if no subscribers
- NotificationReceiver is a BroadcastReceiver that handles reminder alarm intents
- DAO queries support prefix match search via `LIKE :query || '%'` (case-insensitive for ASCII by default in SQLite)
- Product flavors are `demo` and `full` with no feature differences between them
- DB operations run on `Schedulers.io()` via RxJava, LiveData observed on main thread

## Scope Exclusions
- Social login or third-party authentication
- Cloud sync or backup
- Espresso / instrumented UI tests
- New feature development beyond what exists in the current codebase
- Changes to product flavor feature differentiation
- Custom EventBus handling for missing subscribers

## Deferred Items
- None
