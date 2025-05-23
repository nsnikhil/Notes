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
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.RequestManager
import com.jakewharton.rxbinding2.view.RxView
import com.nrs.nsnik.notes.MyApplication
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.data.FolderEntity
import com.nrs.nsnik.notes.data.NoteEntity
import com.nrs.nsnik.notes.util.AppUtil
import com.nrs.nsnik.notes.util.FileUtil
import com.nrs.nsnik.notes.view.MainActivity
import com.nrs.nsnik.notes.view.fragments.ListFragment
import com.nrs.nsnik.notes.view.listeners.ItemTouchListener
import com.nrs.nsnik.notes.databinding.SingleFolderLayoutBinding // Added ViewBinding import
import com.nrs.nsnik.notes.databinding.SingleListHeaderBinding // Added ViewBinding import
import com.nrs.nsnik.notes.databinding.SingleNoteLayoutBinding // Added ViewBinding import
import com.nrs.nsnik.notes.view.listeners.ListHeaderClickListener
import com.nrs.nsnik.notes.view.listeners.NoteItemClickListener
import io.reactivex.disposables.CompositeDisposable
// import kotlinx.android.synthetic.main.item_indicator.view.* // Removed synthetic import
// import kotlinx.android.synthetic.main.single_folder_layout.view.* // Removed synthetic import
// import kotlinx.android.synthetic.main.single_list_header.view.* // Removed synthetic import
// import kotlinx.android.synthetic.main.single_note_layout.view.* // Removed synthetic import
import timber.log.Timber
import java.io.File


/**
 * This adapter takes in a uri queries the uri and
 * registers for change in uri i.e. if any items in
 * that uri changes the adapter is notified and then
 * adapters clear the old list re draws the entire list
 * with the new data set
 */

