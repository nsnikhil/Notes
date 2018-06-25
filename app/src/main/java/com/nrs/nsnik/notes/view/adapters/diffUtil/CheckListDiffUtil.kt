package com.nrs.nsnik.notes.view.adapters.diffUtil

import androidx.recyclerview.widget.DiffUtil
import com.nrs.nsnik.notes.model.CheckListObject

class CheckListDiffUtil : DiffUtil.ItemCallback<CheckListObject>() {

    override fun areItemsTheSame(oldItem: CheckListObject, newItem: CheckListObject): Boolean {
        return false
    }

    override fun areContentsTheSame(oldItem: CheckListObject, newItem: CheckListObject): Boolean {
        return false
    }


}