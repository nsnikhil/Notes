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
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
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
import com.nrs.nsnik.notes.databinding.SingleCheckListItemBinding // Added ViewBinding import
import com.nrs.nsnik.notes.model.CheckListObject
import com.nrs.nsnik.notes.view.adapters.diffUtil.CheckListDiffUtil
import com.nrs.nsnik.notes.view.listeners.AdapterType
import com.nrs.nsnik.notes.view.listeners.OnAddClickListener
import com.nrs.nsnik.notes.view.listeners.OnItemRemoveListener
import io.reactivex.disposables.CompositeDisposable
// import kotlinx.android.synthetic.main.single_check_list_item.view.* // Removed synthetic import
import java.util.concurrent.TimeUnit


class CheckListAdapter(private val mOnAddClickListener: OnAddClickListener, private val onItemRemoveListener: OnItemRemoveListener) : ListAdapter<CheckListObject, CheckListAdapter.MyViewHolder>(CheckListDiffUtil()) {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        context = parent.context
        val binding = SingleCheckListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false) // Inflate with ViewBinding
        return MyViewHolder(binding) // Pass binding to ViewHolder
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val checkList = getItem(position)
        holder.binding.checkListItem.setText(checkList.text) // Use binding
        holder.binding.checkListTicker.isChecked = checkList.done // Use binding
        if (holder.binding.checkListItem.text.toString().isNotEmpty()) modifyText(holder.binding.checkListItem, checkList.done) // Use binding
        if (position == itemCount - 1) setFocus(holder.binding.checkListItem) // Use binding
    }

    private fun setFocus(editText: EditText) {
        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun modifyText(textView: TextView, done: Boolean) {
        textView.apply {
            paintFlags = if (done) textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG else textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            if (done) textView.setTextColor(ContextCompat.getColor(context, R.color.grey)) else textView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
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

    inner class MyViewHolder internal constructor(val binding: SingleCheckListItemBinding) : RecyclerView.ViewHolder(binding.root) { // ViewHolder accepts binding
        // Direct view references removed, will use binding.checkListTicker, binding.checkListItem, binding.checkListRemove

        init {
            compositeDisposable.addAll(

                    RxTextView.textChanges(binding.checkListItem).debounce(500, TimeUnit.MILLISECONDS).subscribe { // Use binding
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            val newContent: String = it.toString()
                            getItem(adapterPosition).text = newContent
                        }
                    },

                    RxTextView.editorActions(binding.checkListItem).subscribe { // Use binding
                        if (it == EditorInfo.IME_ACTION_NEXT) mOnAddClickListener.addClickListener()
                    },

                    RxCompoundButton.checkedChanges(binding.checkListTicker).subscribe { // Use binding
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            val value: Boolean = it
                            getItem(adapterPosition).done = value
                            modifyText(binding.checkListItem, value) // Use binding
                        }
                    },

                    RxView.clicks(binding.checkListRemove).subscribe { // Use binding
                        if (adapterPosition != RecyclerView.NO_POSITION)
                            onItemRemoveListener.onItemRemoved(adapterPosition, AdapterType.CHECKLIST_ADAPTER)
                    }

            )
        }
    }
}
