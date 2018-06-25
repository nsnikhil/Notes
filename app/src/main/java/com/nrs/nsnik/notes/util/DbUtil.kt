/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
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
                for (noteEntity in noteEntities) {
                    try {
                        mFileUtil.saveNote(noteEntity, noteEntity.fileName!!)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
                for (aLong in longs) {
                    Timber.d(aLong.toString())
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
                for (noteEntity in noteEntities) {
                    try {
                        mFileUtil.saveNote(noteEntity, noteEntity.fileName!!)
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

    fun insertFolder(vararg folderEntities: FolderEntity) {
        val single = Single.fromCallable { mNotesDatabase.folderDao.insertFolders(*folderEntities) }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        single.subscribe(object : SingleObserver<LongArray> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onSuccess(longs: LongArray) {
                for (aLong in longs) {
                    Timber.d(aLong.toString())
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

    fun getFolderByPin(isPinned: Int): LiveData<List<FolderEntity>> {
        return mNotesDatabase.folderDao.getFolderByPin(isPinned)
    }

    fun getFolderByLock(isLocked: Int): LiveData<List<FolderEntity>> {
        return mNotesDatabase.folderDao.getFolderByLock(isLocked)
    }

    fun getFolderByColor(color: String): LiveData<List<FolderEntity>> {
        return mNotesDatabase.folderDao.getFolderByColor(color)
    }
}
