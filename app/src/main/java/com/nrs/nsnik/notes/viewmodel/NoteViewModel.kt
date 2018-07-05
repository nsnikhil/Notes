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
import com.nrs.nsnik.notes.data.NoteEntity
import com.nrs.nsnik.notes.util.DbUtil


class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val mDbUtil: DbUtil = (application as MyApplication).dbUtil
    private val noteList: LiveData<List<NoteEntity>> = mDbUtil.noteList

    fun insertNote(vararg noteEntities: NoteEntity) {
        mDbUtil.insertNote(*noteEntities)
    }

    fun updateNote(vararg noteEntities: NoteEntity) {
        mDbUtil.updateNote(*noteEntities)
    }

    fun deleteNote(vararg noteEntities: NoteEntity) {
        mDbUtil.deleteNote(*noteEntities)
    }

    fun deleteNoteByFolderName(folderName: String) {
        mDbUtil.deleteNoteByFolderName(folderName)
    }

    fun getNoteById(id: Int): LiveData<NoteEntity> {
        return mDbUtil.getNoteById(id)
    }

    fun getNoteByFolderName(folderName: String): LiveData<List<NoteEntity>> {
        return mDbUtil.getNoteByFolderName(folderName)
    }

    fun getNoteByFolderNameNoPinNoLock(folderName: String): LiveData<List<NoteEntity>> {
        return mDbUtil.getNoteByFolderNameNoPinNoLock(folderName)
    }

    fun getNoteByFolderNamePinNoLock(folderName: String): LiveData<List<NoteEntity>> {
        return mDbUtil.getNoteByFolderNamePinNoLock(folderName)
    }

    fun getNoteByFolderNameNoPinLock(folderName: String): LiveData<List<NoteEntity>> {
        return mDbUtil.getNoteByFolderNameNoPinLock(folderName)
    }

    fun getNoteByFolderNamePinLock(folderName: String): LiveData<List<NoteEntity>> {
        return mDbUtil.getNoteByFolderNamePinLock(folderName)
    }

    fun getNoteByFolderNameOrdered(folderName: String): LiveData<List<NoteEntity>> {
        return mDbUtil.getNoteByFolderNameOrdered(folderName)
    }

    fun searchNote(query: String): LiveData<List<NoteEntity>> {
        return mDbUtil.searchNote(query)
    }

    fun getNoteByPin(isPinned: Int): LiveData<List<NoteEntity>> {
        return mDbUtil.getNotesByPin(isPinned)
    }

    fun getNoteByLock(isLocked: Int): LiveData<List<NoteEntity>> {
        return mDbUtil.getNotesByLock(isLocked)
    }

    fun getNoteByColor(color: String): LiveData<List<NoteEntity>> {
        return mDbUtil.getNotesByColor(color)
    }

    fun changeNotePinStatus(id: Int, pin: Int) {
        return mDbUtil.changeNotePinStatus(id, pin)
    }

    fun changeNoteLockStatus(id: Int, lock: Int) {
        return mDbUtil.changeNoteLockStatus(id, lock)
    }

    fun changeNoteFolder(id: Int, folderName: String) {
        return mDbUtil.changeNoteFolder(id, folderName)
    }
}
