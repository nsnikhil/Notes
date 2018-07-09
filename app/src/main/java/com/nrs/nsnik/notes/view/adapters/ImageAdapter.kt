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
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.jakewharton.rxbinding2.view.RxView
import com.nrs.nsnik.notes.MyApplication
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.view.adapters.diffUtil.StringDiffUtil
import com.nrs.nsnik.notes.view.listeners.AdapterType
import com.nrs.nsnik.notes.view.listeners.OnItemRemoveListener
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.single_image_view.view.*
import timber.log.Timber
import java.io.File

/**
 * IMAGE ADAPTER TAKES IN LIST OF STRING WHERE EACH
 * STRING REPRESENTS A FILE NAME, IT USES THIS FILENAME TO
 * CREATE A NEW BITMAP FILE AND DISPLAY EACH IMAGE IN ITS
 * RECYCLER VIEW, IT ALSO TAKES A onItemRemoveListener INTERFACE WHICH
 * IS RESPONSIBLE FOR LETTING THE RECYCLER VIEWS ACTIVITY/FRAGMENT
 * KNOW ABOUT THE CHANGE IN SIZE OF THE ADAPTER
 */

class ImageAdapter(private val mFullScreen: Boolean, private val onItemRemoveListener: OnItemRemoveListener) : ListAdapter<String, ImageAdapter.MyViewHolder>(StringDiffUtil()) {

    private lateinit var glideRequestManager: RequestManager
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        context = parent.context
        glideRequestManager = (context.applicationContext as MyApplication).requestManager
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.single_image_view, parent, false))
    }

    override fun onBindViewHolder(holder: ImageAdapter.MyViewHolder, position: Int) {
        glideRequestManager
                .load(File((context.applicationContext as MyApplication).rootFolder, getItem(position)))
                .apply(RequestOptions().placeholder(R.color.grey).error(R.color.grey))
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                        Timber.d(e?.message)
                        return false
                    }

                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        holder.image.visibility = View.VISIBLE
                        holder.progress.visibility = View.GONE
                        return false
                    }
                })
                .into(holder.image)

    }

    private fun cleanUp() {
        compositeDisposable.clear()
        compositeDisposable.dispose()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        cleanUp()
    }

    inner class MyViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val image: ImageView = itemView.singleImage

        private val remove: ImageView = itemView.singleImageCancel

        val progress: ProgressBar = itemView.singleImageProgress

        init {

            /*
            if the adapter is set for a full screen
            activity/fragment then remove the option
            remove the image or click on image
             */

            if (mFullScreen) {
                remove.visibility = View.GONE
                image.scaleType = ImageView.ScaleType.FIT_CENTER
            } else {
                compositeDisposable.addAll(
                        RxView.clicks(remove).subscribe {
                            if (adapterPosition != RecyclerView.NO_POSITION) {
                                onItemRemoveListener.onItemRemoved(adapterPosition, AdapterType.IMAGE_ADAPTER)
                            }
                        },
                        RxView.clicks(image).subscribe {
                            if (adapterPosition != RecyclerView.NO_POSITION) {
                                //EventBus.getDefault().post(FullScreenEvent(adapterPosition))
                            }
                        }
                )
            }

        }
    }
}
