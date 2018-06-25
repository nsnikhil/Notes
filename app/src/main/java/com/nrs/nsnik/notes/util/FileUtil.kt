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

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.nrs.nsnik.notes.dagger.qualifiers.RootFolder
import com.nrs.nsnik.notes.dagger.scopes.ApplicationScope
import com.nrs.nsnik.notes.data.NoteEntity
import okio.Buffer
import okio.ByteString
import okio.Okio
import java.io.*
import javax.inject.Inject

@ApplicationScope
class FileUtil @Inject
internal constructor(@param:ApplicationScope @param:RootFolder val rootFolder: File) {

    @Throws(IOException::class)
    internal fun saveNote(noteEntity: NoteEntity, fileName: String) {
        val file = File(rootFolder, fileName)
        val sink = Okio.buffer(Okio.sink(file))
        sink.write(serialize(noteEntity))
        sink.close()
    }

    @Throws(IOException::class)
    fun saveImage(image: Bitmap, fileName: String) {
        val file = File(rootFolder, fileName)
        val sink = Okio.buffer(Okio.sink(file))
        val stream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, stream)
        sink.write(stream.toByteArray())
        sink.close()
    }

    @Throws(IOException::class)
    fun getImage(fileName: String): Bitmap {
        val file = File(rootFolder, fileName)
        val source = Okio.buffer(Okio.source(file))
        val byteArray = source.readByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    private fun deleteFiles(fileName: String) {

    }

    fun deleteNote(fileName: List<String>) {

    }

    @Throws(Exception::class)
    fun getNote(fileName: String): NoteEntity {
        val file = File(rootFolder, fileName)
        val source = Okio.buffer(Okio.source(file))
        val entity = deSerialize(source.readByteString())
        source.close()
        return entity
    }

    @Throws(IOException::class)
    private fun serialize(noteEntity: NoteEntity): ByteString {
        val buffer = Buffer()
        val stream = ObjectOutputStream(buffer.outputStream())
        stream.writeObject(noteEntity)
        stream.flush()
        return buffer.readByteString()
    }

    @Throws(Exception::class)
    private fun deSerialize(data: ByteString): NoteEntity {
        val buffer = Buffer().write(data)
        val stream = ObjectInputStream(buffer.inputStream())
        return stream.readObject() as NoteEntity
    }
}
