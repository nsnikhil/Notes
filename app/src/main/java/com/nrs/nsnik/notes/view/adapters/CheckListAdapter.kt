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
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxCompoundButton
import com.jakewharton.rxbinding2.widget.RxTextView
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.model.CheckListObject
import com.nrs.nsnik.notes.view.adapters.diffUtil.CheckListDiffUtil
import com.nrs.nsnik.notes.view.listeners.OnAddClickListener
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.single_check_list_item.view.*

class CheckListAdapter(private val mOnAddClickListener: OnAddClickListener) : ListAdapter<CheckListObject, CheckListAdapter.MyViewHolder>(CheckListDiffUtil()) {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        context = parent.context
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.single_check_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val checkList = getItem(position)
        if (position == itemCount - 1) holder.addNew.visibility = View.VISIBLE else holder.addNew.visibility = View.GONE
        holder.text.setText(checkList.text)
        holder.checkBox.isChecked = checkList.done
        if (holder.text.text.toString().isNotEmpty()) modifyText(holder.text, checkList.done)
    }

    private fun modifyText(textView: TextView, done: Boolean) {
        textView.apply {
            paintFlags = if (done) textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG else textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            if (done) textView.setTextColor(ContextCompat.getColor(context, R.color.grey)) else textView.setTextColor(ContextCompat.getColor(context, android.R.color.background_light))
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

    inner class MyViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val checkBox: CheckBox = itemView.checkListTicker
        val text: EditText = itemView.checkListItem
        val addNew: ImageButton = itemView.checkListAdd

        init {

            compositeDisposable.addAll(
                    RxTextView.textChanges(text).subscribe {
                        if (adapterPosition != RecyclerView.NO_POSITION) {
//                            mCheckList!![adapterPosition] = CheckListObject.builder()
//                                    .text(it.toString())
//                                    .done(checkBox.isChecked)
//                                    .build()
                        }
                    },
                    RxTextView.editorActionEvents(text).subscribe { textViewEditorActionEvent ->
                        if (textViewEditorActionEvent.actionId() == EditorInfo.IME_ACTION_NEXT) {
                            mOnAddClickListener.addClickListener()
                            text.requestFocus()
                        }
                    },
                    RxView.clicks(addNew).subscribe { mOnAddClickListener.addClickListener() },
                    RxCompoundButton.checkedChanges(checkBox).subscribe { aBoolean ->
                        if (adapterPosition != RecyclerView.NO_POSITION) {
//                                mCheckList!![adapterPosition] = CheckListObject.builder()
//                                        .text(text.text.toString())
//                                        .done(aBoolean)
//                                        .build()
                            if (text.text.toString().isNotEmpty()) {
                                modifyText(text, aBoolean)
                            }
                        }
                    }
            )

        }
    }
}