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

package com.nrs.nsnik.notes.util

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.navigation.findNavController
import com.nrs.nsnik.notes.MyApplication
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.view.fragments.NewNoteFragment
import java.util.*

class AppUtil {

    companion object {

        fun stateList(colorString: String?): ColorStateList {
            val states = arrayOf(intArrayOf(android.R.attr.state_enabled), intArrayOf(-android.R.attr.state_enabled), intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_pressed))
            val color = Color.parseColor(colorString)
            val colors = intArrayOf(color, color, color, color)
            return ColorStateList(states, colors)
        }

        fun goToIntro(activity: Activity?) {
            val sharedPreferences = (activity?.applicationContext as MyApplication).sharedPreferences
            val isWatched = sharedPreferences.getBoolean(activity.resources.getString(R.string.bundleIntroWatched), false)
            if (!isWatched) activity.findNavController(R.id.mainNavHost).navigate(R.id.introFragment)
        }

        fun saveIsVisitedIntro(activity: Activity?) {
            val sharedPreferences = (activity?.applicationContext as MyApplication).sharedPreferences
            sharedPreferences.edit().putBoolean(activity.resources.getString(R.string.bundleIntroWatched), true).apply()
        }

        fun getTimeDifference(diff: Long): String {
            val seconds = diff / 1000
            if (seconds < 60) return "$seconds sec ago"
            val minutes = seconds / 60
            if (minutes < 60) return "$minutes min ago"
            val hours = minutes / 60
            return "$hours hrs ago"
        }

        fun makeName(type: NewNoteFragment.FILE_TYPES): String {
            return when (type) {
                NewNoteFragment.FILE_TYPES.TEXT -> Calendar.getInstance().timeInMillis.toString() + ".txt"
                NewNoteFragment.FILE_TYPES.IMAGE -> Calendar.getInstance().timeInMillis.toString() + ".jpg"
                NewNoteFragment.FILE_TYPES.AUDIO -> Calendar.getInstance().timeInMillis.toString() + ".3gp"
            }
        }

    }

}