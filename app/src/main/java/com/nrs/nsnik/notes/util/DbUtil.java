/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.util;


import android.arch.lifecycle.LiveData;

import com.nrs.nsnik.notes.dagger.scopes.ApplicationScope;
import com.nrs.nsnik.notes.data.FolderEntity;
import com.nrs.nsnik.notes.data.NoteEntity;
import com.nrs.nsnik.notes.data.NotesDatabase;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


@ApplicationScope
public class DbUtil {

    private final NotesDatabase mNotesDatabase;
    private final FileUtil mFileUtil;

    @Inject
    DbUtil(NotesDatabase notesDatabase, @NotNull @ApplicationScope FileUtil fileUtil) {
        this.mNotesDatabase = notesDatabase;
        this.mFileUtil = fileUtil;
    }

    public void insertNote(NoteEntity... noteEntities) {
        Single<long[]> single = Single.fromCallable(() -> mNotesDatabase.getNoteDao().insertNotes(noteEntities)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        single.subscribe(new SingleObserver<long[]>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(long[] longs) {
                for (NoteEntity noteEntity : noteEntities) {
                    try {
                        mFileUtil.saveNote(noteEntity, noteEntity.getFileName());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                for (long aLong : longs) {
                    Timber.d(String.valueOf(aLong));
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.d(e.getMessage());
            }
        });
    }

    public void deleteNote(NoteEntity... noteEntities) {
        Completable completable = Completable.fromCallable(() -> {
            mNotesDatabase.getNoteDao().deleteNotes(noteEntities);
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                Timber.d("Delete successful");
            }

            @Override
            public void onError(Throwable e) {
                Timber.d(e.getMessage());
            }
        });
    }

    public void deleteNoteByFolderName(String folderName) {
        Completable completable = Completable.fromCallable(() -> {
            mNotesDatabase.getNoteDao().deleteNoteByFolderName(folderName);
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                Timber.d("Delete successful");
            }

            @Override
            public void onError(Throwable e) {
                Timber.d(e.getMessage());
            }
        });
    }

    public void updateNote(NoteEntity... noteEntities) {
        Single<Integer> single = Single.fromCallable(() -> mNotesDatabase.getNoteDao().updateNote(noteEntities)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        single.subscribe(new SingleObserver<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(Integer integer) {
                for (NoteEntity noteEntity : noteEntities) {
                    try {
                        mFileUtil.saveNote(noteEntity, noteEntity.getFileName());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Timber.d(String.valueOf(integer));
            }

            @Override
            public void onError(Throwable e) {
                Timber.d(e.getMessage());
            }
        });
    }

    public LiveData<List<NoteEntity>> getNoteList() {
        return mNotesDatabase.getNoteDao().getNotesList();
    }

    public LiveData<NoteEntity> getNoteById(int id) {
        return mNotesDatabase.getNoteDao().getNote(id);
    }

    public LiveData<List<NoteEntity>> getNoteByFolderName(String folderName) {
        return mNotesDatabase.getNoteDao().getNoteByFolderName(folderName);
    }

    public LiveData<List<NoteEntity>> getNoteByFolderNameNoPinNoLock(String folderName) {
        return mNotesDatabase.getNoteDao().getNotesByFolderNameNotPinnedNotLocked(folderName);
    }

    public LiveData<List<NoteEntity>> getNoteByFolderNamePinNoLock(String folderName) {
        return mNotesDatabase.getNoteDao().getNotesByFolderNamePinnedNotLocked(folderName);
    }

    public LiveData<List<NoteEntity>> getNoteByFolderNameNoPinLock(String folderName) {
        return mNotesDatabase.getNoteDao().getNotesByFolderNameNotPinnedLocked(folderName);
    }

    public LiveData<List<NoteEntity>> getNoteByFolderNamePinLock(String folderName) {
        return mNotesDatabase.getNoteDao().getNotesByFolderNamePinnedLocked(folderName);
    }

    public LiveData<List<NoteEntity>> searchNote(String query) {
        return mNotesDatabase.getNoteDao().getNoteByQuery(query);
    }

    public LiveData<List<NoteEntity>> getNotesByPin(int isPinned) {
        return mNotesDatabase.getNoteDao().getNoteByPin(isPinned);
    }

    public LiveData<List<NoteEntity>> getNotesByLock(int isLocked) {
        return mNotesDatabase.getNoteDao().getNoteByLock(isLocked);
    }

    public LiveData<List<NoteEntity>> getNotesByColor(String color) {
        return mNotesDatabase.getNoteDao().getNoteByColor(color);
    }

    public void insertFolder(FolderEntity... folderEntities) {
        Single<long[]> single = Single.fromCallable(() -> mNotesDatabase.getFolderDao().insertFolders(folderEntities)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        single.subscribe(new SingleObserver<long[]>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(long[] longs) {
                for (long aLong : longs) {
                    Timber.d(String.valueOf(aLong));
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.d(e.getMessage());
            }
        });

    }

    public void deleteFolder(FolderEntity... folderEntities) {
        Completable completable = Completable.fromCallable(() -> {
            mNotesDatabase.getFolderDao().deleteFolders(folderEntities);
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                Timber.d("Delete Successful");
            }

            @Override
            public void onError(Throwable e) {
                Timber.d(e.getMessage());
            }
        });
    }

    public void deleteFolderByName(String folderName) {
        Completable completable = Completable.fromCallable(() -> {
            mNotesDatabase.getFolderDao().deleteFolderByName(folderName);
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                Timber.d("Delete Successful");
            }

            @Override
            public void onError(Throwable e) {
                Timber.d(e.getMessage());
            }
        });
    }

    public void deleteFolderByParent(String parentFolderName) {
        Completable completable = Completable.fromCallable(() -> {
            mNotesDatabase.getFolderDao().deleteFolderByParent(parentFolderName);
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                Timber.d("Delete Successful");
            }

            @Override
            public void onError(Throwable e) {
                Timber.d(e.getMessage());
            }
        });
    }

    public void updateFolder(FolderEntity... folderEntities) {
        Single<Integer> single = Single.fromCallable(() -> mNotesDatabase.getFolderDao().updateFolders(folderEntities)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        single.subscribe(new SingleObserver<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(Integer integer) {
                Timber.d(String.valueOf(integer));
            }

            @Override
            public void onError(Throwable e) {
                Timber.d(e.getMessage());
            }
        });
    }

    public LiveData<List<FolderEntity>> getFolderList() {
        return mNotesDatabase.getFolderDao().getFoldersList();
    }

    public LiveData<FolderEntity> getFolderById(int id) {
        return mNotesDatabase.getFolderDao().getFolder(id);
    }

    public LiveData<FolderEntity> getFolderByName(String name) {
        return mNotesDatabase.getFolderDao().getFolderByName(name);
    }

    public LiveData<List<FolderEntity>> searchFolder(String query) {
        return mNotesDatabase.getFolderDao().getFolderByQuery(query);
    }

    public LiveData<List<FolderEntity>> getFolderByParent(String parentFolder) {
        return mNotesDatabase.getFolderDao().getFolderByParent(parentFolder);
    }

    public LiveData<List<FolderEntity>> getFolderByParentNoPinNoLock(String parentFolder) {
        return mNotesDatabase.getFolderDao().getFolderByParentNoPinNoLock(parentFolder);
    }

    public LiveData<List<FolderEntity>> getFolderByParentPinNoLock(String parentFolder) {
        return mNotesDatabase.getFolderDao().getFolderByParentPinNoLock(parentFolder);
    }

    public LiveData<List<FolderEntity>> getFolderByParentNoPinLock(String parentFolder) {
        return mNotesDatabase.getFolderDao().getFolderByParentNoPinLock(parentFolder);
    }

    public LiveData<List<FolderEntity>> getFolderByParentPinLock(String parentFolder) {
        return mNotesDatabase.getFolderDao().getFolderByParentPinLock(parentFolder);
    }

    public LiveData<List<FolderEntity>> getFolderByPin(int isPinned) {
        return mNotesDatabase.getFolderDao().getFolderByPin(isPinned);
    }

    public LiveData<List<FolderEntity>> getFolderByLock(int isLocked) {
        return mNotesDatabase.getFolderDao().getFolderByLock(isLocked);
    }

    public LiveData<List<FolderEntity>> getFolderByColor(String color) {
        return mNotesDatabase.getFolderDao().getFolderByColor(color);
    }
}
