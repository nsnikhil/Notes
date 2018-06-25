package com.nrs.nsnik.notes.view.adapters.diffUtil

import androidx.recyclerview.widget.DiffUtil

class StringDiffUtil : DiffUtil.ItemCallback<String>() {

    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return false
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return false
    }

}