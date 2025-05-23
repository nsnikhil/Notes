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
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.databinding.SingleIntroItemBinding // Added ViewBinding import
import com.nrs.nsnik.notes.view.adapters.diffUtil.StringDiffUtil
// import kotlinx.android.synthetic.main.single_intro_item.view.* // Removed synthetic import

class IntroAdapter : ListAdapter<String, IntroAdapter.MyViewHolder>(StringDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = SingleIntroItemBinding.inflate(LayoutInflater.from(parent.context), parent, false) // Inflate with ViewBinding
        return MyViewHolder(binding) // Pass binding to ViewHolder
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.introText.text = getItem(position) // Use binding
    }

    class MyViewHolder internal constructor(val binding: SingleIntroItemBinding) : RecyclerView.ViewHolder(binding.root) { // ViewHolder accepts binding
        // Direct view reference removed, will use binding.introText
    }

}