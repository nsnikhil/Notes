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

package com.nrs.nsnik.notes.util.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

import com.nrs.nsnik.notes.R

class NotificationReceiver : BroadcastReceiver() {

    private var mContext: Context? = null

    override fun onReceive(context: Context, intent: Intent) {
        mContext = context
        buildNotification(intent)
    }

    private fun buildNotification(i: Intent) {
        val notificationBuilder = NotificationCompat.Builder(mContext!!, mContext!!.resources.getString(R.string.notificationChannelReminder))
        notificationBuilder.setSmallIcon(R.drawable.ic_alarm_add_white_48px)
        if (i.extras != null) {
            notificationBuilder.setContentTitle(i.extras!!.getString(mContext!!.resources.getString(R.string.notificationTitle)))
        }
        val notificationManager = mContext!!.getSystemService(NotificationManager::class.java)

        notificationManager?.notify(1, notificationBuilder.build())
    }
}
