/*
 *     Credit Card Security V1  Copyright (C) 2018  sid-sun
 *     This program comes with ABSOLUTELY NO WARRANTY; for details type `show w'.
 *     This is free software, and you are welcome to redistribute it
 *     under certain conditions; type `show c' for details.
 *
 * The hypothetical commands `show w' and `show c' should show the appropriate
 * parts of the General Public License.  Of course, your program's commands
 * might be different; for a GUI interface, you would use an "about box".
 *
 *   You should also get your employer (if you work as a programmer) or school,
 * if any, to sign a "copyright disclaimer" for the program, if necessary.
 * For more information on this, and how to apply and follow the GNU GPL, see
 * <http://www.gnu.org/licenses/>.
 *
 *   The GNU General Public License does not permit incorporating your program
 * into proprietary programs.  If your program is a subroutine library, you
 * may consider it more useful to permit linking proprietary applications with
 * the library.  If this is what you want to do, use the GNU Lesser General
 * Public License instead of this License.  But first, please read
 * <http://www.gnu.org/philosophy/why-not-lgpl.html>.
 */

package com.nrs.nsnik.notes.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {

    @get:Query("SELECT * FROM NoteEntity")
    val notesList: LiveData<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity WHERE uid = :id")
    fun getNote(id: Int): LiveData<NoteEntity>

    @Query("SELECT * FROM NoteEntity WHERE folderName = :folderName")
    fun getNoteByFolderName(folderName: String): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity WHERE folderName = :folderName ORDER BY pinned DESC")
    fun getNoteByFolderNameOrdered(folderName: String): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity WHERE folderName = :folderName AND locked = 0 And pinned = 0")
    fun getNotesByFolderNameNotPinnedNotLocked(folderName: String): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity WHERE folderName = :folderName AND locked = 0 And pinned = 1")
    fun getNotesByFolderNamePinnedNotLocked(folderName: String): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity WHERE folderName = :folderName AND locked = 1 And pinned = 0")
    fun getNotesByFolderNameNotPinnedLocked(folderName: String): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity WHERE folderName = :folderName AND locked = 1 And pinned = 1")
    fun getNotesByFolderNamePinnedLocked(folderName: String): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity WHERE title LIKE :query|| '%'")
    fun getNoteByQuery(query: String): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity WHERE pinned = :isPinned")
    fun getNoteByPin(isPinned: Int): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity WHERE locked = :isLocked")
    fun getNoteByLock(isLocked: Int): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity WHERE color = :color")
    fun getNoteByColor(color: String): LiveData<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNotes(vararg noteEntity: NoteEntity): LongArray

    @Delete
    fun deleteNotes(vararg noteEntities: NoteEntity)

    @Query("DELETE FROM NoteEntity WHERE folderName = :folderName")
    fun deleteNoteByFolderName(folderName: String)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateNote(vararg noteEntities: NoteEntity): Int

    @Query("UPDATE NoteEntity SET pinned = :pin WHERE uid = :id")
    fun changeNotePinStatus(id: Int, pin: Int)

    @Query("UPDATE NoteEntity SET locked = :lock WHERE uid = :id")
    fun changeNoteLockStatus(id: Int, lock: Int)

    @Query("UPDATE NoteEntity SET folderName = :folderName WHERE uid = :id")
    fun changeNoteFolder(id: Int, folderName: String)
}

