# Feature: Post-Modernization Verification and Fix for Android Notes App

## Summary
Systematically verify and fix every feature in the Android Notes app following a modernization effort. The scope covers 15 feature categories — from core CRUD operations through checklists, media attachments, reminders, folder management, navigation, DI, and ViewModels — ensuring each works correctly and is covered by unit tests that run under Robolectric with an in-memory Room database.

## Detailed Requirements

### 1. Note Creation/Editing
1. Users can create a new note with title and body
2. Users can read/view an existing note
3. Users can update a note's title and body
4. Users can delete a note
5. Note entities serialize and deserialize correctly (e.g., via nav args)
6. Date fields (created, modified) are stored and retrieved accurately

### 2. Checklist Feature
7. Users can add checklist items to a note
8. Users can check (mark complete) a checklist item
9. Users can uncheck (mark incomplete) a checklist item
10. Users can remove a checklist item
11. CheckListAdapter correctly processes list updates via `submitList`

### 3. Image Attachment
12. Users can attach an image to a note
13. Users can remove an attached image from a note
14. Image attachment references serialize and deserialize correctly for storage

### 4. Audio Recording
15. Users can record and attach audio to a note
16. Users can remove an attached audio recording from a note
17. Audio attachment references serialize and deserialize correctly for storage

### 5. Reminders
18. Users can set a reminder on a note
19. Reminders schedule alarms via AlarmManager with PendingIntent using FLAG_IMMUTABLE
20. NotificationReceiver fires and displays a notification when a reminder triggers

### 6. Pin/Unpin Notes and Folders
21. Users can pin a note so it appears at the top of the list
22. Users can unpin a previously pinned note
23. Users can pin a folder
24. Users can unpin a previously pinned folder
25. Pin state is persisted as a database field toggle
26. DAO queries return pinned items filtered/sorted appropriately

### 7. Lock/Unlock Notes and Folders
27. Users can lock a note with a password
28. Users can unlock a previously locked note by entering the correct password
29. Users can lock a folder with a password
30. Users can unlock a previously locked folder by entering the correct password
31. Password is encrypted using RSA via PasswordUtil
32. PasswordDialogFragment presents the lock/unlock UI flow

### 8. Folder Operations
33. Users can create a folder
34. Users can read/view a folder's contents
35. Users can update a folder's name
36. Users can delete a folder, which cascade-deletes its contents
37. Nested folders are supported (folders within folders)
38. Users can move notes into a folder
39. Users can move folders into another folder

### 9. Search
40. Users can search notes by title
41. Users can search folders by name
42. DAO queries support partial-match search

### 10. Navigation
43. Navigation drawer displays the correct menu items and navigates to destinations
44. List-to-new-note-to-back navigation flow works without crashes or data loss
45. NoteEntity is correctly serialized and passed via navigation arguments

### 11. Color Picker
46. Users can select a color for a note
47. Selected color is persisted in the database
48. Color selection is communicated via EventBus event delivery

### 12. File Operations
49. FileUtil writes files to storage using Okio
50. FileUtil reads files from storage using Okio
51. FileUtil deletes files from storage
52. Missing or inaccessible files are handled gracefully without crashes

### 13. Dagger Dependency Injection
53. All 7 Dagger components build successfully
54. Each component provides non-null dependencies to its consumers

### 14. ViewModels
55. NoteViewModel correctly delegates operations and exposes LiveData
56. FolderViewModel correctly delegates operations and exposes LiveData
57. LiveData emissions reflect underlying data changes

