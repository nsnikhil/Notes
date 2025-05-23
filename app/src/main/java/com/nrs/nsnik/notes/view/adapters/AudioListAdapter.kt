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

package com.nrs.nsnik.notes.view.adapters

import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.nrs.nsnik.notes.MyApplication
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.dagger.components.DaggerMediaComponent
import com.nrs.nsnik.notes.databinding.SingleAudioItemBinding // Added ViewBinding import
import com.nrs.nsnik.notes.view.adapters.diffUtil.StringDiffUtil
import com.nrs.nsnik.notes.view.listeners.AdapterType
import com.nrs.nsnik.notes.view.listeners.OnItemRemoveListener
import io.reactivex.disposables.CompositeDisposable
// import kotlinx.android.synthetic.main.single_audio_item.view.* // Removed synthetic import
import java.io.File
import java.io.IOException


class AudioListAdapter(private val onItemRemoveListener: OnItemRemoveListener) : ListAdapter<String, AudioListAdapter.MyViewHolder>(StringDiffUtil()), Runnable {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var context: Context
    private lateinit var mMediaPlayer: MediaPlayer
    private var mAdapterSeekBar: SeekBar? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        context = parent.context
        val binding = SingleAudioItemBinding.inflate(LayoutInflater.from(parent.context), parent, false) // Inflate with ViewBinding
        return MyViewHolder(binding) // Pass binding to ViewHolder
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // holder.bind(getItem(position)) // Example of how you might bind data
    }

    private fun playAudio(position: Int, play: ImageView) {
        mMediaPlayer = DaggerMediaComponent.builder().build().mediaPlayer
        try {
            mMediaPlayer.setDataSource(File((context.applicationContext as MyApplication).rootFolder, getItem(position)).toString())
            mMediaPlayer.prepareAsync()

        } catch (e: IOException) {
            e.printStackTrace()
        }

        mMediaPlayer.setOnPreparedListener {
            if (!mMediaPlayer.isPlaying) {
                mMediaPlayer.start()
                if (mAdapterSeekBar != null) {
                    mAdapterSeekBar!!.max = mMediaPlayer.duration
                }
                Thread(this).start()
            }
        }
        mMediaPlayer.setOnCompletionListener {
            mMediaPlayer.reset()
            if (mAdapterSeekBar != null) {
                mAdapterSeekBar!!.setProgress(0, true)
            }
            play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play_arrow_24px))
        }

    }

    override fun run() {
        var currentPosition: Int
        currentPosition = mMediaPlayer.currentPosition
        val total = mMediaPlayer.duration
        while (currentPosition < total) {
            try {
                Thread.sleep(1000)
                currentPosition = mMediaPlayer.currentPosition
            } catch (e: Exception) {
                return
            }
            if (mAdapterSeekBar != null) {
                mAdapterSeekBar!!.setProgress(currentPosition, true)
            }
        }
    }

    private fun cleanUp() {
        compositeDisposable.clear()
        compositeDisposable.dispose()
        if (mMediaPlayer != null) {
            mMediaPlayer.reset()
            mMediaPlayer.release()
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        cleanUp()
    }

    inner class MyViewHolder internal constructor(private val binding: SingleAudioItemBinding) : RecyclerView.ViewHolder(binding.root) { // ViewHolder accepts binding

        // Removed direct view references, will use binding.seekBar, binding.play, binding.remove

        init {
            compositeDisposable.addAll(
                    RxView.clicks(binding.audioPlay).subscribe { // Use binding.audioPlay
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            mAdapterSeekBar = binding.audioProgressBar // Use binding.audioProgressBar
                            binding.audioPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_stop_black_24px)) // Use binding.audioPlay
                            playAudio(adapterPosition, binding.audioPlay) // Use binding.audioPlay
                        }
                    },
                    RxView.clicks(binding.audioRemove).subscribe { // Use binding.audioRemove
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            onItemRemoveListener.onItemRemoved(adapterPosition, AdapterType.AUDIO_ADAPTER)
                        }
                    }
            )
        }
    }
}
