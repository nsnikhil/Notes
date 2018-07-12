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

package com.nrs.nsnik.notes.util.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.view.SplashActivity


class NotificationReceiver : BroadcastReceiver() {

    private lateinit var context: Context

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        buildNotification(intent)
    }

    private fun buildNotification(i: Intent) {

        val notificationIntent = Intent(context, SplashActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(SplashActivity::class.java)
        stackBuilder.addNextIntent(notificationIntent)

        val pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder = NotificationCompat.Builder(context, context.resources.getString(R.string.notificationChannelReminder))
                .setSmallIcon(R.drawable.ic_alarm_add_white_48px)
                .setContentTitle(i.extras?.getString(context.resources.getString(R.string.notificationTitle)))
                .setContentText(i.extras?.getString(context.resources.getString(R.string.notificationContent)))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)


        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager?.notify(1, notificationBuilder.build())

    }
}
