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
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.util.receiver.NotificationReceiver
import java.util.*

class ReminderUtil {

    companion object {


        fun setReminder(activity: Activity, title: String, body: String): MutableLiveData<Int> {
            val hasReminders = MutableLiveData<Int>()
            val calendar = Calendar.getInstance()
            val time = TimePickerDialog(activity, { timePicker, hour, minutes ->
                hasReminders.value = 1
                setNotification(activity, calendar, hour, minutes, title, body)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)
            time.show()
            return hasReminders
        }

        private fun setNotification(activity: Activity, calendar: Calendar, hour: Int, minutes: Int, title: String, body: String) {
            val myIntent = Intent(activity, NotificationReceiver::class.java)
            myIntent.putExtra(activity.resources.getString(R.string.notificationTitle), title)
            myIntent.putExtra(activity.resources.getString(R.string.notificationContent), body)

            val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val pendingIntent = PendingIntent.getBroadcast(activity, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minutes)

            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
        }

        fun cancelReminder(activity: Activity) {
            val receiver = ComponentName(activity, NotificationReceiver::class.java)

            val packageManager: PackageManager = activity.packageManager
            packageManager.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)

            val intent = Intent(activity, NotificationReceiver::class.java)

            val pendingIntent = PendingIntent.getBroadcast(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)

            pendingIntent.cancel()
        }
    }
}