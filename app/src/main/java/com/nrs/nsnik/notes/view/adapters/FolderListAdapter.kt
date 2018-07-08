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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.data.FolderEntity
import com.nrs.nsnik.notes.util.AppUtil
import com.nrs.nsnik.notes.view.adapters.diffUtil.FolderDiffUtil
import com.nrs.nsnik.notes.view.listeners.ItemPositionClickListener
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.item_indicator.view.*
import kotlinx.android.synthetic.main.single_folder_layout.view.*

class FolderListAdapter(val itemPositionClickListener: ItemPositionClickListener) : ListAdapter<FolderEntity, FolderListAdapter.MyViewHolder>(FolderDiffUtil()) {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.single_folder_layout, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val folderEntity = getItem(position)
        holder.folderName.text = folderEntity.folderName
        holder.folderName.compoundDrawableTintList = AppUtil.stateList(folderEntity.color)
        holder.container.elevation = 0f
        holder.container.minimumWidth = 300
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val folderName: TextView = itemView.singleFolderName
        val isPinned: ImageView = itemView.itemIndicatorPin
        val isLocked: ImageView = itemView.itemIndicatorLock
        val container: CardView = itemView.singleFolderContainer

        init {
            compositeDisposable.addAll(
                    RxView.clicks(container).subscribe { itemPositionClickListener.itemClicked(adapterPosition) }
            )
        }
    }

    private fun cleanUp() {
        compositeDisposable.clear()
        compositeDisposable.dispose()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        cleanUp()
    }

}