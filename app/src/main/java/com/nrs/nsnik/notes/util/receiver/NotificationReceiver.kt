/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
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
