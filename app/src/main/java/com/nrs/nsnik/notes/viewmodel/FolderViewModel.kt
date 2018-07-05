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

package com.nrs.nsnik.notes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

import com.nrs.nsnik.notes.MyApplication
import com.nrs.nsnik.notes.data.FolderEntity
import com.nrs.nsnik.notes.util.DbUtil

class FolderViewModel(application: Application) : AndroidViewModel(application) {

    private val mDbUtil: DbUtil = (application as MyApplication).dbUtil
    val folderList: LiveData<List<FolderEntity>> = mDbUtil.folderList

    fun insertFolder(vararg folderEntities: FolderEntity) {
        mDbUtil.insertFolder(*folderEntities)
    }

    fun updateFolder(vararg folderEntities: FolderEntity) {
        mDbUtil.updateFolder(*folderEntities)
    }

    fun deleteFolder(vararg folderEntities: FolderEntity) {
        mDbUtil.deleteFolder(*folderEntities)
    }

    fun deleteFolderByName(name: String) {
        mDbUtil.deleteFolderByName(name)
    }

    fun deleteFolderByParent(parentFolderName: String) {
        mDbUtil.deleteFolderByParent(parentFolderName)
    }

    fun getFolders(): LiveData<List<FolderEntity>> {
        return mDbUtil.getFolders()
    }

    fun getFolderById(id: Int): LiveData<FolderEntity> {
        return mDbUtil.getFolderById(id)
    }

    fun getFolderByName(name: String): LiveData<FolderEntity> {
        return mDbUtil.getFolderByName(name)
    }

    fun searchFolder(query: String): LiveData<List<FolderEntity>> {
        return mDbUtil.searchFolder(query)
    }

    fun getFolderByParent(parentFolderName: String): LiveData<List<FolderEntity>> {
        return mDbUtil.getFolderByParent(parentFolderName)
    }

    fun getFolderByParentNoPinNoLock(parentFolderName: String): LiveData<List<FolderEntity>> {
        return mDbUtil.getFolderByParentNoPinNoLock(parentFolderName)
    }

    fun getFolderByParentPinNoLock(parentFolderName: String): LiveData<List<FolderEntity>> {
        return mDbUtil.getFolderByParentPinNoLock(parentFolderName)
    }

    fun getFolderByParentNoPinLock(parentFolderName: String): LiveData<List<FolderEntity>> {
        return mDbUtil.getFolderByParentNoPinLock(parentFolderName)
    }

    fun getFolderByParentPinLock(parentFolderName: String): LiveData<List<FolderEntity>> {
        return mDbUtil.getFolderByParentPinLock(parentFolderName)
    }

    fun getFolderByParentOrdered(parentFolderName: String): LiveData<List<FolderEntity>> {
        return mDbUtil.getFolderByParentOrdered(parentFolderName)
    }

    fun getFolderByPin(isPinned: Int): LiveData<List<FolderEntity>> {
        return mDbUtil.getFolderByPin(isPinned)

    }

    fun getFolderByLock(isLocked: Int): LiveData<List<FolderEntity>> {
        return mDbUtil.getFolderByLock(isLocked)

    }

    fun getFolderByColor(color: String): LiveData<List<FolderEntity>> {
        return mDbUtil.getFolderByColor(color)
    }

    fun changeFolderPinStatus(id: Int, pin: Int) {
        return mDbUtil.changeFolderPinStatus(id, pin)
    }

    fun changeFolderLockStatus(id: Int, lock: Int) {
        return mDbUtil.changeFolderLockStatus(id, lock)
    }

    fun changeFolderParent(id: Int, parentFolderName: String) {
        return mDbUtil.changeFolderParent(id, parentFolderName)
    }
}
