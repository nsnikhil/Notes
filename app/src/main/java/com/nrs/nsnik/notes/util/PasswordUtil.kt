/*
 *     Credit Card Security V1  Copyright (C) 2018  sid-sun
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

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.FragmentManager
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.view.fragments.dialogFragments.PasswordDialogFragment

class PasswordUtil {

    companion object {

        private const val default: String = "NA"

        private fun passwordExists(sharedPreferences: SharedPreferences, context: Context): Boolean {
            return sharedPreferences.getString(context.resources?.getString(R.string.sharedPreferencePasswordKey), default) != default
        }

        fun checkLock(sharedPreferences: SharedPreferences, context: Context, fragmentManager: FragmentManager, tag: String): Boolean {
            if (!passwordExists(sharedPreferences, context)) {
                showPasswordDialog(fragmentManager, tag)
                return false
            }
            return true
        }

        fun showPasswordDialog(fragmentManager: FragmentManager, tag: String) {
            PasswordDialogFragment().show(fragmentManager, tag)
        }

        fun encrypt(plainText: String): String {
            return plainText
        }

        fun decrypt(cypherText: String): String {
            return cypherText
        }

    }
}