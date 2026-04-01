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
import androidx.lifecycle.MutableLiveData
import com.nrs.nsnik.notes.dagger.qualifiers.RootFolder
import com.nrs.nsnik.notes.dagger.scopes.ApplicationScope
import com.nrs.nsnik.notes.data.NoteEntity
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableObserver
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.serialization.json.Json
import okio.ByteString
import okio.ByteString.Companion.toByteString
import okio.buffer
import okio.sink
import okio.source
import timber.log.Timber
import java.io.*
import javax.inject.Inject

@ApplicationScope
class FileUtil @Inject
internal constructor(@param:ApplicationScope @param:RootFolder val rootFolder: File) {

    @Throws(IOException::class)
    internal fun saveNote(noteEntity: NoteEntity, fileName: String) {
        val completable: Completable = Completable.fromCallable {
            val file = File(rootFolder, fileName)
            val sink = file.sink().buffer()
            sink.write(serialize(noteEntity))
            sink.close()
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
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
        val completable: Completable = Completable.fromCallable {
            val file = File(rootFolder, fileName)
            val sink = file.sink().buffer()
            val stream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.PNG, 100, stream)
            sink.write(stream.toByteArray())
            sink.close()
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
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
        val source = file.source().buffer()
        val byteArray = source.readByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    fun deleteNote(fileName: String) {
        val single: Single<Boolean> = Single.fromCallable {
            val file = File(rootFolder, fileName)
            if (file.exists()) file.delete() else false
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        single.subscribe(object : SingleObserver<Boolean> {
            override fun onSuccess(t: Boolean) {
                Timber.d(t.toString())
            }

            override fun onSubscribe(d: Disposable) {

            }

            override fun onError(e: Throwable) {
                Timber.d(e)
            }
        })
    }

    fun deleteNoteImages(fileName: List<String>) {
        val single: Single<Boolean> = Single.fromCallable {
            var allDeleted = false
            fileName.forEach {
                val file = File(rootFolder, it)
                if (file.exists()) allDeleted = file.delete()
            }
            allDeleted
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        single.subscribe(object : SingleObserver<Boolean> {
            override fun onSuccess(t: Boolean) {
                Timber.d(t.toString())
            }

            override fun onSubscribe(d: Disposable) {

            }

            override fun onError(e: Throwable) {
                Timber.d(e)
            }
        })
    }

    fun deleteNoteAudio(fileName: List<String>) {
        val single: Single<Boolean> = Single.fromCallable {
            var allDeleted = false
            fileName.forEach {
                val file = File(rootFolder, it)
                if (file.exists()) allDeleted = file.delete()
            }
            allDeleted
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        single.subscribe(object : SingleObserver<Boolean> {
            override fun onSuccess(t: Boolean) {
                Timber.d(t.toString())
            }

            override fun onSubscribe(d: Disposable) {

            }

            override fun onError(e: Throwable) {
                Timber.d(e)
            }
        })
    }

    fun deleteNoteResources(noteEntity: NoteEntity) {
        if (noteEntity.imageList != null) deleteNoteImages(noteEntity.imageList!!)
        if (noteEntity.audioList != null) deleteNoteAudio(noteEntity.audioList!!)
        if (noteEntity.fileName != null) deleteNote(noteEntity.fileName!!)
    }

    @Throws(Exception::class)
    fun getLiveNote(fileName: String): MutableLiveData<NoteEntity> {
        val liveNote = MutableLiveData<NoteEntity>()

        val single: Single<NoteEntity> = Single.fromCallable {
            val file = File(rootFolder, fileName)
            val source = file.source().buffer()
            val entity = deSerialize(source.readByteString())
            source.close()
            entity
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        single.subscribe(object : SingleObserver<NoteEntity> {
            override fun onSuccess(t: NoteEntity) {
                liveNote.value = t
            }

            override fun onSubscribe(d: Disposable) {

            }

            override fun onError(e: Throwable) {
                Timber.d(e)
            }

        })
        return liveNote
    }

    @Throws(IOException::class)
    private fun serialize(noteEntity: NoteEntity): ByteString {
        val jsonString = Json.encodeToString(NoteEntity.serializer(), noteEntity)
        return jsonString.encodeToByteArray().toByteString()
    }

    @Throws(Exception::class)
    private fun deSerialize(data: ByteString): NoteEntity {
        val jsonString = data.utf8()
        return Json.decodeFromString(NoteEntity.serializer(), jsonString)
    }
}
