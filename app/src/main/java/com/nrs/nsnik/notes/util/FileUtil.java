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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import com.nrs.nsnik.notes.dagger.qualifiers.RootFolder;
import com.nrs.nsnik.notes.dagger.scopes.ApplicationScope;
import com.nrs.nsnik.notes.data.NoteEntity;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import javax.inject.Inject;

import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;

@ApplicationScope
public class FileUtil {

    private final File mRootFolder;

    @Inject
    FileUtil(@NotNull @ApplicationScope @RootFolder File rootFolder) {
        mRootFolder = rootFolder;
    }

    public File getRootFolder() {
        return mRootFolder;
    }

    void saveNote(NoteEntity noteEntity, String fileName) throws IOException {
        File file = new File(mRootFolder, fileName);
        BufferedSink sink = Okio.buffer(Okio.sink(file));
        sink.write(serialize(noteEntity));
        sink.close();
    }

    public void saveImage(Bitmap image, String fileName) throws IOException {
        File file = new File(mRootFolder, fileName);
        BufferedSink sink = Okio.buffer(Okio.sink(file));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        sink.write(stream.toByteArray());
        sink.close();
    }

    public Bitmap getImage(String fileName) throws IOException {
        File file = new File(mRootFolder, fileName);
        BufferedSource source = Okio.buffer(Okio.source(file));
        byte[] byteArray = source.readByteArray();
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    private void deleteFiles(String fileName) {

    }

    public void deleteNote(List<String> fileName) {

    }

    public NoteEntity getNote(String fileName) throws Exception {
        File file = new File(mRootFolder, fileName);
        BufferedSource source = Okio.buffer(Okio.source(file));
        NoteEntity entity = deSerialize(source.readByteString());
        source.close();
        return entity;
    }

    @NonNull
    private ByteString serialize(NoteEntity noteEntity) throws IOException {
        Buffer buffer = new Buffer();
        ObjectOutputStream stream = new ObjectOutputStream(buffer.outputStream());
        stream.writeObject(noteEntity);
        stream.flush();
        return buffer.readByteString();
    }

    private NoteEntity deSerialize(ByteString data) throws Exception {
        Buffer buffer = new Buffer().write(data);
        ObjectInputStream stream = new ObjectInputStream(buffer.inputStream());
        return (NoteEntity) stream.readObject();
    }
}
