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

import android.app.Activity
import android.media.MediaRecorder
import androidx.appcompat.app.AlertDialog
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.view.fragments.NewNoteFragment
import com.nrs.nsnik.notes.view.listeners.AudioRecordListener
import java.io.File
import java.io.IOException

class AudioUtil {

    companion object {

        fun recordAudio(activity: Activity, rootFolder: File, audioRecordListener: AudioRecordListener) {

            val audioFileName = AppUtil.makeName(NewNoteFragment.FILE_TYPES.AUDIO)
            val audioFileAbsolutePath = File(rootFolder, audioFileName)
            //Initializing the media recorder
            val recorder = MediaRecorder()
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            recorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
            recorder.setOutputFile(audioFileAbsolutePath.absolutePath)

            //Creating the audio recorder dialog
            val record = AlertDialog.Builder(activity)
            record.setMessage(activity.resources.getString(R.string.audioRecording))
            record.setNeutralButton(activity.resources.getString(R.string.audioStopRecording)) { dialogInterface, i ->
                recorder.stop()
                recorder.reset()
                recorder.release()
                audioRecordListener.audioRecorded(audioFileName)
                //addAudioToList(audioFileName)
            }
            record.setPositiveButton(activity.resources?.getString(R.string.cancel)) { dialogInterface, i ->
                recorder.stop()
                recorder.reset()
                recorder.release()
                if (audioFileAbsolutePath.exists()) audioFileAbsolutePath.delete()
                record.create().dismiss()
            }
            record.setCancelable(false)
            record.create().show()

            try {
                recorder.prepare()
                recorder.start()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

    }

}