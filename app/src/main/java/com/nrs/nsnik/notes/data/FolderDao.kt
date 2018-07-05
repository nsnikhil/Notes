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
interface FolderDao {

    @get:Query("SELECT * FROM FolderEntity")
    val foldersList: LiveData<List<FolderEntity>>

    @Query("SELECT * FROM FolderEntity")
    fun getFolders(): LiveData<List<FolderEntity>>

    @Query("SELECT * FROM FolderEntity WHERE uid = :id")
    fun getFolder(id: Int): LiveData<FolderEntity>

    @Query("SELECT * FROM FolderEntity WHERE folderName = :folderName")
    fun getFolderByName(folderName: String): LiveData<FolderEntity>

    @Query("SELECT * FROM FolderEntity WHERE folderName LIKE :query|| '%'")
    fun getFolderByQuery(query: String): LiveData<List<FolderEntity>>

    @Query("SELECT * FROM FolderEntity WHERE parentFolderName = :parentFolder")
    fun getFolderByParent(parentFolder: String): LiveData<List<FolderEntity>>

    @Query("SELECT * FROM FolderEntity WHERE parentFolderName = :parentFolder AND pinned = 0 AND locked = 0")
    fun getFolderByParentNoPinNoLock(parentFolder: String): LiveData<List<FolderEntity>>

    @Query("SELECT * FROM FolderEntity WHERE parentFolderName = :parentFolder AND pinned = 1 AND locked = 0")
    fun getFolderByParentPinNoLock(parentFolder: String): LiveData<List<FolderEntity>>

    @Query("SELECT * FROM FolderEntity WHERE parentFolderName = :parentFolder AND pinned = 0 AND locked = 1")
    fun getFolderByParentNoPinLock(parentFolder: String): LiveData<List<FolderEntity>>

    @Query("SELECT * FROM FolderEntity WHERE parentFolderName = :parentFolder AND pinned = 1 AND locked = 1")
    fun getFolderByParentPinLock(parentFolder: String): LiveData<List<FolderEntity>>

    @Query(" SELECT * FROM FolderEntity WHERE parentFolderName = :parentFolder ORDER BY pinned DESC")
    fun getFolderByParentOrdered(parentFolder: String): LiveData<List<FolderEntity>>

    @Query("SELECT * FROM FolderEntity WHERE pinned = :isPinned")
    fun getFolderByPin(isPinned: Int): LiveData<List<FolderEntity>>

    @Query("SELECT * FROM FolderEntity WHERE locked = :isLocked")
    fun getFolderByLock(isLocked: Int): LiveData<List<FolderEntity>>

    @Query("SELECT * FROM FolderEntity WHERE color = :color")
    fun getFolderByColor(color: String): LiveData<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFolders(vararg folderEntities: FolderEntity): LongArray

    @Delete
    fun deleteFolders(vararg folderEntities: FolderEntity)

    @Query("DELETE FROM FolderEntity WHERE folderName = :folderName")
    fun deleteFolderByName(folderName: String)

    @Query("DELETE FROM FolderEntity WHERE parentFolderName = :parentFolderName")
    fun deleteFolderByParent(parentFolderName: String)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateFolders(vararg folderEntities: FolderEntity): Int

    @Query("UPDATE FolderEntity SET pinned = :pin WHERE uid = :id")
    fun changeFolderPinStatus(id: Int, pin: Int)

    @Query("UPDATE FolderEntity SET locked = :lock WHERE uid = :id")
    fun changeFolderLockStatus(id: Int, lock: Int)

    @Query("UPDATE FolderEntity SET parentFolderName = :parentFolderName WHERE uid = :id")
    fun changeFolderParent(id: Int, parentFolderName: String)

}
