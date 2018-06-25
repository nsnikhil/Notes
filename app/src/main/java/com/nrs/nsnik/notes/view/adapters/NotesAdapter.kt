/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.view.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.RequestManager
import com.jakewharton.rxbinding2.view.RxView
import com.nrs.nsnik.notes.MyApplication
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.data.FolderEntity
import com.nrs.nsnik.notes.data.NoteEntity
import com.nrs.nsnik.notes.util.FileUtil
import com.nrs.nsnik.notes.view.listeners.ItemTouchListener
import com.nrs.nsnik.notes.view.listeners.NoteItemClickListener
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.single_folder_layout.view.*
import kotlinx.android.synthetic.main.single_list_header.view.*
import kotlinx.android.synthetic.main.single_note_layout.view.*
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
                   private var mNotesList: List<NoteEntity>?,
                   private var mFolderList: List<FolderEntity>?,
                   private val mNoteItemClickListener: NoteItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ItemTouchListener {


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
        when (viewType) {
            FOLDER -> return FolderViewHolder(mLayoutInflater.inflate(R.layout.single_folder_layout, parent, false))
            NOTES -> return NoteViewHolder(mLayoutInflater.inflate(R.layout.single_note_layout, parent, false))
            HEADER -> return HeaderViewHolder(mLayoutInflater.inflate(R.layout.single_list_header, parent, false))
        }
        return NoteViewHolder(mLayoutInflater.inflate(R.layout.single_note_layout, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var pos = mFolderList!!.size + 2
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
        return if (mNotesList != null && mFolderList != null) mNotesList!!.size + mFolderList!!.size + 2 else 2
    }

    /**
     * @param holder   HeaderViewHolder object
     * @param position Position of/in the list
     *
     *
     * this function binds the data for header view-holder type
     * it check if folder or note list is size is greater than 0
     * if yes than shows the appropriate headers otherwise
     * hides them
     */
    private fun bindHeaderData(holder: RecyclerView.ViewHolder, position: Int) {
        val headerViewHolder = holder as HeaderViewHolder
        if (position == 0) {
            if (mFolderList!!.isNotEmpty()) {
                headerViewHolder.itemHeader.visibility = View.VISIBLE
                headerViewHolder.itemHeader.text = mContext.resources.getString(R.string.headingFolder)
            } else {
                headerViewHolder.itemHeader.visibility = View.GONE
            }
        } else {
            if (mNotesList!!.isNotEmpty()) {
                headerViewHolder.itemHeader.visibility = View.VISIBLE
                headerViewHolder.itemHeader.text = mContext.resources.getString(R.string.headingNotes)
            } else {
                headerViewHolder.itemHeader.visibility = View.GONE
            }
        }
    }

    /**
     * @param holder   FolderViewHolder object
     * @param position Position of/in the list
     *
     *
     * this function binds the data for FolderViewHolder type
     * it takes data from folder list and sets them on textview of
     * FolderViewHolder
     */
    private fun bindFolderData(holder: RecyclerView.ViewHolder, position: Int) {
        val folderViewHolder = holder as FolderViewHolder
        folderViewHolder.folderName.text = mFolderList!![position].folderName
        folderViewHolder.folderName.compoundDrawableTintList = stateList(mFolderList!![position].color)
    }

    /**
     * @param holder   NoteViewHolder object
     * @param position Position of/in the list
     *
     *
     * this function binds the data for NoteViewHolder type
     * it takes data from Notes list and sets title and
     * note content on textviews and others values.
     */
    private fun bindNotesData(holder: RecyclerView.ViewHolder, position: Int) {
        val noteViewHolder = holder as NoteViewHolder
        val noteEntity: NoteEntity
        try {
            noteEntity = mFileUtil!!.getNote(mNotesList!![position].fileName!!)

            //TITLE

            if (noteEntity.title != null && !noteEntity.title!!.isEmpty()) {
                noteViewHolder.noteTitle.visibility = View.VISIBLE
                noteViewHolder.noteTitle.text = noteEntity.title
                noteViewHolder.noteTitle.setTextColor(Color.parseColor(noteEntity.color))
            } else {
                noteViewHolder.noteTitle.visibility = View.GONE
            }


            //CONTENT

            if (noteEntity.noteContent != null && !noteEntity.noteContent!!.isEmpty()) {
                noteViewHolder.noteContent.visibility = View.VISIBLE
                noteViewHolder.noteContent.text = noteEntity.noteContent
            } else {
                noteViewHolder.noteContent.visibility = View.GONE
            }


            //IMAGES

            if (noteEntity.imageList != null && noteEntity.imageList!!.isNotEmpty()) {
                noteViewHolder.noteImage.visibility = View.VISIBLE
                mRequestManager!!.load(File(mRootFolder, noteEntity.imageList!![0])).into(noteViewHolder.noteImage)
            } else {
                noteViewHolder.noteImage.visibility = View.GONE
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

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
        if (position == 0 || position == mFolderList!!.size + 1) {
            return HEADER
        } else if (position > 0 && position <= mFolderList!!.size) {
            return FOLDER
        } else if (position > mFolderList!!.size + 1 && position < mNotesList!!.size) {
            return NOTES
        }
        return super.getItemViewType(position)
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
            startPos = mFolderList!!.size + 2
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

    private fun stateList(colorString: String?): ColorStateList {
        val states = arrayOf(intArrayOf(android.R.attr.state_enabled), intArrayOf(-android.R.attr.state_enabled), intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_pressed))
        val color = Color.parseColor(colorString)
        val colors = intArrayOf(color, color, color, color)
        return ColorStateList(states, colors)
    }

    private fun cleanUp() {
        mCompositeDisposable.dispose()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        cleanUp()
        super.onDetachedFromRecyclerView(recyclerView)
    }

    internal inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val noteTitle: TextView = itemView.singleNoteTitle
        val noteContent: TextView = itemView.singleNoteContent
        val noteImage: ImageView = itemView.singleNoteImage

        init {

            mCompositeDisposable.addAll(

                    RxView.clicks(itemView).subscribe({
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            mNoteItemClickListener.onClick(adapterPosition, itemViewType)
                        }
                    }, { throwable -> Timber.d(throwable.message) }),

                    RxView.longClicks(itemView).subscribe({
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            mNoteItemClickListener.onLongClick(adapterPosition, itemViewType)
                        }
                    }, { throwable -> Timber.d(throwable.message) })
            )
        }
    }


    internal inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val folderName: TextView = itemView.singleFolderName

        init {
            mCompositeDisposable.addAll(
                    RxView.clicks(itemView).subscribe {
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            mNoteItemClickListener.onClick(adapterPosition, itemViewType)
                        }
                    },
                    RxView.longClicks(itemView).subscribe({
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            mNoteItemClickListener.onLongClick(adapterPosition, itemViewType)
                        }
                    }, { throwable -> Timber.d(throwable.message) })

            )
        }
    }

    internal inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val itemHeader: TextView = itemView.itemHeader

        init {

        }
    }
}