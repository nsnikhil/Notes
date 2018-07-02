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
import android.widget.ImageView
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
import com.nrs.nsnik.notes.view.listeners.AdapterType
import com.nrs.nsnik.notes.view.listeners.OnAddClickListener
import com.nrs.nsnik.notes.view.listeners.OnItemRemoveListener
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.single_check_list_item.view.*
import java.util.concurrent.TimeUnit

class CheckListAdapter(private val mOnAddClickListener: OnAddClickListener, private val onItemRemoveListener: OnItemRemoveListener) : ListAdapter<CheckListObject, CheckListAdapter.MyViewHolder>(CheckListDiffUtil()) {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        context = parent.context
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.single_check_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val checkList = getItem(position)
        holder.text.setText(checkList.text)
        holder.checkBox.isChecked = checkList.done
        if (holder.text.text.toString().isNotEmpty()) modifyText(holder.text, checkList.done)
    }

    private fun modifyText(textView: TextView, done: Boolean) {
        textView.apply {
            paintFlags = if (done) textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG else textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            if (done) textView.setTextColor(ContextCompat.getColor(context, R.color.grey)) else textView.setTextColor(ContextCompat.getColor(context, android.R.color.background_dark))
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
        private val remove: ImageView = itemView.checkListRemove

        init {
            compositeDisposable.addAll(

                    RxTextView.textChanges(text).debounce(500, TimeUnit.MILLISECONDS).subscribe {
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            val newContent: String = it.toString()
                            getItem(adapterPosition).text = newContent
                        }
                    },

                    RxTextView.editorActions(text).subscribe {
                        if (it == EditorInfo.IME_ACTION_NEXT) mOnAddClickListener.addClickListener()
                    },

                    RxCompoundButton.checkedChanges(checkBox).subscribe {
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            val value: Boolean = it
                            getItem(adapterPosition).done = value
                            modifyText(text, value)
                        }
                    },

                    RxView.clicks(remove).subscribe {
                        if (adapterPosition != RecyclerView.NO_POSITION)
                            onItemRemoveListener.onItemRemoved(adapterPosition, AdapterType.CHECKLIST_ADAPTER)
                    }

            )
        }
    }
}