### 15. Bug Fixes
58. Any feature found broken during verification is fixed

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
11. Given a note, when the user sets a reminder time, then an alarm is scheduled via AlarmManager with FLAG_IMMUTABLE on the PendingIntent
12. Given a scheduled reminder, when the alarm fires, then NotificationReceiver displays a notification
13. Given a note or folder, when the user pins it, then the pin DB field is toggled to true and the item sorts to the top
14. Given a pinned note or folder, when the user unpins it, then the pin DB field is toggled to false and normal sort order resumes
15. Given a note or folder, when the user locks it with a password, then the password is RSA-encrypted via PasswordUtil and stored
16. Given a locked note or folder, when the user enters the correct password in PasswordDialogFragment, then access is granted
17. Given a locked note or folder, when the user enters an incorrect password, then access is denied
18. Given a folder, when the user deletes it, then all contained notes and nested folders are cascade-deleted
19. Given a note or folder, when the user moves it into a folder, then the parent reference updates and the item appears in the target folder
20. Given a search query, when the user types a partial title or folder name, then matching results are returned by the DAO
21. Given the navigation drawer, when the user selects a destination, then the app navigates to the correct screen
22. Given the new-note screen, when the user presses back, then they return to the list without data loss or crash
23. Given a NoteEntity, when it is passed via navigation arguments, then it serializes and deserializes without data loss
24. Given the color picker, when the user selects a color, then it is saved to the DB and delivered via EventBus
25. Given a file path, when FileUtil writes then reads the file, then the content round-trips correctly via Okio
26. Given a missing file path, when FileUtil attempts to read or delete, then it handles the error gracefully without crashing
27. Given the Dagger dependency graph, when all 7 components are built, then every provided dependency is non-null
28. Given NoteViewModel and FolderViewModel, when underlying data changes, then LiveData emissions reflect those changes
29. Given the test suite, when `./gradlew testDemoDebugUnitTest` is executed, then all tests pass
30. Given both product flavors, when the project is built, then both flavors compile successfully

## Constraints
- No Espresso or instrumented tests — all tests must reside in `app/src/test/`
- Robolectric must be used for any tests requiring the Android framework
- Room must use an in-memory database for testing
- Tests must pass via `./gradlew testDemoDebugUnitTest`
- Existing tests must not be removed
- Both product flavors must build successfully
- Bug fixes must not introduce regressions in other features

## Dependencies
- Android Room persistence library (in-memory DB for tests)
- Robolectric (Android framework simulation in unit tests)
- Dagger (dependency injection — 7 components)
- AlarmManager / PendingIntent (system alarm scheduling)
- EventBus (color picker event delivery)
- Okio (file read/write/delete operations)
- Android Navigation Component (nav args serialization)
- RSA encryption APIs (PasswordUtil)

## Non-Functional Requirements
- All unit tests must execute and pass within the `testDemoDebugUnitTest` Gradle task
- Both product flavors must compile without errors
- File operations must handle missing files gracefully (no unhandled exceptions)
- PendingIntent must use FLAG_IMMUTABLE (Android 12+ compliance)

## Technical Notes
- CheckListAdapter uses `submitList` for list diffing — tests should verify correct diff behavior
- NoteEntity is serialized via navigation arguments — Parcelable or Serializable implementation is in use
- PasswordUtil uses RSA encryption for lock/unlock passwords
- PasswordDialogFragment handles the UI flow for entering lock/unlock passwords
- Dagger has 7 components that must all build and provide non-null dependencies
- FileUtil uses Okio for file I/O
- EventBus is used for color picker event delivery
- NotificationReceiver is a BroadcastReceiver that handles reminder alarm intents
- DAO queries support filtered/sorted results for pinned items and partial-match search

## Open Questions
1. What are the specific 7 Dagger components? Are there scoping or lifecycle requirements for each?
2. What is the exact serialization mechanism for NoteEntity in nav args — Parcelable, Serializable, or Safe Args plugin?
3. What are the specific DAO query signatures for search — is it `LIKE '%query%'` or something else?
4. What is the expected behavior when a reminder is set for a time in the past?
5. What happens to reminders when a note is deleted — are scheduled alarms cancelled?
6. Is there a maximum file size or type restriction for image and audio attachments?
7. What is the expected behavior for nested folder depth — is there a limit?
8. How should cascade delete interact with locked folders — does it bypass the lock?
9. What are the two product flavors, and do they differ in feature availability?
10. Are there specific LiveData threading requirements (main thread observation vs. background emission)?
11. What is the expected behavior when EventBus has no subscribers for a color picker event?
12. Should search be case-insensitive?
13. What RSA key size and padding scheme does PasswordUtil use?
14. Are there any accessibility requirements for the UI components (PasswordDialogFragment, color picker, navigation drawer)?
