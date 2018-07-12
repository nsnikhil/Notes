/*
 *     Notes  Copyright (C) 2018  Nikhil Soni
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

package com.nrs.nsnik.notes.util


import androidx.lifecycle.LiveData
import com.nrs.nsnik.notes.dagger.scopes.ApplicationScope
import com.nrs.nsnik.notes.data.FolderEntity
import com.nrs.nsnik.notes.data.NoteEntity
import com.nrs.nsnik.notes.data.NotesDatabase
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject


@ApplicationScope
class DbUtil @Inject
internal constructor(private val mNotesDatabase: NotesDatabase, @param:ApplicationScope private val mFileUtil: FileUtil) {

    val noteList: LiveData<List<NoteEntity>>
        get() = mNotesDatabase.noteDao.notesList

    val folderList: LiveData<List<FolderEntity>>
        get() = mNotesDatabase.folderDao.foldersList

    fun insertNote(vararg noteEntities: NoteEntity) {
        val single = Single.fromCallable { mNotesDatabase.noteDao.insertNotes(*noteEntities) }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        single.subscribe(object : SingleObserver<LongArray> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onSuccess(longs: LongArray) {
                noteEntities.forEach {
                    try {
                        mFileUtil.saveNote(it, it.fileName!!)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
                longs.forEach {
                    Timber.d(it.toString())
                }
            }

            override fun onError(e: Throwable) {
                Timber.d(e.message)
            }
        })
    }

    fun deleteNote(vararg noteEntities: NoteEntity) {
        val completable = Completable.fromCallable {
            mNotesDatabase.noteDao.deleteNotes(*noteEntities)
            null
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        completable.subscribe(object : CompletableObserver {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onComplete() {
                Timber.d("Delete successful")
            }

            override fun onError(e: Throwable) {
                Timber.d(e.message)
            }
        })
    }

    fun deleteNoteByFolderName(folderName: String) {
        val completable = Completable.fromCallable {
            mNotesDatabase.noteDao.deleteNoteByFolderName(folderName)
            null
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        completable.subscribe(object : CompletableObserver {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onComplete() {
                Timber.d("Delete successful")
            }

            override fun onError(e: Throwable) {
                Timber.d(e.message)
            }
        })
    }

    fun updateNote(vararg noteEntities: NoteEntity) {
        val single = Single.fromCallable { mNotesDatabase.noteDao.updateNote(*noteEntities) }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        single.subscribe(object : SingleObserver<Int> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onSuccess(t: Int) {
                noteEntities.forEach {
                    try {
                        mFileUtil.saveNote(it, it.fileName!!)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                Timber.d(t.toString())
            }

            override fun onError(e: Throwable) {
                Timber.d(e.message)
            }
        })
    }

    fun getNoteById(id: Int): LiveData<NoteEntity> {
        return mNotesDatabase.noteDao.getNote(id)
    }

    fun getNoteByFolderName(folderName: String): LiveData<List<NoteEntity>> {
        return mNotesDatabase.noteDao.getNoteByFolderName(folderName)
    }

    fun getNoteByFolderNameNoPinNoLock(folderName: String): LiveData<List<NoteEntity>> {
        return mNotesDatabase.noteDao.getNotesByFolderNameNotPinnedNotLocked(folderName)
    }

    fun getNoteByFolderNamePinNoLock(folderName: String): LiveData<List<NoteEntity>> {
        return mNotesDatabase.noteDao.getNotesByFolderNamePinnedNotLocked(folderName)
    }

    fun getNoteByFolderNameNoPinLock(folderName: String): LiveData<List<NoteEntity>> {
        return mNotesDatabase.noteDao.getNotesByFolderNameNotPinnedLocked(folderName)
    }

    fun getNoteByFolderNamePinLock(folderName: String): LiveData<List<NoteEntity>> {
        return mNotesDatabase.noteDao.getNotesByFolderNamePinnedLocked(folderName)
    }

    fun getNoteByFolderNameOrdered(folderName: String): LiveData<List<NoteEntity>> {
        return mNotesDatabase.noteDao.getNoteByFolderNameOrdered(folderName)
    }

    fun getNoteByFolderNameOrderedLock(folderName: String): LiveData<List<NoteEntity>> {
        return mNotesDatabase.noteDao.getNoteByFolderNameOrderedLock(folderName)
    }

    fun searchNote(query: String): LiveData<List<NoteEntity>> {
        return mNotesDatabase.noteDao.getNoteByQuery(query)
    }

    fun getNotesByPin(isPinned: Int): LiveData<List<NoteEntity>> {
        return mNotesDatabase.noteDao.getNoteByPin(isPinned)
    }

    fun getNotesByLock(isLocked: Int): LiveData<List<NoteEntity>> {
        return mNotesDatabase.noteDao.getNoteByLock(isLocked)
    }

    fun getNotesByColor(color: String): LiveData<List<NoteEntity>> {
        return mNotesDatabase.noteDao.getNoteByColor(color)
    }

    fun changeNotePinStatus(id: Int, pin: Int) {
        val single = Single.fromCallable {
            mNotesDatabase.noteDao.changeNotePinStatus(id, pin)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        single.subscribe(object : SingleObserver<Unit> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onSuccess(t: Unit) {
                Timber.d("Updated")
            }

            override fun onError(e: Throwable) {
                Timber.d(e.message)
            }
        })
    }

    fun changeNoteLockStatus(id: Int, lock: Int) {
        val single = Single.fromCallable {
            mNotesDatabase.noteDao.changeNoteLockStatus(id, lock)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        single.subscribe(object : SingleObserver<Unit> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onSuccess(t: Unit) {
                Timber.d("Updated")
            }

            override fun onError(e: Throwable) {
                Timber.d(e.message)
            }
        })
    }


    fun changeNoteFolder(id: Int, folderName: String) {
        val single = Single.fromCallable {
            mNotesDatabase.noteDao.changeNoteFolder(id, folderName)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        single.subscribe(object : SingleObserver<Unit> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onSuccess(t: Unit) {
                Timber.d("Updated")
            }

            override fun onError(e: Throwable) {
                Timber.d(e.message)
            }
        })
    }

    /**
     * FOLDER
     */

    fun insertFolder(vararg folderEntities: FolderEntity) {
        val single = Single.fromCallable {
            mNotesDatabase.folderDao.insertFolders(*folderEntities)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        single.subscribe(object : SingleObserver<LongArray> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onSuccess(longs: LongArray) {
                longs.forEach {
                    Timber.d(it.toString())
                }
            }

            override fun onError(e: Throwable) {
                Timber.d(e.message)
            }
        })

    }

    fun deleteFolder(vararg folderEntities: FolderEntity) {
        val completable = Completable.fromCallable {
            mNotesDatabase.folderDao.deleteFolders(*folderEntities)
            null
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        completable.subscribe(object : CompletableObserver {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onComplete() {
                Timber.d("Delete Successful")
            }

            override fun onError(e: Throwable) {
                Timber.d(e.message)
            }
        })
    }

    fun deleteFolderByName(folderName: String) {
        val completable = Completable.fromCallable {
            mNotesDatabase.folderDao.deleteFolderByName(folderName)
            null
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        completable.subscribe(object : CompletableObserver {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onComplete() {
                Timber.d("Delete Successful")
            }

            override fun onError(e: Throwable) {
                Timber.d(e.message)
            }
        })
    }

    fun deleteFolderByParent(parentFolderName: String) {
        val completable = Completable.fromCallable {
            mNotesDatabase.folderDao.deleteFolderByParent(parentFolderName)
            null
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        completable.subscribe(object : CompletableObserver {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onComplete() {
                Timber.d("Delete Successful")
            }

            override fun onError(e: Throwable) {
                Timber.d(e.message)
            }
        })
    }

    fun updateFolder(vararg folderEntities: FolderEntity) {
        val single = Single.fromCallable { mNotesDatabase.folderDao.updateFolders(*folderEntities) }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        single.subscribe(object : SingleObserver<Int> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onSuccess(t: Int) {
                Timber.d(t.toString())
            }

            override fun onError(e: Throwable) {
                Timber.d(e.message)
            }
        })
    }

    fun getFolders(): LiveData<List<FolderEntity>> {
        return mNotesDatabase.folderDao.getFolders()
    }

    fun getFolderById(id: Int): LiveData<FolderEntity> {
        return mNotesDatabase.folderDao.getFolder(id)
    }

    fun getFolderByName(name: String): LiveData<FolderEntity> {
        return mNotesDatabase.folderDao.getFolderByName(name)
    }

    fun searchFolder(query: String): LiveData<List<FolderEntity>> {
        return mNotesDatabase.folderDao.getFolderByQuery(query)
    }

    fun getFolderByParent(parentFolder: String): LiveData<List<FolderEntity>> {
        return mNotesDatabase.folderDao.getFolderByParent(parentFolder)
    }

    fun getFolderByParentNoPinNoLock(parentFolder: String): LiveData<List<FolderEntity>> {
        return mNotesDatabase.folderDao.getFolderByParentNoPinNoLock(parentFolder)
    }

    fun getFolderByParentPinNoLock(parentFolder: String): LiveData<List<FolderEntity>> {
        return mNotesDatabase.folderDao.getFolderByParentPinNoLock(parentFolder)
    }

    fun getFolderByParentNoPinLock(parentFolder: String): LiveData<List<FolderEntity>> {
        return mNotesDatabase.folderDao.getFolderByParentNoPinLock(parentFolder)
    }

    fun getFolderByParentPinLock(parentFolder: String): LiveData<List<FolderEntity>> {
        return mNotesDatabase.folderDao.getFolderByParentPinLock(parentFolder)
    }

    fun getFolderByParentOrdered(parentFolder: String): LiveData<List<FolderEntity>> {
        return mNotesDatabase.folderDao.getFolderByParentOrdered(parentFolder)
    }

    fun getFolderByParentOrderedLock(parentFolder: String): LiveData<List<FolderEntity>> {
        return mNotesDatabase.folderDao.getFolderByParentOrderedLock(parentFolder)
    }

    fun getFolderByPin(isPinned: Int): LiveData<List<FolderEntity>> {
        return mNotesDatabase.folderDao.getFolderByPin(isPinned)
    }

    fun getFolderByLock(isLocked: Int): LiveData<List<FolderEntity>> {
        return mNotesDatabase.folderDao.getFolderByLock(isLocked)
    }

    fun getFolderByColor(color: String): LiveData<List<FolderEntity>> {
        return mNotesDatabase.folderDao.getFolderByColor(color)
    }

    fun changeFolderPinStatus(id: Int, pin: Int) {
        val single = Single.fromCallable {
            mNotesDatabase.folderDao.changeFolderPinStatus(id, pin)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        single.subscribe(object : SingleObserver<Unit> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onSuccess(t: Unit) {
                Timber.d("Updated")
            }

            override fun onError(e: Throwable) {
                Timber.d(e.message)
            }
        })
    }

    fun changeFolderLockStatus(id: Int, lock: Int) {
        val single = Single.fromCallable {
            mNotesDatabase.folderDao.changeFolderLockStatus(id, lock)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        single.subscribe(object : SingleObserver<Unit> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onSuccess(t: Unit) {
                Timber.d("Updated")
            }

            override fun onError(e: Throwable) {
                Timber.d(e.message)
            }
        })
    }

    fun changeFolderParent(id: Int, parentFolderName: String) {
        val single = Single.fromCallable {
            mNotesDatabase.folderDao.changeFolderParent(id, parentFolderName)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        single.subscribe(object : SingleObserver<Unit> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onSuccess(t: Unit) {
                Timber.d("Updated")
            }

            override fun onError(e: Throwable) {
                Timber.d(e.message)
            }
        })
    }


}
