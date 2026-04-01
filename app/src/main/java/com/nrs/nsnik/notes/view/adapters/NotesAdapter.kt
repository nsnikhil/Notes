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
import com.jakewharton.rxbinding4.view.clicks
import com.jakewharton.rxbinding4.view.longClicks
import com.nrs.nsnik.notes.MyApplication
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.data.FolderEntity
import com.nrs.nsnik.notes.data.NoteEntity
import com.nrs.nsnik.notes.databinding.SingleFolderLayoutBinding
import com.nrs.nsnik.notes.databinding.SingleListHeaderBinding
import com.nrs.nsnik.notes.databinding.SingleNoteLayoutBinding
import com.nrs.nsnik.notes.util.AppUtil
import com.nrs.nsnik.notes.util.FileUtil
import com.nrs.nsnik.notes.view.MainActivity
import com.nrs.nsnik.notes.view.fragments.ListFragment
import com.nrs.nsnik.notes.view.listeners.ItemTouchListener
import com.nrs.nsnik.notes.view.listeners.ListHeaderClickListener
import com.nrs.nsnik.notes.view.listeners.NoteItemClickListener
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import java.io.File


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

    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(mContext)
    private val mRequestManager: RequestManager? = (mContext.applicationContext as MyApplication).requestManager
    private val mFileUtil: FileUtil? = (mContext.applicationContext as MyApplication).fileUtil
    private val mCompositeDisposable: CompositeDisposable = CompositeDisposable()
    private val mRootFolder: File = mFileUtil!!.rootFolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            FOLDER -> FolderViewHolder(SingleFolderLayoutBinding.inflate(mLayoutInflater, parent, false))
            NOTES -> NoteViewHolder(SingleNoteLayoutBinding.inflate(mLayoutInflater, parent, false))
            HEADER -> HeaderViewHolder(SingleListHeaderBinding.inflate(mLayoutInflater, parent, false))
            else -> NoteViewHolder(SingleNoteLayoutBinding.inflate(mLayoutInflater, parent, false))
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
            headerViewHolder.itemHeaderContainer.visibility = if (mFolderList.isNotEmpty()) View.VISIBLE else View.GONE
            if (headerViewHolder.itemHeader.isVisible) headerViewHolder.itemHeader.text = mContext.resources.getString(R.string.headingFolder)
        } else {
            headerViewHolder.itemHeaderContainer.visibility = if (mNotesList.isNotEmpty()) View.VISIBLE else View.GONE
            if (headerViewHolder.itemHeader.isVisible) headerViewHolder.itemHeader.text = mContext.resources.getString(R.string.headingNotes)
        }
    }

    private fun bindFolderData(holder: RecyclerView.ViewHolder, position: Int) {
        val folderViewHolder = holder as FolderViewHolder
        val folderEntity: FolderEntity = mFolderList[position]
        folderViewHolder.folderName.text = folderEntity.folderName
        folderViewHolder.folderName.compoundDrawableTintList = AppUtil.stateList(folderEntity.color)
        changeVisibility(folderViewHolder.isPinned, folderEntity.pinned)
        changeVisibility(folderViewHolder.isLocked, folderEntity.locked)
    }

    private fun bindNotesData(holder: RecyclerView.ViewHolder, position: Int) {
        val noteViewHolder = holder as NoteViewHolder

        mFileUtil?.getLiveNote(mNotesList[position].fileName!!)?.observe(mContext as MainActivity, Observer {

            if (it.title != null && !it.title!!.isEmpty()) {
                noteViewHolder.noteTitle.visibility = View.VISIBLE
                noteViewHolder.noteTitle.text = it.title
                noteViewHolder.noteTitle.setTextColor(Color.parseColor(it.color))
            } else {
                noteViewHolder.noteTitle.visibility = View.GONE
            }

            if (it.locked == 0) {
                if (it.noteContent != null && !it.noteContent!!.isEmpty()) {
                    noteViewHolder.noteContent.visibility = View.VISIBLE
                    noteViewHolder.noteContent.text = it.noteContent
                    noteViewHolder.noteContent.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                    noteViewHolder.noteContent.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                    noteViewHolder.noteContent.compoundDrawablePadding = 0
                } else {
                    noteViewHolder.noteContent.visibility = View.GONE
                }

                if (it.imageList != null && it.imageList!!.isNotEmpty()) {
                    noteViewHolder.noteImage.visibility = View.VISIBLE
                    mRequestManager?.load(File(mRootFolder, it.imageList!![0]))?.into(noteViewHolder.noteImage)
                } else {
                    noteViewHolder.noteImage.visibility = View.GONE
                }

            } else {
                noteViewHolder.noteContent.text = mContext.resources?.getString(R.string.hidden)
                noteViewHolder.noteImage.visibility = View.GONE
                noteViewHolder.noteContent.setTextColor(ContextCompat.getColor(mContext, R.color.grey))
                noteViewHolder.noteContent.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lock_48px, 0, 0, 0)
                noteViewHolder.noteContent.compoundDrawablePadding = 4
            }

            changeVisibility(noteViewHolder.isPinned, it.pinned)
        })
    }

    private fun changeVisibility(imageView: ImageView, value: Int) {
        imageView.visibility = if (value == 1) View.VISIBLE else View.GONE
    }

    fun updateNotesList(noteList: List<NoteEntity>) {
        mNotesList = noteList
        notifyDataSetChanged()
    }

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

    override fun onItemDismiss(position: Int) {
    }

    private fun cleanUp() {
        mCompositeDisposable.dispose()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        cleanUp()
        super.onDetachedFromRecyclerView(recyclerView)
    }

    internal inner class NoteViewHolder(binding: SingleNoteLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        val noteTitle: TextView = binding.singleNoteTitle
        val noteContent: TextView = binding.singleNoteContent
        val noteImage: ImageView = binding.singleNoteImage
        val isPinned: ImageView = itemView.findViewById(R.id.itemIndicatorPin)
        val isLocked: ImageView = itemView.findViewById(R.id.itemIndicatorLock)

        init {
            mCompositeDisposable.addAll(
                    itemView.clicks().subscribe({
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            mNoteItemClickListener.onClick(adapterPosition, itemViewType)
                        }
                    }, { throwable -> Timber.d(throwable.message) }),

                    itemView.longClicks().subscribe({
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            mNoteItemClickListener.onLongClick(adapterPosition, itemViewType, itemView)
                        }
                    }, { throwable -> Timber.d(throwable.message) })
            )
        }
    }


    internal inner class FolderViewHolder(binding: SingleFolderLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        val folderName: TextView = binding.singleFolderName
        val isPinned: ImageView = itemView.findViewById(R.id.itemIndicatorPin)
        val isLocked: ImageView = itemView.findViewById(R.id.itemIndicatorLock)

        init {
            mCompositeDisposable.addAll(
                    itemView.clicks().subscribe {
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            mNoteItemClickListener.onClick(adapterPosition, itemViewType)
                        }
                    },
                    itemView.longClicks().subscribe({
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            mNoteItemClickListener.onLongClick(adapterPosition, itemViewType, itemView)
                        }
                    }, { throwable -> Timber.d(throwable.message) })

            )
        }
    }

    internal inner class HeaderViewHolder(binding: SingleListHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

        val itemHeader: TextView = binding.itemHeader
        private val itemHeaderMore: ImageView = binding.itemHeaderMore
        val itemHeaderContainer: ConstraintLayout = binding.itemHeaderContainer

        init {
            mCompositeDisposable.addAll(
                    itemHeaderMore.clicks().subscribe {
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            listHeaderClickListener.headerClick(
                                    if (itemHeader.text.toString() == mContext.resources.getString(R.string.headingFolder)) ListFragment.ItemType.FOLDER
                                    else ListFragment.ItemType.NOTES, itemHeaderMore)
                        }
                    }

            )
        }
    }
}
