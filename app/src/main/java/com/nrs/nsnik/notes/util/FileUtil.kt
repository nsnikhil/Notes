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

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.nrs.nsnik.notes.dagger.qualifiers.RootFolder
import com.nrs.nsnik.notes.dagger.scopes.ApplicationScope
import com.nrs.nsnik.notes.data.NoteEntity
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.internal.operators.completable.CompletableFromCallable
import io.reactivex.schedulers.Schedulers
import okio.Buffer
import okio.ByteString
import okio.Okio
import timber.log.Timber
import java.io.*
import java.util.concurrent.Callable
import javax.inject.Inject

@ApplicationScope
class FileUtil @Inject
internal constructor(@param:ApplicationScope @param:RootFolder val rootFolder: File) {

    @Throws(IOException::class)
    internal fun saveNote(noteEntity: NoteEntity, fileName: String) {
        val completable: Completable = CompletableFromCallable(Callable {
            val file = File(rootFolder, fileName)
            val sink = Okio.buffer(Okio.sink(file))
            sink.write(serialize(noteEntity))
            sink.close()
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        completable.subscribe(object : CompletableObserver {
            override fun onComplete() {
                Timber.d("Note Saved")
            }

            override fun onSubscribe(d: Disposable) {
            }

            override fun onError(e: Throwable) {
                Timber.d(e)
            }

        })
    }

    @Throws(IOException::class)
    fun saveImage(image: Bitmap, fileName: String) {
        val completable: Completable = CompletableFromCallable(Callable {
            val file = File(rootFolder, fileName)
            val sink = Okio.buffer(Okio.sink(file))
            val stream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.PNG, 100, stream)
            sink.write(stream.toByteArray())
            sink.close()
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        completable.subscribe(object : CompletableObserver {
            override fun onComplete() {
                Timber.d("Image Saved")
            }

            override fun onSubscribe(d: Disposable) {
            }

            override fun onError(e: Throwable) {
                Timber.d(e)
            }

        })
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