class NotesAdapter(private val mContext: Context,
                   private var mNotesList: List<NoteEntity>,
                   private var mFolderList: List<FolderEntity>,
                   private val mNoteItemClickListener: NoteItemClickListener,
                   private val listHeaderClickListener: ListHeaderClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ItemTouchListener {

    companion object {
        private const val NOTES = 0
        private const val FOLDER = 1
        private const val HEADER = 2
    }

    private val mLayoutInflater: LayoutInflater
    private val mRequestManager: RequestManager?
    private val mFileUtil: FileUtil?
    private val mCompositeDisposable: CompositeDisposable
    private val mRootFolder: File

    init {

        mRequestManager = (mContext.applicationContext as MyApplication).requestManager
        mFileUtil = (mContext.applicationContext as MyApplication).fileUtil
        mRootFolder = mFileUtil.rootFolder

        mLayoutInflater = LayoutInflater.from(mContext)
        mCompositeDisposable = CompositeDisposable()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            FOLDER -> {
                val binding = SingleFolderLayoutBinding.inflate(mLayoutInflater, parent, false)
                FolderViewHolder(binding)
            }
            NOTES -> {
                val binding = SingleNoteLayoutBinding.inflate(mLayoutInflater, parent, false)
                NoteViewHolder(binding)
            }
            HEADER -> {
                val binding = SingleListHeaderBinding.inflate(mLayoutInflater, parent, false)
                HeaderViewHolder(binding)
            }
            else -> { // Should ideally not happen if getItemViewType is correct
                val binding = SingleNoteLayoutBinding.inflate(mLayoutInflater, parent, false)
                NoteViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var pos = mFolderList.size + 2
        val tempPOS = position - 1
        when (holder.itemViewType) {
            FOLDER -> bindFolderData(holder, tempPOS)
            NOTES -> {
                pos = position - pos
                bindNotesData(holder, pos)
            }
            HEADER -> {
                try {
                    val layoutParams = holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
                    layoutParams.isFullSpan = true
                } catch (e: ClassCastException) {
                    e.printStackTrace()
                }
                bindHeaderData(holder, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return mNotesList.size + mFolderList.size + 2
    }

    private fun bindHeaderData(holder: RecyclerView.ViewHolder, position: Int) {
        val headerViewHolder = holder as HeaderViewHolder
        if (position == 0) {
            headerViewHolder.binding.itemHeaderContainer.visibility = if (mFolderList.isNotEmpty()) View.VISIBLE else View.GONE
            if (headerViewHolder.binding.itemHeader.isVisible) headerViewHolder.binding.itemHeader.text = mContext.resources.getString(R.string.headingFolder)
        } else {
            headerViewHolder.binding.itemHeaderContainer.visibility = if (mNotesList.isNotEmpty()) View.VISIBLE else View.GONE
            if (headerViewHolder.binding.itemHeader.isVisible) headerViewHolder.binding.itemHeader.text = mContext.resources.getString(R.string.headingNotes)
        }
    }

    private fun bindFolderData(holder: RecyclerView.ViewHolder, position: Int) {
        val folderViewHolder = holder as FolderViewHolder
        val folderEntity: FolderEntity = mFolderList[position]
        folderViewHolder.binding.singleFolderName.text = folderEntity.folderName
        folderViewHolder.binding.singleFolderName.compoundDrawableTintList = AppUtil.stateList(folderEntity.color)
        // Assuming itemIndicatorPin and itemIndicatorLock are part of SingleFolderLayoutBinding
        // If the include tag for item_indicator.xml has an ID, e.g., android:id="@+id/indicator",
        // then it would be folderViewHolder.binding.indicator.itemIndicatorPin
        changeVisibility(folderViewHolder.binding.itemIndicatorPin, folderEntity.pinned)
        changeVisibility(folderViewHolder.binding.itemIndicatorLock, folderEntity.locked)
    }

    private fun bindNotesData(holder: RecyclerView.ViewHolder, position: Int) {
        val noteViewHolder = holder as NoteViewHolder

        mFileUtil?.getLiveNote(mNotesList[position].fileName!!)?.observe(mContext as MainActivity, Observer {

            if (it.title != null && !it.title!!.isEmpty()) {
                noteViewHolder.binding.singleNoteTitle.visibility = View.VISIBLE
                noteViewHolder.binding.singleNoteTitle.text = it.title
                noteViewHolder.binding.singleNoteTitle.setTextColor(Color.parseColor(it.color))
            } else {
                noteViewHolder.binding.singleNoteTitle.visibility = View.GONE
            }

            if (it.locked == 0) {
                if (it.noteContent != null && !it.noteContent!!.isEmpty()) {
                    noteViewHolder.binding.singleNoteContent.visibility = View.VISIBLE
                    noteViewHolder.binding.singleNoteContent.text = it.noteContent
                    noteViewHolder.binding.singleNoteContent.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                    noteViewHolder.binding.singleNoteContent.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                    noteViewHolder.binding.singleNoteContent.compoundDrawablePadding = 0
                    //noteViewHolder.binding.singleNoteContent.setTextColor(ContextCompat.getColor(mContext, R.color.contentColor))
                } else {
                    noteViewHolder.binding.singleNoteContent.visibility = View.GONE
                }

                if (it.imageList != null && it.imageList!!.isNotEmpty()) {
                    noteViewHolder.binding.singleNoteImage.visibility = View.VISIBLE
                    mRequestManager?.load(File(mRootFolder, it.imageList!![0]))?.into(noteViewHolder.binding.singleNoteImage)
                } else {
                    noteViewHolder.binding.singleNoteImage.visibility = View.GONE
                }

            } else {
                noteViewHolder.binding.singleNoteContent.text = mContext.resources?.getString(R.string.hidden)
                noteViewHolder.binding.singleNoteImage.visibility = View.GONE
                noteViewHolder.binding.singleNoteContent.setTextColor(ContextCompat.getColor(mContext, R.color.grey))
                noteViewHolder.binding.singleNoteContent.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lock_48px, 0, 0, 0)
                noteViewHolder.binding.singleNoteContent.compoundDrawablePadding = 4
            }
            // Assuming itemIndicatorPin and itemIndicatorLock are part of SingleNoteLayoutBinding
            changeVisibility(noteViewHolder.binding.itemIndicatorPin, it.pinned)
        })
    }

    private fun changeVisibility(imageView: ImageView, value: Int) {
        imageView.visibility = if (value == 1) View.VISIBLE else View.GONE
    }

    //TODO CHANGE NOTIFY-DATA-SET-CHANGE WITH DIFF UTIL
    fun updateNotesList(noteList: List<NoteEntity>) {
        mNotesList = noteList
        notifyDataSetChanged()
    }

    //TODO CHANGE NOTIFY-DATA-SET-CHANGE WITH DIFF UTIL
    fun updateFolderList(folderList: List<FolderEntity>) {
        mFolderList = folderList
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 || position == mFolderList.size + 1) HEADER
        else if (position > 0 && position <= mFolderList.size) FOLDER
        else if (position > mFolderList.size + 1 && position < mNotesList.size) NOTES
        else super.getItemViewType(position)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) {
        if (viewHolder.itemViewType == target.itemViewType) {
            notifyItemMoved(fromPosition, toPosition)
        }
    }

    //TODO
    override fun onItemMoved(fromPosition: Int, toPosition: Int, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        var startPos = -100
        if (viewHolder.itemViewType == NOTES) {
            startPos = mFolderList.size + 2
        }
        if (viewHolder.itemViewType == FOLDER) {
            startPos = 1
        }
        val fromPos = fromPosition - startPos
        val toPos = toPosition - startPos
    }

    //TODO
    override fun onItemDismiss(position: Int) {
        //notifyItemRemoved(position);
    }

    private fun cleanUp() {
        mCompositeDisposable.dispose()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        cleanUp()
        super.onDetachedFromRecyclerView(recyclerView)
    }

    internal inner class NoteViewHolder(val binding: SingleNoteLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        // Views accessed via binding.singleNoteTitle, binding.singleNoteContent, etc.
        // Assuming itemIndicatorPin and itemIndicatorLock are part of SingleNoteLayoutBinding
        init {
            mCompositeDisposable.addAll(
                    RxView.clicks(binding.root).subscribe({
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            mNoteItemClickListener.onClick(adapterPosition, itemViewType)
                        }
                    }, { throwable -> Timber.d(throwable.message) }),

                    RxView.longClicks(binding.root).subscribe({
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            mNoteItemClickListener.onLongClick(adapterPosition, itemViewType, binding.root)
                        }
                    }, { throwable -> Timber.d(throwable.message) })
            )
        }
    }


    internal inner class FolderViewHolder(val binding: SingleFolderLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        // Views accessed via binding.singleFolderName, etc.
        // Assuming itemIndicatorPin and itemIndicatorLock are part of SingleFolderLayoutBinding
        init {
            mCompositeDisposable.addAll(
                    RxView.clicks(binding.root).subscribe {
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            mNoteItemClickListener.onClick(adapterPosition, itemViewType)
                        }
                    },
                    RxView.longClicks(binding.root).subscribe({
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            mNoteItemClickListener.onLongClick(adapterPosition, itemViewType, binding.root)
                        }
                    }, { throwable -> Timber.d(throwable.message) })
            )
        }
    }

    internal inner class HeaderViewHolder(val binding: SingleListHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        // Views accessed via binding.itemHeader, binding.itemHeaderMore, etc.
        init {
            mCompositeDisposable.addAll(
                    RxView.clicks(binding.itemHeaderMore).subscribe {
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            listHeaderClickListener.headerClick(
                                    if (binding.itemHeader.text.toString() == mContext.resources.getString(R.string.headingFolder)) ListFragment.ItemType.FOLDER
                                    else ListFragment.ItemType.NOTES, binding.itemHeaderMore)
                        }
                    }
            )
        }
    }
}