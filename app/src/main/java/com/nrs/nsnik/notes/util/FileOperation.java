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

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.nrs.nsnik.notes.model.dagger.qualifiers.ApplicationQualifier;
import com.nrs.nsnik.notes.model.data.TableNames;
import com.nrs.nsnik.notes.model.data.TableNames.table1;
import com.nrs.nsnik.notes.model.objects.NoteObject;
import com.nrs.nsnik.notes.view.MyApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@Singleton
public class FileOperation {

    private final File mRootFolder;
    private final Context mContext;
    private final DatabaseOperations mDatabaseOperations;

    @Inject
    public FileOperation(@ApplicationQualifier Context context, DatabaseOperations databaseOperations) {
        mContext = context;
        mDatabaseOperations = databaseOperations;
        mRootFolder = ((MyApplication) mContext.getApplicationContext()).getRootFolder();
    }

    /**
     * @param fileName     the name by which the file wil be saved
     *                     each new notes has a different file name
     * @param noteObject   the object that is written to file
     */
    public void saveNote(@NonNull String fileName, NoteObject noteObject, int isPinned, int isLocked, String time, String color) {
        Completable completable = Completable.fromCallable(() -> {
            FileOutputStream fos = null;
            ObjectOutputStream oos = null;
            File f = new File(mRootFolder, fileName);
            try {
                fos = new FileOutputStream(f);
                oos = new ObjectOutputStream(fos);
                oos.writeObject(noteObject);
                oos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    fos.close();
                }
                if (oos != null) {
                    oos.close();
                }
            }
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
                mDatabaseOperations.insertNote(fileName, noteObject, isPinned, isLocked, time, color);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Timber.d(e.getMessage());
            }
        });

    }

    /**
     * @param fileName         the name by which the file wil be saved
     * @param noteObject       the object that will be written to file
     * @param uri              the uri ton which the update operation will be performed
     */
    public void updateNote(@NonNull String fileName, @NonNull NoteObject noteObject, Uri uri, int isPinned, int isLocked, String time, String color) {
        Completable completable = Completable.fromCallable(() -> {
            FileOutputStream fos = null;
            ObjectOutputStream oos = null;
            File f = new File(mRootFolder, fileName);
            try {
                fos = new FileOutputStream(f);
                oos = new ObjectOutputStream(fos);
                oos.writeObject(noteObject);
                oos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    fos.close();
                }
                if (oos != null) {
                    oos.close();
                }
            }
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
                mDatabaseOperations.updateNote(noteObject.title(), uri, isPinned, isLocked, time, color);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Timber.d(e.getMessage());
            }
        });
    }

    public void deleteFileList(@NonNull List<String> fileList) {
        Completable completable = Completable.fromCallable(() -> {
            for (String s : fileList) {
                deleteFile(s);
            }
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
                Timber.d("All files deleted");
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Timber.d(e.getMessage());
            }
        });
    }

    private void deleteFile(@NonNull String fileName) {
        File f = new File(mRootFolder, fileName);
        boolean isDeleted = false;
        if (f.exists()) {
            isDeleted = f.delete();
        }
        if (!isDeleted) {
            Timber.d("Error while deleting " + f.toString());
        }
    }

    /**
     * @param fileName     the name of image file
     * @param image        the image
     */
    public void saveImage(@NonNull String fileName, @NonNull Bitmap image) {
        Completable completable = Completable.fromCallable(() -> {
            File f = new File(mRootFolder, fileName);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) fos.close();
            }
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
                Timber.d("Image Saved");
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Timber.d(e.getMessage());
            }
        });
    }

    /**
     * @param fileName     the name of that file that contains a note object
     */
    @Nullable
    public NoteObject readFile(@NonNull String fileName) throws IOException {
        File f = new File(mRootFolder, fileName);
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        NoteObject object = null;
        try {
            fis = new FileInputStream(f);
            ois = new ObjectInputStream(fis);
            object = (NoteObject) ois.readObject();
        } catch (@NonNull IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (ois != null) {
                ois.close();
            }
        }
        return object;
    }

    /**
     * @param uri the uri that will be used to get all the images and the file Name of a note and then be deleted
     */
    private void deleteFileBack(@NonNull Uri uri) throws IOException {
        Cursor c = mContext.getContentResolver().query(uri, null, null, null, null);
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        boolean isDeleted;
        try {
            while (c != null && c.moveToNext()) {
                File f = new File(mRootFolder, c.getString(c.getColumnIndex(table1.mFileName)));
                fis = new FileInputStream(f);
                ois = new ObjectInputStream(fis);
                NoteObject obj = (NoteObject) ois.readObject();
                for (int i = 0; i < obj.imagesList().size(); i++) {
                    deleteFile(obj.imagesList().get(i));
                }
                for (int i = 0; i < obj.audioList().size(); i++) {
                    deleteFile(obj.audioList().get(i));
                }
                isDeleted = f.delete();
                if (f.exists() && !isDeleted) {
                    Timber.d("Error while deleting " + f.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (ois != null) {
                ois.close();
            }
            if (c != null) {
                c.close();
            }
        }
    }

    /**
     * @param uri  the uri of the bot that is to be deleted
     *
     *             first the resources related to note are deleted by calling the function
     *             function deleteFileBack(Uri) then the note data is deleted from database
     */
    public void deleteNote(@NonNull Uri uri) {
        Completable completable = Completable.fromCallable(() -> {
            deleteFileBack(uri);
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
                mDatabaseOperations.deleteNote(uri);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Timber.d(e.getMessage());
            }
        });
    }

    /**
     * @param uri      the uri of the folder that is to be deleted
     * @param folderName   the name of the folder that is to deleted
     *
     *                     folder name is to delete all the notes and their
     *                     resources that arw within that folder, first the
     *                     resources related to all notes in folder is deleted then
     *                     all the notes in the database that are stored in that
     *                     folder and finally the folder
     */
    public void deleteFolder(Uri uri, String folderName) {
        Completable completable = Completable.fromCallable(() -> {
            deleteFileBack(Uri.withAppendedPath(TableNames.mContentUri, "parentFolderName/" + folderName));
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
                mDatabaseOperations.deleteFolder(uri, folderName);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Timber.d(e.getMessage());
            }
        });
    }

    @NonNull
    public String makeName(@NonNull FILE_TYPES type) {
        Calendar c = Calendar.getInstance();
        switch (type) {
            case TEXT:
                return c.getTimeInMillis() + ".txt";
            case IMAGE:
                return c.getTimeInMillis() + ".jpg";
            case AUDIO:
                return c.getTimeInMillis() + ".3gp";
            default:
                throw new IllegalArgumentException("Invalid type " + type.toString());
        }
    }

    @NonNull
    public String formatDate(String rawDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(rawDate));

        Calendar calendarPresent = Calendar.getInstance();

        long noteDate = Long.parseLong(rawDate);
        long currentTime = calendarPresent.getTimeInMillis();

        long nowMinutes = TimeUnit.MILLISECONDS.toMinutes(currentTime);
        long secondMinutes = TimeUnit.MILLISECONDS.toMinutes(noteDate);

        long nowHour = TimeUnit.MILLISECONDS.toHours(currentTime);
        long secondHour = TimeUnit.MILLISECONDS.toHours(noteDate);

        long nowDays = TimeUnit.MILLISECONDS.toDays(currentTime);
        long secondDays = TimeUnit.MILLISECONDS.toDays(noteDate);

        if (nowMinutes - secondMinutes < 60) {
            return nowMinutes - secondMinutes + " min ago";
        } else if (nowHour - secondHour < 24) {
            return nowHour - secondHour + " hrs ago";
        } else if (nowDays - secondDays <= 2) {
            return nowDays - secondDays + " days ago";
        } else {
            return calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH);
        }
    }

    public enum FILE_TYPES {TEXT, IMAGE, AUDIO}

}
